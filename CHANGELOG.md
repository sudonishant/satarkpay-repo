# Changelog

Sab notable changes, naye se purane. Format: `date · version — kya`.

## 2026-10-03 · v1.1 — UI pass + deck v2 + live polish

### Naya (iss session)
- **Mobile UI pass** — header controls ek scrollable row (pehle 4 rows kha rahe the), tabs **sticky + horizontal scroll**, KPI cards snap-scroll carousel, phone mockup `max-height:56vh` (judge ko Run button pehli screen par dikhta hai), bade tap targets (chip ≥10px pad, buttons ≥38px) — Tier-2/3 usability ke liye
- **Judge branding** — header: `SANGYAN · SEBI × NSDL × IIT (BHU) · Track A + B`; footer me guardrails line + Team names + live URL
- **KPI consistent** — `<12ms LATENCY` → **`0.012ms MEDIAN`** (measured, VERIFICATION §8 se match)
- **Deck v2 numbers** — cover: 2,443 reg / 51 repos; landscape chart 51-repo data (71% checker · 24% live demo · 8% measured eval); "verified ✓" markers
- **15 screenshots regenerate** — naya UI (header/judge branding/sticky tabs) ke saath; `shoot_new_shots.py` ab reproducible (paths fixed, full set scripted)
- **17 absolute paths fixed** — deck.html images (`/home/user/…` baked paths → relative) + build script root cause (`HERE.resolve()` + `hsrc()`)

---

## 2026-10-03 · v1.0 — SANGYAN submission (final)

### Naya (aaj ka kaam)
- **R38 · Fraud CONFIRM emergency layer** (aapka ask): confirm hone par teen kadam — (1) 📧 cyber cell + bank ko **ready email draft** (Bihar cyber cell 3 IDs + nodal officer cc, body me txn/UTR/amount/annexure), (2) 📞 **one-tap dialer** (1930 · 1909 · bank/PSP · Bihar cyber cell) + 5-line call script, (3) ⛔ **payment-stop request** (txn-wise dispute/chargeback + beneficiary par hold (CFCFRMS) + account freeze/limit + mandate revoke) + optional step 4 parivaar/community alert (masked numbers). Golden-hour clock. Auto-send **nahi** — sab user ke tap par. → `web/_part_app.js`, `web/_part_m3m6.html`, rules `R38`
- **Deck poora naya (light theme)** — black/blue hata ke cream `#FFFBF4` + saffron/teal/indigo; 18 slides; ek content model se **PPTX + PDF + HTML**; 2-column bullets; auto-height diagram boxes → `web/deck/`
- **3 AI-generated illustrations** (family + shield, chat→UPI hourglass, emergency call) → `web/deck/images/`
- **Diagrams naye**: journey (3 lanes) · M1 matrix · R38 emergency flow (SVG + PNG, light) → `web/deck/diagrams/`
- **Charts light theme**: eval confusion matrix + buckets · 31-repo coverage → `web/deck/charts/`
- **UI_SPEC.md** — screen-by-screen blueprint (S0–S11 + analyst console C1–C4), wireframes, states/copy, 5 mermaid flows, permissions matrix, acceptance checklist
- **eval/ harness** — 33-message labelled set + `run_eval.js`/`run_eval.py` (jsdom), live P/R/F, `latest_results.json`
- **app/android-skeleton/** — Kotlin skeleton (GateEngine, VerdictEngine, DomainEngine, EvidenceBuilder, EmergencyActions (R38), signals, consent, Room store, manifest), har file me `TODO(finale)` + port order
- **tools/** — crawler + evidence pipeline scripts + `requirements.txt`
- **docs/** — ARCHITECTURE · PRIVACY (DPDP checklist) · RULES (R1–R38 table + schema) · DEMO_SCRIPT (3-min beats + judge Q&A) · PLAY_STORE_NOTES · ROADMAP · SUBMISSION · OSINT_SUMMARY · VERIFICATION · REPO_MAP
- **Repo meta** — LICENSE (MIT), NOTICE (idea credits + license position), CONTRIBUTING, CODE_OF_CONDUCT, SECURITY, Makefile, publish.sh, `.github/` (CI + issue/PR templates)
- **R26–R38 rule pack** — registry check (format INZ/INH/INA + 9 digits), negation-aware handling (R27), client-side PII redaction (R28), 6 naye scam families (advance-fee, OTP-maang, sextortion, lottery-fee, emergency/voice-clone, insider-tip), eval harness (R37), emergency action (R38)
- **Hindi voice + senior mode** — Web Speech `hi-IN` + bada font/high contrast toggles

### Badla (fixes)
- JS bug: template literal me unescaped backtick → poora script `ReferenceError` de raha tha → fix
- Smoke test ke chip selectors index-based the (naye chips add hone par fail) → `data-e` attribute-based
- M4 engine ek jagah consolidate: `classify()` — ab chat, eval harness aur console **wahi** function use karte hain
- SEBI registry: “SEBI awareness” message par jhoothi flag aati thi → money-ask ke saath hi flag
- Negation dono word-order handle: “kabhi bhi OTP share mat karo” + “OTP share karne ko nahi kahenge”

### Gaadi (policy decisions)
- **Zero code copying** — 30/31 peer repos par license hi nahi (all rights reserved), 1 MIT → sirf ideas + `NOTICE.md` credit
- Koi auto-block / auto-send / SMS-access / Accessibility abuse **nahi** (guardrail set)

## 2026-10-02 · v0.9 — M2 module pack (base)
- M1 Chat-Before-Pay (4-cell matrix), M2 Screenshot Radar + R22 staircase, M3 Domain Trust ladder, M4 AI Sanchalak, M5 Wallet/AutoPay audit, M6 Intel Desk (live crawler), M7 Auto-Report (crop/OCR/QR/redact/hash → NCRP + email pack)
- Asli fraud case par evidence pipeline (28 files, hashes, complaint drafts)
- Rules R15–R25 + deck ke R1–R14
