# SatarkPay v3: THEHRO engine

**RAKSHAM 2026 · IIT Delhi · Problem Statement 02, AI-Driven Scam Pattern Recognition**

> Detect emerging AI-enabled scam workflows in UPI, banking or wallet contexts and deliver timely, useful alerts without creating alert fatigue.

SatarkPay stops a scam in the 60 seconds between tapping **Pay** and typing the **PIN**.
It does not try to spot a deepfake. It reads the **workflow** around the payment (unknown caller, a long video call, screen sharing, a payee created this week, an amount 40× the user's normal) and matches it against **playbook cards** for known scam workflows. Then it picks the **lowest alert that will work**, so 9 out of 10 normal payments see nothing at all.

**Team SCΛMURΛI:** Nishant Kumar · Prince Kumar Singh · Kartik Singh · Gourvendra Kumar Jha

| | |
|---|---|
| Live demo (web, runs offline) | [`webapp/thehro.html`](webapp/thehro.html) · GitHub Pages: `https://sudonishant.github.io/satarkpay-repo/webapp/thehro.html` |
| Reference engine (Python) | [`thehro/engine.py`](thehro/engine.py) |
| Android SDK port (Kotlin) | [`app/.../com/example/thehro/`](app/satarkpay-android/app/src/main/java/com/example/thehro/) |
| Playbook cards (12 P1 workflows) | [`thehro/playbooks.json`](thehro/playbooks.json) |
| Simulated evaluation | [`thehro/simulate.py`](thehro/simulate.py) → [`thehro/results/sim_results.json`](thehro/results/sim_results.json) |
| Round-1 notes, safeguards, finale plan | [`docs/RAKSHAM.md`](docs/RAKSHAM.md) |
| Previous version (SANGYAN 2026, v2) | [`docs/README_v2_SANGYAN.md`](docs/README_v2_SANGYAN.md) |

---

## The problem in one minute

| | |
|---|---|
| **Primary user** | Someone paying by UPI, bank app or wallet while being talked into it: a retired teacher, a student, a kirana owner |
| **Harmful moment** | The ~60 seconds between tapping Pay and entering the PIN. After that, UPI settles in seconds |
| **Threat pattern** | AI-scripted, multi-step workflows: contact → credibility → pressure → isolation → instruction → extraction |

Most tools check one thing: a message, a link or a number. Scams now arrive as a sequence of steps, often written by an LLM and voiced or filmed by AI. Each step looks harmless on its own. The pattern is what gives it away.

## How it works

```
 signals (metadata only)         THEHRO engine (on the phone)                     user sees
 ───────────────────────         ──────────────────────────────────────────       ─────────────────
 call state, VoIP/video  ─┐      1 predicates      22 yes/no checks               T0  nothing
 screen-share / new APK   │      2 kill-chain      match 12 playbook cards        T1  trust badge
 payee age, saved or not  ├────► 3 risk fusion     p = σ(w·[stage, anomaly,       T2  one question
 amount vs own baseline   │                         payee, device])               T3  reason + 60 s hold
 'credited' SMS vs ledger │      4 floors          rules a model can't override   T4  hold + guardian
 QR source, UPI note     ─┘      5 budget          ≤ 3 questions / 30 days            + 1930 option
```

* **Playbook cards** are small signed JSON files. A new scam wave means a new card, pushed over the air. No app update needed. Each card has an `anchor` stage it cannot complete without, so generic signals like "unknown caller + new payee" never trigger a severe card alone.
* **Deterministic floors:** a fake "credited" SMS with no real credit → at least T3. Screen control on the pay screen → T4. Saved or recurring payee → never above T1.
* **Attention budget:** questions (T2) are rationed per user. When the budget is spent, T2 becomes a quiet badge. T3 and T4 are never suppressed.
* **Anti-splitting:** payments to new payees are added up over 72 h, so ₹4.8 lakh split into ten payments is still "40× normal".
* **Retry after a hold:** if the user was held today and tries again to a new payee, it goes straight to T3. Digital-arrest callers usually call back.
* **Real time:** `thehro/realtime.py` keeps one live session per payment journey. Events (call start, screen share on, payee opened, amount typed) update it as they happen, and call length comes from the clock.
* **Emerging workflows:** `thehro/radar.py` reduces risky sessions that matched no card to a content-free signature. When one signature surges (≥ 5 distinct reporters in 24 h and ≥ 3× its 7-day average), it drafts a new card for an analyst to approve. Nothing ships to phones without that approval.
* **No content read.** No SMS body, chat text or call audio enters the engine. The only text is the short UPI note the user typed for this payment.

## Results so far (simulated, labelled as such)

`python3 -m thehro.simulate` runs 50 scam sessions (about 4 variants for each of the 12 P1 workflows, some with signals deliberately missing) and 200 everyday payments, including hard benign cases (long call with family, big first-time purchase, real refund, SIP to an `@valid` handle).

| Metric | Result | Finale target |
|---|---|---|
| Scam sessions alerted (T2 and above) | **49 / 50** | — |
| Scam sessions held (T3 and above) | **46 / 50** | ≥ 45 / 50 |
| Correct playbook named | 47 / 50 | — |
| Precision at T3 and above | 1.00 (0 false holds in 200 normal payments) | ≥ 90 % |
| Interruptions per 100 normal payments | 0 (188 silent, 12 badge only) | ≤ 3 |
| Decision time (Python, laptop) | median 0.03 ms | < 150 ms on phone |

**Read this honestly:** we wrote these sessions ourselves from public case reports. They prove the logic does what we say. They do **not** measure real-world accuracy. The one miss (fake helpline, no screen share, normal amount) is a real blind spot and is listed in [`docs/RAKSHAM.md`](docs/RAKSHAM.md). Real numbers come from the finale test on phones.

## Run it

```bash
# reference engine + tests (Python 3.9+, standard library only)
python3 -m thehro.engine                                    # one digital-arrest decision as JSON
python3 -m thehro.simulate                                  # 250-session simulated evaluation
python3 -m unittest discover -s thehro/tests -t .           # 23 tests (engine, real-time tracker, radar)
cd web && bash run_smoke.sh                                 # 66 v2 checks + 78 THEHRO checks = 144

# web demo (needs a local server because it fetches playbooks.json)
python3 -m http.server 8000      # then open http://localhost:8000/webapp/thehro.html

# check the JS port gives identical decisions to Python
python3 tools/export_parity.py && node tools/parity_check.js   # 250/250 identical

# Android: Kotlin port + JVM unit tests
cd app/satarkpay-android && ./gradlew testDebugUnitTest --tests "com.example.thehro.*"
```

## Repository map

```
thehro/               engine, real-time tracker, emerging-workflow radar, cards, simulator, tests ← new in v3
webapp/thehro.html    step-through demo of the engine                      ← new in v3
webapp/assets/thehro.js   JS port (parity-checked)                         ← new in v3
app/satarkpay-android/    Android app (Kotlin, Compose)
  .../thehro/             ThehroEngine.kt, PlaybookCards.kt, CallStateProbe.kt ← new in v3
webapp/               v2 web app: payment check, link check, evidence pack, emergency
rules/  data/         v2 rule packs and scam library
tools/                crawler, deck builders, parity + Kotlin card generators
docs/                 RAKSHAM.md (this round), architecture, privacy, v2 docs
```

## What changed from v2 (SANGYAN) to v3 (RAKSHAM)

* New THEHRO engine: playbook cards → kill-chain → fusion → floors → tier with attention budget (Python, Kotlin, JS, all kept in sync).
* Android: call state now comes from `AudioManager` mode instead of a hard-coded `true`. The view model computes a THEHRO decision next to the v2 score.
* v2 wording corrected: the old "Bharat-BERT" classifier is a keyword and weight scorer, not a BERT model. It stays as the v2 message checker and is not part of the THEHRO decision.
* Scope narrowed to PS 02 (scam workflows). Voice-clone detection is PS 01 and is not claimed here.

## Responsible use

Metadata only, on the device. Only risk numbers and a hashed payee ID leave the phone, and only if the user opts in. The user always decides. A guardian is opt-in. Banks hold money only under their own rules. See [`docs/RAKSHAM.md`](docs/RAKSHAM.md#build-for-trust) and [`docs/PRIVACY.md`](docs/PRIVACY.md).

## Attribution

Public data used for scam workflows and figures: I4C / MHA (NCRP), ORF, RBI, NPCI, UK PSR, Google Play Integrity and Android TelephonyCallback docs. Full list in the 50-scam catalogue and `tools/sources.json`. No third-party model weights are bundled. The demo scenarios are simulated.

MIT licence. See [LICENSE](LICENSE).
