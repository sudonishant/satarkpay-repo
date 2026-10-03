# Play Store / policy notes (finale ke liye)

## Sensitive permissions — declaration text (copy-paste)

**`PACKAGE_USAGE_STATS` (Usage access)**
> SatarkPay uses Usage Access to measure only *how long the user was on a messaging app immediately before opening a payments app* — a known social-engineering pattern (scam happens in chat, payment happens in UPI). We store only durations and app categories on-device for 7 days. We do not read message content, and no data leaves the device.

**`BIND_NOTIFICATION_LISTENER_SERVICE` (Notification access)**
> SatarkPay reads only mandate/payment notifications from banking and UPI apps to surface AutoPay abuse (e.g. a “one-time” mandate that is actually Daily). Parsed fields stay on-device 30 days. No notification content is transmitted.

**`READ_MEDIA_IMAGES` (Photos — user-selected, opt-in)**
> Used only when the user chooses a payment screenshot to verify its source and build an evidence pack after fraud. Images are not uploaded; the file is processed on-device and the image is deleted after entity + hash extraction (user can keep the evidence pack locally).

## Kya hum NAHI maangte (policy risk zero)

`READ_SMS` · `READ_CONTACTS` · `BIND_ACCESSIBILITY_SERVICE` · `QUERY_ALL_PACKAGES` · Location · Microphone · `READ_CALL_LOG`

## Data safety form (draft)

| Question | Answer |
|---|---|
| Data collected? | No personal data transmitted to servers (demo/offline). Optional OTA sync shares only verdict codes + entity hashes. |
| Data shared with third parties? | No. |
| Data encrypted in transit? | N/A (nothing transmitted by default). |
| Users can request deletion? | Yes — one-tap local wipe (Settings → Privacy). |
| Financial info collected? | Only what the user pastes/shares; stays on device; user can delete. |

## Accessibility statement

Senior mode (larger type, higher contrast, labelled buttons), Hindi voice output, TalkBack-friendly verdict cards, 56px tap targets — WCAG-AA aligned for older users.

## Risk register

| Risk | Mitigation |
|---|---|
| Play review rejects Usage access | Manual mode shipping (dropdown dwell) — app fully usable; usage access = enhancement |
| False alarm on legit payment | 4-bucket verdict + “Cancel” always available + per-payee fast path |
| Phishing of SatarkPay itself | App never asks for OTP/PIN; UI me bhi likha (“SatarkPay kabhi OTP nahi maangega”) |
| Over-trust in “SEEMS OK” | Copy me likha: “ye pattern legit lag raha hai, par khud verify karna free hai” |
| Evidence misuse | Masked copy default; full copy sirf official channels ke liye; originals immutable + hashes |
