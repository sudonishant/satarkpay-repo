# Security & Privacy

## Vulnerabilities report karna

Ye ek hackathon prototype hai, par security seriously lete hain.

- **Email:** `security@satarkpay.example` (team SCΛMURΛI) — subject me `[SECURITY]`.
- **Kabhi bhi** public issue me exploit, real user data, ya kisi ka PII na daalein.
- 72 ghante me acknowledge karte hain; fix ke baad credit denge (naam chahe to skip).

## Hamara threat model (chhota)

| Threat | Kaise handle karte hain |
|---|---|
| Prompt injection (LLM layer) | LLM sirf **explanation** likhta hai; verdict deterministic rules + registry + domain engine se aata hai. User text untrusted treat hota hai; tool-calling nahi. |
| PII leak | Analysis se pehle client-side redaction (R28); raw text device par; demo me network call zero. |
| Screenshot leak | Image **store nahi** hoti — sirf extracted entity + SHA-256 (7 din). Evidence pack user-driven share hota hai. |
| Evidence tampering | Originals modify nahi hote; per-file SHA-256 + annexure index (chain of custody, R25). |
| Fake “SEBI registered” badge | Hum certificate **kabhi** nahi dete — sirf format (INZ/INH/INA + 9 digits), official domain, aur watch-list check; user ko sebi.gov.in par bhejte hain. |
| Auto-send abuse | Koi auto email/call/payment-stop nahi — sab user ke tap par (R38). |
| Over-permissioning | SMS / Contacts / Accessibility / `QUERY_ALL_PACKAGES` **nahi** maangte. UsageStats + NotificationListener + Photos — teen, woh bhi optional. |

## Demo ke liye

`web/satarkpay_m2.html` **fully offline** hai (koi CDN, koi API, koi font fetch). Isko kisi bhi sandbox me khol sakte ho — network band karke bhi chalta hai.
