#!/usr/bin/env python3
"""OCR all screenshots -> evidence_toolkit/ocr/<name>.txt + ocr_all.json (word boxes ke saath)."""
import json, sys
from pathlib import Path
from rapidocr_onnxruntime import RapidOCR
from PIL import Image

RAW = Path('/home/user/scam_evidence/raw')
OUT = Path('/home/user/evidence_toolkit/ocr'); OUT.mkdir(parents=True, exist_ok=True)
ocr = RapidOCR()
allj = {}
files = sorted(RAW.glob('*.jpg'))
for i, f in enumerate(files, 1):
    res, _ = ocr(str(f))
    lines = []
    boxes = []
    if res:
        for box, txt, conf in res:
            xs = [p[0] for p in box]; ys = [p[1] for p in box]
            boxes.append({"t": txt, "c": round(float(conf), 3),
                          "x0": int(min(xs)), "y0": int(min(ys)), "x1": int(max(xs)), "y1": int(max(ys))})
            lines.append(txt)
    (OUT / (f.stem + '.txt')).write_text('\n'.join(lines), encoding='utf-8')
    allj[f.name] = {"size": Image.open(f).size, "boxes": boxes}
    print(f"[{i}/{len(files)}] {f.name}: {len(lines)} lines", flush=True)
(OUT / 'ocr_all.json').write_text(json.dumps(allj, ensure_ascii=False, indent=1), encoding='utf-8')
print("saved", OUT/'ocr_all.json')
