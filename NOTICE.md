# NOTICE — idea credits, license position, aur "kya nahi liya"

## 1 · License position (kyun zero code copying)

3 Oct 2026 par SANGYAN se jude **31 public peer repos** scan kiye (`docs/OSINT_SUMMARY.md`, `data/repo_matrix.csv`, `data/repo_licenses.json`):

| License | Repos |
|---|---|
| **NONE** (koi license file nahi = default *all rights reserved*) | **30** |
| MIT | 1 — `anuragtiwari-ux/Sangyan-Suraksha` |

**Faisla:** kisi bhi peer repo se code, UI, strings, assets, dataset ya text **copy nahi kiya**. Sirf *ideas* liye aur apna implementation likha. MIT wale se bhi code nahi uthaya — sirf concept + credit.

## 2 · Idea credits (concept → SatarkPay feature)

| Peer repo (public) | Concept | SatarkPay me |
|---|---|---|
| `prakshithamalla-art/scam-shield-sangyan` | negation-aware matching | **R27** — dono word-order handle; bank ka apna OTP-warning SEEMS OK |
| `adityakr-git/sangyan` ("Ruko") | client-side PII redaction + local JSON lists | **R28** `redactPII()`; R26 ke registry lists inline (koi server call nahi) |
| `patelsachin9879-gif/Sangyan-Kavach`, `pyharshcodes/Sangyan_hackathon` | registration-number format/checksum validator | **R26** `sebiCheck()` — `INZ/INH/INA + 9 digits` + official-domain + watch-list |
| `anuragtiwari-ux/Sangyan-Suraksha` (**MIT**) | labelled test set par measured score; "pehle 1 ghante" recovery | **R37** eval harness (33 messages, live P/R/F + miss/false-alarm tags); **R30** first-hour checklist |
| `surdish/scamshield-bharat` | senior mode | senior mode (font + contrast + tap targets) — **S11** |
| `arcimillion/nivesh` | voice-first output | Hindi voice (Web Speech `hi-IN`) — `🔊 voice` toggle |
| `Algo-explorer/Grievance-Clock`, `hafsakhan09090/haq-sangyan` | escalation ladder + follow-ups | SCORES → SMART ODR rows + follow-up checklist (M7/S10) |
| `Kshitijsawant17/PATIBIMB` | deterministic manipulation patterns (bina AI) | **R31–R36** families (advance-fee, OTP-maang, sextortion, lottery-fee, emergency/voice-clone, insider-tip) |
| `Enky-yy/sangyan-sebi` | registry-check ko P0 banana | R26 + M3 ka registry card |
| `subdil/-SurakshaLens-…` | mobile packaging precedent | roadmap ka Android path (`app/`) |

## 3 · Kya jaan-boojh ke NAHI liya

- Koi peer **code / dataset / UI / asset / string** nahi.
- Peer demos ke **numbers quote nahi kiye** — humne apna self-authored 33-message set chalaya aur jo aaya wahi likha (P 95% / R 90% / F1 93%), limits ke saath.
- Koi peer demo site hit nahi ki — sirf public GitHub metadata + README padhe.
- **Name collision:** `adityagrinds/SangyanAI` aur `SangyanAI-2` disaster-response projects hain — competitor count me nahi gine.

## 4 · External APIs & data

| Cheez | Source | Use |
|---|---|---|
| Google News RSS, RBI/Google feeds | public, keyless | M6 crawler (`tools/intel_crawler.py`) |
| SEBI SCORES / SMART ODR / NCRP / 1930 | official portals | complaint routing (deep-links, koi scraping nahi) |
| Bihar cyber cell contacts | public police notices | R38 email presets (editable) |
| Real fraud case (28 screenshots) | team member ka apna case | M7 pipeline test — repo me sirf **masked** documents |

## 5 · Third-party libraries

Python: `playwright` (deck render), `python-pptx`, `matplotlib`, `reportlab`, `pymupdf`, `rapidocr-onnxruntime`, `opencv-python`, `Pillow`.
Web demo: **koi library nahi** — zero dependency, zero CDN (jaan-boojh ke, taki offline/sandbox me chale).
Node (sirf test): `jsdom`.
Sabki licenses unke apne repos me hain; ye repo unhe distribute nahi karta (sirf import karta hai).
