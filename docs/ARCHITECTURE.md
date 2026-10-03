# Architecture

## 0 · Design stance

1. **On-device pehle** — jo signal device par hai (app-switch, screenshot, notification), wahi wahin process hota hai. Network optional.
2. **Deterministic verdict** — verdict rules + registry + domain engine se aata hai; LLM (production) sirf explanation likhta hai → hallucination se protection.
3. **Human-in-the-loop** — kuch bhi auto-publish, auto-block, auto-send nahi. Analyst queue + user confirmation.
4. **Offline-capable demo** — `web/satarkpay_m2.html` ek file me sab kuch inline (CSS/JS/data), zero network.

## 1 · Layers

```
┌─────────────────────────── Android app (Kotlin/Compose) ───────────────────────────┐
│ UI layer        Home · Gate · Radar · Domain · Sanchalak · Wallet · Intel · Report  │
│ Domain layer    M1 GateEngine · M2 BurstEngine · M3 DomainEngine · M4 VerdictEngine  │
│                 M5 MandateAuditor · M7 EvidenceBuilder · R38 EmergencyActions       │
│ Signal layer    UsageStatsWatcher · MediaStoreObserver · NotificationListener        │
│ Data layer      Room (verdicts, hashes) · EncryptedFile (evidence) · no analytics    │
└───────────────┬──────────────────────────────────────────────────────────────────────┘
                │ sirf: extracted entity + SHA-256 + verdict code (metadata-only)
┌───────────────┴───────────────┐      ┌──────────────────────────────────────────────┐
│ Analyst Console (web, same JS)│◄─────┤ Intel crawler (python, cron 6h)              │
│ C1 queue · C2 case · C3 review│      │ RSS + Google News → filter → dedupe →        │
│ C4 registry diff + rollback   │      │ family classify → novelty → REVIEW queue     │
└───────────────────────────────┘      └──────────────────────────────────────────────┘
```

## 2 · M1 GateEngine (interception)

```
events: (app_switch[from,to,t], payee, amount, contact_saved?)
gate(payment):
  dwell = foreground_dwell("chat_app")            # sirf chat screen ka time
  cell  = (contact_saved ? SAVED : UNKNOWN) x (dwell < 8min ? SHORT : LONG)
  tier  = MATRIX[cell]                            # T1 fast / T1 notify / T2 cooling / T3 hold
  if answers["officer_or_offer"] == yes: tier = T3
  return tier   # kabhi block nahi — friction + options
```

Anti-cheat: foreground-dwell only · 5 s me wapas = credit nahi · “It's me” se escalate clear nahi hota · fast path per-payee 24h me ek baar.

## 3 · M4 VerdictEngine (shared by app + eval + console)

```
classify(text):
  t = redactPII(text)                       # R28
  pass_rule = first PASS rule match
  hit = first non-PASS rule match
  if negated(hit) and no scam-words: hit = None      # R27
  S = sebiCheck(t)                          # R26
  dom = checkDomain(linkIn(t))              # M3
  verdict = SCAM LIKELY | CAUTION | SEEMS OK | UNCERTAIN(PAUSE)
  return {verdict, reasons, karo, mat_karo, citations}
```

**Ek hi function** app me bhi chalta hai aur `eval/` ke 33 messages par bhi — isliye eval honest hai.

## 4 · M7 EvidenceBuilder

```
ingest(files) → crop(smart: status/nav/letterbox hatana) → ocr(offline) → entities(UPI, txn, UTR, link, ₹)
             → qr_decode → redact(2 copies: full + masked) → sha256(per file) → annexure.csv
             → ncrp_text(HI+EN) + email.eml + bundle.zip
```

## 5 · R38 EmergencyActions

```
onConfirm(): clock.start()
  emailDraft()   → mailto: cyber-cell(3 IDs) + cc nodal officer  (user tap)
  callHelpline() → tel:1930 / 1909 / bank / Bihar CC + script   (user tap)
  paymentStop()  → dispute text + CFCFRMS hold + freeze + mandate revoke
  optional: familyAlert() → wa.me share (masked numbers)
```

## 6 · Threat feeds & publish path

Review queue → analyst 15-sec approve → registry JSON diff → app OTA (versioned, rollbackable). Auto-publish **nahi**.

## 7 · Failure modes (accepted)

| Failure | Behaviour |
|---|---|
| UsageStats band | manual dropdown (kitni der chat) → phir bhi matrix |
| Notification access band | M5 manual mandate entry |
| iOS par M1/M2/M5 partial | manual paste-check mode |
| OCR galat | redaction “verify” step + user edit |
| Registry offline | sirf format check + “khud sebi.gov.in par check karo” |
