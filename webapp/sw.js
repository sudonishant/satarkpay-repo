/* ==========================================================================
   SatarkPay — service worker
   Cache-first for the whole shell. The app is the same set of files every
   time, so there is nothing to be clever about: precache them and serve
   from cache. Update by bumping VERSION.
   ========================================================================== */

var VERSION = 'satarkpay-v6';

var ASSETS = [
  './',
  'index.html',
  'payment.html',
  'check.html',
  'domain.html',
  'library.html',
  'evidence.html',
  'emergency.html',
  'about.html',
  'assets/app.css',
  'assets/app.js',
  'assets/engine.js',
  'assets/chain.js',
  'assets/data.js',
  'assets/icon.svg',
  'assets/scams/digital-arrest.jpg',
  'assets/scams/kyc-apk.jpg',
  'assets/scams/qr-refund.jpg',
  'assets/scams/fake-job.jpg',
  'manifest.webmanifest'
];

self.addEventListener('install', function (e) {
  e.waitUntil(
    caches.open(VERSION).then(function (c) {
      /* Add one at a time so a single missing file cannot abort the install
         and leave the app with no offline shell at all. */
      return Promise.all(ASSETS.map(function (u) {
        return c.add(new Request(u, { cache: 'reload' })).catch(function () { return null; });
      }));
    }).then(function () { return self.skipWaiting(); })
  );
});

self.addEventListener('activate', function (e) {
  e.waitUntil(
    caches.keys().then(function (keys) {
      return Promise.all(keys.map(function (k) {
        return k === VERSION ? null : caches.delete(k);
      }));
    }).then(function () { return self.clients.claim(); })
  );
});

self.addEventListener('fetch', function (e) {
  var req = e.request;
  if (req.method !== 'GET') return;

  var url = new URL(req.url);
  if (url.origin !== location.origin) return;

  e.respondWith(
    caches.match(req, { ignoreSearch: true }).then(function (hit) {
      if (hit) return hit;
      return fetch(req).then(function (res) {
        if (res && res.ok && res.type === 'basic') {
          var copy = res.clone();
          caches.open(VERSION).then(function (c) { c.put(req, copy); });
        }
        return res;
      }).catch(function () {
        /* Last resort for a navigation while offline. */
        if (req.mode === 'navigate') return caches.match('index.html');
        return new Response('', { status: 504, statusText: 'offline' });
      });
    })
  );
});
