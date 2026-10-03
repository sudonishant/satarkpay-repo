# Privacy & Data Protection (DPDP-aligned)

## Data hum kya collect karte hain (device par)

| Data | Kis liye | Kitna din | Kahan |
|---|---|---|---|
| App-switch + chat dwell time | M1 matrix | 7 din (rolling) | device (Room) |
| Payment screenshot ka **entity + hash** | M2 provenance | 7 din | device |
| Bank/mandate notifications se **parsed fields** | M5 audit | 30 din | device |
| User ke paste kiye text ka **masked** version + verdict | M4 history | 30 din (user clear kar sakta hai) | device |
| Evidence pack | M7/R38 | user ke folder me, user delete kare | device / user ka share |

## Kya server par jaata hai (agar user OTA/registry sync on kare)

- **Sirf:** verdict code, rule id, entity **hash** (naam/number/UPI nahi), aur anonymous counters (“kitne log ne R26 flag dekha”).
- Kabhi nahi: raw chat, raw screenshot, PII, evidence files.

## Kya hum **nahi** maangte

SMS (`READ_SMS`) · Contacts · Accessibility · `QUERY_ALL_PACKAGES` · Location · Microphone (voice output device ke TTS se, recording nahi).

## User ke controls (Settings → Privacy)

1. PII redaction ON/OFF (default ON).
2. OCR opt-in (default OFF).
3. Screenshot retention = 0 (default).
4. “Mera data delete karo” — ek tap me local DB wipe.
5. Evidence folder ka location + share log (kisko bheja, kab).

## DPDP checklist

- [x] Purpose limitation (har data ka ek kaam, likha hua)
- [x] Data minimisation (entity + hash, image nahi)
- [x] Storage limitation (7/30 din ka rolling window)
- [x] Consent notice simple Hinglish me, granular
- [x] Erasure (one-tap wipe)
- [x] No dark patterns (Cancel = Pay jitna bada button)
- [ ] Grievance officer ka naam/email (production me add karna hai)
- [ ] Audit log export (enterprise/PSP ke liye)
