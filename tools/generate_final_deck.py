#!/usr/bin/env python3
"""
SatarkPay — SANGYAN 2026 hackathon deck (10 slides, 16:9, premium light).
Run:  python3 tools/generate_final_deck.py
Out:  SatarkPay_SANGYAN_Final.pptx
"""
import pathlib
from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

ROOT = pathlib.Path(__file__).resolve().parent.parent
DECK = ROOT / "web" / "deck"
SCAMS = DECK / "scam_shots"
SHOTS = DECK / "screenshots"
WEB = DECK / "screenshots_webapp"
SHIELD = pathlib.Path("/tmp/shield.png")

W, H = 13.333, 7.5

# ---- premium light palette (matches webapp v6) -----------------------------
INK    = RGBColor(0x0C, 0x14, 0x24)
INK2   = RGBColor(0x33, 0x40, 0x5A)
MUTED  = RGBColor(0x64, 0x70, 0x8A)
FAINT  = RGBColor(0x97, 0xA2, 0xB6)
BLUE   = RGBColor(0x2F, 0x6B, 0xF6)
BLUE_D = RGBColor(0x1C, 0x4F, 0xD6)
VIOLET = RGBColor(0x7A, 0x5A, 0xF8)
TEAL   = RGBColor(0x0F, 0xB5, 0xA5)
GREEN  = RGBColor(0x0D, 0x9F, 0x6D)
AMBER  = RGBColor(0xB4, 0x53, 0x09)
RED    = RGBColor(0xE0, 0x2F, 0x2F)
WHITE  = RGBColor(0xFF, 0xFF, 0xFF)
LINE   = RGBColor(0xE4, 0xE9, 0xF2)
SOFT   = RGBColor(0xF4, 0xF7, 0xFC)
BLUE_S = RGBColor(0xEA, 0xF1, 0xFF)
RED_S  = RGBColor(0xFD, 0xEC, 0xEC)
AMB_S  = RGBColor(0xFD, 0xF3, 0xE5)
GRN_S  = RGBColor(0xE6, 0xF8, 0xF1)
VIO_S  = RGBColor(0xF0, 0xEC, 0xFF)

prs = Presentation()
prs.slide_width, prs.slide_height = Inches(W), Inches(H)
BLANK = prs.slide_layouts[6]


def slide():
    s = prs.slides.add_slide(BLANK)
    bg = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, Inches(W), Inches(H))
    bg.fill.solid(); bg.fill.fore_color.rgb = WHITE
    bg.line.fill.background()
    bg.shadow.inherit = False
    # soft washes
    for (x, y, w, h, col, transp) in [
        (-1.6, -1.8, 6.2, 4.6, RGBColor(0xE9, 0xF0, 0xFF), 55),
        (9.6, -1.2, 5.6, 4.2, RGBColor(0xF0, 0xEC, 0xFF), 60),
        (4.2, 5.9, 5.2, 2.6, RGBColor(0xE6, 0xF8, 0xF1), 70),
    ]:
        ov = s.shapes.add_shape(MSO_SHAPE.OVAL, Inches(x), Inches(y), Inches(w), Inches(h))
        ov.fill.solid(); ov.fill.fore_color.rgb = col
        try:
            ov.fill.transparency = transp
        except Exception:
            pass
        ov.line.fill.background(); ov.shadow.inherit = False
    return s


def tb(s, x, y, w, h, anchor=MSO_ANCHOR.TOP):
    box = s.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = anchor
    tf.margin_left = tf.margin_right = tf.margin_top = tf.margin_bottom = 0
    return tf


def para(tf, text, size, color, bold=False, first=False, space_after=4,
         align=PP_ALIGN.LEFT, italic=False, font="Arial"):
    p = tf.paragraphs[0] if first else tf.add_paragraph()
    p.text = text
    p.alignment = align
    p.space_after = Pt(space_after)
    for r in p.runs:
        r.font.size = Pt(size); r.font.bold = bold; r.font.italic = italic
        r.font.color.rgb = color; r.font.name = font
    return p


def header(s, kicker, title, tag=None):
    if kicker:
        t = tb(s, 0.75, 0.42, 8.6, 0.34)
        para(t, kicker.upper(), 10.5, BLUE_D, bold=True, first=True)
    t = tb(s, 0.75, 0.74, 10.2, 0.85)
    para(t, title, 25, INK, bold=True, first=True)
    bar = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.75), Inches(1.56), Inches(0.62), Inches(0.055))
    bar.fill.solid(); bar.fill.fore_color.rgb = BLUE; bar.line.fill.background(); bar.shadow.inherit = False
    if tag:
        b = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(10.55), Inches(0.52), Inches(2.05), Inches(0.44))
        b.fill.solid(); b.fill.fore_color.rgb = BLUE_S; b.line.color.rgb = RGBColor(0xCF, 0xE0, 0xFF)
        b.shadow.inherit = False
        tf = b.text_frame; tf.vertical_anchor = MSO_ANCHOR.MIDDLE
        para(tf, tag, 10, BLUE_D, bold=True, first=True, align=PP_ALIGN.CENTER, space_after=0)


def footer(s, n, label="SatarkPay · सतर्कपे · SANGYAN 2026"):
    t = tb(s, 0.75, 7.06, 8.0, 0.3)
    para(t, label, 8.5, FAINT, first=True, space_after=0)
    t2 = tb(s, 12.0, 7.06, 0.6, 0.3)
    para(t2, f"{n:02d}", 8.5, FAINT, bold=True, first=True, align=PP_ALIGN.RIGHT, space_after=0)


def card(s, x, y, w, h, fill=WHITE, border=LINE, radius=True):
    shp = s.shapes.add_shape(
        MSO_SHAPE.ROUNDED_RECTANGLE if radius else MSO_SHAPE.RECTANGLE,
        Inches(x), Inches(y), Inches(w), Inches(h))
    shp.fill.solid(); shp.fill.fore_color.rgb = fill
    shp.line.color.rgb = border; shp.line.width = Pt(1)
    shp.shadow.inherit = False
    return shp


def chip(s, x, y, w, h, text, fill, color):
    c = card(s, x, y, w, h, fill=fill, border=fill)
    tf = c.text_frame; tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    para(tf, text, 9.5, color, bold=True, first=True, align=PP_ALIGN.CENTER, space_after=0)


def pic(s, path, x, y, w=None, h=None):
    kw = {}
    if w: kw["width"] = Inches(w)
    if h: kw["height"] = Inches(h)
    return s.shapes.add_picture(str(path), Inches(x), Inches(y), **kw)


def bullets(tf, items, size=11.5, color=INK2, bold_lead=True, space=5):
    first = True
    for it in items:
        if isinstance(it, tuple):
            lead, rest = it
            p = tf.paragraphs[0] if first else tf.add_paragraph()
            first = False
            p.space_after = Pt(space)
            r = p.add_run(); r.text = "•  " + lead
            r.font.size = Pt(size); r.font.bold = bold_lead; r.font.color.rgb = INK; r.font.name = "Arial"
            r2 = p.add_run(); r2.text = rest
            r2.font.size = Pt(size); r2.font.color.rgb = color; r2.font.name = "Arial"
        else:
            para(tf, "•  " + it, size, color, first=first, space_after=space)
            first = False


# ============================================================ 1 · TITLE
s = slide()
card(s, 0, 0, W, H, fill=WHITE, border=WHITE, radius=False)
for (x, y, w, h, col) in [(-1.4, -1.6, 6.4, 4.8, BLUE_S), (9.4, -1.0, 5.8, 4.4, VIO_S),
                          (3.6, 5.6, 6.2, 3.0, RGBColor(0xF2, 0xF7, 0xFF))]:
    ov = s.shapes.add_shape(MSO_SHAPE.OVAL, Inches(x), Inches(y), Inches(w), Inches(h))
    ov.fill.solid(); ov.fill.fore_color.rgb = col; ov.line.fill.background(); ov.shadow.inherit = False

if SHIELD.exists():
    pic(s, SHIELD, 9.15, 1.95, w=2.9)

chip(s, 0.85, 0.95, 3.45, 0.42, "SANGYAN 2026  ·  IIT-BHU  ·  SEBI × NSDL", BLUE_S, BLUE_D)
t = tb(s, 0.85, 1.62, 8.2, 1.5)
para(t, "SatarkPay", 54, INK, bold=True, first=True, space_after=0)
para(t, "सतर्कपे", 26, BLUE_D, bold=True, space_after=0)
t = tb(s, 0.85, 3.32, 7.9, 1.15)
para(t, "Fraud detection that runs in the sixty seconds BEFORE the PIN —", 17, INK2, first=True, space_after=2)
para(t, "not after the money has gone.", 17, INK2, space_after=0)
bar = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.85), Inches(4.62), Inches(0.8), Inches(0.06))
bar.fill.solid(); bar.fill.fore_color.rgb = VIOLET; bar.line.fill.background(); bar.shadow.inherit = False
t = tb(s, 0.85, 4.88, 8.2, 1.2)
para(t, "Nine attack-chain signals · Four honest tiers · Zero bytes uploaded", 12.5, MUTED, bold=True, first=True, space_after=6)
para(t, "Team SCΛMURΛI — Nishant Kumar · Prince Singh · Kartik Singh", 11.5, INK2, space_after=2)
para(t, "Track A: Fraud Resilience   ·   Track B: Awareness & Grievance   ·   Track D: Behavioral Security", 10, FAINT, space_after=0)
footer(s, 1)

# ============================================================ 2 · PROBLEM
s = slide()
header(s, "The problem", "UPI fraud is not a hack. It is a conversation.", "01 · PROBLEM")
t = tb(s, 0.75, 1.78, 7.2, 0.75)
para(t, "Nobody breaks UPI encryption. They talk to you — a call, a deadline, a fake officer — "
        "and the money moves in one rushed moment, with one six-digit PIN.", 12.5, MUTED, first=True)

rows = [
    ("60 seconds", "before the PIN is where every real defence must live", BLUE),
    ("1930 / NCRP", "react after the loss — refunds are never guaranteed", AMBER),
    ("Blacklists", "are stale the day a new scam domain is registered", VIOLET),
]
x = 0.75
for (big, small, col) in rows:
    c = card(s, x, 2.75, 2.32, 1.62)
    t = tb(s, x + 0.18, 2.92, 2.0, 1.3)
    para(t, big, 16.5, col, bold=True, first=True, space_after=3)
    para(t, small, 9.5, MUTED, space_after=0)
    x += 2.5

t = tb(s, 0.75, 4.75, 7.2, 1.9)
para(t, "WHAT THE ATTACK LOOKS LIKE", 9.5, FAINT, bold=True, first=True, space_after=6)
bullets(t, [
    ("Urgency + isolation — ", "“stay on the call, do not tell anyone”"),
    ("Authority costume — ", "fake CBI / bank / courier / SEBI officer"),
    ("Wrong direction of money — ", "scan a QR to “receive”, pay a “fee” to get a loan/job"),
    ("Converging signals — ", "any one is survivable; two or three together is an attack"),
], size=11)

pic(s, SCAMS / "01_digital_arrest.png", 8.38, 1.95, h=4.2)
c = card(s, 8.28, 6.28, 2.42, 0.52, fill=RED_S, border=RED_S)
t = tb(s, 8.4, 6.38, 2.22, 0.34)
para(t, "Fake “digital arrest”", 9.5, RED, bold=True, first=True, space_after=0)
pic(s, SCAMS / "03_qr_refund.png", 10.9, 1.95, h=4.2)
c = card(s, 10.8, 6.28, 2.42, 0.52, fill=AMB_S, border=AMB_S)
t = tb(s, 10.92, 6.38, 2.22, 0.34)
para(t, "“Scan to receive refund”", 9.5, AMBER, bold=True, first=True, space_after=0)
footer(s, 2)

# ============================================================ 3 · SCAM PATTERNS
s = slide()
header(s, "Documented patterns", "Ten scams, four seen in the wild", "02 · LIBRARY")
t = tb(s, 0.75, 1.72, 11.8, 0.4)
para(t, "Each pattern in the library names the physical fact that makes the ask impossible — "
        "that fact is what the engine encodes.", 12, MUTED, first=True)

shots = [
    ("01_digital_arrest.png", "Digital arrest", "No agency arrests over video call or collects a “security deposit”.", RED_S, RED),
    ("02_kyc_apk.png", "KYC / fake APK", "The installed APK reads the OTP — the message is only bait.", AMB_S, AMBER),
    ("03_qr_refund.png", "Refund QR", "A QR scan can only debit money. Never credit.", AMB_S, AMBER),
    ("04_fake_job.png", "Fake job / loan", "Salary and loans are never unlocked by a registration fee.", VIO_S, VIOLET),
]
x = 0.75
for (f, name, desc, fill, col) in shots:
    pic(s, SCAMS / f, x, 2.18, h=3.42)
    c = card(s, x - 0.02, 5.72, 1.72, 1.12, fill=fill, border=fill)
    t = tb(s, x + 0.1, 5.82, 1.5, 0.95)
    para(t, name, 10, col, bold=True, first=True, space_after=2)
    para(t, desc, 8, INK2, space_after=0)
    x += 2.28

c = card(s, 10.05, 2.18, 2.55, 4.66)
t = tb(s, 10.25, 2.4, 2.15, 4.3)
para(t, "AND SIX MORE", 9.5, FAINT, bold=True, first=True, space_after=7)
bullets(t, [
    "Fake bank / KYC call",
    "UPI collect-request scam",
    "OTP / PIN / CVV theft",
    "Fake customer-care numbers",
    "Remote-access apps (AnyDesk)",
    "SIM-swap & “wrong refund”",
], size=10.5, bold_lead=False, space=7)
para(t, "All ten ship in Hindi too —", 9.5, MUTED, space_after=2)
para(t, "data/scam_library_hi.json", 9, BLUE_D, space_after=0)
footer(s, 3)

# ============================================================ 4 · SOLUTION
s = slide()
header(s, "The solution", "Nine signals, one honest arithmetic", "03 · HOW IT WORKS")
t = tb(s, 0.75, 1.72, 11.8, 0.4)
para(t, "Every weight is a whole number, added in the open. The arithmetic IS the explanation — "
        "which is the only way a warning is ever believed.", 12, MUTED, first=True)

sig = [
    ("On a call while paying", "+4"), ("“Scan to receive money”", "+4"),
    ("Screen sharing active", "+3"), ("First time paying this payee", "+3"),
    ("Amount ≫ your usual", "+3"), ("Instructions over chat", "+2"),
    ("Long build-up before the ask", "+2"), ("Handle is not a real PSP code", "+2"),
    ("Message matches known script", "+5"),
]
c = card(s, 0.75, 2.28, 6.55, 4.5)
t = tb(s, 1.0, 2.48, 6.1, 4.1)
para(t, "THE ATTACK CHAIN (EXAMPLE)", 9.5, FAINT, bold=True, first=True, space_after=7)
for (lab, w) in sig:
    p = t.add_paragraph(); p.space_after = Pt(5)
    r = p.add_run(); r.text = lab + "  ····  "
    r.font.size = Pt(11); r.font.color.rgb = INK2; r.font.name = "Arial"
    r2 = p.add_run(); r2.text = w
    r2.font.size = Pt(11); r2.font.bold = True; r2.font.color.rgb = RED; r2.font.name = "Arial"
p = t.add_paragraph(); p.space_after = Pt(0)
r = p.add_run(); r.text = "score 23  →  CRITICAL  →  pay action withheld"
r.font.size = Pt(12); r.font.bold = True; r.font.color.rgb = RED; r.font.name = "Arial"

tiers = [
    ("LOW · 0–2", "Pay when ready — still match the payee name yourself.", GRN_S, GREEN),
    ("CAUTION · 3–5", "30-second cooling-off. Long enough to think, not to feel punished.", BLUE_S, BLUE_D),
    ("HIGH · 6–9", "60-second cooling + a written alternative route per signal.", AMB_S, AMBER),
    ("CRITICAL · 10+", "Pay withheld. No countdown — it never “becomes available”.", RED_S, RED),
]
y = 2.28
for (name, desc, fill, col) in tiers:
    card(s, 7.65, y, 4.95, 1.02, fill=fill, border=fill)
    t = tb(s, 7.9, y + 0.13, 4.5, 0.82)
    para(t, name, 11.5, col, bold=True, first=True, space_after=2)
    para(t, desc, 9.5, INK2, space_after=0)
    y += 1.17
footer(s, 4)

# ============================================================ 5 · WEB APP 1
s = slide()
header(s, "Live product · Web", "SatarkPay web app — offline, no install", "04 · DEMO")
t = tb(s, 0.75, 1.74, 5.3, 3.2)
para(t, "WHAT IT RUNS", 9.5, FAINT, bold=True, first=True, space_after=7)
bullets(t, [
    ("Payment gate — ", "nine questions → score → tier → gate"),
    ("Message checker — ", "13 fraud patterns + 4 legitimacy markers"),
    ("Link ladder — ", "decomposition instead of blacklists"),
    ("Redaction first — ", "UPI IDs, cards, OTPs masked on-device"),
    ("Evidence pack — ", "structured, redacted, 1930-ready"),
    ("Emergency mode — ", "seven steps in the order that matters"),
    ("Hindi voice + senior mode — ", "for elders, in their language"),
], size=11)
c = card(s, 0.75, 5.35, 5.25, 1.35, fill=BLUE_S, border=BLUE_S)
t = tb(s, 1.0, 5.53, 4.8, 1.05)
para(t, "8/8 pages work with the network cut.", 11.5, BLUE_D, bold=True, first=True, space_after=3)
para(t, "No account, no server, no analytics. Verified by switching the browser offline and reloading every page.", 9.5, INK2, space_after=0)

pic(s, WEB / "w1_home.png", 6.45, 1.82, w=6.15)
c = card(s, 6.45, 6.18, 6.15, 0.62, fill=SOFT, border=SOFT)
t = tb(s, 6.68, 6.31, 5.75, 0.4)
para(t, "Dashboard · attack-chain · tiers · 8 offline pages", 10, INK2, bold=True, first=True, space_after=0)
footer(s, 5)

# ============================================================ 6 · WEB APP 2
s = slide()
header(s, "Live product · Web", "Check a message · library · evidence", "04 · DEMO")
pic(s, WEB / "w3_check.png", 0.75, 1.78, w=5.95)
pic(s, WEB / "w4_library.png", 6.95, 1.78, w=5.6)
c = card(s, 0.75, 6.12, 11.85, 0.72, fill=SOFT, border=SOFT)
t = tb(s, 1.0, 6.26, 11.4, 0.5)
para(t, "WhatsApp Scam Checker: paste any suspicious SMS / WhatsApp / job or loan offer → "
        "pattern match + legitimacy markers + “what not to do” + official complaint routes.", 10.5, INK2, bold=True, first=True, space_after=0)
footer(s, 6)

# ============================================================ 7 · ANDROID
s = slide()
header(s, "Live product · Android", "Native app — Kotlin + Jetpack Compose", "05 · DEMO")
t = tb(s, 0.75, 1.74, 5.15, 3.4)
para(t, "14 SCREENS · FULL STACK", 9.5, FAINT, bold=True, first=True, space_after=7)
bullets(t, [
    ("Chat-before-Pay gate — ", "the same engine, native"),
    ("Screenshot Radar — ", "WhatsApp burst detection + triage"),
    ("Sanchalak AI chat — ", "Hindi voice, audio notes (Gemini)"),
    ("Wallet AutoPay audit — ", "silent mandates, exposed"),
    ("Emergency + Grievance — ", "1930 pack, SCORES/ODR ladder"),
    ("App-security scanner — ", "remote-access app detector"),
    ("Room DB + offline rules — ", "core protects with zero network"),
], size=11)
c = card(s, 0.75, 5.5, 5.15, 1.2, fill=GRN_S, border=GRN_S)
t = tb(s, 1.0, 5.66, 4.7, 0.95)
para(t, "Premium light fintech theme —", 10.5, GREEN, bold=True, first=True, space_after=2)
para(t, "same design system as the web app. Senior mode, Hindi TTS, PII masking built in.", 9.5, INK2, space_after=0)

pic(s, SHOTS / "02_screenshot_radar.png", 6.25, 1.78, w=3.05)
pic(s, SHOTS / "04_ai_sanchalak.png", 9.5, 1.78, w=3.05)
pic(s, SHOTS / "07_auto_report_1930_pack.png", 6.25, 4.02, w=3.05)
pic(s, SHOTS / "10_senior_mode_voice.png", 9.5, 4.02, w=3.05)
footer(s, 7)

# ============================================================ 8 · EMERGENCY
s = slide()
header(s, "When money is already gone", "10-minute emergency mode + complaint packet", "06 · SUPPORT")
t = tb(s, 0.75, 1.76, 5.2, 0.4)
para(t, "IN THE ORDER THAT CHANGES THE OUTCOME", 9.5, FAINT, bold=True, first=True)
steps = [
    "Call 1930 immediately — golden hour for freezing funds",
    "Report on cybercrime.gov.in (Financial Fraud)",
    "Bank + UPI app: block / freeze cards, UPI, net-banking",
    "Preserve evidence: UTR, txn IDs, chats, screenshots",
    "Get a written complaint number from the bank",
    "Alert family — the “recovery agent” call is coming",
    "Never pay anyone promising recovery",
]
c = card(s, 0.75, 2.2, 5.2, 4.55)
t = tb(s, 1.0, 2.42, 4.75, 4.2)
first = True
for i, st in enumerate(steps, 1):
    p = t.paragraphs[0] if first else t.add_paragraph()
    first = False
    p.space_after = Pt(8)
    r = p.add_run(); r.text = f"{i}.  "
    r.font.size = Pt(11.5); r.font.bold = True; r.font.color.rgb = BLUE_D; r.font.name = "Arial"
    r2 = p.add_run(); r2.text = st
    r2.font.size = Pt(11); r2.font.color.rgb = INK2; r2.font.name = "Arial"

pic(s, WEB / "w6_emergency.png", 6.85, 1.78, w=5.75)
c = card(s, 6.3, 5.85, 6.3, 1.0, fill=SOFT, border=SOFT)
t = tb(s, 6.55, 5.97, 5.85, 0.82)
para(t, "Complaint Packet Generator + Hindi voice", 11, INK, bold=True, first=True, space_after=3)
para(t, "One structured redacted record for cybercrime.gov.in / the bank — plus voice guidance for seniors.", 9.5, INK2, space_after=0)
footer(s, 8)

# ============================================================ 9 · RESULTS
s = slide()
header(s, "Verified in this repository", "Small numbers, honestly labelled", "07 · RESULTS")
t = tb(s, 0.75, 1.72, 11.8, 0.4)
para(t, "Self-authored, small-sample, reproducible — the commands are in docs/VERIFICATION.md.", 12, MUTED, first=True)

metrics = [
    ("92.7 %", "Message classifier F1", "P 95.0 · R 90.5 on 33 labelled messages"),
    ("11.7 ms", "Engine latency p50", "p95 31.5 ms over 5,000 runs"),
    ("139 / 139", "Assertions passing", "logic + UI + offline in a real browser"),
    ("8 / 8", "Pages offline", "service-worker precache, verified cut-off"),
]
x = 0.75
for (big, mid, small) in metrics:
    card(s, x, 2.35, 2.85, 1.85)
    t = tb(s, x + 0.22, 2.58, 2.45, 1.5)
    para(t, big, 23, BLUE_D, bold=True, first=True, space_after=2)
    para(t, mid, 11, INK, bold=True, space_after=3)
    para(t, small, 9, MUTED, space_after=0)
    x += 3.05

c = card(s, 0.75, 4.55, 5.85, 2.15, fill=RED_S, border=RED_S)
t = tb(s, 1.0, 4.75, 5.4, 1.85)
para(t, "TWO REAL BUGS THE ASSERTIONS FOUND", 9.5, RED, bold=True, first=True, space_after=6)
bullets(t, [
    "A missing default in a weight map made score NaN for unknown sources — silently pushing every verdict to LOW.",
    "A countdown was displayed at CRITICAL, implying the payment would unlock. It never does.",
], size=10, bold_lead=False, space=6)

c = card(s, 6.85, 4.55, 5.75, 2.15, fill=BLUE_S, border=BLUE_S)
t = tb(s, 7.1, 4.75, 5.3, 1.85)
para(t, "THE HONEST BOUNDARY", 9.5, BLUE_D, bold=True, first=True, space_after=6)
para(t, "Two misses in the classifier are frauds carried by a phone number and a context "
        "text rules cannot see — exactly the gap the on-device signal chain closes.", 10, INK2, space_after=6)
para(t, "Payee-name verification against the bank record needs a PSP API. Until then, "
        "the app keeps saying: verify the name yourself.", 10, INK2, space_after=0)
footer(s, 9)

# ============================================================ 10 · ROADMAP + TEAM
s = slide()
header(s, "Where this goes", "Roadmap · team · thank you", "08 · NEXT")
cols = [
    ("Phase 1 — 48 hours", BLUE_S, BLUE_D, [
        "Web + Android demo complete",
        "Rules R1–R38 + eval harness",
        "Evidence pack → 1930 workflow",
    ]),
    ("Phase 2 — Pilot", VIO_S, VIOLET, [
        "Signed, versioned pattern updates",
        "PSP payee-name verification hook",
        "District-level awareness pilot",
    ]),
    ("Phase 3 — Scale", GRN_S, GREEN, [
        "Android accessibility service gate",
        "Bank / PSP grievance integrations",
        "Vernacular voice in 8 languages",
    ]),
]
x = 0.75
for (title, fill, col, items) in cols:
    card(s, x, 1.85, 3.85, 2.75, fill=fill, border=fill)
    t = tb(s, x + 0.24, 2.05, 3.4, 2.4)
    para(t, title, 12.5, col, bold=True, first=True, space_after=6)
    bullets(t, items, size=10, bold_lead=False, space=5)
    x += 4.05

c = card(s, 0.75, 4.85, 11.85, 1.55)
t = tb(s, 1.05, 5.08, 11.3, 1.2)
para(t, "Team SCΛMURΛI", 13, INK, bold=True, first=True, space_after=4)
para(t, "Nishant Kumar · Prince Singh · Kartik Singh — SANGYAN 2026, SEBI × NSDL × SNTC, IIT-BHU", 11, INK2, space_after=4)
para(t, "“Paisa bhejne se pehle 60 second, fraud ke baad 60 minute.”", 11.5, BLUE_D, bold=True, space_after=0)

t = tb(s, 0.75, 6.55, 11.85, 0.5)
para(t, "Thank you — questions welcome.  ·  Live: sudonishant.github.io/satarkpay-repo/webapp/", 11, MUTED, bold=True, first=True, space_after=0)
footer(s, 10)

out = ROOT / "SatarkPay_SANGYAN_Final.pptx"
prs.save(str(out))
print("saved", out, "slides:", len(prs.slides))
