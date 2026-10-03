<div align="center">

# 🛡 SatarkPay
### UPI fraud se pehle rok do — aur fraud ho jaye to 3 minute me complaint

**SANGYAN** (SEBI × NSDL × SNTC, IIT-BHU) · Track A + B + D · Team **SCΛMURΛI**

</div>

**SANGYAN** (SEBI × NSDL × SNTC, IIT BHU) hackathon submission · Team **SCΛMURΛI** — Nishant Kumar · Prince Singh · Kartik Singh
**Track:** A (fraud/scam resilience) + B (awareness & grievance rights) + D (habits/behaviour)

> **SatarkPay ek chat se payment tak ka guard hai.** Jab aap WhatsApp/Telegram par baat karke seedha UPI app me jaate ho, SatarkPay beech me khada hota hai — contact saved hai ya nahi, baat kitni der chali, payee naya hai ya purana. Phir verdict deta hai, aur agar fraud ho hi gaya to **3 minute me cyber cell complaint + payment stop** ka pura pack ready kar deta hai.

<p align="center">
  <img src="web/deck/screenshots/01a_saved_contact_fastpath.png" width="24%"/>
  <img src="web/deck/screenshots/04_ai_sanchalak.png" width="24%"/>
  <img src="web/deck/screenshots/11_emergency_confirm_action.png" width="24%"/>
  <img src="web/deck/screenshots/06_intel_desk_live.png" width="24%"/>
</p>

---

## ⚡ 60-second quickstart

```bash
# 1) Demo — kuch install nahi chahiye, offline chalega
xdg-open web/satarkpay_m2.html        # ya browser me khol lo

# 2) Automated checks (66) + live eval harness
cd web && ./run_smoke.sh              # npm + jsdom chahiye (script khud install karta hai)

# 3) Deck — ready files, ya source se rebuild
open web/deck/satarkpay_deck.pdf        # 18 slides, light theme
open web/deck/satarkpay_deck.pptx       # editable PowerPoint
cd web/deck && python3 build_deck.py    # rebuild (pip install python-pptx playwright matplotlib)
```

## 📑 Deck (18 slides · PPTX + PDF + HTML)

| File | Kya |
|---|---|
| `web/deck/satarkpay_deck.pdf` | presentation PDF (submission/share ke liye) |
| `web/deck/satarkpay_deck.pptx` | editable PowerPoint (16:9, 18 slides) |
| `web/deck/deck.html` | browser me same deck |
| `web/deck/images/` | 3 AI-generated illustrations (family, chat→UPI, emergency call) |
| `web/deck/diagrams/` | journey map · M1 matrix · R38 emergency flow (SVG + PNG) |
| `web/deck/charts/` | eval confusion matrix · 31-repo landscape |
| `web/deck/screenshots/` | asli demo ke 12 screenshots |

Theme **light** (cream `#FFFBF4` + saffron/teal/indigo); har slide par heading + saffron underline + bottom honest note.
Rebuild guide: [`web/deck/README.md`](web/deck/README.md)

---

## 🧩 Kya bana hua hai (7 modules + emergency layer)

| # | Module | Kya karta hai | Naya rule | Demo tab |
|---|---|---|---|---|
| **M1** | **Chat-Before-Pay Guard** | Saved contact ≈1 min (ya 1-tap call-confirm) → seedha pay · unknown = 8 min guided cooling · 10+ min chat ke turant baad UPI = notification · officer/offer mila to hold + analyst callback | R15, R16, R23 | `01` |
| **M2** | **Screenshot Radar + Provenance** | Payment screenshot ka source (app/domain) + burst detection (30 min me 3+/5+) + same payee ko 3rd payment par cooling (staircase) | R17, R22 | `02` |
| **M3** | **Domain Trust ladder** | L1 verified → L4 fake/lookalike; gateway achha ≠ merchant achha; + SEBI/NSDL registry check | R18, **R26** | `03` |
| **M4** | **AI Sanchalak** | Chat/email/SMS paste karo → 4-bucket verdict + 3 reasons + KARO/MAT KARO; negation-aware; client-side PII redaction; Hindi voice | R19, **R27, R28, R31–R36** | `04` |
| **M5** | **Wallet / AutoPay / Link audit** | Connected apps, mandates, “one-time” batakar daily wala mandate, delegates, one-tap revoke | R20 | `05` |
| **M6** | **Intel Desk (live crawler)** | Aaj: **784 raw → 172 relevant → 63 novel**; analyst 15-sec review ke baad hi publish | R21 | `06` |
| **M7** | **Auto-Report + Evidence pack** | 28 screenshots → crop · OCR · QR decode · redact · SHA-256 → NCRP text (HI+EN) + email + annexure + ZIP | R24, R25 | `07` |
| **★ R38** | **Fraud CONFIRM → 3 kadam** | (1) 📧 cyber cell + bank ko ready email, (2) 📞 1930/1909/bank/bihar-cyber-cell dialer + call script, (3) ⛔ payment-stop request (dispute + CFCFRMS hold + account freeze + mandate revoke) | **R38** | `07` → 🚨 |

**Naya is round me (peer repos se ideas, code zero — credit [`NOTICE.md`](NOTICE.md)):** SEBI registry check, negation handling, PII redaction, Hindi voice, senior mode, measured eval harness, 6 naye scam families, emergency action layer.

---

## 📊 Numbers jo hum claim karte hain (aur unki limit)

| Claim | Value | Honest limit |
|---|---|---|
| Eval precision / recall / F1 | **95% / 90% / 93%** (33 labelled messages: 22 scam + 11 legit) | Set **humne khud likha** hai → ceiling hai, field accuracy nahi. Table me `miss` aur `false-alarm` dono dikhte hain |
| Automated checks | **66/66** (`web/smoke_test.js`, jsdom) | jsdom me `scrollTo` stub hai; asli browser me page errors 0 |
| Deck | 18 slides · PPTX + PDF + HTML ek hi source se | PPTX emoji Windows fonts par depend karte hain; PDF me kuch emoji outline ho jaate hain |
| Intel crawler (aaj ka run) | 784 raw → 172 relevant → 63 novel | Headline-level (article body nahi), DPDP-safe |
| Evidence pack | 28 screenshots · ₹5,000 evidence-visible · 9 scam families | Ye **asli case** hai — raw originals privacy ke liye private rakhe gaye, repo me sirf masked documents hain |
| Peer landscape | 31 repos, 77% “paste message → score”, sirf 23% registry check | Snapshot 3 Oct 2026 ka; licenses 30/31 **NONE** → humne code copy nahi kiya |

---

## 🧠 Architecture (ek nazar me)

```
┌─ Mobile app (Android-first) ────────────────────────────────────────────┐
│  M1 UsageStats gate   M2 MediaStore observer   M5 NotificationListener  │
│  M3 Domain engine     M4 Rules + verdict       M7 Evidence pack         │
│  on-device first · network optional (registry/intel OTA)                │
└───────────────┬─────────────────────────────────────────────────────────┘
                │  (sirf: extracted entity + hash + verdict code)
┌───────────────┴───────────────┐   ┌──────────────────────────────────┐
│ Analyst Console (web)         │   │ Intel crawler (python, cron)     │
│ queue · case · review · regis.│◄──┤ RSS + Google News → novel → queue│
└───────────────────────────────┘   └──────────────────────────────────┘
```

Poora detail: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) · Rules: [`rules/`](rules) · UI spec: [`docs/UI_SPEC.md`](docs/UI_SPEC.md)

---

## 🗂 Repo structure

```
satarkpay/
├── README.md                  ← aap yahan ho
├── LICENSE                    ← MIT (code ke liye)
├── NOTICE.md                  ← peer repo credits + license position
├── web/                       ← offline demo (asli deliverable)
│   ├── satarkpay_m2.html      ← ★ ek file, browser me kholo
│   ├── _base.html, _part_*.html, _part_app.js, build.py
│   ├── smoke_test.js, run_smoke.sh
│   └── deck/                  ← PPTX + PDF + HTML deck, charts, diagrams, screenshots
├── app/                       ← Android skeleton (Kotlin) + manifest/permission notes
├── eval/                      ← 33-message labelled set + harness (live P/R/F)
├── rules/                     ← R15–R25 + R26–R38 (machine-readable JSON)
├── tools/                     ← crawler, evidence toolkit (crop/ocr/report)
├── data/                      ← intel snapshot + sanitized case-pack documents
└── docs/                      ← UI_SPEC, ARCHITECTURE, PRIVACY, RULES, DEMO_SCRIPT, ROADMAP…
```

---

## 🔐 Privacy (ye claim nahi, design hai)

- **PII client-side redaction** — phone/UPI/aadhaar/account mask hokar hi analysis me jaate hain (R28). Demo me **network call zero**.
- **Screenshot kabhi store nahi** — sirf extracted entity + hash, 7 din.
- **OCR opt-in**, image device par.
- **Koi SMS / Contacts / Accessibility / `QUERY_ALL_PACKAGES` nahi.**
- **Koi auto-send nahi** — email/call/payment-stop sab user ke tap par (R38).
- Poora detail: [`docs/PRIVACY.md`](docs/PRIVACY.md) · vulnerabilities: [`SECURITY.md`](SECURITY.md)

---

## 🚫 Kya hum nahi karte (guardrails)

Koi stock tip / buy-sell-hold / price prediction / trading algo **nahi** · koi monetisation funnel **nahi** · koi SMS/OTP/PII harvesting **nahi** · koi auto-block **nahi** (payment rukta hai, final call user ki) · koi “SEBI registered ✅” certificate **nahi** (hum format + domain + list check karte hain aur sebi.gov.in par bhejte hain) · koi recovery guarantee **nahi**.

---

## 🗺 Roadmap (48 ghante → scale)

`h0–4` Android skeleton (UsageStats + MediaStore + consent) · `h4–12` M1 matrix + M3 engine shared module · `h12–20` M4 real LLM + library RAG · `h20–30` M5 NotificationListener mandate parse · `h30–40` M6 cron + console, M7 PSP/bank exports · `h40–48` red-team + Hindi copy user-test + demo rehearsal.

Detail: [`docs/ROADMAP.md`](docs/ROADMAP.md)

---

## 👥 Team + submission

**SCΛMURΛI** — Nishant Kumar · Prince Singh · Kartik Singh
Live demo + 3–5 min video + deck · SANGYAN submission deadline: **4 Oct 2026, 11:59 PM IST**

**Credits:** peer repos se sirf *ideas* liye (licenses nahi hain — 30/31 NONE) → [`NOTICE.md`](NOTICE.md) · OSINT scan → [`docs/OSINT_SUMMARY.md`](docs/OSINT_SUMMARY.md)
**Verification log:** [`docs/VERIFICATION.md`](docs/VERIFICATION.md) · **Changes:** [`CHANGELOG.md`](CHANGELOG.md) · **Repo map:** [`docs/REPO_MAP.md`](docs/REPO_MAP.md)

> “Paisa bhejne se pehle 60 second — aur fraud ke baad 60 minute. Dono humare paas hain.”
