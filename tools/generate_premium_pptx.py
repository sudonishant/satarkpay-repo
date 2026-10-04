#!/usr/bin/env python3
"""
SatarkPay Executive Master Presentation Generator (16:9 Widescreen)
Generates high-impact, professional pitch decks in PPTX and updates HTML deck.
"""
import os
import pathlib
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

REPO_ROOT = pathlib.Path("/home/nee/Desktop/iitb/satarkpay-repo")
DECK_DIR = REPO_ROOT / "web" / "deck"
SHOTS = DECK_DIR / "screenshots"
CHARTS = DECK_DIR / "charts"
DIAGS = DECK_DIR / "diagrams"

# Executive Cyber Palette
C_BG       = RGBColor(8, 13, 26)       # #080D1A Deep Obsidian
C_CARD     = RGBColor(17, 26, 46)      # #111A2E Sleek Panel
C_BORDER   = RGBColor(30, 45, 74)      # #1E2D4A Subtle Border
C_CYAN     = RGBColor(0, 242, 254)     # #00F2FE Electric Cyan
C_GREEN    = RGBColor(16, 185, 129)    # #10B981 Emerald
C_AMBER    = RGBColor(245, 158, 11)    # #F59E0B Sunset Amber
C_RED      = RGBColor(239, 68, 68)     # #EF4444 Crimson
C_WHITE    = RGBColor(255, 255, 255)   # #FFFFFF Pure White
C_SLATE    = RGBColor(226, 232, 240)   # #E2E8F0 Light Slate
C_MUTED    = RGBColor(148, 163, 184)   # #94A3B8 Muted Silver

FONT_TITLE = 'Arial'
FONT_BODY  = 'Arial'

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
BLANK = prs.slide_layouts[6]

def create_slide(bg=C_BG):
    s = prs.slides.add_slide(BLANK)
    # Background fill
    bg_shape = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, Inches(13.333), Inches(7.5))
    bg_shape.fill.solid()
    bg_shape.fill.fore_color.rgb = bg
    bg_shape.line.color.rgb = bg
    return s

def add_header(s, title, kicker=None, tag=None):
    # Kicker
    if kicker:
        tx_box = s.shapes.add_textbox(Inches(0.8), Inches(0.42), Inches(10), Inches(0.4))
        tf = tx_box.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
        p = tf.paragraphs[0]
        p.text = kicker.upper()
        p.font.name = FONT_BODY
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = C_CYAN
    
    # Title
    top_pos = Inches(0.72) if kicker else Inches(0.5)
    tx_box = s.shapes.add_textbox(Inches(0.8), top_pos, Inches(10.5), Inches(0.65))
    tf = tx_box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
    p = tf.paragraphs[0]
    p.text = title
    p.font.name = FONT_TITLE
    p.font.size = Pt(25)
    p.font.bold = True
    p.font.color.rgb = C_WHITE

    # Right Tag Badge
    if tag:
        tag_box = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(10.8), Inches(0.5), Inches(1.7), Inches(0.42))
        tag_box.fill.solid()
        tag_box.fill.fore_color.rgb = C_CARD
        tag_box.line.color.rgb = C_CYAN
        tag_box.line.width = Pt(1)
        tf = tag_box.text_frame
        tf.vertical_anchor = MSO_ANCHOR.MIDDLE
        p = tf.paragraphs[0]
        p.text = tag
        p.alignment = PP_ALIGN.CENTER
        p.font.name = FONT_BODY
        p.font.size = Pt(10)
        p.font.bold = True
        p.font.color.rgb = C_CYAN

def add_card(s, left, top, width, height, title=None, border_col=C_BORDER, fill_col=C_CARD):
    card = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(height))
    card.fill.solid()
    card.fill.fore_color.rgb = fill_col
    card.line.color.rgb = border_col
    card.line.width = Pt(1.2)
    if title:
        tx = s.shapes.add_textbox(Inches(left + 0.25), Inches(top + 0.18), Inches(width - 0.5), Inches(0.4))
        tf = tx.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
        p = tf.paragraphs[0]
        p.text = title
        p.font.name = FONT_TITLE
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = C_WHITE
    return card

def add_bullet_list(s, left, top, width, height, bullets, sz=12.5, col=C_SLATE, line_spacing=1.3):
    tx = s.shapes.add_textbox(Inches(left), Inches(top), Inches(width), Inches(height))
    tf = tx.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
    for i, b in enumerate(bullets):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.text = ("•  " if not b.startswith("•") else "") + b
        p.font.name = FONT_BODY
        p.font.size = Pt(sz)
        p.font.color.rgb = col
        p.space_after = Pt(7)

def add_stat_box(s, left, top, width, height, number, label, sublabel=None, num_col=C_CYAN):
    card = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(height))
    card.fill.solid()
    card.fill.fore_color.rgb = C_CARD
    card.line.color.rgb = num_col
    card.line.width = Pt(1.4)

    tx = s.shapes.add_textbox(Inches(left + 0.15), Inches(top + 0.12), Inches(width - 0.3), Inches(height - 0.24))
    tf = tx.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
    
    # Number
    p1 = tf.paragraphs[0]
    p1.text = number
    p1.font.name = FONT_TITLE
    p1.font.size = Pt(28)
    p1.font.bold = True
    p1.font.color.rgb = num_col
    p1.alignment = PP_ALIGN.CENTER
    
    # Label
    p2 = tf.add_paragraph()
    p2.text = label
    p2.font.name = FONT_BODY
    p2.font.size = Pt(11)
    p2.font.bold = True
    p2.font.color.rgb = C_WHITE
    p2.alignment = PP_ALIGN.CENTER
    p2.space_before = Pt(3)

    if sublabel:
        p3 = tf.add_paragraph()
        p3.text = sublabel
        p3.font.name = FONT_BODY
        p3.font.size = Pt(9.5)
        p3.font.color.rgb = C_MUTED
        p3.alignment = PP_ALIGN.CENTER
        p3.space_before = Pt(2)

def add_image_safe(s, path, left, top, width=None, height=None):
    if os.path.exists(path):
        if width and height:
            return s.shapes.add_picture(str(path), Inches(left), Inches(top), Inches(width), Inches(height))
        elif width:
            return s.shapes.add_picture(str(path), Inches(left), Inches(top), width=Inches(width))
        elif height:
            return s.shapes.add_picture(str(path), Inches(left), Inches(top), height=Inches(height))
    return None

def add_footer(s, slide_num, total_slides=12):
    # Bottom subtle line
    line = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(7.0), Inches(11.733), Inches(0.015))
    line.fill.solid()
    line.fill.fore_color.rgb = C_BORDER
    line.line.color.rgb = C_BORDER

    # Left text
    tx = s.shapes.add_textbox(Inches(0.8), Inches(7.05), Inches(8), Inches(0.3))
    tf = tx.text_frame
    p = tf.paragraphs[0]
    p.text = "SatarkPay · SANGYAN Hackathon (SEBI × NSDL × SNTC, IIT-BHU) · Team SCΛMURΛI"
    p.font.name = FONT_BODY
    p.font.size = Pt(9.5)
    p.font.color.rgb = C_MUTED

    # Right page
    tx2 = s.shapes.add_textbox(Inches(11.5), Inches(7.05), Inches(1.0), Inches(0.3))
    tf2 = tx2.text_frame
    p2 = tf2.paragraphs[0]
    p2.text = f"{slide_num:02d} / {total_slides:02d}"
    p2.alignment = PP_ALIGN.RIGHT
    p2.font.name = FONT_BODY
    p2.font.size = Pt(9.5)
    p2.font.bold = True
    p2.font.color.rgb = C_CYAN

# =========================================================================
# SLIDE 1: COVER
# =========================================================================
s1 = create_slide()
# Title Block
t_box = s1.shapes.add_textbox(Inches(1.0), Inches(1.4), Inches(11.3), Inches(1.8))
tf = t_box.text_frame
tf.word_wrap = True
p = tf.paragraphs[0]
p.text = "🛡️ SatarkPay (सतर्कपे)"
p.font.name = FONT_TITLE
p.font.size = Pt(46)
p.font.bold = True
p.font.color.rgb = C_WHITE

p2 = tf.add_paragraph()
p2.text = "Next-Gen Real-Time UPI Fraud Interception & Evidence Chain-of-Custody Pipeline"
p2.font.name = FONT_TITLE
p2.font.size = Pt(21)
p2.font.color.rgb = C_CYAN
p2.space_before = Pt(8)

p3 = tf.add_paragraph()
p3.text = "“Paisa bhejne se pehle 60 second — aur fraud ke baad 60 minute. Dono par SatarkPay ka pehra hai.”"
p3.font.name = FONT_BODY
p3.font.size = Pt(15)
p3.font.color.rgb = C_SLATE
p3.space_before = Pt(14)

# 4 Key Stat Badges across bottom
stats = [
    ("60s", "Pre-Pay Interception", "Dwell & Radar Gates", C_GREEN),
    ("<12ms", "Edge AI Latency", "Deterministic Rules R1-R38", C_CYAN),
    ("100%", "On-Device DPDP", "Zero Cloud Plaintext", C_WHITE),
    ("60m", "Golden Hour Recovery", "1-Click 1930 / CFCFRMS", C_AMBER)
]
for i, (num, lbl, sub, col) in enumerate(stats):
    add_stat_box(s1, 1.0 + i * 2.9, 3.8, 2.65, 1.5, num, lbl, sub, col)

# Subtitle / Team
add_card(s1, 1.0, 5.7, 11.333, 0.9, fill_col=RGBColor(12, 18, 33))
meta_tx = s1.shapes.add_textbox(Inches(1.2), Inches(5.82), Inches(10.9), Inches(0.7))
mtf = meta_tx.text_frame
mp = mtf.paragraphs[0]
mp.text = "SANGYAN HACKATHON · SEBI × NSDL × SNTC, IIT-BHU  |  Tracks: A (Resilience) · B (Grievance) · D (Behavioral)"
mp.font.size = Pt(11)
mp.font.bold = True
mp.font.color.rgb = C_CYAN
mp2 = mtf.add_paragraph()
mp2.text = "Team SCΛMURΛI: Nishant Kumar (Architecture & Android) · Prince Singh (Crawler & Evidence) · Kartik Singh (UI & Eval)"
mp2.font.size = Pt(10)
mp2.font.color.rgb = C_MUTED
mp2.space_before = Pt(3)

add_footer(s1, 1)

# =========================================================================
# SLIDE 2: THE PROBLEM (WHY FRAUD SUCCEEDS)
# =========================================================================
s2 = create_slide()
add_header(s2, "The Cognitive Trap of Modern UPI Fraud", "PROBLEM STATEMENT", "MARKET REALITY")

# Left Column: The Problem Breakdown
add_card(s2, 0.8, 1.45, 5.7, 5.2, "Why Existing Fraud Solutions Fail")
add_bullet_list(s2, 1.1, 2.05, 5.1, 4.3, [
    "Scam message padhne se nahi, real-time social engineering aur psychological coercion se hota hai.",
    "Traditional tools sirf 'Paste message -> Check score' karte hain — yaani fraud ho jaane ke BAAD.",
    "Digital Arrest, Fake Police calls, aur Task investment scams victim ko continuous call/chat par engage rakhte hain.",
    "Victim se WhatsApp/Telegram se seedhe UPI app (GPay/PhonePe/Paytm) switch karwaya jata hai under 60 seconds.",
    "First-time Tier-2/3 investors (16 Crore+ Demat accounts) are primary targets with zero prior exposure to financial manipulation."
], sz=13)

# Right Column: The Two Critical Windows
add_card(s2, 6.8, 1.45, 5.7, 2.45, "Window 1: 60 Seconds Pre-Payment", border_col=C_CYAN)
add_bullet_list(s2, 7.1, 2.0, 5.1, 1.7, [
    "Foreground dwell monitoring: Chat session se UPI app transition.",
    "Critical moment: User PIN daalne se pehle 60 second ka cognitive cooling gate chahiye taaki pressure toota ja sake."
], sz=12)

add_card(s2, 6.8, 4.2, 5.7, 2.45, "Window 2: 60 Minutes Golden Recovery", border_col=C_AMBER)
add_bullet_list(s2, 7.1, 4.75, 5.1, 1.7, [
    "Fraud hone ke baad pehla 60 minute hi fund freeze ka asli mauka hota hai.",
    "Victims panicking in isolation struggle to find transaction UTR, cyber cell emails, and official 1930 scripts."
], sz=12)

add_footer(s2, 2)

# =========================================================================
# SLIDE 3: 3-TIER DEFENSE SHIELD ARCHITECTURE
# =========================================================================
s3 = create_slide()
add_header(s3, "3-Tier Defense Shield: Full Journey Protection", "SOLUTION ARCHITECTURE", "CORE INNOVATION")

# 3 Pillars
lanes = [
    ("⚡ LANE 1: PRE-PAYMENT INTERCEPTION", [
        "M1 Chat-Before-Pay Guard: Foreground dwell timer + contact length matrix.",
        "M2 Screenshot Radar: Burst receipt detection (3+ receipts in 30m).",
        "M3 Domain Ladder: 4-level domain trust + Gateway ≠ Merchant warning.",
        "Deterministic cooling delays (1m to 8m) — zero auto-blocks."
    ], C_CYAN),
    ("🛡️ LANE 2: IN-FLIGHT COGNITIVE FRICTION", [
        "M4 AI Sanchalak: On-device intent classifier with 3 honest decision buckets.",
        "SEBI / NSDL Registry format validation (INZ/INH/INA + 9 digits).",
        "Negation-Aware matching (Bank OTP warnings flag nahi hote).",
        "Vernacular Hindi Voice TTS + Senior Mode AAA accessibility."
    ], C_GREEN),
    ("🚨 LANE 3: GOLDEN HOUR POST-FRAUD RECOVERY", [
        "M7 Evidence Dossier: Automated OCR, PII masking, SHA-256 integrity hashes.",
        "R38 Instant SOS Trio: Pre-composed Cyber Cell email with bank CC.",
        "Helpline 1930 operator script + CFCFRMS payment freeze request.",
        "Follow-up grievance routing: SEBI SCORES 2.0 & SMART ODR."
    ], C_AMBER)
]

for i, (title, points, col) in enumerate(lanes):
    add_card(s3, 0.8 + i * 3.95, 1.45, 3.8, 5.2, title, border_col=col)
    add_bullet_list(s3, 1.05 + i * 3.95, 2.1, 3.3, 4.3, points, sz=12, col=C_SLATE)

add_footer(s3, 3)

# =========================================================================
# SLIDE 4: MODULE 1 — CHAT-BEFORE-PAY GUARD
# =========================================================================
s4 = create_slide()
add_header(s4, "M1 · Chat-Before-Pay: Intelligent Dwell Matrix", "PRE-PAYMENT DEFENSE", "MODULE M1")

# Left Column: The 4-Cell Matrix
add_card(s4, 0.8, 1.45, 6.2, 5.2, "Usage Dwell × Contact Relationship Matrix")
add_bullet_list(s4, 1.05, 2.05, 5.7, 4.3, [
    "Known Contact + Short Chat (<8m): FAST PATH — 1 min countdown ya 1-tap call-to-confirm.",
    "Known Contact + Long Chat (>10m): CAUTION ALERT — Account takeover / coercion alert.",
    "Unknown Number + Short Chat (<8m): COOLING PERIOD — 8-min guided verification + 3 reflective questions.",
    "Unknown Number + Long Chat (>8m): HARD HOLD — High probability digital arrest; Tier-3 analyst callback required.",
    "Zero Arbitrary Blocks: SatarkPay introduces cognitive delays — user always holds the final sovereign authority."
], sz=13)

# Right Column: Live Simulator Screenshots
add_card(s4, 7.3, 1.45, 5.2, 5.2, "Real-Time Mobile Simulation")
add_image_safe(s4, SHOTS / "01a_saved_contact_fastpath.png", 7.55, 2.05, width=2.25)
add_image_safe(s4, SHOTS / "01c_unknown_hold_callback.png", 10.0, 2.05, width=2.25)

add_footer(s4, 4)

# =========================================================================
# SLIDE 5: MODULE 2 & 3 — RADAR & DOMAIN TRUST LADDER
# =========================================================================
s5 = create_slide()
add_header(s5, "M2 & M3 · Screenshot Radar & Domain Trust Ladder", "MULTI-VECTOR DEFENSE", "MODULES M2 & M3")

# Left Box: M2 Screenshot Radar
add_card(s5, 0.8, 1.45, 5.7, 5.2, "M2: Screenshot Radar (Rules R17, R22)")
add_image_safe(s5, SHOTS / "02_screenshot_radar.png", 1.05, 2.05, width=2.4)
add_bullet_list(s5, 3.6, 2.05, 2.7, 4.3, [
    "Burst Detection: Flags 3+ fake payment receipts captured in 30 min (task scam pattern).",
    "Staircase Velocity: Flags repeat payments to same new payee within 24h.",
    "Privacy: Raw screenshots are purged post-OCR; only entities and SHA-256 hashes are preserved."
], sz=11.5)

# Right Box: M3 Domain Trust Ladder
add_card(s5, 6.8, 1.45, 5.7, 5.2, "M3: 4-Tier Domain Trust Ladder (R18, R26)")
add_image_safe(s5, SHOTS / "03_domain_trust.png", 7.05, 2.05, width=2.4)
add_bullet_list(s5, 9.6, 2.05, 2.7, 4.3, [
    "L1 Verified: Official regulatory & banking domains (sebi.gov.in, rbi.org.in).",
    "L2 Merchant: Verified payment gateways.",
    "L3 Unverified Clean: Amber advisory friction.",
    "L4 Malicious Lookalike: Typosquatting, punycode & IP-literal URLs.",
    "Gateway ≠ Merchant Mismatch: Flags legit payment gateway checkout hosting unverified fraudulent sellers."
], sz=11.5)

add_footer(s5, 5)

# =========================================================================
# SLIDE 6: MODULE 4 — AI SANCHALAK & SEBI REGISTRY
# =========================================================================
s6 = create_slide()
add_header(s6, "M4 · AI Sanchalak: Deterministic Edge Assistant", "ZERO-CLOUD AI", "MODULE M4")

add_card(s6, 0.8, 1.45, 6.2, 5.2, "On-Device Intent Scanner & Registry Validation")
add_bullet_list(s6, 1.05, 2.05, 5.7, 4.3, [
    "SEBI / NSDL Registration Check (R26): Validates intermediary registration number syntax (INZ/INH/INA + 9 digits) against official portal rules.",
    "Negation Understanding (R27): Recognizes legitimate banking safety alerts ('Bank will NEVER ask for OTP') without triggering false alarms.",
    "Client-Side PII Redaction (R28): Phone numbers, account numbers, Aadhaar, and VPAs are automatically masked on-device before rule evaluation.",
    "3 Honest Decision Buckets: 'SCAM LIKELY', 'CAUTION' (soft friction), and 'UNSURE' — avoiding dangerous false confidence.",
    "Accessibility: Full Hindi Voice TTS synthesis + Senior AAA Mode (+25% font scale, high contrast)."
], sz=13)

# Right Column: Screenshot
add_card(s6, 7.3, 1.45, 5.2, 5.2, "M4 Engine Live Evaluation")
add_image_safe(s6, SHOTS / "08_m4_sebi_registry_negation.png", 7.55, 2.1, width=4.7)

add_footer(s6, 6)

# =========================================================================
# SLIDE 7: MODULE 7 & R38 — GOLDEN HOUR EMERGENCY ACTION
# =========================================================================
s7 = create_slide()
add_header(s7, "M7 & R38 · Golden Hour Emergency Action Pipeline", "INCIDENT RECOVERY", "MODULE M7 & R38")

add_card(s7, 0.8, 1.45, 6.2, 5.2, "The 1-Click Disaster Recovery Pipeline")
add_bullet_list(s7, 1.05, 2.05, 5.7, 4.3, [
    "Golden-Hour Live Clock: 60-minute countdown tracking the critical window for banking inter-bank freeze.",
    "1. Instant Cyber Cell Email: Pre-composed complaint addressed to district cyber cell with automatic bank nodal officer in CC.",
    "2. 1930 Helpline Call Script: Integrated dialer + structured script providing victim with UTR, txn ID, and timestamp at glance.",
    "3. Immediate Payment Stop & Lien Request: Invokes CFCFRMS inter-bank dispute protocol with temporary account freeze advice.",
    "Chain of Custody: Automatic compilation of cropped screenshots, OCR text, and SHA-256 tamper-evident hashes."
], sz=13)

add_card(s7, 7.3, 1.45, 5.2, 5.2, "R38 Emergency Panel")
add_image_safe(s7, SHOTS / "11_emergency_confirm_action.png", 7.55, 2.1, width=4.7)

add_footer(s7, 7)

# =========================================================================
# SLIDE 8: NATIVE ANDROID JETPACK COMPOSE APPLICATION
# =========================================================================
s8 = create_slide()
add_header(s8, "Native Android App: Modern White Fintech Experience", "MOBILE APPLICATION", "KOTLIN & COMPOSE")

# Left Column: Tech Stack & Architecture
add_card(s8, 0.8, 1.45, 5.7, 5.2, "Production Native Architecture")
add_bullet_list(s8, 1.05, 2.05, 5.1, 4.3, [
    "Modern Kotlin & Jetpack Compose: 100% declarative UI with clean architecture.",
    "Clean White Fintech Theme: High-contrast surfaces (#FFFFFF), soft canvas (#F8FAFC), and vibrant fintech cyan/emerald accents.",
    "AndroidX Room Database: All alerts, evidence logs, and threat digests stored in local encrypted SQLite.",
    "Lightweight Footprint: Zero heavy bloat, pre-compiled debug APK is only 23 MB.",
    "Ready To Deploy: SatarkPay-WhiteTheme.apk available for immediate device testing."
], sz=13)

# Right Column: Key Screens Highlight
add_card(s8, 6.8, 1.45, 5.7, 5.2, "Android Features & Screen Suite")
add_bullet_list(s8, 7.05, 2.05, 5.1, 4.3, [
    "HomeScreen: Live threat status, dwell radar cards, and 1-tap quick actions.",
    "SanchalakChatScreen: Interactive Vernacular AI assistant with voice playback.",
    "ScreenshotRadarScreen: On-device OCR evidence extractor and burst detector.",
    "EmergencyActionScreen: Full R38 emergency workflow with live golden-hour timer.",
    "AnalystConsoleScreen: Telemetry inspection, audit logs, and fraud rule explorer."
], sz=13)

add_footer(s8, 8)

# =========================================================================
# SLIDE 9: EMPIRICAL BENCHMARKS & EVALUATION
# =========================================================================
s9 = create_slide()
add_header(s9, "Measured, Not Guessed: Empirical Performance", "VERIFICATION", "BENCHMARKS")

# 4 Stat KPI boxes
add_stat_box(s9, 0.8, 1.45, 2.7, 1.5, "95.2%", "Precision", "Minimal False Alarms", C_GREEN)
add_stat_box(s9, 3.75, 1.45, 2.7, 1.5, "90.9%", "Recall", "High Catch Rate", C_CYAN)
add_stat_box(s9, 6.7, 1.45, 2.7, 1.5, "93.0%", "F1 Score", "Harmonic Balance", C_WHITE)
add_stat_box(s9, 9.65, 1.45, 2.7, 1.5, "<12ms", "Edge Latency", "Deterministic Execution", C_AMBER)

# Bottom row: Chart + Test Suite Details
add_card(s9, 0.8, 3.2, 5.7, 3.5, "Confusion Matrix & Evaluation Set")
add_image_safe(s9, CHARTS / "chart_eval.png", 1.05, 3.75, width=5.2)

add_card(s9, 6.8, 3.2, 5.7, 3.5, "Automated 66-Point Test Assertion Suite")
add_bullet_list(s9, 7.05, 3.75, 5.1, 2.7, [
    "smoke_test.js: 66 automated state-machine & DOM assertions pass with 100% success.",
    "Evaluated across 33 stratified test messages (22 adversarial scams + 11 legitimate banking alerts).",
    "Zero Network Dependencies: Rule matching and regex parsing execute 100% offline.",
    "Transparent Error Tagging: Every miss and friction item is visibly classified."
], sz=12)

add_footer(s9, 9)

# =========================================================================
# SLIDE 10: PRIVACY BY DESIGN (DPDP ACT 2023)
# =========================================================================
s10 = create_slide()
add_header(s10, "Privacy By Design: DPDP Act 2023 Conformance", "PRIVACY & SECURITY", "ZERO COMPROMISE")

add_card(s10, 0.8, 1.45, 5.7, 5.2, "What SatarkPay Strictly Avoids")
add_bullet_list(s10, 1.05, 2.05, 5.1, 4.3, [
    "❌ No Continuous Screen Recording: SatarkPay never records screen video or reads private chats.",
    "❌ No Invasive Android Permissions: Strictly avoids READ_SMS, READ_CONTACTS, or QUERY_ALL_PACKAGES.",
    "❌ No Background Accessibility Scraping: Avoids malware-like accessibility service abuse.",
    "❌ No Raw PII Storage: Screenshots are purged after OCR; only masked hashes are kept locally.",
    "❌ No Cloud Plaintext Transmission: Zero user messages or telemetry leave the local device."
], sz=13)

add_card(s10, 6.8, 1.45, 5.7, 5.2, "Architectural Privacy Guarantees")
add_bullet_list(s10, 7.05, 2.05, 5.1, 4.3, [
    "✅ Client-Side Redaction (R28): Phone numbers, VPAs, and Aadhaar masked before engine processing.",
    "✅ User-Controlled Actions: No auto-sending of emails or emergency calls; user triggers every transmission.",
    "✅ Local Cryptographic Hashing: Tamper-evident SHA-256 evidence chain stored in private sandbox.",
    "✅ Public Good Ethos: Zero monetization funnels, zero trading tips, zero financial data selling."
], sz=13)

add_footer(s10, 10)

# =========================================================================
# SLIDE 11: SCALABILITY & REGULATORY ROADMAP
# =========================================================================
s11 = create_slide()
add_header(s11, "Scalability & Regulatory Integration Roadmap", "FUTURE ROADMAP", "DEPLOYMENT")

phases = [
    ("PHASE 1: CLIENT-SIDE APP (NOW)", [
        "Standalone Android Kotlin App + Ready APK.",
        "Interactive Web Cyber Simulator for browser use.",
        "On-device 38 deterministic threat rules.",
        "60s Pre-Pay & 60m Post-Fraud Golden Hour pipeline."
    ], C_CYAN),
    ("PHASE 2: UPI GATEWAY & PSP SDK", [
        "Embed SatarkPay Engine as SDK inside UPI apps.",
        "Pre-transaction dwell hook inside GPay, PhonePe, Paytm.",
        "Merchant verification against NPCI registry.",
        "Over-The-Air (OTA) threat intelligence rule updates."
    ], C_GREEN),
    ("PHASE 3: NATIONAL REGULATORY MESH", [
        "Direct API integration with SEBI SCORES 2.0.",
        "Automated docket submission to I4C / CFCFRMS 1930.",
        "DoT Sanchar Saathi Chakshu reporting bridge.",
        "Collaborative banking fraud telemetry exchange."
    ], C_AMBER)
]

for i, (title, points, col) in enumerate(phases):
    add_card(s11, 0.8 + i * 3.95, 1.45, 3.8, 5.2, title, border_col=col)
    add_bullet_list(s11, 1.05 + i * 3.95, 2.1, 3.3, 4.3, points, sz=12, col=C_SLATE)

add_footer(s11, 11)

# =========================================================================
# SLIDE 12: CONCLUSION & DELIVERABLES SUMMARY
# =========================================================================
s12 = create_slide()
add_header(s12, "SatarkPay: Ready For Real-World Protection", "CONCLUSION", "DELIVERABLES")

add_card(s12, 0.8, 1.45, 7.2, 5.2, "Core Submission Deliverables")
add_bullet_list(s12, 1.05, 2.05, 6.7, 4.3, [
    "📱 Native Android App: app/satarkpay-android/ (Clean White Fintech Theme, Kotlin Compose).",
    "📦 Pre-Compiled APK: SatarkPay-WhiteTheme.apk (23 MB ready to install).",
    "🌐 Web Cyber Simulator: web/satarkpay_m2.html (Glassmorphism, Audio haptics, Live ticker).",
    "📑 Interactive Pitch Deck: web/deck/deck.html (12 High-impact executive slides).",
    "⚙️ Rule Packs R1-R38: rules/ (Deterministic on-device patterns covering 18 fraud families).",
    "🧪 Test Verification: 66/66 smoke tests passing (web/run_smoke.sh) with <12ms latency."
], sz=13)

# Right Box: Team Summary
add_card(s12, 8.3, 1.45, 4.2, 5.2, "Team SCΛMURΛI", border_col=C_CYAN)
add_bullet_list(s12, 8.55, 2.1, 3.7, 4.3, [
    "Nishant Kumar\nSystem Architecture, Android Compose & Rule Engine",
    "Prince Singh\nThreat Intelligence Crawling & Evidence Dossier",
    "Kartik Singh\nUI/UX Design System, Evaluation & Web Simulator",
    "Submitted for SANGYAN Hackathon (SEBI × NSDL × SNTC, IIT-BHU)"
], sz=12, col=C_WHITE)

add_footer(s12, 12)

# Save PPTX outputs
out_pptx_deck = DECK_DIR / "satarkpay_deck.pptx"
out_pptx_pitch = REPO_ROOT / "web" / "pitch" / "SANGYAN_SatarkPay_Pitch.pptx"
out_pptx_root = REPO_ROOT / "SatarkPay_Executive_Presentation.pptx"

prs.save(str(out_pptx_deck))
prs.save(str(out_pptx_pitch))
prs.save(str(out_pptx_root))

print(f"[✓] Generated Executive PPTX: {out_pptx_deck} ({os.path.getsize(out_pptx_deck)/1024:.1f} KB)")
print(f"[✓] Generated Executive PPTX: {out_pptx_pitch}")
print(f"[✓] Generated Executive PPTX: {out_pptx_root}")
