#!/usr/bin/env python3
"""Assemble satarkpay_m2.html from parts.
  _base.html        : shell + CSS + M1/M2 markup
  _part_m3m6.html   : M3..M6 markup
  _part_app.js      : all JS (with __INTEL_JSON__ placeholder)
  _intel.json       : live-crawl snapshot (Intel Desk)
  data/scam_library_hi.json : M8 public-safety library (inline hota hai)
Run:  python3 build.py   ->  satarkpay_m2.html (+ _extracted.js for node checks)
"""
from pathlib import Path
import re

H = Path(__file__).parent
base = (H / '_base.html').read_text(encoding='utf-8')
part = (H / '_part_m3m6.html').read_text(encoding='utf-8')
js = (H / '_part_app.js').read_text(encoding='utf-8').replace(
    '__INTEL_JSON__', (H / '_intel.json').read_text(encoding='utf-8'))

# M8 · Scam Library (Hindi) — offline ke liye build time par inline
_scam = H.parent / 'data' / 'scam_library_hi.json'
js = js.replace('__SCAM_LIBRARY__',
                _scam.read_text(encoding='utf-8') if _scam.exists() else '{}')

assert base.count('</main>') == 1, 'base must have exactly one </main>'
out = base.replace('</main>', part + '\n</main>', 1)
out = out.replace('</body>', '<script>\n' + js + '\n</script>\n</body>', 1)
(H / 'satarkpay_m2.html').write_text(out, encoding='utf-8')
(H / '_extracted_for_check.js').write_text(re.search(r'<script>(.*)</script>', out, re.S).group(1), encoding='utf-8')
(H / '_extracted_for_check.js').unlink()
print('built satarkpay_m2.html', len(out) // 1024, 'KB')
