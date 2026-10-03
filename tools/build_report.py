#!/usr/bin/env python3
"""
SatarkPay · Auto-Report builder (rule R24/R25)
Input:  OCR output + crop manifest + case_facts.json
Output: entities.json · annexure_index.csv · complaint_ncrp.md (EN+HI) ·
        complaint_email.html · complaint_email.eml (ready-to-send, crops attached) · evidence_summary.md

Email bhejne ke liye (optional):  SMTP_USER=... SMTP_PASS=... python3 build_report.py --send
(bina --send ke sirf .eml file banti hai — aap usse Gmail me open karke bhej sakte ho.)
"""
from __future__ import annotations
import argparse, csv, hashlib, html, json, os, re, smtplib, ssl, sys
from datetime import datetime
from email.message import EmailMessage
from pathlib import Path

TOOL = Path('/home/user/evidence_toolkit')
CASE = Path('/home/user/case_evidence')
RAW = Path('/home/user/scam_evidence/raw')
OCR = TOOL / 'ocr'
SUBJECT_PREFIX = '[CYBER FRAUD COMPLAINT]'

UPI_RX = re.compile(r'[A-Za-z0-9._%+-]{2,}@(?:ptaxis|ybl|okaxis|oksbi|okhdfcbank|okicici|paytm|axl|ibl|upi|apl|jio|airtel|fbl|hdfcbank|icici|sbi|kotak|barodampay|pnb|yesbank|indus|axisbank|yapl|rapl|waaxis|waicici|wahdfcbank|wasbi|idfcbank|naviaxis|sliceaxis)')
UTR_RX = re.compile(r'(?:PhonePe|Google\s?Pay|Paytm)?\s*(?:Transaction\s?ID|Txn\s?ID|UTR)\s*[:#]?\s*([A-Za-z0-9]{6,})', re.I)
TXN_RX = re.compile(r'\bT\d{15,}\b')   # PhonePe/UPI style txn ids
AMT_RX = re.compile(r'(?:₹|Rs\.?|INR|R)\s?([0-9][0-9,]{1,9})(?:\.\d{2})?', re.I)
LINK_RX = re.compile(r'(?:https?://|www\.)[^\s<>"\')]+|[a-z0-9-]+\.(?:ly|pe|me|gl|co|in|com|net|org)(?:/[^\s<>"\')]*)?', re.I)
SHORTENERS = ('cutt.ly', 'bit.ly', 'tinyurl.com', 'rb.gy', 't.co', 'ow.ly', 'is.gd', 'shorturl.at')


def sha256(p: Path) -> str:
    h = hashlib.sha256()
    with open(p, 'rb') as f:
        for chunk in iter(lambda: f.read(1 << 20), b''):
            h.update(chunk)
    return h.hexdigest()


def extract_entities():
    qr = json.loads((CASE / 'crop_manifest.json').read_text(encoding='utf-8'))
    qr_payloads = [q['payload'] for q in qr.get('qr_found', [])]
    ents = {'upi_ids': set(), 'utrs': set(), 'txn_ids': set(), 'amounts': set(),
            'links': set(), 'shortlinks': set(), 'qr_payloads': qr_payloads}
    for f in sorted(OCR.glob('Screenshot_*.txt')):
        t = f.read_text(encoding='utf-8')
        ents['upi_ids'] |= set(m.group(0) for m in UPI_RX.finditer(t))
        for m in UTR_RX.finditer(t):
            v = m.group(1)
            (ents['txn_ids'] if v.upper().startswith('T2') else ents['utrs']).add(v)
        for v in TXN_RX.findall(t):
            ents['txn_ids'].add(v)
        ents['amounts'] |= set(m.group(1) for m in AMT_RX.finditer(t))
        for m in LINK_RX.finditer(t):
            u = m.group(0).rstrip('.')
            if any(s in u for s in SHORTENERS):
                ents['shortlinks'].add(u)
            elif '/' in u or u.startswith('http'):
                ents['links'].add(u)
    for p in qr_payloads:                       # QR payload se UPI ID
        for m in UPI_RX.finditer(p):
            ents['upi_ids'].add(m.group(0))
    return {k: sorted(v) for k, v in ents.items()}


def annexure_rows():
    rows = []
    for i, f in enumerate(sorted((CASE / 'crops_full').glob('*.jpg')), 1):
        stem = f.stem
        src = RAW / f.name
        rows.append({'sl': i, 'evidence_id': f'A{i:02d}', 'file': f.name,
                     'sha256_crop': sha256(f)[:32], 'sha256_original': sha256(src)[:32] if src.exists() else '',
                     'size_kb': round(f.stat().st_size / 1024), 'notes': ''})
    return rows


def facts():
    return json.loads((CASE / 'case_facts.json').read_text(encoding='utf-8'))


def complaint_md(f, ents, rows):
    L = []
    A = L.append
    A(f"# CYBER CRIME / UPI FRAUD COMPLAINT\n")
    A(f"**Case ID (internal):** {f['case_id']}  |  **Date:** 02 Oct 2026  |  **District:** {f['victim']['district']}, {f['victim']['state']}\n")
    A("> Ye text NCRP portal (cybercrime.gov.in) ke 'Report Cyber Financial Fraud' form me copy-paste karne ke liye hai.\n")
    A("## 1 · Complaint (English)\n")
    A("I wish to report a series of online financial frauds committed against me through UPI, WhatsApp and phishing SMS. "
      "I have preserved all evidence as screenshots on my device and I am attaching cropped copies with this complaint.\n")
    A("**Money actually transferred to fraudsters (evidence attached):**\n")
    for l in f['losses']:
        if 'status' in l and l['status'].startswith(('demand', 'loan')):
            continue
        acc = f", a/c {l.get('account_masked')}" if l.get('account_masked') else ""
        A(f"- ₹{l['amount']:,} paid via {l['app']} to **{l['paid_to']}**{acc}"
          + (f", UPI ID `{l['upi_id']}`" if l.get('upi_id') else "")
          + f", Transaction ID `{l['txn_id']}`, UTR `{l['utr']}`, on {l['date_visible']}.")
    A(f"\n**Total transferred (visible in evidence): ₹{f['visible_loss_total']:,}**\n")
    A("**Further amounts demanded (not paid / to be confirmed):**\n")
    for l in f['losses']:
        if not ('status' in l and l['status'].startswith(('demand', 'loan'))):
            continue
        A(f"- ₹{l['amount']:,} — {l['item']}" + (f" (suspect: {l['suspect']})" if l.get('suspect') else ""))
    A("\n**Modus operandi (as seen in the evidence):**\n")
    for p in f['scam_patterns']:
        A(f"- **[{p['id']}] {p['name']}** — {p['detail']}")
    A("\n**Suspect identifiers extracted from evidence:**\n")
    A(f"- UPI ID (decoded from the payment QR in the screenshot): `{ents['upi_ids'][0] if ents['upi_ids'] else 'N/A'}` (payee name shown: AYESHA MEHAK)")
    A(f"- Transaction IDs: {', '.join('`'+x+'`' for x in sorted(ents['txn_ids']))}")
    A(f"- UTRs: {', '.join('`'+x+'`' for x in sorted(ents['utrs']))}")
    if ents['shortlinks']:
        A(f"- Phishing short-links: {', '.join('`'+x+'`' for x in ents['shortlinks'])}")
    if ents['links']:
        A(f"- Other links seen: {', '.join('`'+x+'`' for x in sorted(ents['links'])[:8])}")
    A("\n**Suspect phone numbers:** screenshots me partially masked hain (+91 9XXXXX). "
      "Full numbers complainant ke phone me hain aur NCRP form me diye jayenge.\n")
    A("## 2 · Evidence\n")
    A(f"- {len(rows)} cropped screenshots, each with SHA-256 hash in the annexure (chain of custody).")
    A("- Originals unedited device par preserved hain; annexure me original file ke hashes bhi hain.\n")
    A("## 3 · Relief sought\n")
    A("- Freeze/trace the beneficiary accounts & UPI IDs and recover the transferred amount under CFCFRMS.")
    A("- Action against the accused under relevant provisions (BNS / IT Act 66C-66D).")
    A("- Directions to the PSP/telecom to block the fraudulent handles and numbers.\n")
    A("## 4 · शिकायत (हिंदी — NCRP form में paste करने के लिए)\n")
    A("मैं सूचित करना चाहता/चाहती हूँ कि मेरे साथ UPI, WhatsApp और फ़िशिंग SMS के ज़रिए धोखाधड़ी हुई है। "
      "मेरे पास सभी स्क्रीनशॉट सबूत के रूप में सुरक्षित हैं, जिनकी क्रॉप की हुई कॉपियाँ इस शिकायत के साथ भेजी गई हैं।\n")
    A(f"- UPI से भेजी गई राशि (सबूत के साथ): **₹{f['visible_loss_total']:,}** "
      f"(₹3,000 — AYESHA MEHAK, UPI `7349045416@ptaxis`, Txn `T2606252059539226326113`, UTR `462HD163`; "
      f"₹2,000 — N RAJESH, अकाउंट XXXXXXXX4707, Txn `T2505301200368307075101`, UTR `589501728666`)।")
    A("- और माँगी गई रकम (भुगतान नहीं की / पुष्टि करनी है): ₹30,000 (लोन एडवांस फ़ीस), ₹375 (नकली नौकरी 'टेस्ट टास्क' फ़ीस), ₹250 ('पुराना सिक्का' रजिस्ट्रेशन फ़ीस)।")
    A("- तरीका: नकली पार्सल/रिफंड, नकली फ़ाइनेंस कंपनी 'Ram Mudra Finance', नकली नौकरी, QR कोड पेमेंट ट्रैप, फ़िशिंग SMS (short-link), और फ़ोटो-ब्लैकमेल की धमकी।")
    A("- अनुरोध: दोषी अकाउंट/UPI ID फ्रीज़ कराकर राशि वापस दिलाई जाए (CFCFRMS), और दोषियों पर BNS/IT Act के तहत कार्रवाई हो।\n")
    A("## 5 · Immediate steps (complainant ke liye checklist)\n")
    for i, s in enumerate(f['immediate_actions'], 1):
        A(f"{i}. {s}")
    return "\n".join(L)


def complaint_email_html(f, ents, rows):
    def esc(x): return html.escape(str(x))
    loss_rows = "".join(
        f"<tr><td>₹{l['amount']:,}</td><td>{esc(l['app'])}</td><td>{esc(l['paid_to'])}"
        f"{'<br><span class=mono>'+esc(l['upi_id'])+'</span>' if l.get('upi_id') else ''}"
        f"{'<br>a/c '+esc(l.get('account_masked','')) if l.get('account_masked') else ''}</td>"
        f"<td class=mono>{esc(l['txn_id'])}<br>{esc(l['utr'])}</td><td>{esc(l['date_visible'])}</td></tr>"
        for l in f['losses'] if not ('status' in l and l['status'].startswith(('demand', 'loan'))))
    dem_rows = "".join(f"<li>₹{l['amount']:,} — {esc(l['item'])}</li>"
                       for l in f['losses'] if 'status' in l and l['status'].startswith(('demand', 'loan')))
    pat_rows = "".join(f"<li><b>[{esc(p['id'])}] {esc(p['name'])}</b> — {esc(p['detail'])}</li>" for p in f['scam_patterns'])
    thumb = ""
    if (CASE / 'contact_sheet_redacted.jpg').exists():
        thumb = f'<p><img src="cid:sheet" style="max-width:100%;border-radius:10px" alt="evidence sheet"></p>'
    return f"""<html><body style="font-family:system-ui,Segoe UI,Roboto,sans-serif;background:#0b0f19;color:#e8eef9;padding:18px">
<div style="max-width:760px;margin:auto;background:#111a2c;border:1px solid #22304c;border-radius:14px;padding:20px">
<h2 style="margin:0 0 6px">Cyber fraud complaint — {esc(f['case_id'])}</h2>
<p style="color:#93a4c2;margin:0 0 14px">Patna, Bihar · 02 Oct 2026 · UPI / WhatsApp / phishing fraud · evidence: {len(rows)} cropped screenshots (hashes annexure me)</p>
<p>Respected Sir/Madam,<br>
I am reporting online financial fraud committed against me. Amount actually transferred (with evidence):
<b>₹{f['visible_loss_total']:,}</b>. Further amounts demanded: <b>≈₹{f['demanded_total_approx']:,}</b>. Please register the complaint
and initiate CFCFRMS freeze/trace on the beneficiary accounts and UPI IDs.</p>
<h3>1 · Transferred amounts (evidence attached)</h3>
<table style="width:100%;border-collapse:collapse;font-size:13px" cellpadding="6">
<tr style="background:#16233c"><th align="left">Amount</th><th align="left">App</th><th align="left">Paid to</th><th align="left">Txn / UTR</th><th align="left">Date</th></tr>
{loss_rows}</table>
<h3>2 · Further demands (not paid / to confirm)</h3><ul>{dem_rows}</ul>
<h3>3 · Modus operandi</h3><ul>{pat_rows}</ul>
<h3>4 · Key identifiers</h3>
<ul>
<li>UPI ID (QR se decode): <span class="mono">{esc(ents['upi_ids'][0] if ents['upi_ids'] else '')}</span> — payee name: AYESHA MEHAK</li>
<li>Transaction IDs: {esc(', '.join(sorted(ents['txn_ids'])))}</li>
<li>UTRs: {esc(', '.join(sorted(ents['utrs'])))}</li>
<li>Phishing links: {esc(', '.join(ents['shortlinks']) or '—')}</li>
<li>Suspect numbers: screenshots me masked (+91 9XXXXX) — full numbers NCRP form me.</li>
</ul>
{thumb}
<p style="color:#93a4c2;font-size:12.5px">Attachments: most-probative cropped screenshots (poora set evidence_bundle.zip me) + evidence sheet.
Originals ke SHA-256 hashes annexure_index.csv me hain. Koi bhi file badalne par hash match nahi hoga.</p>
<p>Regards,<br>— Complainant ({esc(f['victim']['name_note'])}), {esc(f['victim']['district'])}, {esc(f['victim']['state'])}<br>
Helpline reference: 1930 · NCRP acknowledgement: ______</p>
</div></body></html>"""


def build_eml(f, ents, rows, html_body):
    msg = EmailMessage()
    msg['Subject'] = f"{SUBJECT_PREFIX} UPI/WhatsApp fraud — ₹{f['visible_loss_total']:,} transferred, ₹{f['demanded_total_approx']:,} demanded — {f['case_id']}"
    msg['From'] = os.environ.get('SMTP_USER', 'complainant@example.com')
    tos = [c['value'] for c in f['reporting_channels'] if c.get('type') == 'email']
    msg['To'] = ', '.join(tos)
    msg.set_content("Ye HTML email hai — kripya HTML view me dekhein (evidence table + summary attached).")
    msg.add_alternative(html_body, subtype='html')

    # redacted sheet inline
    sheet = CASE / 'contact_sheet_redacted.jpg'
    if sheet.exists():
        html_part = msg.get_payload()[-1]
        html_part.add_related(sheet.read_bytes(), maintype='image', subtype='jpeg', cid='<sheet>')
    # full crops attach
    KEY = {'215408','215404','215400','215419','215423','215427','214930','214938','214753','215014'}
    key_rows = [r for r in rows if any(k in r['file'] for k in KEY)]
    for r in (key_rows or rows[:10]):
        p = CASE / 'crops_full' / r['file']
        if p.exists():
            msg.add_attachment(p.read_bytes(), maintype='image', subtype='jpeg', filename=p.name)
    return msg, tos


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--send', action='store_true', help='SMTP se bhej do (SMTP_HOST/SMTP_USER/SMTP_PASS env chahiye)')
    a = ap.parse_args()

    f = facts()
    ents = extract_entities()
    rows = annexure_rows()

    # annexure csv
    with open(CASE / 'annexure_index.csv', 'w', newline='', encoding='utf-8') as fh:
        wr = csv.DictWriter(fh, fieldnames=list(rows[0].keys()))
        wr.writeheader(); wr.writerows(rows)

    (CASE / 'entities.json').write_text(json.dumps(ents, ensure_ascii=False, indent=1), encoding='utf-8')
    (CASE / 'complaint_ncrp.md').write_text(complaint_md(f, ents, rows), encoding='utf-8')

    html_body = complaint_email_html(f, ents, rows)
    (CASE / 'complaint_email.html').write_text(html_body, encoding='utf-8')

    msg, tos = build_eml(f, ents, rows, html_body)
    (CASE / 'complaint_email.eml').write_bytes(msg.as_bytes())

    print(f"✓ entities.json  → UPI: {ents['upi_ids']} | txn: {ents['txn_ids']} | utr: {ents['utrs']} | shortlinks: {ents['shortlinks']}")
    print(f"✓ annexure_index.csv ({len(rows)} items, sha256 ke saath)")
    print(f"✓ complaint_ncrp.md (EN + HI)  · complaint_email.html  · complaint_email.eml")
    print(f"✓ To: {', '.join(tos)}")

    if a.send:
        host = os.environ.get('SMTP_HOST', 'smtp.gmail.com'); port = int(os.environ.get('SMTP_PORT', 465))
        user, pwd = os.environ.get('SMTP_USER'), os.environ.get('SMTP_PASS')
        if not (user and pwd):
            print('! --send ke liye SMTP_USER / SMTP_PASS env chahiye (app password). Abhi sirf .eml bana hai.'); return
        msg['From'] = user; msg.replace_header('To', ', '.join(tos)) if 'To' in msg else msg['To']
        ctx = ssl.create_default_context()
        with smtplib.SMTP_SSL(host, port, context=ctx) as s:
            s.login(user, pwd); s.send_message(msg)
        print(f"✓ email bhej diya → {tos}")


if __name__ == '__main__':
    main()
