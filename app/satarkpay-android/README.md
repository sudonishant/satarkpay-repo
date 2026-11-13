# SatarkPay — Android App (Kotlin + Jetpack Compose)

**Native companion to the SatarkPay web app.** THEHRO reviews available signals
before an Android UPI handoff and explains its tier, evidence and next action.
It does not verify the recipient with a bank or see the bank's payment result.

> Premium light fintech theme · Hindi voice (TTS) · Senior mode · PII masking ·
> Full offline rule engine · Room persistence · Gemini-assisted Sanchalak chat

---

## Feature map (14 screens)

| Screen | What it does |
|---|---|
| **Splash / Consent (S0)** | Guard ON/OFF consent — the app intercepts nothing without it |
| **Home (S1)** | Dashboard: local payee history, permission summary and a user-controlled 1930 dialer shortcut |
| **SafePay (S2)** | One THEHRO decision drives the verdict card and UPI handoff gate; T3/T4 hold payment in-app |
| **Screenshot Radar (S3)** | WhatsApp/SMS screenshot triage — burst detection, OCR-ready slots |
| **Domain Trust (S4)** | Link decomposition (brand + action word + punycode + bulk TLD), no blacklist |
| **Sanchalak Chat (S5)** | Hinglish AI assistant (Gemini) with Hindi voice output + audio notes |
| **Wallet Audit (S6)** | AutoPay/mandate audit — what is silently active |
| **Intel Feed (S7)** | Scam-pattern intel desk (snapshot crawler output) |
| **Report + Evidence (S8)** | Draft preview only; no PDF/ZIP is created and no complaint is submitted |
| **Emergency (S9)** | Legacy action screen; not part of the SafePay route and not an official reporting integration |
| **Grievance Ladder (S10)** | SCORES / SMART ODR / NCRP follow-up route |
| **App Security (S11)** | Installed-app indicators; an installed app is not proof of active remote control |
| **Analyst Console** | Ops timeline, hold/callback queue for a human analyst |
| **Settings** | Senior mode · Hindi voice · PII mask toggle · model picker · consent centre |

## Architecture

```
ui/           Compose screens + components (VerdictCard, SigRow, GoldenHourClock, TopBarHeader)
  theme/      Premium light palette shared with the web app
  MainViewModel   single source of truth (StateFlow), navigation stack
engine/       RuleEngine (R1–R38 offline) · AppSecurityScanner
data/         Room (AppDatabase, Entities) · SatarkRepository
network/      GeminiService (optional — everything core works offline)
util/         TtsManager (hi-IN voice) · AudioRecorderHelper
```

- **THEHRO scoring and the payee ledger run locally.** Configured assistant/chat
  requests may use the network; they are not part of the payment decision.
- **The app cannot see the bank result.** After returning from a UPI app, any
  completion record is explicitly user-reported and is not bank-confirmed.
- **Limited signals are shown honestly.** Call/audio-mode sensing cannot prove
  every call is absent, and active screen-control is a user-observed input rather
  than automatic detection. Demo presets are simulations.
- **Gemini is optional sugar** — Sanchalak chat and grounding toggles degrade
  gracefully without an API key.
- **Redaction runs first** — account numbers, card numbers, UPI handles, emails
  and OTP/PIN digits are masked before display, share, or save.

## Build

```bash
cd app/satarkpay-android
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
./gradlew assembleRelease        # release (needs KEYSTORE_PATH / STORE_PASSWORD env)
```

Requirements: Android Studio Ladybug+ (SDK 36, JDK 17). `GEMINI_API_KEY` is
read via the secrets gradle plugin (see `.env.example`) — optional.

## THEHRO verification status

The Python engine, JavaScript port and flexible web Judge Mode have automated
checks in `../../.github/workflows/ci.yml`. Android JVM tests and a debug APK
build are configured in the same workflow. These checks establish code
consistency, not scam-detection accuracy on real users or device compatibility.
The app still needs a clean CI run and testing on representative Android
devices before a production release. Bank-side recipient verification,
automatic active-screen-control detection, and complaint-file export are not
implemented.

## Design system

The palette is the same as `webapp/assets/app.css` v6: `#F3F6FB` canvas,
`#2F6BF6` accent, `#0C1424` ink, 14–24 dp radii, hairline `#E4E9F2` borders,
one shadow scale. Senior mode scales type +25% and lifts contrast to AAA.

## Privacy

No account, no analytics. Screenshots are not stored — only derived entities +
a hash. See [`docs/PRIVACY.md`](../../docs/PRIVACY.md).
