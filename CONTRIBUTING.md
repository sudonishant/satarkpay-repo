# Contributing

Shukriya! SatarkPay par contribution ke 4 aasan rules:

## 1 · Kya add kar sakte ho (welcome)

- **Naye scam patterns** → `rules/rules_R26_R38.json` ke format me (trigger, action, tier, honest_limit, borrowed_from).
- **Eval messages** → `eval/eval_set.json` me (label `scam`/`legit` + 1 line reason). Har naya message chhorna hai: kis family ka hai, ya "outside-library" hai.
- **UI copy / translations** → Hinglish master se translate karo (Bhojpuri, Maithili, Bengali, Marathi, Tamil, Telugu, Kannada, Malayalam, Gujarati, Odia, Punjabi).
- **Domain datasets** → official TLD/brand lists (source URL ke saath).
- **Tests** → `web/smoke_test.js` me naye checks.

## 2 · Kya NAHI kar sakte

- Third-party code bina valid permissive license copy karna mana hai — all contributions must be original work.
- Koi buy/sell/hold/price prediction/trading algo, ya broker/instrument promotion.
- Real user ka evidence (screenshots, UPI ids, numbers) bina written consent.
- `READ_SMS`, `QUERY_ALL_PACKAGES`, mandatory Accessibility — ye guardrail set hai, isko todne wala PR reject hoga.

## 3 · Process

```bash
git checkout -b feat/short-name
# changes karo
cd web && ./run_smoke.sh          # 66 checks pass hone chahiye
cd web && python3 build.py        # HTML rebuild
python3 eval/run_eval.py          # P/R/F table update (agar rules badle)
git commit -m "feat(rules): R39 — <kya add kiya>"
```

PR me likho: (1) kya add hua, (2) kis evidence se, (3) eval par kya asar pada (pehle/baad ke numbers), (4) koi privacy/guardrail risk.

## 4 · Commit style

`feat(ui): …` · `feat(rules): …` · `fix(engine): …` · `docs: …` · `eval: …` · `chore: …`
Message ka pehla line Hinglish/English dono chalega — bas saaf ho.
