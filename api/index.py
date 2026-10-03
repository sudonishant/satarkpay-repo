#!/usr/bin/env python3
"""SatarkPay · Vercel serverless function `/api/index` + fallback entrypoint.

YE FILE KYUN HAI?
  Vercel project `satarkpay-repo-tools` build fail ho raha tha:
      "No python entrypoint found. Set tool.vercel.entrypoint ... or define an
       entrypoint in one of: app.py, ..., api/index.py, ..."
  Vercel entrypoint `app.py` (root) ya `api/index.py` — dono me se koi bhi
  chunta hai. Isliye dono jagah diya hai (root `app.py` me poora app hai).

KAISE KAAM KARTI HAI?
  1. Agar root `app.py` import ho jaye (app-entrypoint mode, poora repo bundle
     hota hai) -> wahi WSGI app serve karta hai, yaani asli demo file.
  2. Agar function-only bundle ho (root app import na ho) -> ek minimal
     stdlib-only WSGI app, jo premium landing deta hai (instant redirect to
     live demo) + `/api/health`. Koi dependency, koi includeFiles config nahi.
"""

import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.normpath(os.path.join(HERE, ".."))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

try:  # mode 1 — root app.py
    from app import app as app  # noqa: F401  (Vercel isi naam ko dhoondta hai)
    SOURCE = "root-app.py"

except Exception:  # mode 2 — inline fallback (stdlib only)
    SOURCE = "inline-fallback"
    LIVE_URL = "https://sudonishant.github.io/satarkpay-repo/"

    LANDING = (
        '<!doctype html><html lang="en"><head><meta charset="utf-8">'
        '<meta name="viewport" content="width=device-width, initial-scale=1">'
        '<meta http-equiv="refresh" content="0; url=' + LIVE_URL + '">'
        "<title>SatarkPay (\u0938\u0924\u0930\u094d\u0915\u092a\u0947) — Live Demo</title>"
        "<style>"
        ":root{--bg:#060a12;--txt:#e8eefc;--dim:#8a94a6;--gold:#c9a86a;--sand:#e6d3ac;--jade:#2fb59a;"
        "--line:rgba(255,255,255,.08)}"
        "body{margin:0;min-height:100vh;display:grid;place-items:center;background:var(--bg);color:var(--txt);"
        'font-family:system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}'
        ".card{text-align:center;padding:2.2rem 2.6rem;border:1px solid var(--line);border-radius:16px;"
        "background:linear-gradient(180deg,rgba(18,28,48,.72),rgba(11,17,30,.9))}"
        "h1{margin:.6rem 0 .35rem;font-size:1.4rem}"
        ".sub{margin:0 0 .9rem;color:var(--dim);font-size:.86rem}"
        ".bar{height:2px;width:120px;margin:.9rem auto;border-radius:2px;"
        "background:linear-gradient(90deg,var(--gold),rgba(201,168,106,.12))}"
        "a{color:var(--sand);text-decoration:none;border-bottom:1px solid var(--gold)}"
        ".meta{margin-top:1.1rem;font-size:.74rem;color:var(--dim);line-height:1.7}"
        ".meta b{color:var(--jade);font-weight:600}"
        "</style></head><body><div class='card'>"
        '<svg width="46" height="46" viewBox="0 0 24 24" fill="none">'
        '<path d="M12 2.5 4 6v6.2c0 4.7 3.3 8.6 8 9.8 4.7-1.2 8-5.1 8-9.8V6l-8-3.5Z" stroke="#c9a86a" stroke-width="1.8"/>'
        '<path d="m8.6 12.2 2.3 2.3 4.5-4.6" stroke="#e6d3ac" stroke-width="2.1" stroke-linecap="round"/></svg>'
        "<h1>SatarkPay <span style='color:var(--dim);font-weight:400'>(\u0938\u0924\u0930\u094d\u0915\u092a\u0947)</span></h1>"
        "<p class='sub'>SANGYAN 2026 · SEBI × NSDL × IIT (BHU) · Team SC\u039bMUR\u039bI</p>"
        "<div class='bar'></div>"
        "<p class='sub'>Live demo khul raha hai…</p>"
        '<p><a href="' + LIVE_URL + '">Yahan click karo agar apne-aap na khule →</a></p>'
        "<p class='meta'><b>Offline bhi chalta hai</b> · 164 KB single file · koi install nahi<br>"
        "Paisa bhejne se pehle 60 second · fraud ke baad 60 minute</p>"
        "</div></body></html>"
    )

    def app(environ, start_response):
        path = (environ.get("PATH_INFO") or "/").rstrip("/") or "/"
        if path in ("/api/health", "/health"):
            body = json.dumps({
                "ok": True,
                "service": "satarkpay-repo-tools",
                "mode": SOURCE,
                "live": LIVE_URL,
            }).encode()
            start_response("200 OK", [("Content-Type", "application/json"), ("Cache-Control", "no-store")])
            return [body]
        start_response("200 OK", [("Content-Type", "text/html; charset=utf-8"),
                                  ("Cache-Control", "public, max-age=60")])
        return [LANDING.encode()]

    handler = app


if __name__ == "__main__":  # local test: python3 api/index.py
    from wsgiref.simple_server import make_server
    print("mode:", SOURCE)
    with make_server("0.0.0.0", 8098, app) as srv:
        print("SatarkPay /api/index WSGI on http://0.0.0.0:8098 (Ctrl+C to stop)")
        srv.serve_forever()
