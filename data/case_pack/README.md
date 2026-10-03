# case_pack · asli fraud case (masked)

Ye ek **asli UPI fraud case** hai jo team member ke saath hua — M7 evidence pipeline isi par chali.

| File | Kya |
|---|---|
| `case_facts.json` | case id, payments (₹3,000 + ₹2,000), txn/UTR, demands (₹30,625) |
| `annexure_index.csv` | 28 files ka index + SHA-256 (chain of custody) |
| `entities.json` | extracted UPI VPA, links, amounts |
| `complaint_ncrp.md` | NCRP ke liye complaint text (Hindi + English) |
| `complaint_email.html` | cyber-cell email draft (attachments ke saath) |
| `EVIDENCE_SUMMARY.md` | poora summary, 9 scam families |

**Privacy:** yahan sirf **masked** versions hain (`7349••••••@ptaxis`, `XXXXXXXX4707`). Raw screenshots aur unmasked bundle repo me **nahi** hain — wo victim ke private evidence folder me hain. Naya evidence add karne se pehle `tools/crop_tool.py --mask` chalao.
