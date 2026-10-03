# Roadmap

## Phase 0 · Hackathon (ab tak ho gaya)

- [x] 7 modules ka offline demo (`web/satarkpay_m2.html`), 66 automated checks
- [x] Live intel crawler (784 → 172 → 63 novel)
- [x] Asli case par evidence pipeline (28 screenshots, hashes, complaint drafts)
- [x] Idea-borrowing + license sweep (zero code copy)
- [x] Eval harness (33 messages, live P/R/F, honest buckets)
- [x] R38 emergency layer (email + call + payment stop)

## Phase 1 · 48 ghante (finale)

| Hours | Kaam | Done matlab |
|---|---|---|
| h0–4 | Kotlin/Compose skeleton: `UsageStatsWatcher`, `MediaStoreObserver`, consent centre | Do phone par app chale, permission deny karne par manual mode |
| h4–12 | M1 matrix + M3 domain engine ko shared module (app + console parity test) | 7 test cases dono jagah same result |
| h12–20 | M4 real LLM (HI/EN) + library RAG + low-confidence → human queue | Judge ka apna text paste kare, verdict + reasons aaye |
| h20–30 | M5 NotificationListener → mandate parse + revoke flow | Synthetic bank notifications par daily mandate red |
| h30–40 | M6 cron + analyst console; M7 pack exports (bank/PSP format) | Ek naya crawl pattern analyst approve kare → app me dikhe |
| h40–48 | Red-team (mentor scripts), Hindi copy native review, demo rehearsal ×5 | 3-min demo bina rukawat |

## Phase 2 · Pilot (6–10 hafte)

- 2 Tier-2/3 city me 50-user pilot (Bihar + ek doosra state), consent + IRB-style note
- Field calibration: apne eval set ke saath **real labelled messages** se precision/recall dobara naapo
- Bank/PSP partner: UPI Help API + chargeback flow (schema share)
- Sanchar Saathi tak number-report automation (partnership ke through)
- Regional expansion: Bhojpuri, Maithili, Bengali, Marathi + voice (Bhashini)

## Phase 3 · Scale

- **Registry OTA**: rule JSON diff → app update bina Play review
- **PSP SDK**: bank apps me embedded guard (SDK, 200 KB)
- **Telecom**: Sanchar Saathi + DoT FRI (fraud number/domain intel) two-way
- **Investor angle**: SCORES/SMART ODR ke saath complaint-status tracker
- **Accessibility**: IVR/USSD fallback (feature phone), offline pack for sub-2G

## Metrics jo hum track karenge

| Metric | Target | Kaise |
|---|---|---|
| Fraud attempts intercepted (user ne cancel kiya) | baseline +40% | in-app event (anonymous) |
| Time-to-complaint (fraud → mail sent) | < 10 min | R38 clock |
| Complaint acknowledgement rate | > 70% | user feedback |
| False-alarm rate (legit payment par friction) | < 5% | user “ye theek tha” feedback |
| Naye pattern → registry push | < 6 ghante | crawler + analyst SLA |
| PII leak incidents | 0 | threat model + audit |
