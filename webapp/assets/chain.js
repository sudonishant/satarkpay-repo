/* ==========================================================================
   SatarkPay — chain.js
   The core idea: fraud is not one event, it is a sequence. Any single
   signal here is survivable. Two or three together are the shape of an
   active attack, and that shape is detectable on the device, offline,
   in the last seconds before the PIN is entered.
   ========================================================================== */

window.SP = window.SP || {};

(function () {
  'use strict';

  /* ---------------------------------------------------------- the signals --
     Weight = how much this signal, on its own, narrows the field to "attack".
     These are deliberately whole numbers so a user can add them up by hand
     and get the same tier the app shows.
     ------------------------------------------------------------------------ */

  var SIGNALS = [
    { id: 'call',     w: 4, lab: 'You are on a call right now',
      note: 'Live guidance on the call is how the victim is walked through the screen they do not understand.',
      ask: 'Are you talking to someone about this payment right now?' },
    { id: 'scanrecv', w: 4, lab: 'Someone said "scan this to receive money"',
      note: 'Scanning a UPI QR can only send money. There is no receive-by-scan.',
      ask: 'Did someone ask you to scan a code to receive money?' },
    { id: 'first',    w: 3, lab: 'First time paying this payee',
      note: 'A new payee plus pressure is the single most common loss pattern.',
      ask: 'Have you paid this payee before?' },
    { id: 'amount',   w: 3, lab: 'Amount is far above your usual',
      note: 'Scammers push the largest amount they can get before the victim hesitates.',
      ask: 'Is this much larger than what you normally send?' },
    { id: 'device',   w: 3, lab: 'Screen sharing or an unknown app is active',
      note: 'A screen-share session means the caller can read your balance as you open it.',
      ask: 'Have you installed an app someone asked you to install?' },
    { id: 'chat',     w: 2, lab: 'The instructions arrived over chat',
      note: 'Chat is unaccountable. Instructions that matter arrive through official channels.',
      ask: 'Did the instructions come on WhatsApp or Telegram?' },
    { id: 'longchat', w: 2, lab: 'Long build-up before the payment',
      note: 'Days of rapport are investment in you — they make the eventual request feel normal.',
      ask: 'Have you been talking to this person for days or weeks?' },
    { id: 'psp',      w: 2, lab: 'Payment handle is not a known PSP',
      note: 'NPCI issues handles on a fixed list of PSP codes. Anything else is not a real UPI address.',
      ask: 'Do you recognise the part after the @ in the payment address?' }
  ];

  /* The message check feeds in as a ninth signal — the highest weight,
     because a message that already matches a known scam script is the
     clearest evidence available. */
  var MSG_W = 5;

  SP.CHAIN = {
    signals: SIGNALS,
    msgWeight: MSG_W,
    tiers: function () { return SP.TIERS; },

    /* ------------------------------------------------------------ score --
       Accepts the answer map plus an optional message text. Returns the
       full, auditable result: each contributing signal with its weight,
       the total, the tier, and the cooling-off period.
       ------------------------------------------------------------------ */
    assess: function (answers, msgText) {
      answers = answers || {};
      var hits = [];

      SIGNALS.forEach(function (s) {
        if (answers[s.id]) hits.push({ id: s.id, w: s.w, lab: s.lab, note: s.note });
      });

      var msg = null;
      if (msgText && String(msgText).trim()) {
        msg = SP.scoreMessage(msgText);
        if (msg.tier.key === 'high' || msg.tier.key === 'critical') {
          hits.push({ id: 'msg', w: MSG_W, lab: 'Message matches a known scam script',
            note: 'The pasted text set off ' + msg.signals.filter(function (x) { return x.kind === 'risk'; }).length +
                  ' pattern' + (msg.signals.filter(function (x) { return x.kind === 'risk'; }).length === 1 ? '' : 's') +
                  ' on its own (' + msg.tier.name + ', ' + msg.score + ' points).' });
        } else if (msg.tier.key === 'caution') {
          hits.push({ id: 'msg-soft', w: 2, lab: 'Message raised minor flags',
            note: 'One pattern matched in the pasted text. Not conclusive by itself.' });
        }
      }

      var score = hits.reduce(function (a, h) { return a + h.w; }, 0);
      var tier = SP.tierFor(score);

      /* Cooling-off: the pause is proportional to the score. At CRITICAL it
         is not a pause at all — the pay action is simply withheld, and the
         countdown is not shown, because a countdown implies you may proceed. */
      var cool = 0;
      if (tier.key === 'caution') cool = 30;
      if (tier.key === 'high') cool = 60;
      var canProceed = tier.key !== 'critical';

      return {
        answers: answers,
        hits: hits,
        score: score,
        tier: tier,
        message: msg,
        coolSeconds: cool,
        canProceed: canProceed,
        /* what a user should do instead, in the words they will actually use */
        instead: SP.insteadFor(tier.key, hits)
      };
    }
  };

  /* --------------------------------------------------- "what to do instead" --
     The honest half of the product. A warning with no alternative gets
     ignored, so every tier gets a concrete route that still completes the
     user s real goal. */
  SP.insteadFor = function (tierKey, hits) {
    var ids = (hits || []).map(function (h) { return h.id; });
    var out = [];

    if (ids.indexOf('call') > -1) {
      out.push({ t: 'Hang up first', d: 'End the call. If what you are doing is legitimate, the payment will still work in ten minutes. A caller who objects to you hanging up is the reason you are hanging up.' });
    }
    if (ids.indexOf('scanrecv') > -1 || ids.indexOf('msg') > -1) {
      out.push({ t: 'To receive money, share — never scan', d: 'Send the other person your own UPI ID or show your own QR in your app. If they refuse, the money was never coming.' });
    }
    if (ids.indexOf('first') > -1) {
      out.push({ t: 'Verify on a channel you opened', d: 'Call the number printed on the bill, card, or website — not the number in the chat. Ask them to confirm the account number, not just the name.' });
    }
    if (ids.indexOf('amount') > -1) {
      out.push({ t: 'Split it or send a token', d: 'Send ₹1 first and confirm it arrives and is acknowledged. Then send the rest. Never send a large first-time amount in one go.' });
    }
    if (ids.indexOf('device') > -1) {
      out.push({ t: 'Remove the remote access', d: 'Uninstall the screen-sharing app, then change your UPI PIN and net-banking password from the bank s own app or branch.' });
    }
    if (ids.indexOf('psp') > -1) {
      out.push({ t: 'Pay to a handle, not a "person"', d: 'Ask for their UPI ID and check the part after @. If it is not a PSP code you can find on NPCI s public list, do not pay it.' });
    }

    if (tierKey === 'critical') {
      out.unshift({ t: 'Do not proceed. This is an active attack pattern.', d: 'Call 1930 (national cyber-fraud helpline) or file at cybercrime.gov.in if any money has already left. The first 24 hours matter most for a hold.' });
    }
    if (tierKey === 'low') {
      out.push({ t: 'Nothing found — keep your own habit', d: 'Check the payee name shown by your app against the name you were told, every single time. That one habit defeats most of this list.' });
    }
    return out;
  };

  /* ============================================================ ledger ====
     Payee memory without storing anyone s identity. The ledger keeps a
     salted hash of the VPA and the size of the last few payments. It can
     answer "have I paid this before, and how much" without ever holding a
     contact list.
     ====================================================================== */

  SP.hashVPA = function (vpa) {
    /* A tiny deterministic hash. Not cryptography — this is a demo build with
       no backend. It exists to show the shape: store the fingerprint, not the
       handle. The production replacement is named on about.html. */
    var s = String(vpa || '').toLowerCase(), h = 2166136261;
    for (var i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = (h * 16777619) >>> 0; }
    return ('00000000' + h.toString(16)).slice(-8);
  };

  SP.ledgerRisk = function (rows) {
    rows = rows || SP.SAMPLE_LEDGER;
    var amounts = rows.map(function (r) { return r.amt; }).sort(function (a, b) { return a - b; });
    var median = amounts[Math.floor(amounts.length / 2)] || 0;
    var max = amounts[amounts.length - 1] || 0;

    return rows.map(function (r) {
      var p = SP.parseVPA(r.vpa);
      var flags = [];
      if (!r.known) flags.push('First time with this payee');
      if (r.amt > median * 5) flags.push('More than 5× your usual payment');
      if (p && !p.knownPSP) flags.push('Unknown PSP handle');
      if (p && p.brandish) flags.push('Handle uses a bank name');
      if (p && p.numericHeavy) flags.push('Handle is digit-heavy');

      var level = flags.length >= 2 ? 'bad' : flags.length === 1 ? 'warn' : 'ok';
      return {
        raw: r, vpa: r.vpa, fp: SP.hashVPA(r.vpa), amt: r.amt, when: r.when,
        known: r.known, whose: r.n, psp: p ? p.psp : '?', pspKnown: p ? p.knownPSP : false,
        flags: flags, level: level
      };
    });
  };

  SP.ledgerStats = function (rows) {
    var parsed = SP.ledgerRisk(rows);
    var amts = parsed.map(function (p) { return p.amt; }).sort(function (a, b) { return a - b; });
    return {
      rows: parsed,
      median: amts[Math.floor(amts.length / 2)] || 0,
      max: amts[amts.length - 1] || 0,
      newPayees: parsed.filter(function (p) { return !p.known; }).length,
      flagged: parsed.filter(function (p) { return p.level !== 'ok'; }).length
    };
  };

  /* =========================================================== autopay ====
     Mandate audit. The failure here is quiet: one approval, many debits.
     ====================================================================== */

  SP.auditMandates = function (rows) {
    rows = rows || SP.SAMPLE_MANDATES;
    return rows.map(function (r) {
      var flags = [];
      if (/trial/i.test(r.src)) flags.push('Approved during a free or ₹1 trial');
      if (/unknown/i.test(r.m)) flags.push('Merchant name cannot be traced to anything you signed up for');
      if (r.amt >= 400) flags.push('Large monthly amount for a subscription');
      if (/^\d+ Oct$/.test(r.next) === false && /today|tomorrow/i.test(r.next)) flags.push('Debits within 24 hours');
      var level = flags.length >= 2 ? 'bad' : flags.length === 1 ? 'warn' : 'ok';
      return { raw: r, m: r.m, amt: r.amt, per: r.per, next: r.next, src: r.src, flags: flags, level: level };
    });
  };

  SP.mandateTotal = function (rows) {
    return (rows || SP.SAMPLE_MANDATES).reduce(function (a, r) { return a + r.amt; }, 0);
  };

  /* =========================================================== evidence ===
     Builds the text block a user pastes into cybercrime.gov.in or hands to
     a bank. Redaction runs over the whole thing before it is returned.
     ====================================================================== */

  SP.evidence = function (opts) {
    opts = opts || {};
    var r = opts.assessment;
    var L = [];

    L.push('SATARKPAY INCIDENT RECORD');
    L.push('Generated on the device, offline. No data left this phone.');
    L.push('');

    if (opts.whenText) L.push('Time noted (device): ' + opts.whenText);
    if (opts.payee) L.push('Payee as shown in app: ' + SP.redact(opts.payee));
    if (opts.amount) L.push('Amount involved: ' + SP.money(opts.amount));
    L.push('');

    if (r) {
      L.push('RISK ASSESSMENT');
      L.push('  Tier: ' + r.tier.name + '  |  score ' + r.score + ' (thresholds: 3 / 6 / 10)');
      L.push('  Signals that fired:');
      if (!r.hits.length) L.push('    - none');
      r.hits.forEach(function (h) { L.push('    - [' + (h.w > 0 ? '+' + h.w : h.w) + '] ' + h.lab); });
      L.push('');
    }

    if (opts.message) {
      var m = SP.scoreMessage(opts.message);
      L.push('MESSAGE AS RECEIVED (redacted)');
      L.push('  ' + SP.redact(opts.message).replace(/\n/g, '\n  '));
      L.push('  Classification: ' + m.tier.name + ' (' + m.score + ' points, ' +
             m.signals.filter(function (s) { return s.kind === 'risk'; }).length + ' patterns)');
      L.push('');
    }

    if (opts.actions && opts.actions.length) {
      L.push('WHAT WAS DONE');
      opts.actions.forEach(function (a) { L.push('  - ' + a); });
      L.push('');
    }

    if (opts.notes) {
      L.push('NOTES');
      L.push('  ' + SP.redact(opts.notes).replace(/\n/g, '\n  '));
      L.push('');
    }

    L.push('WHERE TO FILE');
    L.push('  - cybercrime.gov.in  (National Cyber Crime Reporting Portal)');
    L.push('  - 1930  (national cyber-fraud financial helpline)');
    L.push('  - Your bank s fraud number, printed on the card or the bank website');
    L.push('');
    L.push('WHAT THIS RECORD IS NOT');
    L.push('  It is a structured note of what you saw, in your own words, with');
    L.push('  identifiers masked. It is not a police complaint and it is not legal');
    L.push('  evidence on its own. Attach your bank statement and the original');
    L.push('  message from your own phone when you file.');

    return L.join('\n');
  };

  SP.redactAll = SP.redact;
})();
