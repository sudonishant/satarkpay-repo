# data/ · snapshots (privacy-safe)

| File | Kya | Note |
|---|---|---|
| `intel_snapshot.json` | M6 crawler threat intelligence output | Public headlines + source links (DPDP/copyright-safe) |
| `problem_statement.txt` | SANGYAN official problem statement notes | Reference guidelines |
| `case_pack/` | asli fraud case ke **documents** | ⚠️ sirf masked versions: `annexure_index.csv`, `case_facts.json`, `complaint_ncrp.md`, `complaint_email.html` |
| `case_pack/README.md` | case ka summary + kya mask kiya | raw screenshots **private** hain (kisi ka asli PII) |

## Privacy rule (repo ke liye)

- Yahan **koi raw screenshot / asli UPI ID / phone number / account number commit nahi hota**.
- Har payment figure masked ya synthetic hai (`7349••••••@ptaxis`, `XXXXXXXX4707`).
- Naya evidence add karna ho to pehle `tools/crop_tool.py --mask` chalao, phir `data/case_pack/` me daalo.
