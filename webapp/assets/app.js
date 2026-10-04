/* ==========================================================================
   SatarkPay — app.js
   Shell, icons, and the small shared pieces every page leans on.
   No framework, no network, no dependencies.
   ========================================================================== */

window.SP = window.SP || {};

SP.PAGES = [
  { f: 'index.html',    t: 'Dashboard',              sub: 'Where you stand right now',      i: 'grid' },
  { f: 'payment.html',  t: 'Check a payment',        sub: 'Nine signals before the PIN',    i: 'shield' },
  { f: 'check.html',    t: 'Check a message',        sub: 'SMS, WhatsApp, email',           i: 'message' },
  { f: 'domain.html',   t: 'Check a link',           sub: 'Domain ladder, no blacklist',    i: 'link' },
  { f: 'library.html',  t: 'Scam library',           sub: 'Ten patterns, explained',        i: 'book' },
  { f: 'evidence.html', t: 'Evidence pack',          sub: 'What to hand to 1930',           i: 'file' },
  { f: 'emergency.html',t: 'Money already gone',     sub: 'Emergency order of action',      i: 'alert' },
  { f: 'about.html',    t: 'What works, what cannot', sub: 'Capability matrix',             i: 'check' }
];

/* ---------------------------------------------------------------- icons --
   24×24, stroke-based, single currentColor. An icon set that matches the
   type weight is most of what makes a UI feel finished, so this is inline
   rather than a sprite file — no request, no flash.
   ------------------------------------------------------------------------ */

var I = {
  grid:    '<path d="M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z"/>',
  shield:  '<path d="M12 2.5 20 6v6.2c0 5-3.4 8.5-8 9.8-4.6-1.3-8-4.8-8-9.8V6z"/><path d="m8.6 12.2 2.5 2.5 4.5-5"/>',
  message: '<path d="M21 12a8 8 0 0 1-8 8H7l-4 3v-3.6A8 8 0 0 1 3 12a8 8 0 0 1 8-8h2a8 8 0 0 1 8 8z"/>',
  link:    '<path d="M10 13.4a4 4 0 0 0 6 .4l2.6-2.6a4 4 0 0 0-5.7-5.7l-1.4 1.5"/><path d="M14 10.6a4 4 0 0 0-6-.4l-2.6 2.6a4 4 0 0 0 5.7 5.7l1.4-1.5"/>',
  book:    '<path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H19v16H6.5A2.5 2.5 0 0 0 4 20.5z"/><path d="M4 20.5A2.5 2.5 0 0 1 6.5 18H19v4H6.5A2.5 2.5 0 0 1 4 19.5z"/>',
  file:    '<path d="M13 2.5H7a2 2 0 0 0-2 2v15a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8.5z"/><path d="M13 2.5v6h6"/><path d="M9 13h6M9 17h4"/>',
  check:   '<path d="M20.5 11.2V12a8.5 8.5 0 1 1-5-7.8"/><path d="m8.6 11.4 2.9 2.9 8-8"/>',
  bolt:    '<path d="M13 2 4 14h6l-1 8 9-12h-6z"/>',
  lock:    '<rect x="4" y="10.5" width="16" height="10.5" rx="2.2"/><path d="M8 10.5V7a4 4 0 0 1 8 0v3.5"/>',
  phone:   '<rect x="6" y="2.5" width="12" height="19" rx="2.6"/><path d="M10.5 18.6h3"/>',
  alert:   '<path d="M12 3.5 21.2 19H2.8z"/><path d="M12 9.5v4M12 16.4v.1"/>',
  clock:   '<circle cx="12" cy="12" r="9"/><path d="M12 7v5.2l3.3 2"/>',
  scan:    '<path d="M4 8V6a2 2 0 0 1 2-2h2M16 4h2a2 2 0 0 1 2 2v2M20 16v2a2 2 0 0 1-2 2h-2M8 20H6a2 2 0 0 1-2-2v-2"/><path d="M4 12h16"/>',
  arrow:   '<path d="M5 12h14M13 6l6 6-6 6"/>',
  eye:     '<path d="M2 12s3.6-6.5 10-6.5S22 12 22 12s-3.6 6.5-10 6.5S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>',
  layers:  '<path d="m12 2.8 9 4.7-9 4.7-9-4.7z"/><path d="m3 12.5 9 4.7 9-4.7"/><path d="m3 17 9 4.7 9-4.7"/>',
  chip:    '<rect x="6.5" y="6.5" width="11" height="11" rx="2.2"/><path d="M10 2.6v3.9M14 2.6v3.9M10 17.5v3.9M14 17.5v3.9M2.6 10h3.9M2.6 14h3.9M17.5 10h3.9M17.5 14h3.9"/>',
  users:   '<circle cx="9" cy="8" r="3.4"/><path d="M2.6 20.4c0-3.5 2.9-5.6 6.4-5.6s6.4 2.1 6.4 5.6"/><path d="M16.5 5.2a3.4 3.4 0 0 1 0 6.6M18 14.9c3 .5 3.4 2.9 3.4 5.5"/>',
  refresh: '<path d="M20.5 11.5A8.5 8.5 0 0 0 6.4 6.2L3.5 9"/><path d="M3.5 4v5h5"/><path d="M3.5 12.5a8.5 8.5 0 0 0 14.1 5.3l2.9-2.8"/><path d="M20.5 20v-5h-5"/>'
};

SP.icon = function (name, size) {
  var d = I[name] || I.grid;
  var s = size || 18;
  return '<svg viewBox="0 0 24 24" width="' + s + '" height="' + s + '" fill="none" ' +
    'stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" ' +
    'aria-hidden="true">' + d + '</svg>';
};

SP.$  = function (s, r) { return (r || document).querySelector(s); };
SP.$$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };

SP.currentFile = function () {
  var p = location.pathname.split('/').pop();
  return (!p || p === '') ? 'index.html' : p;
};

/* --------------------------------------------------------------- the mark --
   A shield drawn from a single path, with the tick cut out rather than
   overlaid, so it holds up at 20px in a browser tab.
   -------------------------------------------------------------------------- */

SP.mark = function (size) {
  var s = size || 32;
  return '<svg class="mk" viewBox="0 0 40 40" width="' + s + '" height="' + s + '" aria-hidden="true">' +
    '<defs><linearGradient id="spm" x1="0" y1="0" x2="1" y2="1">' +
      '<stop offset="0" stop-color="#3a77f5"/><stop offset="1" stop-color="#7a5af8"/>' +
    '</linearGradient></defs>' +
    '<path d="M20 2.5 35 8.7v11c0 8.6-5.9 15.4-15 18.8C10.9 35.1 5 28.3 5 19.7v-11z" fill="url(#spm)"/>' +
    '<path d="M13.4 20.2 18 24.8 27 14.2" fill="none" stroke="#fff" stroke-width="3.2" ' +
      'stroke-linecap="round" stroke-linejoin="round"/>' +
  '</svg>';
};

/* --------------------------------------------------------------- gauge -----
   A filled arc for a 0–15 score. Three ticks mark the tier thresholds so the
   number has somewhere to sit: the reader can see how far past HIGH it is.
   -------------------------------------------------------------------------- */

SP.gauge = function (score, max) {
  max = max || 15;
  var pct = Math.max(0, Math.min(1, score / max));
  var R = 42, C = Math.PI * R;                  // half circumference
  var on = C * pct;
  var col = score >= 10 ? 'var(--bad)' : score >= 6 ? 'var(--warn)' : score >= 3 ? 'var(--accent)' : 'var(--ok)';

  function tick(v) {
    var a = Math.PI - (Math.PI * (v / max));
    var x1 = 54 + Math.cos(a) * (R - 7), y1 = 56 - Math.sin(a) * (R - 7);
    var x2 = 54 + Math.cos(a) * (R + 6), y2 = 56 - Math.sin(a) * (R + 6);
    return '<line x1="' + x1.toFixed(1) + '" y1="' + y1.toFixed(1) + '" x2="' + x2.toFixed(1) +
           '" y2="' + y2.toFixed(1) + '" stroke="#dfe3ea" stroke-width="1.5" stroke-linecap="round"/>';
  }

  return '<div class="gauge">' +
    '<svg viewBox="0 0 108 62" aria-hidden="true">' +
      '<path d="M12 56 A42 42 0 0 1 96 56" fill="none" stroke="#eef1f5" stroke-width="8" stroke-linecap="round"/>' +
      tick(3) + tick(6) + tick(10) +
      '<path d="M12 56 A42 42 0 0 1 96 56" fill="none" stroke="' + col + '" stroke-width="8" ' +
        'stroke-linecap="round" stroke-dasharray="' + on.toFixed(1) + ' ' + C.toFixed(1) + '"/>' +
    '</svg>' +
    '<div class="val" style="color:' + col + '">' + score + '</div>' +
    '<div class="lbl">risk score</div>' +
  '</div>';
};

/* ------------------------------------------------------------ count up -----
   Numbers that arrive are read; numbers that are already there are skipped.
   Only used where the number is the point.
   -------------------------------------------------------------------------- */

SP.countUp = function (el, to, opts) {
  opts = opts || {};
  var dec = opts.decimals || 0;
  var dur = opts.duration || 620;
  var from = 0, t0 = null;
  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    el.textContent = to.toFixed(dec); return;
  }
  function frame(t) {
    if (t0 === null) t0 = t;
    var k = Math.min(1, (t - t0) / dur);
    var e = 1 - Math.pow(1 - k, 3);
    el.textContent = (from + (to - from) * e).toFixed(dec);
    if (k < 1) requestAnimationFrame(frame);
  }
  requestAnimationFrame(frame);
};

/* --------------------------------------------------------------- shell ----- */

SP.shell = function () {
  var here = SP.currentFile();
  var cur = SP.PAGES.filter(function (p) { return p.f === here; })[0] || SP.PAGES[0];
  document.title = cur.t + ' · SatarkPay';

  var nav = SP.PAGES.map(function (p) {
    return '<a href="' + p.f + '"' + (p.f === here ? ' class="on"' : '') + '>' +
      SP.icon(p.i, 17) + '<span>' + p.t + '</span>' +
      (p.f === here ? '<span class="tail">●</span>' : '') + '</a>';
  }).join('');

  var side = document.createElement('aside');
  side.className = 'side';
  side.innerHTML =
    '<a class="brand" href="index.html">' + SP.mark(32) +
      '<span><b>SatarkPay</b><small>Nothing leaves this phone</small></span>' +
    '</a>' +
    '<div class="side-label">Workspace</div>' +
    '<nav class="nav">' + nav + '</nav>' +
    '<div class="side-label">This session</div>' +
    '<div class="nav" id="sideSession"></div>' +
    '<div class="side-foot">' +
      '<span class="badge b-live"><span class="dot pulse"></span>Offline ready</span>' +
      '<div class="small faint" style="margin-top:9px;line-height:1.5">' +
        'Eight pages, three scripts, no server. Verified with the network cut.' +
      '</div>' +
    '</div>';

  var authored = SP.$$('body > *').filter(function (n) {
    return n.tagName !== 'SCRIPT' && n.tagName !== 'STYLE';
  });

  var top = document.createElement('div');
  top.className = 'topbar';
  top.innerHTML =
    '<button id="burger" aria-label="Menu">' + SP.icon('layers', 17) + '</button>' +
    '<div class="crumb">SatarkPay ' + SP.icon('arrow', 12) + ' <b>' + cur.t + '</b></div>' +
    '<div class="right">' +
      '<span class="badge b-partner" id="netBadge"><span class="dot"></span><span id="netText">offline ready</span></span>' +
      '<a class="btn sm primary" href="payment.html">' + SP.icon('shield', 14) + ' Check a payment</a>' +
    '</div>';

  var main = document.createElement('main');
  main.className = 'main';
  main.appendChild(top);
  var body = document.createElement('div');
  body.className = 'content';
  authored.forEach(function (n) { body.appendChild(n); });
  main.appendChild(body);

  var progress = document.createElement('div');
  progress.id = 'progress';

  var app = document.createElement('div');
  app.className = 'app';
  app.appendChild(side);
  app.appendChild(main);

  document.body.appendChild(progress);
  document.body.appendChild(app);

  SP.$('#burger').addEventListener('click', function (e) {
    e.stopPropagation();
    document.body.classList.toggle('nav-open');
  });
  document.body.addEventListener('click', function (e) {
    if (document.body.classList.contains('nav-open') &&
        !e.target.closest('.side') && !e.target.closest('#burger')) {
      document.body.classList.remove('nav-open');
    }
  });
  SP.$$('.nav a').forEach(function (a) {
    a.addEventListener('click', function () { document.body.classList.remove('nav-open'); });
  });

  SP.progressBar();
};

SP.progressBar = function () {
  var el = SP.$('#progress');
  if (!el) return;
  el.classList.add('on');
  el.style.width = '10%';
  [32, 60, 82, 100].forEach(function (w, i) {
    setTimeout(function () { el.style.width = w + '%'; }, 60 * (i + 1));
  });
  setTimeout(function () { el.classList.remove('on'); el.style.width = '0'; }, 660);
};

SP.netBadge = function () {
  var t = SP.$('#netText'), b = SP.$('#netBadge');
  if (!t || !b) return;
  function upd() {
    var on = navigator.onLine;
    t.textContent = on ? 'online' : 'offline — still working';
    b.className = 'badge ' + (on ? 'b-partner' : 'b-live');
  }
  upd();
  window.addEventListener('online', upd);
  window.addEventListener('offline', upd);
};

/* Session memory: enough for the dashboard to reflect what you just did,
   and for the evidence page to carry a verdict forward. Dies with the tab. */

SP.session = {
  get: function (k, d) {
    try { var v = sessionStorage.getItem('sp.' + k); return v == null ? d : JSON.parse(v); }
    catch (e) { return d; }
  },
  set: function (k, v) { try { sessionStorage.setItem('sp.' + k, JSON.stringify(v)); } catch (e) {} },
  clear: function () {
    try {
      Object.keys(sessionStorage).forEach(function (k) {
        if (k.indexOf('sp.') === 0) sessionStorage.removeItem(k);
      });
    } catch (e) {}
  }
};

SP.renderSession = function () {
  var host = SP.$('#sideSession');
  if (!host) return;
  var last = SP.session.get('lastPayment', null);
  var msgs = SP.session.get('msgCount', 0);
  var doms = SP.session.get('domCount', 0);

  host.innerHTML =
    (last
      ? '<a href="payment.html">' + SP.icon('shield', 15) + '<span>' + last.tier + ' · ' + last.score + ' pts</span></a>'
      : '<a href="payment.html" class="faint">' + SP.icon('shield', 15) + '<span>No payment checked</span></a>') +
    '<a href="check.html"' + (msgs ? '' : ' class="faint"') + '>' + SP.icon('message', 15) +
      '<span>Messages</span><span class="tail">' + (msgs || 0) + '</span></a>' +
    '<a href="domain.html"' + (doms ? '' : ' class="faint"') + '>' + SP.icon('link', 15) +
      '<span>Links</span><span class="tail">' + (doms || 0) + '</span></a>';
};

/* --------------------------------------------------------------- boot ------ */

SP.boot = function () {
  SP.shell();
  SP.netBadge();
  SP.renderSession();
  /* Icons are declared in markup as <span data-icon="lock">. The icon is
     prepended to whatever text is there, or appended when data-after is set. */
  SP.$$('[data-icon]').forEach(function (n) {
    var svg = SP.icon(n.getAttribute('data-icon'), +n.getAttribute('data-size') || 18);
    if (n.hasAttribute('data-after')) n.insertAdjacentHTML('beforeend', svg);
    else n.insertAdjacentHTML('afterbegin', svg);
  });
  SP.$$('[data-count]').forEach(function (n) {
    SP.countUp(n, parseFloat(n.getAttribute('data-count')), { decimals: +(n.getAttribute('data-dec') || 0) });
  });

  if ('serviceWorker' in navigator && location.protocol !== 'file:') {
    window.addEventListener('load', function () {
      navigator.serviceWorker.register('sw.js').catch(function () {});
    });
  }
};

if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', SP.boot);
else SP.boot();
