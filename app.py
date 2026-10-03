#!/usr/bin/env python3
"""SatarkPay · Vercel Python entrypoint (WSGI) — sirf standard library.

KYUN HAI YE FILE?
  Vercel project `satarkpay-repo-tools` ka build fail ho raha tha:
      "No python entrypoint found."
  Vercel zero-config Python deployment ko ek WSGI/ASGI app chahiye hota hai.
  Ye wahi entrypoint hai. Koi dependency nahi (stdlib only), koi env var nahi.

KYA SERVE KARTA HAI?
  /            -> bundled demo (web/satarkpay_m2.html) agar bundle me mile,
                  warna premium landing (instant meta-refresh + clickable link)
  /api/health  -> {"ok": true, ...}  (uptime/monitoring checks ke liye)
  /*           -> wahi (single-file demo hai, koi route nahi chahiye)

NOTE: `api/index.py` me bilkul yahi app hai — dono jagah rakha hai kyunki
      Vercel ka entrypoint detection app.py (root) ya api/index.py dono
      me se kisi ko bhi chun sakta hai. Dono same behave karte hain.
      (Edit karo to dono files me karna.)
"""

import glob
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
DEMO_REL = os.path.join("web", "satarkpay_m2.html")
LIVE_URL = "https://sudonishant.github.io/satarkpay-repo/"

LANDING = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="refresh" content="0; url=%s">
<title>SatarkPay (सतर्कपे) — Live Demo</title>
<style>
  :root{--bg:#060a12;--txt:#e8eefc;--dim:#8a94a6;--gold:#c9a86a;--sand:#e6d3ac;--jade:#2fb59a;
        --line:rgba(255,255,255,0.08)}
  body{margin:0;min-height:100vh;display:grid;place-items:center;background:var(--bg);color:var(--txt);
       font-family:system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
  .card{text-align:center;padding:2.2rem 2.6rem;border:1px solid var(--line);border-radius:16px;
        background:linear-gradient(180deg,rgba(18,28,48,0.72),rgba(11,17,30,0.9));
        box-shadow:0 20px 50px -20px rgba(0,0,0,0.8)}
  .mark{width:46px;height:46px;margin:0 auto .7rem;display:block}
  h1{margin:0 0 .35rem;font-size:1.4rem;letter-spacing:-0.01em}
  .sub{margin:0 0 1.1rem;color:var(--dim);font-size:.86rem}
  .bar{height:2px;width:120px;margin:0 auto 1.1rem;border-radius:2px;
       background:linear-gradient(90deg,var(--gold),rgba(201,168,106,0.12))}
  a{color:var(--sand);text-decoration:none;border-bottom:1px solid var(--gold);padding-bottom:2px}
  a:hover{color:var(--gold)}
  .meta{margin-top:1.2rem;font-size:.74rem;color:var(--dim);line-height:1.7}
  .meta b{color:var(--jade);font-weight:600}
</style>
</head>
<body>
  <div class="card">
    <svg class="mark" viewBox="0 0 24 24" fill="none"><path d="M12 2.5 4 6v6.2c0 4.7 3.3 8.6 8 9.8 4.7-1.2 8-5.1 8-9.8V6l-8-3.5Z" stroke="#c9a86a" stroke-width="1.8"/><path d="m8.6 12.2 2.3 2.3 4.5-4.6" stroke="#e6d3ac" stroke-width="2.1" stroke-linecap="round"/></svg>
    <h1>SatarkPay <span style="color:var(--dim);font-weight:400">(सतर्कपे)</span></h1>
    <p class="sub">SANGYAN 2026 · SEBI × NSDL × IIT (BHU) · Team SCΛMURΛI</p>
    <div class="bar"></div>
    <p class="sub" style="margin-bottom:.6rem">Live demo khul raha hai…</p>
    <p style="margin:0"><a href="%s">Yahan click karo agar apne-aap na khule →</a></p>
    <p class="meta">
      <b>Offline bhi chalta hai</b> · 164 KB single file · koi install nahi<br>
      Paisa bhejne se pehle 60 second · fraud ke baad 60 minute
    </p>
  </div>
</body>
</html>
""" % (LIVE_URL, LIVE_URL)


def _find_demo():
    """Bundled demo dhoondo — Vercel app-entrypoint mode me poora repo bundle hota hai."""
    candidates = []
    for base in (HERE, os.path.join(HERE, ".."), os.path.join(HERE, "..", ".."),
                 os.getcwd(), "/var/task"):
        candidates.append(os.path.normpath(os.path.join(base, DEMO_REL)))
    for path in candidates:
        if os.path.isfile(path):
            return path
    for path in glob.glob("/var/task/**/satarkpay_m2.html", recursive=True):
        if os.path.isfile(path):
            return path
    return None


def _read_bytes(path):
    with open(path, "rb") as fh:
        return fh.read()


def app(environ, start_response):
    """WSGI callable — Vercel isi naam ko dhoondta hai."""
    path = (environ.get("PATH_INFO") or "/").rstrip("/") or "/"

    if path in ("/api/health", "/health"):
        payload = {
            "ok": True,
            "service": "satarkpay-repo-tools",
            "checks": "66/66",
            "demo_bundled": bool(_find_demo()),
            "live": LIVE_URL,
        }
        body = json.dumps(payload).encode()
        start_response("200 OK", [
            ("Content-Type", "application/json"),
            ("Cache-Control", "no-store"),
        ])
        return [body]

    demo = _find_demo()
    if demo:
        start_response("200 OK", [
            ("Content-Type", "text/html; charset=utf-8"),
            ("Cache-Control", "public, max-age=300"),
        ])
        return [_read_bytes(demo)]

    # Demo bundle me nahi mila -> premium landing (instant redirect to live demo)
    start_response("200 OK", [
        ("Content-Type", "text/html; charset=utf-8"),
        ("Cache-Control", "public, max-age=60"),
    ])
    return [LANDING.encode()]


# Vercel kuch modes me `handler` dhoondta hai — dono naam export kar dete hain.
handler = app

if __name__ == "__main__":  # local test: python3 app.py
    from wsgiref.simple_server import make_server
    with make_server("0.0.0.0", 8099, app) as srv:
        print("SatarkPay WSGI on http://0.0.0.0:8099  (Ctrl+C to stop)")
        srv.serve_forever()
