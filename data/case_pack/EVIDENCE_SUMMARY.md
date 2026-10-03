# Evidence summary — asli fraud case (#SP-CASE-2026-1002-PATNA)

**Source:** 28 screenshots (Drive ZIP, `Screenshot_20261002_2142–2155_Truecaller.jpg`), device me 02 Oct 2026, 21:42–21:55 bane.
**Toolkit ne kya kiya:** smart crop (status bar/nav/black bars hataaye) → redaction (a/c numbers blur, alag copy) →
OCR (offline RapidOCR) → entities extract (UPI ID, txn/UTR, links, amounts) → QR decode → complaint pack.

## 1 · Paisa jo gaya (evidence ke saath, ye sabse important)

| # | Amount | App | Paid to | UPI / a/c | Txn ID | UTR | Date (screen par) | Evidence file |
|---|---|---|---|---|---|---|---|---|
| 1 | ₹3,000 | PhonePe | AYESHA MEHAK | **`7349XXXXX6@ptaxis`** (QR se decode) | T2606252059539226326113 | 462HD163 | 25 Jun 2026, 20:59 | 215419 · 215423 · 215427 |
| 2 | ₹2,000 | PhonePe | N RAJESH | a/c XXXXXXXX4707 · ph +91 91XXXXXX4707 | T2505301200368307075101 | 589501728666 | 30 May 2025, 12:00 pm | 215400 · 215404 · 215408 |

**Total evidence-me-dikhta nuksan: ₹5,000** · Aur maanga gaya (bheja gaya ya nahi — confirm karna hai): ₹30,000 (loan advance) + ₹375 (fake job fee) + ₹250 (coin scheme) ≈ **₹30,625**.

> Note: screenshots me hi dono payees alag-alag hain aur dono "refund/kaam" ke naam par liye gaye — ye **ek se zyada payee + repeat payments** = R22 (staircase) pattern hai.

## 2 · Scam families (aapke case se, library me map)

| Family | Kya hua | Evidence |
|---|---|---|
| F5/F6 parcel-refund | "parcel ka payment refund hoga", "cancel kara dijiye", number par refund ka vaada | 214246, 214308, 214325, 214344 |
| F7/F9 fake finance co. | "Ram Mudra Finance" — 4 din "bank verification", "mam free hote hi", screenshots maangna, voice call no-answer | 214930, 214938, 214942, 214945 |
| F2 APK/banking app | Telegram par "3.5 lakh loan approved" + sold banking APK | 214842, 214859 |
| F6 phishing SMS | fake ₹3,000 "VIP gift / bonus", `cutt.ly` shortlink | 214753, 214842 |
| F3 fake job | "IHCL recruiter", 5-star rating kaam, ₹375 test-task fee | 215102, 215106 |
| F17 parcel/parcel-boy | "I am delivery boy", "wait 5 minut", wapas karne ka chakkar | 215453, 215456, 215459, 215503 |
| F15 blackmail | "photo family ko bhej dunga", gallery dhamki | 215014 |
| F11 fake bill | iPhone invoice ₹1,45,990 (Ekta) | 215456 |
| F1/F17 loan-gang | galat number par loan app + dhamki, fake cheque ₹1.80 lakh (PNB) se trust | 215014, 214508, 214511 |

## 3 · Files jo mil gayi

- `crops_full/` — 28 clean crops (status bar/nav hataaya, kuch blur nahi) → **police / bank ke liye**
- `crops_redacted/` — wahi, a/c numbers blur → **WhatsApp/social share ke liye**
- `focus/` — payment-detail ke tight crops (Txn/UTR/Paid-to) + QR ka crop
- `contact_sheet_full.jpg` / `contact_sheet_redacted.jpg` / `evidence_sheet.pdf` — ek nigah me poora case
- `annexure_index.csv` — har file ka **SHA-256 hash** (chain of custody; file badli to hash match nahi hoga)
- `entities.json` — machine-extracted: UPI IDs, txn IDs, UTRs, links, amounts
- `complaint_ncrp.md` — NCRP form me paste karne wala text (**English + हिंदी**)
- `complaint_email.html` + `complaint_email.eml` — ready email (crops attached, evidence sheet inline)
- `evidence_bundle.zip` — sab kuch ek file me

## 4 · Aage kya karo (aaj hi, order me)

1. **1930** par call karo (golden hour) — txn/UTR ready rakho.
2. **cybercrime.gov.in** → "Report Cyber Financial Fraud" → acknowledgement number save karo.
3. NCRP **Suspect Repository Search** me `7349XXXXX6@ptaxis` + masked numbers daal kar check karo.
4. **PhonePe app** → dono transactions → Help/Report Issue → fraud dispute.
5. **Bank** ko likhit complaint (RBI limited-liability framework) + UPI/card block.
6. Number/UPI report: **Sanchar Saathi** (sancharsaathi.gov.in) + app me "Report".
7. Chats/screenshots **delete na karo**; scammer ko block karo; aur koi bhi naya "fee" mat bharo.

## 5 · Jo abhi confirm karna hai (aapke paas hoga)

- Dono masked numbers ke **full digits** (screenshots me `+91 9XXXXX…`).
- ₹30,000 / ₹375 / ₹250 me se kya-kya **actually transfer** hua (sirf maanga gaya tha?) — uske txn IDs add karne hain.
- Kis bank/app se transfer hua tha (Salary Account, PhonePe) — complaint me daalna hai.
