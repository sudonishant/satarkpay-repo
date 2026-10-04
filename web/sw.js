/* ============================================================
   SatarkPay — Service Worker
   Cache-first · precache core shell · offline fallback

   Drop into: web/sw.js
   Register:  navigator.serviceWorker.register('./sw.js')
   NOTE: web/build.py chalana mat bhoolna (satarkpay_m2.html usi se banta hai)
   ============================================================ */

const CACHE = 'satarkpay-v1';

/* Core shell — offline ke liye zaroori cheezein.
   Sab single-file HTML hai, isliye ye chhoti list hi kaafi hai. */
const PRECACHE = [
  './',
  './satarkpay_m2.html',
  './deck/deck.html',
  './manifest.webmanifest',
  './_intel.json',
  './icon-192.png',
  './icon-512.png'
];

// ---------- install: precache + turant activate ----------
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE)
      .then((cache) => cache.addAll(PRECACHE).catch((err) => {
        // Ek file missing ho to poora install fail na ho
        console.warn('[SW] precache partial:', err);
        return Promise.all(PRECACHE.map((u) => cache.add(u).catch(() => null)));
      }))
      .then(() => self.skipWaiting())
  );
});

// ---------- activate: purane cache saaf karo ----------
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))
      ))
      .then(() => self.clients.claim())
  );
});

// ---------- fetch: cache-first, network fallback, cache update ----------
self.addEventListener('fetch', (event) => {
  const req = event.request;

  // Sirf GET handle karo (POST = Gemini API, wo hamesha network par jayega)
  if (req.method !== 'GET') return;

  // Cross-origin (CDN, fonts) ko chhod do — cache mat karo
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  event.respondWith(
    caches.match(req).then((hit) => {
      if (hit) {
        // Cache se turant do, saath me background me update bhi karo (stale-while-revalidate)
        fetch(req).then((res) => {
          if (res && res.status === 200) {
            caches.open(CACHE).then((c) => c.put(req, res.clone()));
          }
        }).catch(() => { /* offline hai — chupchap rehne do */ });
        return hit;
      }

      // Cache miss → network → cache me daal do
      return fetch(req).then((res) => {
        if (res && res.status === 200 && res.type === 'basic') {
          const copy = res.clone();
          caches.open(CACHE).then((c) => c.put(req, copy));
        }
        return res;
      }).catch(() => {
        // Offline + cache miss → navigation ho to shell dikhao
        if (req.mode === 'navigate') {
          return caches.match('./satarkpay_m2.html');
        }
        return new Response('', { status: 504, statusText: 'Offline' });
      });
    })
  );
});

// ---------- optional: app se cache clear karne ke liye ----------
self.addEventListener('message', (event) => {
  if (event.data === 'CLEAR_CACHE') {
    caches.delete(CACHE).then(() => self.clients.claim());
  }
});
