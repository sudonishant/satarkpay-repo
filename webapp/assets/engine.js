/* ==========================================================================
   SatarkPay — engine.js
   Deterministic, explainable rules. No network, no model, no randomness.
   Every verdict this file produces can be traced to the signal that caused it.
   ========================================================================== */

window.SP = window.SP || {};

(function () {
  'use strict';

  /* ------------------------------------------------------------ redaction --
     Runs before anything is displayed or stored. Two things must never be
     kept: the full bank account number and anything that is a credential.
     ------------------------------------------------------------------------ */

  var REDACT = [
    // 11-16 digit runs (account numbers, card numbers)
    { re: /\b(\d{4})[\s-]?(\d{4})[\s-]?(\d{2,8})\b/g,
      to: function (m, a, b, c) { return a + '••••' + c; } },
    // standalone long digit runs
    { re: /\b(\d{6,})\b/g,
      to: function (m, d) { return d.slice(0, 2) + '••••' + d.slice(-2); } },
    // UPI handles: keep the person, mask the PSP-independent part
    { re: /\b([a-z0-9._-]{3,})@([a-z0-9]+)\b/gi,
      to: function (m, a, b) { return a.slice(0, 3) + '•••@' + b; } },
    // OTP / PIN mentions — replace the digits, keep the sentence
    { re: /\b(OTP|PIN|CVV|password)\b[\s:=-]*\d{3,8}/gi,
      to: function (m, k) { return k + ' ••••'; } },
    // emails
    { re: /\b([a-z0-9._-]{2,})@([a-z0-9.-]+\.(?:com|in|org|net|co))\b/gi,
      to: function (m, a, b) { return a.slice(0, 2) + '•••@' + b; } }
  ];

  SP.redact = function (s) {
    var out = String(s == null ? '' : s);
    var hits = [];
    REDACT.forEach(function (r) {
      out = out.replace(r.re, function () {
        hits.push(1);
        return r.to.apply(null, arguments);
      });
    });
    SP._redactions = hits.length;
    return out;
  };

  /* --------------------------------------------------------- upi parsing --
     A VPA is `localpart@psp`. The PSP half is regulated by NPCI, so an
     unknown PSP half is itself a signal.
     ------------------------------------------------------------------------ */

  SP.parseVPA = function (s) {
    var t = String(s || '').trim();

    /* UPI deep links carry the address in the `pa` parameter. People paste
       these constantly, so they are worth understanding rather than rejecting. */
    if (/^upi:\/\//i.test(t) || /[?&]pa=/i.test(t)) {
      var q = t.match(/[?&]pa=([^&\s]+)/i);
      if (q) t = decodeURIComponent(q[1]);
    }
    t = t.replace(/^upi:\/\//i, '').split('?')[0];

    var m = t.match(/^([a-z0-9._-]+)@([a-z]+)$/i);
    if (!m) return null;
    var psp = m[2].toLowerCase();
    var known = SP.DOMAINS.psp.indexOf(psp) > -1;
    return {
      vpa: t,
      local: m[1],
      psp: psp,
      knownPSP: known,
      /* A handle that tries to look like a bank is a classic misdirection. */
      brandish: /sbi|hdfc|icici|axis|kotak|rbi|npci|paytm|phonepe|gpay/i.test(m[1]),
      numericHeavy: (m[1].match(/\d/g) || []).length >= 4
    };
  };

  /* -------------------------------------------------------- domain ladder --
     Returns a verdict for a URL or a bare domain, with the reason stated in
     plain language. No blacklist is used — only structure.
     ------------------------------------------------------------------------ */

  function registrable (host) {
    var p = host.split('.');
    return p.length <= 2 ? host : p.slice(-2).join('.');
  }

  SP.checkDomain = function (raw) {
    var input = String(raw || '').trim();
    if (!input) return { ok: false, verdict: 'empty', why: 'Enter a link or a domain.' };

    var host = input
      .replace(/^https?:\/\//i, '')
      .replace(/^www\./i, '')
      .split(/[/?#]/)[0]
      .toLowerCase();

    if (!host || host.indexOf('.') < 0) {
      return { ok: false, verdict: 'empty', host: host, why: 'That does not look like a domain. It needs at least one dot, like example.com.' };
    }

    var reg = registrable(host);

    // 1. exact official match
    var off = SP.DOMAINS.official.filter(function (o) { return o.d === reg; })[0];
    if (off) {
      return { ok: true, verdict: 'official', host: host, reg: reg, label: off.who,
        why: 'This is the real domain, spelled correctly. ' + off.who + '.' };
    }

    // 2. look-alike check against official brands
    var brands = ['sbi', 'onlinesbi', 'hdfc', 'hdfcbank', 'icici', 'icicibank', 'axis', 'axisbank',
                  'kotak', 'npci', 'rbi', 'sebi', 'nsdl', 'incometax', 'uidai', 'cybercrime',
                  'digilocker', 'paytm', 'phonepe', 'gpay', 'bhim'];

    var hostSquashed = host.replace(/[^a-z0-9]/g, '');
    var claimed = brands.filter(function (b) { return hostSquashed.indexOf(b) > -1; })[0];

    if (claimed) {
      // which brand did they mean, and what is the real domain?
      var real = SP.DOMAINS.official.filter(function (o) {
        return o.d.indexOf(claimed) > -1 || claimed.indexOf(o.d.split('.')[0]) > -1;
      })[0];

      var lure = null;
      for (var i = 0; i < SP.DOMAINS.lures.length; i++) {
        if (SP.DOMAINS.lures[i].re.test(host)) { lure = SP.DOMAINS.lures[i]; break; }
      }
      // a different registrable domain that merely contains the brand is a copy
      var falseBrand = reg.indexOf(claimed) < 0 ? 'Looks like it is trying to pass for' : 'Contains';

      return {
        ok: false, verdict: 'fake', host: host, reg: reg,
        claimed: claimed, real: real ? real.d : null, lure: lure,
        why: falseBrand + ' ' + claimed.toUpperCase() +
             ', but the real domain is ' + (real ? real.d : '(not in this list)') +
             '. The registered domain here is ' + reg + '.' +
             (lure ? ' ' + lure.why : '')
      };
    }

    // 3. generic structural checks
    for (var j = 0; j < SP.DOMAINS.lures.length; j++) {
      if (SP.DOMAINS.lures[j].re.test(host)) {
        return { ok: false, verdict: 'suspicious', host: host, reg: reg, lure: SP.DOMAINS.lures[j],
          why: SP.DOMAINS.lures[j].tag + ' — ' + SP.DOMAINS.lures[j].why };
      }
    }

    return { ok: true, verdict: 'unknown', host: host, reg: reg,
      why: 'Nothing structural is wrong with ' + reg + '. That is not the same as safe — a domain can be brand-new and clean-looking. Verify it through the organisation s own app before paying anything.' };
  };

  /* ----------------------------------------------------- message scoring --
     Rule-based classification of a message. Rules are intentionally few and
     readable: the point is that a user can see WHY a message was flagged.
     Each rule carries a weight; the tier comes from the total.
     ------------------------------------------------------------------------ */

  var RULES = [
    { id: 'link-short',  w: 3, lab: 'Shortened link', note: 'Shorteners hide the real destination until you are already on it.',
      re: /\b(bit\.ly|tinyurl\.com|t\.co|goo\.gl|is\.gd|rb\.gy|shorturl|tiny\.cc|cutt\.ly|ow\.ly)\b/i },

    { id: 'link-lure',   w: 4, lab: 'Look-alike domain in message', note: 'A brand name with -kyc, -verify, -update or extra digits around it.',
      re: /\b(?:[a-z0-9-]*(?:sbi|hdfc|icici|axis|kotak|paytm|phonepe|npci|rbi)[a-z0-9-]*)\.(?:xyz|top|club|online|site|icu|cyou|click|shop|store|info|buzz|link)\b/i },

    { id: 'brand-hyphen',w: 3, lab: 'Hyphenated brand link', note: 'Banks do not put an action word inside their own domain name.',
      re: /\b[a-z0-9]+-(?:kyc|login|verify|secure|update|support|help|care|refund|pay)\.[a-z]{2,}/i },

    { id: 'panic',       w: 3, lab: 'Deadline or block threat', note: 'Urgency removes the time you would use to check.',
      re: /\b(blocked|block|disconnect|suspend|deactivat|freeze|tonight|today only|immediately|urgent(?:ly)?|hurry|within \d+ hours?|last (?:date|chance)|limited (?:seats|slots|time|offer)|only \d+ (?:seats|slots|left))\b/i },

    { id: 'credential',  w: 5, lab: 'Asks for OTP / PIN / CVV', note: 'No legitimate organisation ever asks for these. Ever.',
      re: /\b(otp|pin|cvv|password|net\s?banking|upi\s?pin|card\s?number|aadhaar\s?number)\b/i },

    { id: 'pay-first',   w: 4, lab: 'Pay a fee to receive or start', note: 'Money that requires money first is not money you are getting.',
      re: /\b(?:processing|joining|registration|refundable|verification|clearance|customs|security|activation|handling|release)\s+(?:fee|charge|charges|deposit|duty|amount)\b|\b(?:advance payment|service charge|tax (?:payment|clearance))\b/i },

    { id: 'prize',       w: 4, lab: 'Unexpected prize or lottery', note: 'You cannot win a draw you never entered, and never need to pay to claim it.',
      re: /\b(won|lucky draw|lottery|jackpot|kbc|prize|winner|cash reward|selected for (?:a )?(?:reward|gift))\b/i },

    { id: 'invest',      w: 4, lab: 'Guaranteed return claim', note: 'Guaranteed returns do not exist in securities markets. Regulated entities cannot promise them.',
      re: /\b(guaranteed (?:return|profit|income)|assured return|double your (?:money|investment)|[0-9]{1,2}\s?% (?:daily|weekly|monthly) (?:return|profit)|risk[- ]free (?:return|investment)|trading tips?)\b/i },

    { id: 'apk',         w: 5, lab: 'APK file sent over chat', note: 'An app handed over as a file is an app that failed app-store review.',
      re: /\.apk\b|\binstall (?:this|the) (?:app|application) from (?:the )?(?:link|below)|download our app from link/i },

    { id: 'isolation',   w: 5, lab: 'Tells you not to tell anyone', note: 'Isolation is the mechanism of the scam. No real process requires secrecy from your family.',
      re: /\b(do not (?:tell|inform|discuss)|don'?t tell anyone|keep (?:this )?(?:secret|between us)|stay on (?:the )?(?:call|line)|do not disconnect|video call with (?:police|officer|cbi)|case (?:has been )?registered)\b/i },

    { id: 'remote',      w: 5, lab: 'Remote-access app requested', note: 'Screen sharing hands over everything on the screen, including your balance.',
      re: /\b(anydesk|teamviewer|quick\s?support|rustdesk|screen shar\w*|remote (?:access|support) (?:app|tool))\b/i },

    { id: 'aadhaar',     w: 3, lab: 'Wants identity documents', note: 'Aadhaar/PAN sent over chat feeds impersonation and loan fraud.',
      re: /\b(send|share|upload|provide)\b[^.]{0,30}\b(aadhaar|aadhar|pan card|passbook|selfie with)\b/i },

    { id: 'urgency-wire',w: 3, lab: 'Pushes an immediate transfer', note: 'Fast transfers are hard to reverse. That is why they are requested.',
      re: /\b(transfer (?:now|immediately|urgently)|pay (?:now|immediately|before)|scan (?:this|the) qr|upi id|gpay|phonepe number|send money to)\b/i }
  ];

  SP.RULES = RULES;

  /* Legitimacy markers — these subtract. A debit alert or an order update
     should not be flagged just because it contains the word "OTP". */
  var GOOD = [
    { id: 'no-share-warning', w: -3, lab: 'Warns you not to share',
      re: /\b(do not share|never share|never ask|we never ask|not share this otp)\b/i },
    { id: 'own-ref',          w: -2, lab: 'Points to your own app / statement',
      re: /\b(view (?:statement|details) in the app|track in the app|check in your (?:app|account))\b/i },
    { id: 'masked-acct',      w: -2, lab: 'Uses a masked account number',
      re: /\b(?:a\/c|acct|account)\s*(?:no\.?\s*)?x{2,}\d{3,4}\b|\bfrom (?:a\/c|acct)\s*x{2,}\d+/i },
    { id: 'real-brand-tld',   w: -2, lab: 'Link points to a real domain',
      re: /\b(?:https?:\/\/)?(?:www\.)?(?:onlinesbi\.sbi|sbi\.co\.in|hdfcbank\.com|icicibank\.com|axisbank\.com|kotak\.com|npci\.org\.in|rbi\.org\.in|cybercrime\.gov\.in|gov\.in)\b/i }
  ];

  SP.scoreMessage = function (text) {
    var t = String(text || '');
    var signals = [];
    var score = 0;

    RULES.forEach(function (r) {
      if (r.re.test(t)) { score += r.w; signals.push({ id: r.id, w: r.w, lab: r.lab, note: r.note, kind: 'risk' }); }
    });

    var credit = 0;
    GOOD.forEach(function (g) {
      if (g.re.test(t)) { credit += g.w; signals.push({ id: g.id, w: g.w, lab: g.lab, note: 'Counts in your favour.', kind: 'good' }); }
    });

    score = Math.max(0, score + credit);
    var tier = SP.tierFor(score, signals.length);

    return {
      score: score,
      raw: score - credit,
      credit: credit,
      tier: tier,
      signals: signals,
      length: t.length,
      urlCount: (t.match(/https?:\/\/|bit\.ly|tinyurl|\b[a-z0-9-]+\.(?:xyz|top|club|online|site|icu|click|shop|store|info)\b/gi) || []).length
    };
  };

  /* --------------------------------------------------------------- tiers --
     Single source of truth for thresholds. chain.js uses the same function.
     ------------------------------------------------------------------------ */

  SP.TIERS = [
    { key: 'critical', name: 'CRITICAL', min: 10, tone: 'v-critical',
      head: 'Stop. Do not pay.', body: 'Multiple independent signals point at fraud. Do not approve anything, do not enter a PIN, do not install anything.' },
    { key: 'high', name: 'HIGH', min: 6, tone: 'v-high',
      head: 'Strong fraud pattern.', body: 'This matches a known scam shape. Verify through an official channel you opened yourself before doing anything.' },
    { key: 'caution', name: 'CAUTION', min: 3, tone: 'v-caution',
      head: 'Something is off.', body: 'One or two signals fired. It may be legitimate, but it is worth two minutes of checking before money moves.' },
    { key: 'low', name: 'LOW', min: 0, tone: 'v-low',
      head: 'Nothing structural stands out.', body: 'No known pattern matched. That is not a guarantee of safety — it means the check found nothing to alarm you about.' }
  ];

  SP.tierFor = function (score) {
    for (var i = 0; i < SP.TIERS.length; i++) if (score >= SP.TIERS[i].min) return SP.TIERS[i];
    return SP.TIERS[SP.TIERS.length - 1];
  };

  SP.money = function (n) {
    var v = Number(n) || 0;
    return '₹' + v.toLocaleString('en-IN', { maximumFractionDigits: 2 });
  };

  SP.esc = function (s) {
    return String(s == null ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  };

  /* Small shared renderer: a list of signals into markup. */
  SP.sigList = function (signals) {
    if (!signals.length) {
      return '<div class="sig"><span class="w">0</span><span><span class="lab">No pattern matched</span>' +
             '<span class="sn">The message did not contain any of the ' + RULES.length +
             ' known risk patterns. Judge it on who sent it and what they are asking for.</span></span></div>';
    }
    return signals.map(function (s) {
      var good = s.kind === 'good';
      return '<div class="sig' + (good ? '' : ' hit') + '">' +
        '<span class="w">' + (s.w > 0 ? '+' + s.w : s.w) + '</span>' +
        '<span><span class="lab">' + SP.esc(s.lab) + '</span>' +
        '<span class="sn">' + SP.esc(s.note) + '</span></span>' +
        '<span class="st">' + (good
          ? '<span class="badge b-live">in your favour</span>'
          : '<span class="badge b-bad">risk</span>') + '</span></div>';
    }).join('');
  };
})();
