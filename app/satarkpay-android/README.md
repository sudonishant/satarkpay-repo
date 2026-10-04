# SatarkPay — Android App (Kotlin + Jetpack Compose)

**Native companion to the SatarkPay web app.** The same idea, on the device
where the payment actually happens: nine attack-chain signals are read *before*
the UPI PIN is entered, and the app produces a verdict whose arithmetic you can
check yourself.

> Premium light fintech theme · Hindi voice (TTS) · Senior mode · PII masking ·
> Full offline rule engine · Room persistence · Gemini-assisted Sanchalak chat

---

## Feature map (14 screens)

| Screen | What it does |
|---|---|
| **Splash / Consent (S0)** | Guard ON/OFF consent — the app intercepts nothing without it |
| **Home (S1)** | Dashboard: cooling timer, burst counter, intel count, permission audit, quick "confirm fraud" |
| **Chat Before Pay (S2)** | The payment gate: nine signals → score → tier → cooling / withhold |
| **Screenshot Radar (S3)** | WhatsApp/SMS screenshot triage — burst detection, OCR-ready slots |
| **Domain Trust (S4)** | Link decomposition (brand + action word + punycode + bulk TLD), no blacklist |
| **Sanchalak Chat (S5)** | Hinglish AI assistant (Gemini) with Hindi voice output + audio notes |
| **Wallet Audit (S6)** | AutoPay/mandate audit — what is silently active |
| **Intel Feed (S7)** | Scam-pattern intel desk (snapshot crawler output) |
| **Report + Evidence (S8)** | Complaint packet generator: incident summary, redacted identifiers, 1930-ready |
| **Emergency (S9)** | 10-minute emergency mode — golden-hour clock, ordered steps, family alert |
| **Grievance Ladder (S10)** | SCORES / SMART ODR / NCRP follow-up route |
| **App Security (S11)** | Installed-app permission audit (remote-access / SMS-forwarding detectors) |
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

- **Everything that protects you runs offline.** The rule engine, redaction,
  tiering and emergency actions never touch the network.
- **Gemini is optional sugar** — Sanchalak chat and grounding toggles degrade
  gracefully without an API key.
- **Redaction runs first** — account numbers, card numbers, UPI handles, emails
  and OTP/PIN digits are masked before display, share, or save.

## Build

```bash
cd app/satarkpay-android
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release (needs KEYSTORE_PATH / STORE_PASSWORD env)
./gradlew test                   # unit + Robolectric tests
```

Requirements: Android Studio Ladybug+ (SDK 36, JDK 17). `GEMINI_API_KEY` is
read via the secrets gradle plugin (see `.env.example`) — optional.

## Design system

The palette is the same as `webapp/assets/app.css` v6: `#F3F6FB` canvas,
`#2F6BF6` accent, `#0C1424` ink, 14–24 dp radii, hairline `#E4E9F2` borders,
one shadow scale. Senior mode scales type +25% and lifts contrast to AAA.

## Privacy

No account, no analytics. Screenshots are not stored — only derived entities +
a hash. See [`docs/PRIVACY.md`](../../docs/PRIVACY.md).
