#!/usr/bin/env python3
"""SatarkPay · SANGYAN pitch deck (10 slides, premium light theme).
Research-backed: action titles · one idea per slide · 2 fonts · 3-5 colors · 60-30-10.
Outputs: SANGYAN_SatarkPay_Pitch.pptx + .pdf  (16:9)
Run: python3 build_pitch.py
"""
import pathlib
from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR

HERE = pathlib.Path(__file__).resolve().parent
DIA  = HERE / 'diagrams'
SHOTS = HERE.parent / 'deck' / 'screenshots'

# ---- premium palette (60-30-10: cream 60 · ink/slate 30 · gold 10) ----
CREAM  = RGBColor.from_string('FBF6EE')
INK    = RGBColor.from_string('14181F')
SLATE  = RGBColor.from_string('2C3E50')
DIM    = RGBColor.from_string('6B7280')
GOLD   = RGBColor.from_string('B08D57')
SAND   = RGBColor.from_string('E3D3B0')
SAGE   = RGBColor.from_string('5F7F6F')
CLAY   = RGBColor.from_string('B0654F')
TEAL   = RGBColor.from_string('3E6E78')
WHITE  = RGBColor.from_string('FFFFFF')
LINE   = RGBColor.from_string('E7DFCD')

F_HEAD = 'Liberation Sans'   # windows/office par Arial fallback ho jayega
F_BODY = 'Liberation Sans'

prs = Presentation()
prs.slide_width, prs.slide_height = Inches(13.333), Inches(7.5)
BLANK = prs.slide_layouts[6]

def slide(bg=CREAM):
    s = prs.slides.add_slide(BLANK)
    s.background.fill.solid(); s.background.fill.fore_color.rgb = bg
    return s

# ---------- auto-fit: text kabhi box se bahar/collide na ho ----------
from PIL import ImageFont as _IF
_FPATH = '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
_FPATH_B = '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'
_PX = 100  # px per inch (measurement only)

def _wrap_lines(text, w_in, sz, bold=False):
    f = _IF.truetype(_FPATH_B if bold else _FPATH, max(6, int(sz * _PX / 72)))
    limit = w_in * _PX - 4
    n, line = 1, ''
    for wd in text.split(' '):
        t = (line + ' ' + wd).strip()
        if f.getlength(t) > limit and line:
            n += 1; line = wd
        else:
            line = t
    return n

def est_height(runs, w_in, sp_after):
    h = 0.0
    for r in runs:
        sz = r.get('sz', 18) * (r.get('_k', 1.0))
        lines = _wrap_lines(str(r['t']), w_in, sz, r.get('b', False))
        h += lines * sz * 1.25 / 72 + sp_after / 72
    return h

def txt_fit(s, x, y, w, max_h, runs, align=PP_ALIGN.LEFT, sp_after=5):
    """runs ko shrink karta hai (min 9.5pt) taaki max_h me fit ho jaye."""
    k = 1.0
    while k > 0.6:
        trial = [dict(r, _k=k) for r in runs]
        if est_height(trial, w, sp_after) <= max_h:
            break
        k -= 0.02
    scaled = []
    for r in runs:
        rr = dict(r); rr['sz'] = max(9.5, r.get('sz', 18) * k); scaled.append(rr)
    return txt(s, x, y, w, max_h, scaled, align=align, sp_after=max(3, sp_after * k))

def txt(s, x, y, w, h, runs, align=PP_ALIGN.LEFT, anchor=MSO_ANCHOR.TOP, sp_after=4):
    tb = s.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h)); tf = tb.text_frame
    tf.word_wrap = True; tf.vertical_anchor = anchor
    first = True
    for r in runs:
        p = tf.paragraphs[0] if first else tf.add_paragraph(); first = False
        p.alignment = align; p.space_after = Pt(sp_after)
        run = p.add_run(); run.text = r['t']
        f = run.font
        f.size = Pt(r.get('sz', 18)); f.bold = r.get('b', False); f.italic = r.get('i', False)
        f.color.rgb = r.get('c', SLATE); f.name = F_HEAD if r.get('head') else F_BODY
    return tb

def rect(s, x, y, w, h, fill, line=None, lw=1.0, shadow=False):
    sh = s.shapes.add_shape(1, Inches(x), Inches(y), Inches(w), Inches(h))
    sh.fill.solid(); sh.fill.fore_color.rgb = fill
    if line: sh.line.color.rgb = line; sh.line.width = Pt(lw)
    else: sh.line.fill.background()
    sh.shadow.inherit = False
    return sh

def kicker(s, t, c=GOLD):
    txt(s, 0.75, 0.42, 11.8, 0.4, [{'t': t, 'sz': 12.5, 'b': True, 'c': c}])

def title(s, t):
    txt(s, 0.75, 0.78, 11.8, 1.0, [{'t': t, 'sz': 30, 'b': True, 'c': INK, 'head': True}])

def rule(s, y=1.72):
    ln = s.shapes.add_shape(1, Inches(0.78), Inches(y), Inches(11.8), Pt(1.6))
    ln.fill.solid(); ln.fill.fore_color.rgb = SAND; ln.line.fill.background(); ln.shadow.inherit = False

def footer(s, n):
    txt(s, 0.75, 7.05, 8.0, 0.3, [{'t': 'SatarkPay · Team SCΛMURΛI · SANGYAN 2026 · live: sudonishant.github.io/satarkpay-repo',
                                    'sz': 9, 'c': DIM}])
    txt(s, 12.35, 7.05, 0.5, 0.3, [{'t': str(n), 'sz': 10, 'b': True, 'c': GOLD}], align=PP_ALIGN.RIGHT)

def pic(s, path, x, y, w=None, h=None):
    return s.shapes.add_picture(str(path), Inches(x), Inches(y), Inches(w) if w else None, Inches(h) if h else None)

def pic_fit_h(path, h_in):
    """image ko di gayi height me fit karo -> returns width_in"""
    from PIL import Image
    im = Image.open(path); ar = im.width / im.height
    return h_in * ar

def pic_measure(path, w_in):
    """returns (x_inset, actual_width_in, height_in) — layout ko measurable banata hai"""
    from PIL import Image
    im = Image.open(path); ar = im.height / im.width
    return w_in * ar

