# eval · labelled test set + harness

## Chalane ka tareeka

```bash
python3 eval/run_eval.py        # jsdom khud install karta hai (JSDOM_DIR se path badal sakte ho)
node eval/run_eval.js           # agar node_modules already set hai
```

Output: console table + `latest_results.json` (precision / recall / F1 + har message ka verdict, family aur tag).

## Set kaise bana

- **33 messages** — 22 scam + 11 legit, Hinglish + English, seedhe threat-library (T1–T24) + M6 intel + deck ke scenarios se likhe gaye.
- **3 messages jaan-boojh ke outside-library** rakhe hain, taki `miss` aur legit par `friction` bhi live dikhe.
- Ye set **self-authored** hai → iska score ek **ceiling** hai, field accuracy nahi. Asli calibration ke liye PS/bank data aur naye variants chahiye.

## Rules

- Positive class = `SCAM LIKELY` (block-worthy).
- `CAUTION` alag bucket (soft catch / legit friction) — inhe `soft` tag milta hai.
- `UNCERTAIN (PAKA NAHI BATA SAKTA)` bhi ek *valid* jawab hai — guess nahi.
- Har confusion ko table me tag milta hai: `miss` / `false-alarm` / `soft`.

## Naya message jodna

`eval/eval_set.json` me `{ "t": "...", "y": "scam" | "legit" }` add karo — ya seedha demo ke `web/_part_app.js` ke `EVAL_SET` me (wahi source of truth hai), phir:

```bash
cd web && python3 build.py && ./run_smoke.sh
python3 ../eval/run_eval.py
```
