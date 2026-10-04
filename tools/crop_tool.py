#!/usr/bin/env python3
"""
SatarkPay · Evidence Toolkit — CROP + REDACT  (rule R25)

Kya karta hai
-------------
1. Smart crop: har screenshot se phone ka status bar, Truecaller ka bottom nav aur
   black letterbox bars hata deta hai -> sirf asli evidence bachta hai.
2. Focus crop: jahan payment/transaction detail hai (Transaction ID, UTR, Paid to,
   Banking Name, QR) wahan ka tight crop — complaint ke saath ye sabse kaam ka hota hai.
3. QR detect + decode (agar QR hai) -> UPI ID / payload nikaal deta hai (complaint me daalne ke liye).
4. Redaction: account-number patterns (XXXXXXX4707, E086XXXXXXXX) blur; --mask se koi bhi
   extra number/text blur kar sakte ho (jo aap share nahi karna chahte).

Output
------
case_evidence/
  crops_full/       <- smart crop, kuch bhi blur nahi  (police / bank ko dene ke liye)
  focus/            <- payment-detail ke tight crops
  crops_redacted/   <- blur wale (social media / WhatsApp forward ke liye)
  contact_sheet_full.jpg / contact_sheet_redacted.jpg / evidence_sheet.pdf

Usage
-----
python3 crop_tool.py --raw /home/user/scam_evidence/raw --out /home/user/case_evidence \
        [--mask "+91 9XXXXXXXXX" --mask "Ekta"]
"""
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path
from PIL import Image, ImageFilter, ImageDraw, ImageFont
import numpy as np

try:
    import cv2
except ImportError:
    cv2 = None

OCR_JSON = Path(os.environ.get('SATARK_OCR_JSON', Path.home()/'satarkpay'/'ocr'/'ocr_all.json'))
NAV_LABELS = {'calls', 'messages', 'scams', 'premium', 'voicemail', 'chats', 'updates', 'communities'}


# ------------------------------------------------------------------ crop helpers
def chrome_trim(boxes, w, h):
    """OCR boxes se status bar + bottom nav hatao."""
    nav_y = min([b['y0'] for b in boxes
                 if b['t'].strip().lower() in NAV_LABELS and b['y0'] > 0.72 * h], default=None)
    top_items = [b for b in boxes if b['y1'] < 0.075 * h]
    top_y = max([b['y1'] for b in top_items], default=None)

    y0 = int(0.030 * h) if not top_y or top_y > 0.10 * h else min(int(top_y + 10), int(0.10 * h))
    y1 = int(0.930 * h) if nav_y is None else max(int(nav_y - 8), int(0.55 * h))
    return y0, y1


def trim_letterbox(img, y0, y1, dark=26, keep_min=0.35):
    a = np.asarray(img.convert('L'))
    h = a.shape[0]
    while y0 < y1 and (y1 - y0) > keep_min * h and a[y0].mean() < dark:
        y0 += 2
    while y1 > y0 and (y1 - y0) > keep_min * h and a[y1 - 1].mean() < dark:
        y1 -= 2
    return y0, y1


def smart_crop(img: Image.Image, boxes, pad=8):
    w, h = img.size
    y0, y1 = chrome_trim(boxes, w, h)
    y0, y1 = trim_letterbox(img, y0, y1)
    return img.crop((0, max(0, y0 - pad), w, min(h, y1 + pad)))


FOCUS_PATTERNS = [
    r"transaction\s*id", r"\butr\b", r"paid to", r"banking name", r"transfer details",
    r"debited from", r"upi\s*id", r"utr[: ]", r"phonepe transaction",
]
def focus_crop(img: Image.Image, boxes, pad=60):
    """Payment-detail block (Transaction ID/UTR/Paid to) ke around tight crop."""
    hits = [b for b in boxes if any(re.search(p, b['t'], re.I) for p in FOCUS_PATTERNS)]
    if not hits:
        return None
    x0 = min(b['x0'] for b in hits); x1 = max(b['x1'] for b in hits)
    y0 = min(b['y0'] for b in hits); y1 = max(b['y1'] for b in hits)
    # block thoda bada karo (aas-paas ke amount/name bhi aane chahiye)
    x0 = max(0, x0 - pad); x1 = min(img.width, x1 + pad)
    y0 = max(0, y0 - int(pad * 2.2)); y1 = min(img.height, y1 + int(pad * 1.4))
    if x1 - x0 < 120 or y1 - y0 < 120:
        return None
    return img.crop((x0, y0, x1, y1))


def qr_scan(img: Image.Image):
    """QR code dhundo aur decode karo (UPI payload complaint me kaam aata hai)."""
    if cv2 is None:
        return None
    a = np.asarray(img.convert('RGB'))[:, :, ::-1].copy()
    det = cv2.QRCodeDetector()
    try:
        ok, decoded, pts, _ = det.detectAndDecodeMulti(a)
    except Exception:
        return None
    if not ok or decoded is None:
        return None
    if pts is None:
        pts = []
    pts = list(pts)
    out = []
    for text, quad in zip(decoded, pts):
        if not text:
            continue
        xs = [p[0] for p in quad]; ys = [p[1] for p in quad]
        out.append({"payload": text,
                    "bbox": [int(min(xs)), int(min(ys)), int(max(xs)), int(max(ys))]})
    return out or None


# ------------------------------------------------------------------ redaction
ACC_PATTERNS = [r"[Xx]{4,}\s?\d{3,6}", r"\d{3,}\s?[Xx]{4,}", r"[EXex]\d{3}[Xx]{4,}", r"\b\d{12,18}\b",
                r"[Xx]{3,}\d{2,}"]
def redact(img: Image.Image, boxes, extra_masks, pad=6):
    """Account-number patterns + user-supplied strings blur karo."""
    pats = list(ACC_PATTERNS)
    for m in extra_masks:
        if m.strip():
            pats.append(re.escape(m.strip()))
    rx = re.compile('|'.join(pats), re.I)
    out = img.copy()
    hit = 0
    for b in boxes:
        if rx.search(b['t']):
            x0 = max(0, b['x0'] - pad); y0 = max(0, b['y0'] - pad)
            x1 = min(img.width, b['x1'] + pad); y1 = min(img.height, b['y1'] + pad)
            if x1 - x0 < 4 or y1 - y0 < 4:
                continue
            region = out.crop((x0, y0, x1, y1)).filter(ImageFilter.GaussianBlur(9))
            out.paste(region, (x0, y0))
            d = ImageDraw.Draw(out)
            d.rectangle([x0, y0, x1, y1], outline=(255, 90, 90), width=2)
            hit += 1
    return out, hit


# ------------------------------------------------------------------ sheets
def label_font(size=17):
    for p in ('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',
              '/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf'):
        if Path(p).exists():
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()


def contact_sheet(items, out_path, cols=5, tile_w=380, title=''):
    """items: [(path, caption)]"""
    font = label_font(15); tfont = label_font(22)
    tiles = []
    for p, cap in items:
        im = Image.open(p).convert('RGB')
        r = tile_w / im.width
        im = im.resize((tile_w, int(im.height * r)))
        canvas = Image.new('RGB', (tile_w, im.height + 54), (14, 18, 28))
        canvas.paste(im, (0, 44))
        d = ImageDraw.Draw(canvas)
        d.text((8, 10), cap[:44], fill=(220, 230, 245), font=font)
        tiles.append(canvas)
    rows = (len(tiles) + cols - 1) // cols
    th = max(t.height for t in tiles)
    sheet = Image.new('RGB', (cols * tile_w, rows * th + 56), (8, 11, 18))
    d = ImageDraw.Draw(sheet)
    d.text((14, 14), title, fill=(120, 200, 255), font=tfont)
    for i, t in enumerate(tiles):
        sheet.paste(t, ((i % cols) * tile_w, (i // cols) * th + 56))
    sheet.save(out_path, quality=88)
    return sheet


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--raw', default=os.getcwd())
    ap.add_argument('--out', default=str(Path.cwd()/'crops'))
    ap.add_argument('--mask', action='append', default=[],
                    help='extra string blur karne ke liye (repeat kar sakte ho)')
    a = ap.parse_args()

    raw = Path(a.raw); out = Path(a.out)
    for sub in ('crops_full', 'crops_redacted', 'focus'):
        (out / sub).mkdir(parents=True, exist_ok=True)

    ocr = json.loads(OCR_JSON.read_text(encoding='utf-8')) if OCR_JSON.exists() else {}
    manifest, full_items, red_items = [], [], []
    qr_found = []

    files = sorted(raw.glob('*.jpg'))
    for f in files:
        meta = ocr.get(f.name, {})
        boxes = meta.get('boxes', [])
        img = Image.open(f).convert('RGB')

        crop = smart_crop(img, boxes)
        cp = out / 'crops_full' / f.name
        crop.save(cp, quality=92)

        red, nhits = redact(crop, boxes, a.mask)
        rp = out / 'crops_redacted' / f.name
        red.save(rp, quality=88)

        foc = focus_crop(img, boxes)
        fp = None
        if foc:
            fp = out / 'focus' / (f.stem + '__payment.jpg')
            foc.save(fp, quality=92)

        qrs = qr_scan(crop)
        if qrs:
            for i, q in enumerate(qrs):
                x0, y0, x1, y1 = q['bbox']
                pad = 30
                qi = crop.crop((max(0, x0 - pad), max(0, y0 - pad),
                                min(crop.width, x1 + pad), min(crop.height, y1 + pad)))
                qip = out / 'focus' / (f.stem + f'__qr{i+1}.jpg')
                qi.save(qip, quality=92)
                qr_found.append({'file': f.name, 'payload': q['payload'], 'crop': str(qip)})
            # QR ke saath smart crop bhi full rakho (payload complaint me jayega)

        manifest.append({'file': f.name, 'original': str(f), 'crop': str(cp), 'redacted': str(rp),
                         'focus': str(fp) if fp else None, 'blur_hits': nhits,
                         'size': list(img.size), 'crop_size': list(crop.size)})
        full_items.append((cp, f.stem.replace('Screenshot_20261002_', '').replace('_Truecaller', '')))
        red_items.append((rp, f.stem.replace('Screenshot_20261002_', '').replace('_Truecaller', '')))
        print(f'✓ {f.name}  crop={crop.size}  blurs={nhits}  focus={"y" if fp else "-"}  qr={len(qrs) if qrs else 0}')

    contact_sheet(full_items, out / 'contact_sheet_full.jpg',
                  title=f'CASE EVIDENCE — full crops ({len(files)} screenshots) · SatarkPay toolkit')
    contact_sheet(red_items, out / 'contact_sheet_redacted.jpg',
                  title=f'CASE EVIDENCE — redacted (account numbers blurred) · {len(files)} screenshots')

    pages = [Image.open(out / 'contact_sheet_full.jpg'), Image.open(out / 'contact_sheet_redacted.jpg')]
    pages[0].save(out / 'evidence_sheet.pdf', save_all=True, append_images=pages[1:], quality=85)

    (out / 'crop_manifest.json').write_text(json.dumps(
        {'generated': __import__('datetime').datetime.now().isoformat(timespec='seconds'),
         'screenshots': len(files), 'qr_found': qr_found, 'items': manifest,
         'extra_masks': a.mask}, ensure_ascii=False, indent=1), encoding='utf-8')
    print(f'\n✓ crops: {out}/crops_full  · focus: {out}/focus  · redacted: {out}/crops_redacted')
    print(f'✓ sheets: contact_sheet_full.jpg · contact_sheet_redacted.jpg · evidence_sheet.pdf')
    if qr_found:
        print(f'✓ QR decoded: {len(qr_found)} → ' + '; '.join(q['payload'][:60] for q in qr_found))


if __name__ == '__main__':
    main()
