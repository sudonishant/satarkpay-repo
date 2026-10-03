# tools/ · scripts (jo demo ke peeche chalte hain)

| Script | Kya karta hai | Chalane ka tareeqa |
|---|---|---|
| `intel_crawler.py` | M6 crawler — RSS + Google News (keyless) → fraud filter (EN/HI) → dedupe → family classify → novelty | `python3 intel_crawler.py --max-age-days 45 --out ../data/intel_snapshot.json` |
| `make_demo_snapshot.py` | crawler output → demo ka `_intel.json` | `python3 make_demo_snapshot.py` |
| `crop_tool.py` | M7 — smart crop, redact, QR decode (2 copies) | `python3 crop_tool.py --raw <folder> --out <out> [--mask "..."]` |
| `ocr_text.py` | M7 — offline OCR (RapidOCR/ONNX) | `python3 ocr_text.py` |
| `build_report.py` | M7 — complaint (HI+EN) + email + annexure CSV + ZIP | `python3 build_report.py [--send]` |
| `requirements.txt` | python deps | `pip install -r requirements.txt` |

**Dependencies:** `rapidocr-onnxruntime`, `opencv-python`, `Pillow`, `pymupdf` (chaaron pip-installable; tesseract ki zarurat nahi).
Sab scripts **offline** chalte hain — sirf crawler ko internet chahiye.
