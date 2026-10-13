# SatarkPay v3 · THEHRO engine

**RAKSHAM 2026 · IIT Delhi · Problem Statement 02: AI-Driven Scam Pattern Recognition**

> Detect emerging AI-enabled scam workflows in UPI, banking or wallet contexts and deliver timely, useful alerts without creating alert fatigue.

SatarkPay is built to catch multi-step scams in the 60 seconds between tapping **Pay** and typing the **PIN**.
It does not try to spot a deepfake. It looks at the **workflow** around the payment: an unknown caller, a long video call, screen sharing, a payee created this week, an amount 40× the user's normal. It matches these against **playbook cards** for known scam workflows, then picks the **lowest alert that will work**. Most normal payments should see nothing. Our design target is about 97 %.

**Team SCΛMURΛI:** Nishant Kumar · Prince Kumar Singh · Kartik Singh · Gourvendra Kumar Jha

| | |
|---|---|
| Live demo (web, runs offline) | [`webapp/thehro.html`](webapp/thehro.html) · GitHub Pages: `https://sudonishant.github.io/satarkpay-repo/webapp/thehro.html` |
| Reference engine (Python) | [`thehro/engine.py`](thehro/engine.py) |
| Real-time tracker · emerging-workflow radar | [`thehro/realtime.py`](thehro/realtime.py) · [`thehro/radar.py`](thehro/radar.py) |
| Android port (Kotlin) | [`app/.../com/example/thehro/`](app/satarkpay-android/app/src/main/java/com/example/thehro/) |
| Playbook cards (12 P1 workflows) | [`thehro/playbooks.json`](thehro/playbooks.json) |
| Simulated evaluation | [`thehro/simulate.py`](thehro/simulate.py) → [`thehro/results/sim_results.json`](thehro/results/sim_results.json) |
| Safeguards, test plan, finale plan, known limits | [`docs/RAKSHAM.md`](docs/RAKSHAM.md) |

---

## The problem in one minute

| | |
|---|---|
| **Primary user** | Someone paying by UPI, bank app or wallet while being talked into it: a retired teacher, a student, a kirana owner |
| **Harmful moment** | The ~60 seconds between tapping Pay and entering the PIN. After that, UPI settles in seconds |
| **Threat pattern** | AI-assisted, multi-step workflows: contact → credibility → pressure → isolation → instruction → extraction |

Most tools check one thing: a message, a link or a number. Scams now arrive as a sequence of steps, and AI makes the scripts, voices and videos cheaper to produce. Each step looks harmless on its own. The pattern is what gives it away.

## Where it runs (integration boundary)

SatarkPay is an SDK **designed for integration with participating PSP, bank and wallet apps**. The partner app calls it just before the PIN screen and applies any hold under its own rules. An Android app cannot read or pause another app's payment screen, so we do not claim to protect payments inside apps that have not integrated it. Which signals are available depends on Android permissions and on what the partner app shares (for example, the real credit ledger).

## How it works

```
 signals (metadata only)         THEHRO engine (on the phone)                     user sees
 ───────────────────────         ──────────────────────────────────────────       ─────────────────
 call state, VoIP/video  ─┐      1 predicates      22 yes/no checks               T0  nothing
 screen-share / new APK   │      2 kill-chain      match 12 playbook cards        T1  trust badge
 payee age, saved or not  ├────► 3 risk score      weighted sum of stage,         T2  one question
 amount vs own baseline   │                         anomaly, payee, device         T3  reason + 60 s hold*
 'credited' SMS vs ledger │      4 floors          rules the score can't lower    T4  hold* + guardian
 QR source, UPI note     ─┘      5 budget          ≤ 3 questions / 30 days            + 1930 draft
                                                                    * applied by the partner app
```

* **Playbook cards** are small JSON files. A new scam wave means a new card, sent over the air, with no app update. Each card has an `anchor` stage it cannot complete without, so generic signals like "unknown caller + new payee" never trigger a severe card alone.
* **Risk score:** `σ(w·[stage, anomaly, payee, device])`. The weights are **hand-set**, not trained or calibrated yet, so we call it a risk score, not a probability.
* **Deterministic floors:** a fake "credited" SMS with no real credit → at least T3. Screen control on the pay screen → T4. Saved or recurring payee → never above T1.
* **Attention budget:** questions (T2) are rationed per user. When the budget is spent, T2 becomes a quiet badge. T3 and T4 are never suppressed.
* **Anti-splitting:** payments to new payees are added up over 72 h, so ₹4.8 lakh split into ten payments still counts as "40× normal".
* **Retry after a hold:** if the user was held today and tries again to a new payee, it goes straight to T3. Digital-arrest callers usually call back.
* **Real time:** `thehro/realtime.py` keeps one live session per payment journey. Events (call start, screen share on, payee opened, amount typed) update it as they happen, and call length comes from the clock.
* **Emerging workflows:** `thehro/radar.py` turns risky sessions that matched no card into a content-free signature. When one signature surges (≥ 5 distinct reporters in 24 h and ≥ 3× its 7-day average), it drafts a new card. An analyst must approve it before it reaches any phone.
* **No content read.** No SMS body, chat text or call audio enters the engine. The only text is the short UPI note the user typed for this payment. The Android app's mic recording path was removed in v3.1.

## What works today and what does not

| Part | Status |
|---|---|
| THEHRO engine in Python, JS and Kotlin (same decisions, 250/250 parity) | ✅ built, tested |
| Real-time session tracker, emerging-workflow radar | ✅ built, tested in Python |
| Web demo with a step-through and a live mode (one step every 3 s) | ✅ built, simulated data |
| 12 playbook cards (P1 workflows) | ✅ built |
| Android: call state from `AudioManager`, THEHRO decision in the view model | ◐ partly. The tier UI on the pay screen is not wired yet |
| Android full build (`./gradlew assembleDebug`) | ⚠️ not verified. Our sandbox ran out of memory. Run it on a laptop |
| Real phone test (call, VoIP, screen-share signals on 3+ brands) | ❌ not done. First finale task |
| Payee risk API (DoT FRI, MuleHunter.AI, RBI DPIP, I4C) | ❌ planned; needs partner access |
| Trained / calibrated model | ❌ planned. Weights are hand-set today |

## Results so far (simulated, labelled as such)

`python3 -m thehro.simulate` runs 50 scam sessions (about 4 variants for each of the 12 P1 workflows, some with signals deliberately missing) and 200 everyday payments, including hard normal cases (long call with family, big first-time purchase, real refund, SIP to an `@valid` handle).

| Metric | Self-authored simulation | Next validation (finale) |
|---|---|---|
| Scam sessions alerted (T2 and above) | 49 / 50 | held-out scenarios written by someone else |
| Scam sessions held (T3 and above) | 46 / 50 | ≥ 45 / 50, compared with a rules-only baseline |
| Correct playbook named | 47 / 50 | same set |
| False holds | 0 / 200 normal payments | ≤ 1 per 100 normal payments |
| Interruptions per 100 normal payments | 0 (188 silent, 12 badge only) | ≤ 3 |
| Decision time | median 0.03 ms (Python, laptop) | < 150 ms on phone, p50 and p95 |
| Do people act on the alert? | not measured | 10-person test: cancel rate, time to decide |

**How to read this:** we wrote these sessions ourselves from public case reports. They show the logic does what we say. They do **not** measure real-world accuracy. The one miss (fake helpline, no screen share, normal amount) is a real blind spot and is listed in [`docs/RAKSHAM.md`](docs/RAKSHAM.md).

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

# Android: Kotlin port + JVM unit tests (needs ~4 GB RAM for Gradle)
cd app/satarkpay-android && ./gradlew testDebugUnitTest --tests "com.example.thehro.*"
```

## Repository map

```
thehro/                   engine, real-time tracker, radar, cards, simulator, tests   (v3)
webapp/thehro.html        step-through + live demo of the engine                    (v3)
webapp/assets/thehro.js   JS port, parity-checked                                   (v3)
app/satarkpay-android/    Android app (Kotlin, Compose)
  .../thehro/             ThehroEngine.kt, PlaybookCards.kt, CallStateProbe.kt     (v3)
webapp/                   v2 web app: payment check, link check, evidence pack, emergency
rules/  data/             v2 rule packs and scam library
tools/                    parity check, Kotlin card generator, source crawler
docs/                     RAKSHAM.md, architecture, privacy, verification
```

## What changed from v2 to v3

* New THEHRO engine: playbook cards → kill-chain → risk score → floors → tier with attention budget (Python, Kotlin and JS, kept in sync).
* v3.1: real-time session tracker, emerging-workflow radar, anti-splitting over 72 h, retry-after-hold floor.
* Android: call state now comes from `AudioManager` mode instead of a hard-coded `true`. The view model computes a THEHRO decision next to the v2 score.
* Android: mic recording and cloud audio transcription removed, along with the `RECORD_AUDIO` permission. SatarkPay records no audio.
* v2 wording corrected: the old "Bharat-BERT" classifier is a keyword and weight scorer, not a BERT model. It stays as the v2 message checker and is not part of the THEHRO decision.
* Old decks, demo-video tools and the previous event's branding were removed from the repo.
* Scope narrowed to PS 02 (scam workflows). Voice-clone detection is PS 01 and is not claimed here.

## Responsible use

Metadata only, processed on the device. If the user opts in, only risk numbers and a hashed payee ID leave the phone. The user always decides. A guardian is opt-in. The "I was scammed" button drafts a 1930 evidence pack; the user files it, nothing is filed automatically. Banks hold money only under their own rules. The optional Gemini chat in the v2 app sends only what the user types, and only when an API key is set. See [`docs/RAKSHAM.md`](docs/RAKSHAM.md#build-for-trust) and [`docs/PRIVACY.md`](docs/PRIVACY.md).

## Attribution

Public data used for scam workflows and figures: I4C / MHA (NCRP), ORF, RBI, NPCI, UK PSR, Google Play Integrity and Android TelephonyCallback docs. Full list in `tools/sources.json`. No third-party model weights are bundled. The demo scenarios are simulated.

MIT licence. See [LICENSE](LICENSE).
