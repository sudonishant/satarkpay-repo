# app/ · Android skeleton (finale ke liye)

Ye **skeleton** hai — jaan-boojh ke adhoora: har file me `TODO(finale)` likha hai kahan implement karna hai.
Ye pure Kotlin/Compose app ka dhaancha dikhata hai taaki judges (aur hum) dekh sakein ki demo ka har feature device par **kahan** baithega — aur kaunsi permission kyun chahiye.

## Files

| File | Feature | Kaam |
|---|---|---|
| `SatarkPayApp.kt` | — | Application + DI (Hilt) root, guard on/off state |
| `MainActivity.kt` | all | Compose nav: Home · Check · Sanchalak · Reports · Settings |
| `consent/ConsentActivity.kt` | S0 | 3 optional permissions ka granular consent + “kya nahi maangte” list |
| `signals/UsageStatsWatcher.kt` | M1 | app-switch + `foregroundDwell(chatApp)` — kuch bhi disk par nahi |
| `signals/ScreenshotObserver.kt` | M2 | MediaStore observer → entity + hash (image delete) |
| `signals/MandateNotifications.kt` | M5 | NotificationListener → mandate parse (synthetic test notifications) |
| `domain/GateEngine.kt` | M1 | matrix: (saved/unknown) × (short/long) → T1/T2/T3, + officer/offer → T3 |
| `domain/VerdictEngine.kt` | M4 | **wahi rules** jo demo me hain — R26/R27/R28 + rule families |
| `domain/DomainEngine.kt` | M3 | L1–L4 ladder, gateway≠merchant, registry-domain check |
| `evidence/EvidenceBuilder.kt` | M7 | crop → OCR → QR → redact → SHA-256 → annexure |
| `emergency/EmergencyActions.kt` | R38 | email draft intent · `tel:` dialer · payment-stop text · optional family alert |
| `data/GuardStore.kt` | all | Room: verdicts + hashes (7/30 din rolling) — koi analytics nahi |

## Permission stance (manifest me sirf ye teen, sab optional)

```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<!-- JAAN-BOOJH KE NAHI: READ_SMS · READ_CONTACTS · BIND_ACCESSIBILITY_SERVICE · QUERY_ALL_PACKAGES -->
```

- Usage access + Notification access **user settings** se special access hain → onboarding me deep-link + "kyun chahiye" text (S0).
- Har permission deny hone par app **manual mode** me chalega (crash nahi) — `ConsentActivity` me `DegradedBanner` dekh lo.
- Play Console ke liye declaration text: `../docs/PLAY_STORE_NOTES.md`

## Port karne ka order (48 ghante)

1. `VerdictEngine` — demo ke `_part_app.js` se rules 1:1 port (parity test: `web/smoke_test.js` ke 8 M4 cases).
2. `GateEngine` + `UsageStatsWatcher` — dwell measurement pehla (permission-free test possible).
3. `DomainEngine` — pure function, test-friendly.
4. `EvidenceBuilder` — ML Kit / Tesseract se OCR; hashing `MessageDigest`.
5. `EmergencyActions` — Intent based (mailto / tel / share) — koi server nahi.
