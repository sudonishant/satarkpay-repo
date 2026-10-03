# data/ · snapshots (privacy-safe)

| File | Kya | Note |
|---|---|---|
| `intel_snapshot.json` | M6 crawler ka aaj ka output (784 raw → 172 relevant → 63 novel) | sirf **headlines + source links** (public), article body nahi — DPDP/copyright-safe |
| `repo_matrix.csv` / `.json` | 31 peer repos ka feature matrix | OSINT scan 3 Oct 2026 |
| `repo_licenses.json` | har repo ka license/stars | **30 × NONE, 1 × MIT** → zero code copying ka aadhaar |
| `OSINT_REPORT.md` | poora landscape + gaps + mapping | Hinglish |
| `problem_statement.pdf` / `.txt` | SANGYAN ka official PS | public |
| `case_pack/` | asli fraud case ke **documents** | ⚠️ sirf masked versions: `annexure_index.csv`, `case_facts.json`, `complaint_ncrp.md`, `complaint_email.html` |
| `case_pack/README.md` | case ka summary + kya mask kiya | raw screenshots **private** hain (kisi ka asli PII) |

## Privacy rule (repo ke liye)

- Yahan **koi raw screenshot / asli UPI ID / phone number / account number commit nahi hota**.
- Har payment figure masked ya synthetic hai (`7349••••••@ptaxis`, `XXXXXXXX4707`).
- Naya evidence add karna ho to pehle `tools/crop_tool.py --mask` chalao, phir `data/case_pack/` me daalo.
