# Submission checklist — SANGYAN (4 Oct 2026, 11:59 PM IST)

## Kya submit hota hai
1. **Live demo** — 🔴 **https://sudonishant.github.io/satarkpay-repo/** (GitHub Pages; fallback: `web/satarkpay_m2.html` offline single file)
2. **3–5 min video** — script: `docs/DEMO_SCRIPT.md`
3. **PPT** — `web/deck/satarkpay_deck.pptx` (PDF bhi: `satarkpay_deck.pdf`)

## Pre-submit (aaj hi)

- [x] `cd web && ./run_smoke.sh` → **66/66** ✅ (verified 3 Oct)
- [ ] `python3 eval/run_eval.py` → **P 95.0% · R 90.5% · F1 92.7%** (latest_results.json update ho gaya)
- [ ] Demo browser me khol ke 7 tabs + R38 emergency panel ek baar chala lo (network **off** karke bhi)
- [ ] Deck PDF ka page-count/tone check: 18 slides, images load ho rahi hain
- [ ] Video record: 3:00–3:20 (script ke beats: M1 → M2/M3 → M4+eval → M7+R38)
- [ ] Team names spell-check: **Nishant Kumar · Prince Singh · Kartik Singh** (Team SCΛMURΛI)
- [ ] Repo ready: `satarkpay-repo/` (ya `satarkpay-repo.zip`) — README, LICENSE, NOTICE, docs poore

## Judges ke sawal ke ready jawab
`docs/DEMO_SCRIPT.md` ke end me 6 sawal + jawab hain (permissions, accuracy, LLM, recovery, license, scale).

## Ek-line pitch (thumbnail/description ke liye)
> **SatarkPay** — chat se payment tak ka guard. Paisa bhejne se pehle 60 second (M1 matrix + rule engine), aur fraud ho jaye to 60 minute (evidence pack + cyber-cell email + 1930 call + payment stop) — offline, privacy-first, Hinglish.

## Deadline ke baad
- Phase 2 pilot plan: `docs/ROADMAP.md`
- Field calibration (asli labelled data par P/R/F dobara) — pehla priority
