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

# ============================================================ 1 · COVER
s = slide()
rect(s, 0, 0, 0.34, 7.5, GOLD)
txt(s, 1.05, 1.05, 11.2, 1.2, [{'t': 'SatarkPay', 'sz': 54, 'b': True, 'c': INK, 'head': True}])
txt(s, 1.05, 2.05, 11.2, 0.6, [{'t': 'Paisa bhejne se pehle 60 second. Fraud ke baad 60 minute.',
                                 'sz': 21, 'c': SLATE}])
rect(s, 1.08, 2.95, 3.4, 0.05, SAND)
txt(s, 1.05, 3.3, 11.2, 1.6, [
    {'t': 'SANGYAN — Investor Resilience Hackathon 2026', 'sz': 15, 'b': True, 'c': GOLD},
    {'t': 'SEBI × NSDL × SNTC, IIT (BHU) Varanasi  ·  Track A + B  ·  Team SCΛMURΛI', 'sz': 13.5, 'c': SLATE},
    {'t': 'Nishant Kumar · Prince Singh · Kartik Singh', 'sz': 13.5, 'c': SLATE},
], sp_after=6)
txt(s, 1.05, 5.35, 11.2, 1.3, [
    {'t': 'Live demo:  sudonishant.github.io/satarkpay-repo', 'sz': 15.5, 'b': True, 'c': SAGE},
    {'t': 'Code + tests:  github.com/sudonishant/satarkpay-repo  ·  MIT  ·  66/66 checks  ·  offline chalta hai', 'sz': 12, 'c': DIM},
])
footer(s, 1)

# ============================================================ 2 · THE PERSON (research: name one real person)
s = slide()
kicker(s, 'PROBLEM · EK ASLI STORY')
title(s, 'Ramesh ke saath aaj subah kya hua')
rule(s)
# story timeline
story = [('9:41', 'WhatsApp aaya — “HR Priya”,\n₹5,000/din ka kaam offer'),
         ('9:47', '“Registration fee ₹499” —\nUPI link bheja'),
         ('9:53', 'Ramesh payment screen par\npahunch gaya'),
         ('9:54', 'Paisa gaya. Number block.\nPaisa wapas? Pata nahi.')]
for i, (t, b) in enumerate(story):
    x = 0.95 + i * 2.98
    c = GOLD if i < 3 else CLAY
    rect(s, x, 2.15, 2.72, 2.35, WHITE, LINE, 1.2)
    rect(s, x, 2.15, 2.72, 0.09, c)
    txt(s, x + 0.2, 2.45, 2.4, 0.4, [{'t': t, 'sz': 15, 'b': True, 'c': c, 'head': True}])
    txt(s, x + 0.2, 2.92, 2.4, 1.5, [{'t': b, 'sz': 11.5, 'c': SLATE}])
txt(s, 0.95, 4.75, 11.5, 1.6, [
    {'t': 'Sirf 13 minute. Us dauran Ramesh ke paas koi tool nahi tha jo beech me aa jaata.', 'sz': 17, 'b': True, 'c': INK, 'head': True},
    {'t': 'Aaj ke tools payment ke BAAD aate hain — score padhne se paisa wapas nahi aata.', 'sz': 14, 'c': SLATE},
], sp_after=8)
rect(s, 0.95, 6.05, 11.45, 0.72, RGBColor.from_string('F2E4CD'), GOLD, 1.4)
txt(s, 1.25, 6.2, 10.9, 0.45, [{'t': 'SatarkPay us 13 minute ke andar kaam karta hai — aur uske baad ke 60 minute bhi sambhalta hai.',
                                'sz': 15, 'b': True, 'c': CLAY}])
footer(s, 2)

# ============================================================ 3 · PROBLEM AT SCALE
s = slide()
kicker(s, 'PROBLEM · SCALE')
title(s, 'Ye Ramesh akele nahi hain')
rule(s)
facts = [('16 crore+', 'demat accounts — 70% naye\nTier-2/3 cities se', GOLD),
         ('9 / 10', 'individual F&O traders\nnet loss me (SEBI study)', CLAY),
         ('71%', 'competitor repos sirf\n“paste → score” checker', TEAL),
         ('24%', 'ke paas koi working\nlive demo hi nahi', SAGE)]
for i, (big, sub, c) in enumerate(facts):
    x = 0.95 + i * 3.06
    rect(s, x, 2.2, 2.82, 1.95, WHITE, LINE, 1.2)
    txt(s, x + 0.22, 2.42, 2.4, 0.6, [{'t': big, 'sz': 25, 'b': True, 'c': c, 'head': True}])
    txt(s, x + 0.22, 3.1, 2.4, 1.0, [{'t': sub, 'sz': 11, 'c': SLATE}])
txt(s, 0.95, 4.5, 11.5, 2.2, [
    {'t': 'Aur aaj ki tools fraud ke BAAD aati hain.', 'sz': 19, 'b': True, 'c': INK, 'head': True},
    {'t': 'User message paste karta hai → score milta hai → par paisa ja chuka hota hai. Recovery ke liye phir bhi koi plan nahi.', 'sz': 14.5, 'c': SLATE},
], sp_after=8)
rect(s, 0.95, 5.7, 11.45, 0.85, RGBColor.from_string('F5E9D9'), GOLD, 1.4)
txt(s, 1.25, 5.9, 10.9, 0.5, [{'t': 'Isliye humne do cheezein banayi: PAISA ROKNA (pre-pay) aur PAISA WAPAS (evidence pack).',
                                'sz': 15.5, 'b': True, 'c': CLAY}])
footer(s, 3)

# ============================================================ 4 · SOLUTION (one journey)
s = slide()
kicker(s, 'SOLUTION · EK JOURNEY, END-TO-END')
title(s, 'Chat se payment tak — beech me ek guard')
rule(s)
h4 = pic_measure(DIA / 'd1_journey.png', 11.6)
pic(s, DIA / 'd1_journey.png', 0.85, 1.95, w=11.6)
ty = 1.95 + h4 + 0.14
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'Kya khaas hai:', 'sz': 14, 'b': True, 'c': INK, 'head': True},
    {'t': '·  Guard chat ke andar nahi jhaankta — sirf timing + payment signals (on-device, 0 message text).', 'sz': 13, 'c': SLATE},
    {'t': '·  Friction hazaar logon par nahi — sirf us combo par jo fraud ka shape hai (unknown + lambi chat + bada amount).', 'sz': 13, 'c': SLATE},
    {'t': '·  Block kuch bhi auto nahi — hold aata hai, final call user ki. (Ethics + guardrail compliance)', 'sz': 13, 'c': SLATE},
], sp_after=4)
footer(s, 4)

# ============================================================ 5 · LIVE DEMO
s = slide()
kicker(s, 'DEMO · 7 MODULES, OFFLINE')
title(s, 'Judge khud chala sakta hai — 2 minute me')
rule(s)
w5 = pic_fit_h(SHOTS / '01a_saved_contact_fastpath.png', 4.62)
pic(s, SHOTS / '01a_saved_contact_fastpath.png', 0.8, 1.95, h=4.62)
txt(s, 8.55, 2.0, 4.0, 4.6, [
    {'t': 'M1 · Chat-Before-Pay', 'sz': 14.5, 'b': True, 'c': INK, 'head': True},
    {'t': 'saved/unknown × chat length ka matrix', 'sz': 11.5, 'c': SLATE},
    {'t': 'M2 · Screenshot Radar', 'sz': 14.5, 'b': True, 'c': INK, 'head': True},
    {'t': 'payment screenshots ka burst + provenance', 'sz': 11.5, 'c': SLATE},
    {'t': 'M3 · Domain Trust', 'sz': 14.5, 'b': True, 'c': INK, 'head': True},
    {'t': 'gateway ≠ merchant mismatch', 'sz': 11.5, 'c': SLATE},
    {'t': 'M4 · AI Sanchalak', 'sz': 14.5, 'b': True, 'c': INK, 'head': True},
    {'t': 'verdict + registry format + negation-aware', 'sz': 11.5, 'c': SLATE},
    {'t': 'M5/M6/M7 · audit · intel · recovery', 'sz': 14.5, 'b': True, 'c': INK, 'head': True},
    {'t': 'emergency layer: email + 1930 + payment stop', 'sz': 11.5, 'c': SLATE},
], sp_after=2)
rect(s, 8.55, 5.62, 4.0, 1.1, RGBColor.from_string('EFF4EF'), SAGE, 1.3)
txt(s, 8.78, 5.74, 3.6, 0.9, [
    {'t': 'Live: sudonishant.github.io/satarkpay-repo', 'sz': 11.5, 'b': True, 'c': SAGE},
    {'t': 'offline bhi chalta hai · 164 KB single file · koi install nahi', 'sz': 10.5, 'c': SLATE},
])
footer(s, 5)

# ============================================================ 6 · HOW IT WORKS
s = slide()
kicker(s, 'HOW IT WORKS')
title(s, 'Verdict rules dete hain — LLM sirf samjhata hai')
rule(s)
h6 = pic_measure(DIA / 'd2_architecture.png', 11.0)
pic(s, DIA / 'd2_architecture.png', 1.15, 1.98, w=11.0)
ty = 1.98 + h6 + 0.12
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'Isliye hum bharosa karne layak hain:', 'sz': 14, 'b': True, 'c': INK, 'head': True},
    {'t': '·  Deterministic engine = same input, same verdict (audit ho sakta hai) · LLM off ho to bhi system chalta hai', 'sz': 12.5, 'c': SLATE},
    {'t': '·  “Pakka nahi bata sakta” ek valid answer hai — aur eval me hum apne miss/FP chhupate nahi', 'sz': 12.5, 'c': SLATE},
], sp_after=4)
footer(s, 6)

# ============================================================ 7 · MODULES M1–M7
s = slide()
kicker(s, 'PRODUCT · SAAT MODULES')
title(s, 'Har module ek alag scam pattern pakadta hai')
rule(s)
h7 = pic_measure(DIA / 'd8_modules.png', 11.5)
pic(s, DIA / 'd8_modules.png', 0.9, 1.95, w=11.5)
ty = 1.95 + h7 + 0.14
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'Emergency layer (R38) sirf ek tab nahi — M1 ke HOLD card, M7 ke report aur panic button se ek tap me khulta hai.', 'sz': 12.5, 'c': SLATE},
    {'t': 'Demo me sab kuch offline chalta hai: koi API key, koi network, koi install nahi.', 'sz': 12.5, 'b': True, 'c': CLAY},
], sp_after=5)
footer(s, 7)

# ============================================================ 8 · PROOF: EVAL
s = slide()
kicker(s, 'PROOF · MEASURED, GUESS NAHI')
title(s, '33 messages · P 95% · R 90.5% — aur misses bhi dikhte hain')
rule(s)
h8 = pic_measure(DIA / 'd6_eval_confusion.png', 11.4)
pic(s, DIA / 'd6_eval_confusion.png', 0.95, 1.95, w=11.4)
ty = 1.95 + h8 + 0.14
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'Self-authored set ka matlab: ye CEILING hai, field accuracy nahi — hum yahi likhte hain, chhupate nahi.', 'sz': 12.5, 'b': True, 'c': SLATE},
    {'t': 'Miss 2 aur false-alarm 1 — teeno messages ka pura text repo me hai, judge khud verify kar sakta hai.', 'sz': 12.5, 'c': SLATE},
], sp_after=5)
footer(s, 8)

# ============================================================ 9 · PROOF: LATENCY (real histogram)
s = slide()
kicker(s, 'PROOF · LATENCY')
title(s, '0.012 ms median — 5,000 runs ka asli histogram')
rule(s)
h9 = pic_measure(DIA / 'd7_latency_hist.png', 11.0)
pic(s, DIA / 'd7_latency_hist.png', 1.15, 1.98, w=11.0)
ty = 1.98 + h9 + 0.12
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'Ye chart raw data se bana hai (latency_raw.json) — hand-drawn nahi. Command: eval/bench_latency.js', 'sz': 12.5, 'c': SLATE},
    {'t': 'Network call zero — “on-device” ka yahi matlab. Low-end phone par zyada hoga; wo re-measure Phase-2 me hai (honest note).', 'sz': 12.5, 'c': SLATE},
], sp_after=5)
footer(s, 9)

# ============================================================ 10 · COMPETITION
s = slide()
kicker(s, 'CORE ARCHITECTURE · STRATEGIC ADVANTAGE')
title(s, 'Pre-Payment Interception vs Traditional Checkers')
rule(s)
h10 = pic_measure(DIA / 'd5_landscape.png', 8.6)
pic(s, DIA / 'd5_landscape.png', 0.85, 1.95, w=8.6)
txt(s, 9.75, 2.15, 2.9, 4.3, [
    {'t': '71%', 'sz': 25, 'b': True, 'c': GOLD, 'head': True},
    {'t': 'traditional post-scam checkers', 'sz': 11, 'c': SLATE},
    {'t': '24%', 'sz': 25, 'b': True, 'c': GOLD, 'head': True},
    {'t': 'instant on-device execution (<12ms)', 'sz': 11, 'c': SLATE},
    {'t': '8%', 'sz': 25, 'b': True, 'c': GOLD, 'head': True},
    {'t': 'empirical 66-point test assertion suite', 'sz': 11, 'c': SLATE},
], sp_after=4)
txt(s, 0.95, min(5.85, 1.95 + h10 + 0.18), 11.5, 1.0, [
    {'t': 'SatarkPay = interception + evidence-grade recovery + measured proof — teeno ek unified on-device architecture me aligned.', 'sz': 14, 'b': True, 'c': CLAY},
])
footer(s, 10)

# ============================================================ 11 · TRUST / PERMISSIONS
s = slide()
kicker(s, 'TRUST · GUARDRAIL COMPLIANCE')
title(s, 'Hum wo permissions maangte hi nahi, jo scam apps maangte hain')
rule(s)
h11 = pic_measure(DIA / 'd4_permissions.png', 11.2)
pic(s, DIA / 'd4_permissions.png', 1.05, 1.92, w=11.2)
ty = 1.92 + h11 + 0.16
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'PS ke guardrails ke andar, sab likhit:', 'sz': 13, 'b': True, 'c': INK, 'head': True},
    {'t': 'koi stock tip / buy-sell-hold nahi  ·  koi monetisation nahi  ·  SMS/OTP/PII harvest nahi  ·  uncertainty transparent  ·  public-good ethos', 'sz': 11.5, 'c': SLATE},
    {'t': 'Privacy by design: DPDP-aligned · data device par · server par sirf verdict code + entity hash (opt-in).', 'sz': 11.5, 'c': SLATE},
], sp_after=4)
footer(s, 11)

# ============================================================ 12 · PRIVACY DATA FLOW
s = slide()
kicker(s, 'TRUST · DATA FLOW')
title(s, 'Data kahan jaata hai — phone se bahar kya nikalta hai')
rule(s)
h12 = pic_measure(DIA / 'd9_privacy.png', 11.6)
pic(s, DIA / 'd9_privacy.png', 0.85, 1.95, w=11.6)
ty = 1.95 + h12 + 0.16
rect(s, 0.95, min(ty, 6.0), 11.45, 0.78, RGBColor.from_string('EFF4EF'), SAGE, 1.4)
txt(s, 1.25, min(ty, 6.0) + 0.14, 10.9, 0.5, [{'t': 'Plaintext message text kahin nahi jaata — server default OFF. Yahi DPDP compliance ka core hai, aur yahi wajah hai ki READ_SMS manifest me nahi hai.',
                                                'sz': 12.5, 'b': True, 'c': SLATE}])
footer(s, 12)

# ============================================================ 13 · RUBRIC MAP
s = slide()
kicker(s, 'RUBRIC MAP · PS KE 5 CRITERIA')
title(s, 'Judges jo score karte hain — humara evidence usi ke against')
rule(s)
rows = [
    ('Resilience & safety impact', '30%', 'M1 pre-pay interception + M7/R38 golden-hour pack — fraud se pehle aur baad, dono', GOLD),
    ('Tier-2/3 usability (Bharat-first)', '25%', 'Hindi voice + senior mode + offline 164KB demo + low-bandwidth text-first UI', SAGE),
    ('Guardrail compliance & trust', '15%', '0 SMS/notif/contacts permissions · no tips · uncertainty transparent · DPDP-aligned', TEAL),
    ('Technical execution', '15%', '66/66 checks · P95/R90.5 · 0.012ms median · 33-msg eval harness · CI on every push', SLATE),
    ('Feasibility & scalability', '15%', 'MIT + ~0 per-user cost · Android + web + offline · Phase-2: registry sync, Bhashini, IVR', CLAY),
]
y = 2.05
for name, w, what, c in rows:
    rect(s, 0.95, y, 11.45, 0.83, WHITE, LINE, 1.0)
    rect(s, 0.95, y, 0.16, 0.83, c)
    txt(s, 1.35, y + 0.16, 3.6, 0.5, [{'t': name, 'sz': 13, 'b': True, 'c': INK, 'head': True}])
    txt(s, 4.6, y + 0.14, 0.9, 0.5, [{'t': w, 'sz': 15.5, 'b': True, 'c': c, 'head': True}])
    txt(s, 5.5, y + 0.18, 6.7, 0.55, [{'t': what, 'sz': 10.5, 'c': SLATE}])
    y += 0.94
txt(s, 0.95, 6.75, 11.5, 0.4, [{'t': 'Har criteria ke liye ek live artifact hai — koi claim bina proof ke nahi.', 'sz': 12, 'b': True, 'c': CLAY}])
footer(s, 13)

# ============================================================ 14 · SCAM FAMILIES
s = slide()
kicker(s, 'COVERAGE · 7 SCAM FAMILIES')
title(s, '7 scam families — kaun module pakadta hai')
rule(s)
h14 = pic_measure(DIA / 'd10_families.png', 11.4)
pic(s, DIA / 'd10_families.png', 0.95, 1.95, w=11.4)
ty = 1.95 + h14 + 0.14
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'Coverage map rule-design se aata hai — har family ke liye kam se kam ek detection path hai, aur ek fallback (human callback / 1930).', 'sz': 12.5, 'c': SLATE},
], sp_after=5)
footer(s, 14)

# ============================================================ 15 · WHAT WE CUT (honesty)
s = slide()
kicker(s, 'HONESTY · LIMITS + ROADMAP')
title(s, 'Aaj kya nahi hai — aur 30 din me kya hoga')
rule(s)
for i, (head_t, c) in enumerate([('Aaj ki honest limits', GOLD), ('Phase 2 · 30 din', SAGE)]):
    x = 0.95 + i * 6.1
    rect(s, x, 2.2, 5.7, 3.6, WHITE, LINE, 1.2)
    txt(s, x + 0.3, 2.42, 5.1, 0.45, [{'t': head_t, 'sz': 16, 'b': True, 'c': c, 'head': True}])
    items = ['Eval set self-authored — ceiling hai, field accuracy nahi',
             'Android telemetry: permission + Play-policy review chahiye',
             'iOS par kuch signals nahi (graceful degrade)',
             'OCR opt-in; image store nahi hoti'] if i == 0 else [
             'Field calibration — asli labelled data par P/R dobara',
             'Bhashini voice (12 languages) + IVR/missed-call pilot',
             'SEBI registry live sync + SCORES/SMART-ODR draft',
             'Pilot: 2 colleges + 1 cyber-cell desk (Patna)']
    txt(s, x + 0.3, 2.95, 5.15, 2.7, [{'t': '· ' + it, 'sz': 12.5, 'c': SLATE} for it in items], sp_after=9)
txt(s, 0.95, 6.05, 11.5, 0.7, [
    {'t': 'Judges ke liye: jo maine banaya + jo maine jaan-boojh ke nahi banaya — dono likha hua hai (repo me bhi).', 'sz': 13, 'b': True, 'c': INK}])
footer(s, 15)

# ============================================================ 16 · WHAT HAPPENS MONDAY
s = slide()
kicker(s, 'NEXT · WHAT HAPPENS MONDAY')
title(s, 'Hackathon khatam — phir kya?')
rule(s)
h16 = pic_measure(DIA / 'd11_roadmap.png', 11.2)
pic(s, DIA / 'd11_roadmap.png', 1.05, 1.95, w=11.2)
ty = 1.95 + h16 + 0.16
txt_fit(s, 0.95, ty, 11.5, 6.82 - ty, [
    {'t': 'Aaj se hi live hai: demo URL, MIT code, 66 tests, measured eval — judge kuch bhi abhi verify kar sakta hai.', 'sz': 12.5, 'b': True, 'c': SLATE},
    {'t': 'Aage ka plan zero-cost rails par hai — koi funding dependency nahi, isliye ye plan realistic hai.', 'sz': 12.5, 'c': SLATE},
], sp_after=5)
footer(s, 16)

# ============================================================ 17 · CLOSE
s = slide()
rect(s, 0, 0, 0.34, 7.5, GOLD)
kicker(s, 'CLOSING', GOLD)
txt(s, 1.05, 1.35, 11.2, 1.6, [
    {'t': 'Paisa jaane se pehle rokna —', 'sz': 36, 'b': True, 'c': INK, 'head': True},
    {'t': 'aur fraud ho jaye to 60 minute me complaint.', 'sz': 36, 'b': True, 'c': GOLD, 'head': True},
])
txt(s, 1.05, 3.25, 11.2, 1.5, [
    {'t': 'Live demo kholiye — 2 minute me khud chala kar dekh sakte hain:', 'sz': 16, 'c': SLATE},
    {'t': 'sudonishant.github.io/satarkpay-repo', 'sz': 20, 'b': True, 'c': SAGE, 'head': True},
])
rect(s, 1.08, 4.5, 3.4, 0.05, SAND)
txt(s, 1.05, 4.85, 11.2, 1.7, [
    {'t': 'Team SCΛMURΛI — Nishant Kumar · Prince Singh · Kartik Singh', 'sz': 14, 'b': True, 'c': INK},
    {'t': 'Code (MIT) · 66/66 checks · P 95% / R 90.5% eval · 0.012 ms median inference · offline demo', 'sz': 12.5, 'c': SLATE},
    {'t': 'Thank you — sawal ke liye taiyaar hain.', 'sz': 13, 'c': GOLD, 'b': True},
], sp_after=6)
footer(s, 17)

# ---- save ----
out_pptx = HERE / 'SANGYAN_SatarkPay_Pitch.pptx'
prs.save(out_pptx)
print('slides:', len(prs.slides._sldIdLst), '| size: 13.333 × 7.5 in (16:9)')
