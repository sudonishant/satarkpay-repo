# Vercel deployment — fix notes (Oct 2026)

## Problem
Vercel project **`satarkpay-repo-tools`** ka production build fail ho raha tha:

```
Build error — No python entrypoint found. Set "tool.vercel.entrypoint" in
pyproject.toml or define an entrypoint in one of: app.py, index.py, ..., api/index.py
```

**Kyun hua:** repo me `.py` files (tools/, eval/, web/) hone ki wajah se Vercel ne project ko
*Python app* maana — par koi WSGI/ASGI entrypoint nahi tha. Isliye deployment kabhi
bana hi nahi (`DEPLOYMENT_NOT_FOUND`).

## Fix (iss repo me kya add hua)
| File | Kaam |
|---|---|
| `app.py` | Root WSGI entrypoint — bundled demo (`web/satarkpay_m2.html`) serve karta hai; demo na mile to premium landing (instant redirect). `/api/health` bhi. |
| `api/index.py` | Vercel function + fallback entrypoint. Root `app.py` import ho to wahi app; warna stdlib-only minimal app (landing + health). |
| `vercel.json` | `/`, `/demo`, `/health` ko `api/index` par rewrite karta hai (function-only mode ke liye). |

Sab kuch **stdlib-only** hai — koi dependency, koi env var, koi build step nahi.

## Verify (local, same commands)
```bash
python3 app.py            # -> http://localhost:8099  (poora demo)
python3 api/index.py      # -> http://localhost:8098  (/api/health JSON)
curl -s localhost:8099/api/health
```

## Deploy ke baad
- Production URL: `https://satarkpay-repo-tools.vercel.app` (auto-deploy on push to `main`)
- Note: **primary live link abhi bhi GitHub Pages hai** —
  https://sudonishant.github.io/satarkpay-repo/ — Vercel iska mirror/backup hai.

## Agar Vercel project ki zarurat nahi hai
Vercel dashboard → project `satarkpay-repo-tools` → **Settings → Delete Project**.
Uske baad failure emails aana band ho jayenge. (GitHub Pages par koi asar nahi.)
