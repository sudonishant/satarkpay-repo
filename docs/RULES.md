# Rules — poora pack (R1–R38)

Machine-readable: [`rules/rules_R15_R25.json`](../rules/rules_R15_R25.json) · [`rules/rules_R26_R38.json`](../rules/rules_R26_R38.json)
R1–R14 deck ke original pack hain (M1 demo me live hain). Ye table human-readable snapshot hai.

## Pack 1 · R15–R25 (core modules)

| Rule | Kya karta hai | Module |
|---|---|---|
| R15 | app-switch + contact-tier + session-duration gate (4-cell matrix) | M1 |
| R16 | tier ke hisaab se friction (1 min / 3 min / 8 min / hold) | M1 |
| R17 | screenshot provenance (source app/domain + opt-in OCR) | M2 |
| R18 | domain trust ladder L1–L4 + gateway≠merchant | M3 |
| R19 | verdict + 3 reasons + KARO/MAT KARO + library citation | M4 |
| R20 | mandate signals (daily/weekly, one-time-claim, naya+unverified) | M5 |
| R21 | intel pipeline (crawl → filter → dedupe → family → novelty → review) | M6 |
| R22 | repeat-payment/staircase guard (24h me 3rd → cooling, 4th → hold) | M2 |
| R23 | matrix ka notification copy + “officer/offer” → T3 escalation | M1 |
| R24 | auto-report pack (NCRP text HI+EN, email, annexure) | M7 |
| R25 | crop · redact · hash (chain of custody) | M7 |

## Pack 2 · R26–R38 (borrowed-ideas + naye families + emergency)

| Rule | Kya karta hai | Type | Honest limit |
|---|---|---|---|
| **R26** | SEBI/NSDL registry check — number format (`INZ/INH/INA + 9 digits`), official portal domain, demo watch-list; “certify” **nahi** karte | rule | live registry download nahi; user ko sebi.gov.in par bhejte hain |
| **R27** | negation-aware verdict — bank ka apna safety-warning scam nahi ginta (dono word-order) | rule | regex-level negation; mixed/sarcastic text “PAKA NAHI” bucket me |
| **R28** | analysis se pehle client-side PII redaction (phone/UPI/aadhaar/account) | privacy | pattern-based — OCR-garbled identifiers bach sakte hain |
| **R29** | shortener + “withdraw instantly” gift-lure flag | rule | legit brands bhi kabhi shortener use karte hain → akela proof nahi |
| **R30** | “pehle 1 ghante” checklist (0–10 / 10–30 / 30–60 min) | procedure | recovery guarantee nahi |
| **R31** | advance-fee family (registration/processing/loan-fee, EMI+legal-threat) | rule | asli lenders fee account se auto-debit karte hain, personal UPI par nahi |
| **R32** | OTP/PIN maangne wala + “warna freeze ho jayega” | rule | asli bank alerts R27 se alag handle hote hain |
| **R33** | sextortion/blackmail dhamki | rule | emotional support flow production me counsellor-routed |
| **R34** | lottery/prize + tax/fee | rule | trigger me “fee/tax” ka hona zaroori |
| **R35** | emergency/voice-clone (“naya number + hospital + urgent paisa”) | rule | asli emergency bhi match karti hai → verify steps wajib |
| **R36** | insider-tip / pump-and-dump pattern | rule | koi market view nahi — sirf tip ke tareeke ka risk |
| **R37** | measured eval harness (33 messages, live P/R/F + miss/FP tags) | measurement | self-authored set = ceiling, field accuracy nahi |
| **R38** | fraud confirm → email draft + helpline dialer + payment-stop (CFCFRMS) + optional family alert | UX/rule | auto-send nahi; user ke tap par |

## Rule ka schema

```json
{
  "id": "R38",
  "feature": "M7 · fraud confirm → turant teen kadam",
  "name": "confirm-fraud emergency action",
  "trigger": "user khud “Fraud CONFIRM hai” dabaye",
  "action": "email draft + dialer + stop-request + optional family alert",
  "tier": "n/a (user-initiated)",
  "status": "UX + RULE",
  "honest_limit": "app khud nahi bhejti/call karti",
  "borrowed_from": "user requirement (3 Oct 2026)"
}
```

Naya rule add karne ka tarika: [`CONTRIBUTING.md`](../CONTRIBUTING.md) (§1) + `rules/rules_R26_R38.json` me append + `web/_part_app.js` ke `RULES4` me family + eval me 1 scam + 1 legit message (parity ke liye).
