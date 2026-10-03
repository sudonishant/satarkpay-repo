# OSINT REPORT — SANGYAN Investor Resilience Hackathon (SNTC, IIT BHU × SEBI × NSDL)

**Taiyaar kiya:** 3 Oct 2026 · **Method:** Unstop listing + official Problem-Statement PDF + GitHub API sweep (9 queries, 47 unique repos) + top repos ke READMEs ka manual read
**Files is folder me:** `problem_statement.pdf` (official, 6 pages) · `problem_statement.txt` · `repo_matrix.csv` (31 repos ka structured data) · `repo_matrix.json`

---

## 1 · TL;DR (30 second)

- **Event:** 4-day online hackathon, **SNTC IIT (BHU) Varanasi × SEBI × NSDL**, 1–4 Oct 2026. Team size 1–4. Applied-AI category.
- **Deadline:** submission **4 Oct 2026, 23:59 IST** (Unstop: 4 Oct 14:29 EDT = 18:29 UTC). Shortlisting 4 Oct → final eval 5 Oct → **results 6 Oct**.
- **Prizes:** Winner **₹1,00,000** · 1st RU **₹50,000** · 2nd RU **₹20,000** (+ certificates). Unstop header "₹1,70,000" likhta hai, description me "₹1,75,000 pool" — organisers ka inconsistency, ₹1.7L hi lagta hai.
- **Scale:** Unstop par **2,364 registrations** dikh rahi hain (individual count, team count nahi). Discord: `discord.gg/Q69UG3cWq`.
- **Submission chahiye:** **live demo link + 3–5 min video + PPT**. (Yahin sabse bada gap hai — neeche §5.)
- **Competition ka asli shape:** GitHub par **31 relevant repos** mile. **77% ek hi cheez bana rahe hain** — "message/link/screenshot paste karo → risk score" checker. **21/31 ke paas live demo link hi nahi hai.**
- **Sabse bada khaali maidan:** pre-transaction **interception** (paisa jaane se pehle rokna), **Track D** (habits/cooling-off), **evidence-grade recovery pack**, **offline/IVR/USSD**, aur **SEBI-registry verification** (sirf 23% kar rahe hain).

---

## 2 · Event facts (verified)

| Cheez | Detail | Source |
|---|---|---|
| Organisers | SNTC, IIT (BHU) Varanasi + **SEBI** + **NSDL** | Unstop listing, PS PDF cover |
| Format | Online, 4-day build sprint (1–4 Oct 2026) | Unstop |
| Tracks | **5 focus tracks (A–E) + 1 Open Track** | PS PDF p.1, p.3–5 |
| Team | 1–4 members | Unstop |
| Registration | 26 Sep onboarding · deadline 4 Oct 23:59 IST | Unstop |
| PS release | 29 Sep (orientation) · build sprint 30 Sep se | Unstop |
| Submission | 1 Oct → **4 Oct 23:59 IST** (Unstop round) | Unstop |
| Judging | Shortlist 4 Oct → final evaluation 5 Oct → **results 6 Oct** | Unstop |
| Prize pool | ₹1,00,000 / ₹50,000 / ₹20,000 (+ participation certificates) | Unstop |
| Registered | 2,364 (Unstop counter) | Unstop |
| Discord | https://discord.gg/Q69UG3cWq | Unstop + PS p.6 |
| Official attachment | `PS_Investor_Resilliance.pdf` (6 pages) | Unstop downloads |

---

## 3 · Official Problem Statement — decode (PS PDF se)

**Problem:** India me **16+ crore demat accounts**, incremental me **70%+ non-metro/Tier-2/3** se — "access ne confidence ko overtake kar liya". SEBI study: **9/10 individual F&O traders net loss** me.

**Mission:** aisa product banao jo **investor resilience** badhaye — **before / during / after** decision. Focus: Tier-2/3, regional-language users, seniors, scam-nearly-victims.

### Focus tracks

| Track | Focus | Suggested directions (PS se) |
|---|---|---|
| **A · Digital Fraud & Scam Resilience** | Deceptive vectors ko **paisa jaane se pehle** detect/warn/intercept | Scam & Claim Verifier · Tip-Group Risk Profiler |
| **B · Investor Awareness, Rights & Grievance** | SCORES, nominee, IEPF jaise process ko first-timer ke liye usable banana | Grievance Assistant · Nominee & Family Wealth Tracker |
| **C · Investor Education for Bharat** | Jargon-heavy disclosure ki jagah samajh-first regional learning | Voice-First Explainer · Consequence Simulator |
| **D · Financial Habits & Behavioural Resilience** | Impulse/FOMO/loss-chasing par pause aur discipline | **Cooling-Off Circuit Breaker** · Decision Journal |
| **E · Misinformation & Financial Content Literacy** | Education vs promotion, confident-claim vs evidence-backed | Claim Evidence-Checker · Promotion-vs-Education Classifier |
| **Open** | Kuch bhi jo resilience badhaye | Accessibility-first (elderly/visually impaired) · **IVR/USSD/offline** · **Account Aggregator/DigiLocker/Bhashini** · community fraud-reporting circles · corporate actions ka plain-language |

### Guardrails — disqualified karne wale (PS p.3, verbatim-ish)

1. **No stock tips / buy-sell-hold / price prediction / trading algo / kisi instrument-broker ka promotion.**
2. **No monetisation** — broking commission, margin nudges, paid upsell.
3. **Privacy by design** — **"no unauthorised harvesting of SMS, OTPs, or personally identifiable financial records."** ← *ye line hamare kuch existing modules ke liye important hai (neeche §6).*
4. **Public-good ethos** — investor-protection infrastructure lagna chahiye, growth product nahi.

### Evaluation weights (yahi targeting karna hai)

| Criterion | Weight | Judges kya dekhenge |
|---|---|---|
| **Resilience & Safety Impact** | **30%** | "Measurably" fraud/loss rokna, safer behaviour |
| **Tier-2/3 Usability (Bharat-First)** | **25%** | Regional language, low-bandwidth/low-end device, voice/visual UX, low cognitive load |
| **Guardrail Compliance & Trust** | **15%** | Non-commercial, no tips, uncertainty transparent, privacy |
| **Technical Execution** | **15%** | AI/ML/NLP/voice/on-device — "useful, novelty ke liye nahi" |
| **Feasibility & Scalability** | **15%** | Hackathon ke baad asli investors tak pahunch sakta hai |

> PS khud kehta hai: **"Depth on one real user journey is valued over breadth across many features"** — ek journey, end-to-end, gehrai se.

**Expected submission (PS p.5):** Live Demo Link + **3–5 min video** (realistic user scenario end-to-end) + **PPT** (stack, user journey).

---

## 4 · GitHub landscape (OSINT ka core)

**Method:** GitHub Search API — 9 queries (`sangyan`, `sangyan hackathon`, `sangyan 2026`, `SANGYAN Investor Resilience`, `SEBI NSDL hackathon`, `SNTC IIT BHU`, `investor resilience hackathon`, `Ruko sangyan`, `scam detection SEBI`) → 47 unique repos → relevance filter → **31 repos** ka metadata + README + file-tree pull (raw data: `repo_matrix.csv`).

### Numbers

| Metric | Value |
|---|---|
| Relevant repos | **31** (baaki 16 name-collision/noise) |
| Languages | Python 11 · JS 5 · TypeScript 5 · HTML 4 · Java 2 · Dart 1 · khaali/README-only 3 |
| **"Checker" archetype** (paste message/link/screenshot → risk score) | **24/31 (77%)** |
| Regional language | 22/31 (71%) — Hindi sabse common, phir Tamil/Telugu/Bengali |
| Voice/TTS/IVR/USSD | 16/31 (52%) |
| OCR (screenshot) | 12/31 (39%) |
| LLM use | 11/31 (35%) — Gemini/Groq/GPT; kuch deliberately **rule-based, no-AI** |
| SEBI registry/verification | **7/31 (23%)** ← gap |
| 1930/SCORES/complaint mention | 18/31 (58%) |
| Privacy/on-device claim | 16/31 (52%) |
| **Live demo link** | **10/31 (32%)** ← bada gap |
| Track keywords (heuristic) | B:16 · A:13 · C:13 · E:7 · D:7 (multi-track claims common) |

### Notable entries (jinke paas live demo hai ya approach strong hai)

| Repo | Track | Stack | USP / standout |
|---|---|---|---|
| `pyharshcodes/Sangyan_hackathon` (Kavach) | A + E + C | TS/Next, Vercel live | **5-layer forensic pipeline**: client-side redaction → claim extractor → **SEBI registry checksum validator** → cloned-domain phishing heuristics → misinformation classifier. Rubric-conformance matrix likely |
| `adityakr-git/sangyan` (Ruko) | A B C D E (sab!) | TS, Vercel live | SEBI datasets bundle (`sebi_debarred.json`, `sebi_entities.json`, `sebi_prefixes.json`, `sebi_regulations.json`) + multimodal (text/voice/screenshot) + client-side PII redaction |
| `Utsav006/sangyan-shield` | A + C | Flask + React PWA + sklearn | **"For the moment you can't copy-paste"** — screenshot-first flows, `sebi_intermediaries.json` dataset, OCR + registry + explain |
| `mdasif-x1/investor-safety-system` | A + E | Next.js 14 + **Spring Boot 3.3 / Java 21** | Tesseract.js **client-side WASM OCR**, 5-stage analysis, "anti-AI visual language" (judges ko institutional lagna) |
| `Kshitijsawant17/PATIBIMB` | A/E/habits | Next.js | **Deterministic, no-AI/API** — 25+ manipulation patterns, "Reality Replay", "Teach-Back" loop (behaviour angle) |
| `subdil/-SurakshaLens` | A | **Flutter app + APK** + Node backend | Mobile app + release APK + SEBI documents ke page-citations wale answers |
| `prakshithamalla-art/scam-shield-sangyan` | A | Python FastAPI | **Negation-aware** rules (bank ka "OTP share na karein" flag nahi hota), EN/HI/**TE**, 30-message labelled test set |
| `anuragtiwari-ux/Sangyan-Suraksha` | A + B + E | Zero-dep Node | **Measured**: 30 labelled messages par precision 93% / recall 88% / acc 90% + post-scam "first hour" recovery checklist |
| `yashashwi-s/Sangyan` (Virasat) | B (nominee) | Next.js live | **6 languages** (EN/HI/BN/MR/TA/UR), sirf ek hi journey (nominee tracker) — PS ki "depth over breadth" ke hisaab se smart |
| `Algo-explorer/Grievance-Clock` | B | Node + Python | Escalation ladder, evidence preservation, **deterministic routing**, assisted filing (SCORES/SMART ODR) |
| `hafsakhan09090/haq-sangyan` | B | HTML, on-device | Voice-first rights guide + **on-device PDF complaint** |
| `ad-suriya/Zuno` | A/C | Next.js + FastAPI + **Sarvam AI/Gemini** + Firestore | Voice-first, adaptive questioning, Indic languages (Tamil MVP) |
| `ithakurhrashit/..._agnes` (Aegis.AI) | A | FastAPI + **Ethereum TCR** + Siamese NN | Telegram groups se APK scrape, UI-clone detection (Siamese NN), decentralized threat registry — ambitious, risk bhi zyada |
| `XNTOL/Ruko-Zara` aur `SunilMaurya-18/Ruko` | A | Flask / Spring | **Do alag teams ne same product naam "Ruko" chuna** 😄 — dono Hindi-first pause-and-verify; `Ruko-Zara` ka strong line: "**Never says a tip is 'safe'**" |
| `arcimillion/nivesh` | A/E | Vite + React 19 + server KB | SEBI/RBI/cyber-portal docs par grounded knowledge base + 6-language voice readout |
| `Yash12-cloud/FinShield`, `Darshan-Patil-18/sangyan`, `MITHUNtech11/Sangyan_Hackathon` (Nambikkai, Track E), `Pardhu-05/...` (screenshot screening + ResNet + mobile app, disclaimers saaf) | A/E | Python/FastAPI | Solid engineering, koi live link nahi / minimal |

### Noise / name-collisions (OSINT ka zaroori hissa — inhe competitor mat gino)

- `adityagrinds/SangyanAI` + `SangyanAI-2` (★3, 28–61 MB) — **disaster/crisis response** dashboard (USGS + weather + Groq agents). "Sangyan" word ka overlap, is event se related nahi.
- `SnehalSwadhin/SIH-2022---Sangyan-Winners` — Smart India Hackathon 2022 ki team "Sangyan".
- `LavishVaishnav/RJPOLICE_HACK_680_Sangyan_07` (Rajasthan Police Hackathon 2024), `Om25091210/...` (InnoHacks 2023), `sangyan11/*` (portfolio), `alok2006/Sangyan` (38 MB, unrelated).
- Khaali shells: `Enky-yy/sangyan-sebi` (7 files, README nahi), `sehajpreet224541-wq/investor-resilience-explainer` (0 KB), `nilaymastaadmi/sangyan-2026` ("build in progress"), `CrowDonut/ScamSatark` (README-only), `sudarshan237/sangyan-scamshield` (2 files).

---

## 5 · Crowding aur gaps (rubric ke against)

**Bhara hua (yahan naya kuch dikhana mushkil):**
1. "Message/link screenshot → risk score + red flags" — **77% yahi bana rahe hain**. Judges ise 30th baar dekh rahe honge.
2. Voice/TTS Hindi explainer — 52% (baseline ban gaya hai).
3. Complaint/1930 mention — 58%, par mostly "link de dete hain", **end-to-end filing nahi**.
4. Multi-track claims — log A+B+C+E ek saath claim kar rahe hain (depth nahi, breadth) — PS isse explicitly discourage karta hai.

**Khaali / kam bhara (yahan differentiate karo):**
| Gap | Evidence | Kyun jeetega |
|---|---|---|
| **Pre-transaction interception** (paisa jaane se pehle rukna — runtime, sirf checker nahi) | Poore sample me koi bhi repo user ke payment flow me nahi baithta; sab "paste karo, hum batayenge" | Rubric ka 30% "measurably avoid fraud" — checker se interception zyada measurable hai |
| **Evidence-grade recovery pack** (crop + redact + OCR entities + OCR se txn/UTR + **SHA-256 chain-of-custody** + ready NCRP/SCORES text + email) | 58% complaint ka zikr karte hain, par koi auto-pack + hash/annexure nahi | Track B ka "Grievance Assistant" + 30% impact + 15% feasibility — sab ek saath |
| **SEBI registry verification at scale** | Sirf 23% (Kavach/Ruko datasets bundle karte hain) | Judges SEBI hain — registry cross-check unke liye sabse credible signal |
| **Track D behavioural** (cooling-off circuit breaker, decision journal) | Track-D keyword sirf 7/31 me aur unme bhi real implementation kam | Sabse under-served track + "safer financial behaviour" (rubric ka pehla criterion) |
| **Offline / low-bandwidth / IVR / USSD / missed-call** | Sirf 26% offline mention | PS ka Open Track literally IVR/USSD maangta hai; Tier-2/3 25% weight |
| **Senior-citizen / low-literacy design** (bade buttons + senior mode + voice-first, sach me) | 1–2 repos | 25% usability weight |
| **Live demo link** | **21/31 ke paas nahi** | Judges demo na khul paye to 15%+ technical execution pe daav lag jata hai |
| **Measured numbers** (precision/recall, latency, test set) | Sirf ~3 repos (Sangyan-Suraksha, scam-shield, THH MITHUNtech tests) | "Measurably" shabd rubric me likha hai |

---

## 6 · SatarkPay × SANGYAN — agar aap submit karte ho (strategic fit)

> ⚠️ Ye section isliye hai kyunki hamare paas SatarkPay M2 ka ready asset hai (7 modules + rules + auto-report toolkit). Lekin SANGYAN ke guardrails **Amazon cyber-hackathon** se thode alag hain — 3 cheezein badalni padengi.

**Guardrail risk (important):**
1. PS saaf likhta hai: **"no unauthorised harvesting of SMS, OTPs, or personally identifiable financial records"** → SatarkPay M2 ke **Screenshot Radar (MediaStore + UsageStats), Wallet Audit (NotificationListener/SMS), Chat-Before-Pay (UsageStats)** ko **opt-in + on-device + no-upload** ke roop me reframe karna hoga, aur SMS-reading **bilkul drop** karni chahiye. Nahi to 15% guardrail + disqualification risk.
2. **No tips** — hamara content waise bhi tips nahi deta, par deck me "investment scam" wale examples me koi buy/sell language nahi honi chahiye.
3. SANGYAN **investor**-centric hai (UPI fraud + investment fraud dono fit, par framing "investor resilience" rakho, "UPI guard" nahi).

**Module → track mapping:**

| SatarkPay asset | SANGYAN track | Rubric weight jahan khaata hai |
|---|---|---|
| M3 Domain Trust (4-level ladder + gateway-merchant mismatch) | **A** | 30% (before money moves) + 15% tech |
| M4 AI Sanchalak (deterministic verdict + LLM explanation + "pakka nahi" honesty) | **A + E** | 30% + 15% (transparent uncertainty) |
| M1 Chat-Before-Pay gate (matrix) | **A** (unique: interception) | 30% — koi aur interception nahi bana raha |
| M2 Screenshot Radar (burst + provenance) | A | 30% · *privacy reframe zaroori* |
| **M7 Auto-Report (1930 pack + crop/redact/hash + txn-UTR extract + QR decode)** | **B** (Grievance Assistant) | 30% impact + 15% feasibility — **sabse strong, koi competitor is level par nahi** |
| M6 Intel Desk crawler (live naye scam patterns) | A/E | 30% + 15% — "measured, live" story |
| M5 Wallet/AutoPay audit | **D** (behaviour) + A | D under-served hai |

**Agar ek hi journey chunni ho (PS ki advice):**
> **"WhatsApp par 'SEBI-registered' tip → screenshot → risk + registry check → pause (friction) → aur agar paisa gaya to 4-minute me evidence-grade complaint pack (SCORES + 1930)."**
Ye ek journey A + B + E teeno ko chhooti hai, aur isme **checker + interception + recovery** teeno hain — jo 77% competitors ke paas nahi hai.

**Compliance ke liye 6 changes (checklist):** SMS/notification reading drop · sab telemetry opt-in + on-device compute · SEBI registry (intermediaries/debarred) check add · SCORES + SMART ODR draft (sirf 1930 nahi) · Hindi voice output (Web Speech/Bhashini) · PPT me "no tips, no monetisation, privacy" slide.

---

## 7 · Agar submit karna hai: aaj raat ka plan (deadline 4 Oct 23:59 IST)

| Time | Kaam | Kyun |
|---|---|---|
| 0:00–0:30 | **Journey freeze** — ek hi journey likho (upar wali) + kis track me entry (A + B) | PS: depth > breadth |
| 0:30–1:30 | SatarkPay M2 demo se **M3+M4+M7** ko sangyan-flavoured banao (SEBI registry check + SCORES draft + investor wording) | 30% + 15% impact |
| 1:30–2:30 | **Live deploy** (Vercel/Netlify/Render) + smoke test (mobile viewport) | **21/31 ke paas live link nahi** — free points |
| 2:30–3:00 | **Hindi voice output** (Web Speech API) + senior mode (bada font, high contrast) | 25% usability |
| 3:00–3:30 | **Numbers slide**: domain engine ke 10 test URLs, assistant ke 5 scenarios, OCR 28-file case (asli case ka masked version!) | "Measurably" |
| 3:30–4:30 | **3–5 min video** shoot: real scenario (Ramesh) — screen record, Hindi voiceover | Required artifact #2 |
| 4:30–5:30 | **PPT** (problem→user→solution→stack→journey→rubric map→privacy→limits) | Required artifact #3 |
| 5:30–6:00 | Unstop pe submit + Discord me clarification agar koi doubt | Deadline buffer |

**Judges ke liye 3 killer lines (deck/video me rakho):**
1. "Hum paste-karo-aur-batayenge tool nahi hain — hum **paisa jaane se pehle** beech me baithte hain." (vs 77% competition)
2. "Complaint ek link nahi, ek **evidence pack** hai — crop, redact, entities, hash. Aaj ke asli case me 28 screenshots → 4 minute me 1930-ready." (vs 58% jinka '1930 link' se aage kuch nahi)
3. "Verdict deterministic hai, LLM sirf samjhata hai — isliye hum 'pakka nahi bata sakta' bol sakte hain." (guardrail + trust)

---

## 8 · Sources

- Unstop listing (dates, prizes, registrations, attachments): https://unstop.com/hackathons/sangyan-iit-bhu-1761145
- Official PS PDF (copy is folder me; original public mirror): https://github.com/MITHUNtech11/Sangyan_Hackathon/blob/main/SANGYAN-Problem-Statement.pdf
- Repos (sample): [pyharshcodes/Sangyan_hackathon](https://github.com/pyharshcodes/Sangyan_hackathon) · [adityakr-git/sangyan](https://github.com/adityakr-git/sangyan) · [Utsav006/sangyan-shield](https://github.com/Utsav006/sangyan-shield) · [mdasif-x1/investor-safety-system](https://github.com/mdasif-x1/investor-safety-system) · [Kshitijsawant17/PATIBIMB](https://github.com/Kshitijsawant17/PATIBIMB) · [subdil/-SurakshaLens...](https://github.com/subdil/-SurakshaLens-AI-Financial-Scam-Claim-Verifier) · [prakshithamalla-art/scam-shield-sangyan](https://github.com/prakshithamalla-art/scam-shield-sangyan) · [anuragtiwari-ux/Sangyan-Suraksha](https://github.com/anuragtiwari-ux/Sangyan-Suraksha) · [yashashwi-s/Sangyan](https://github.com/yashashwi-s/Sangyan) · [Algo-explorer/Grievance-Clock](https://github.com/Algo-explorer/Grievance-Clock) · [XNTOL/Ruko-Zara](https://github.com/XNTOL/Ruko-Zara) · [SunilMaurya-18/Ruko](https://github.com/SunilMaurya-18/Ruko)
- Demo links jo live mile: ruko-zara.onrender.com · ruko-187c.onrender.com · sangyan-hackathon.vercel.app · sangyan-green.vercel.app · investor-safety-system.vercel.app · sangyan-shield.onrender.com · surakshalens.vercel.app · sangyan-xi.vercel.app · gilded-clafoutis-d62dd0.netlify.app

**Honest limits is report ke:** (1) Track classification keyword-based hai — teams multi-track claim karte hain, isliye counts indicative hain. (2) GitHub par sirf public repos mile — bahut teams private repo me bana kar sirf demo/PPT submit karengi, to competitor count ~2,364 registrations ke hisaab se zyada hoga. (3) Unstop page 15 min me update hota hai; deadline/pool ke numbers 3 Oct 2026 ki reading hain. (4) Prize pool me ₹1.70L vs ₹1.75L ka farq Unstop ke apne page par hai.
