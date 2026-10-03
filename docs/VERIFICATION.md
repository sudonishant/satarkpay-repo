# Verification log — kaunse command se kya nikla

Har number jo hum claim karte hain, uska **exact command + output** yahan hai. Sab runs 3 Oct 2026 (Asia/Kolkata) ke hain, is repo ke andar se.

---

## 1 · Demo ke automated checks — **66 / 66 pass**

```bash
cd web && bash run_smoke.sh          # ya: NODE_PATH=... node smoke_test.js
```

```
== M1 matrix (known/unknown × chat length) ==   ✓ 8 checks
== M2 screenshot radar ==                       ✓ 4
== M3 domain trust ==                           ✓ 5
== M4 assistant (rules + registry + negation
   + redaction + eval harness) ==               ✓ 9  (naye: R26 flag, R27 SEEMS OK, R28 note, eval run)
== M5 wallet audit ==                           ✓ 4
== M6 intel desk ==                             ✓ 4
== M7 auto-report ==                            ✓ 4
== M7b emergency (R38: confirm -> email +
   call + payment stop + family alert) ==       ✓ 9
== demo script + speed + runtime errors ==      ✓ 3

RESULT: 66 passed, 0 failed
```

**Kya cover hota hai:** M1 ke chaaron cell, M2 ka burst+R22 staircase, M3 ka L1–L4 + gateway≠merchant, M4 ke 9 case (digital arrest, Telegram task, KYC/APK, refund-trap, legit refund, fake SEBI number, OTP negation, redaction note, eval harness), M5 ke 6 apps + 5 mandates + revoke, M6 ka filter + approve, M7 ka mask toggle + annexure hashes, R38 ke 3 steps + optional step 4.

**Limit:** jsdom me `scrollTo` implement nahi hai (harmless stub). Asli Chromium me page errors 0 — wahi screenshots `web/deck/screenshots/` me hain.

---

## 2 · Eval harness (33 labelled messages) — **P 95.0% · R 90.5% · F1 92.7%**

```bash
python3 eval/run_eval.py            # wahi engine jo user ke paste par chalta hai
```

```
precision 95.0%  ·  recall 90.5%  ·  F1 92.7%
TP 19 · FP 1 · miss(FN) 2 · sahi chhoda(TN) 9
NOTE: set self-authored hai (ceiling) — 3 messages jaan-boojh ke outside-library hain.
```

| Bucket | Kitne | Matlab |
|---|---|---|
| TP (scam → SCAM LIKELY) | 19 | block-worthy catch |
| FP (legit → SCAM LIKELY) | 1 | false alarm (KYC-verification legit message) |
| miss (scam → UNCERTAIN) | 2 | “PAKA NAHI BATA SAKTA” — guess nahi kiya |
| TN (legit → safe) | 9 | sahi chhoda |
| CAUTION bucket | 1 scam (soft) + 1 legit (friction) | alag ginte hain, positive me nahi |

Poora table + har message ka verdict: `eval/latest_results.json` · set: `eval/eval_set.json`

**Limit:** set humne khud likha hai → ye **ceiling** hai, field accuracy nahi. 3 messages jaan-boojh ke rule-library ke bahar hain.

---

## 3 · Intel crawler (live run)

```bash
python3 tools/intel_crawler.py --max-age-days 45 --out data/intel_snapshot.json
```

```
784 raw items → fraud filter (EN+HI) → 172 relevant → dedupe → novelty → 63 novel (REVIEW queue)
```

Sources: Google News RSS + RBI/other keyless feeds (`tools/sources.json`). Crawler sirf **headline level** padhta hai (article body nahi) — DPDP/copyright safe. Auto-publish **nahi** — analyst review ke baad registry push.

---

## 4 · Evidence pipeline (asli case par chali)

```bash
python3 tools/crop_tool.py --raw <screenshots> --out <out>     # crop + redact + QR
python3 tools/ocr_text.py                                      # offline OCR
python3 tools/build_report.py                                  # complaint + email + annexure + zip
```

Nateeja: **28 files** processed → smart crop → OCR → entities (UPI VPA, 2 txn ID, 3 UTR, shortlink, amounts) → QR decode (`7349••••••@ptaxis`) → 2 copies (full + masked) → **SHA-256 per file** (`data/case_pack/annexure_index.csv`) → NCRP text (HI+EN) + email draft + annexure CSV + ZIP.

Case facts (masked): ₹3,000 + ₹2,000 = **₹5,000 evidence-visible** · demands **₹30,625** · **9 scam families**.

---

## 5 · License sweep (31 peer repos)

```bash
python3 - <<'EOF'
import json; from collections import Counter
d=json.load(open('data/repo_licenses.json'))
print(Counter(v['license'] for v in d.values()))
EOF
```

```
Counter({'NONE': 30, 'MIT': 1})
```

Isliye policy: **zero code copying** — sirf ideas + credit (`NOTICE.md`).

---

## 6 · Deck build (ek source → teen output)

```bash
cd web/deck
python3 build_charts.py       # charts/*.png  (eval + landscape, light theme)
python3 build_diagrams.py     # diagrams/*.svg + *.png (journey · M1 matrix · R38)
python3 build_deck.py         # deck.html · satarkpay_deck.pptx · satarkpay_deck.pdf
```

Verify:

```bash
python3 -c "import pymupdf; print(pymupdf.open('satarkpay_deck.pdf').page_count)"   # → 18
python3 -c "from pptx import Presentation; p=Presentation('satarkpay_deck.pptx'); print(len(p.slides._sldIdLst))"  # → 18
```

Aakhri build: **18 slides**, 13.333×7.5 in, PPTX me **18 images embedded** (3 AI illustration + 12 demo screenshot + charts/diagrams).

---

## 8 · Rule-engine latency — **median 0.012 ms** (measured, 5,000 runs)

```bash
cd eval && NODE_PATH=../node_modules node bench_latency.js
```

```
classify() latency · 5000 runs over 33 eval messages
  mean 0.0295 ms · median 0.0124 ms · p95 0.0364 ms · p99 0.4548 ms · max 4.96 ms
  NOTE: Node/jsdom par measured (deterministic engine, no network). Low-end phone par zyada hoga.
```

**Kya measure hota hai:** `classify(text)` — wahi deterministic rule engine (RULES4 + negation + registry format checks) jo user ke paste par chalta hai. Network call zero; yahi "on-device" ka matlab hai.
**Limit:** Node (desktop) par measured hai — low-end Android device par number zyada hoga; SDK 24+ device par dobara measure karna Phase-2 checklist me hai.

---

## 7 · Repo hygiene checks

```bash
git status --short                 # clean
find . -type f -name "*.zip"       # koi evidence bundle repo me nahi
grep -rn "ghp_\|github_pat_" .     # koi token nahi
```

- Koi raw screenshot / asli UPI ID / phone number repo me nahi (sab masked — `data/case_pack/README.md`).
- `evidence_bundle.zip` repo me nahi (privacy + size); rebuild command `data/case_pack/README.md` me.
- `.gitignore` me secrets/env/evidence patterns hain.

---

## Dobara chalane ka tareeka (sab commands)

```bash
cd web && bash run_smoke.sh                    # 66/66
python3 eval/run_eval.py                       # P/R/F + latest_results.json
python3 tools/intel_crawler.py --max-age-days 45 --out data/intel_snapshot.json
cd web/deck && python3 build_charts.py && python3 build_diagrams.py && python3 build_deck.py
```
