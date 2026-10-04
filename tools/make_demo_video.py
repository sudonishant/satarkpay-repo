#!/usr/bin/env python3
"""
SatarkPay demo video builder.
  python3 tools/make_demo_video.py slides     -> assets/demo/slides/sNN.png
  python3 tools/make_demo_video.py assemble   -> assets/demo/SatarkPay_Demo.mp4
Voice clips go in assets/demo/voice/sNN.mp3 (Hindi narration).
"""
import json, math, os, pathlib, re, subprocess, sys
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = pathlib.Path(__file__).resolve().parent.parent
DEMO = ROOT / "assets" / "demo"
SLIDES = DEMO / "slides"
VOICE = DEMO / "voice"
DECK = ROOT / "web" / "deck"
SHOTS = DECK / "screenshots"
WEB = DECK / "screenshots_webapp"
SCAMS = DECK / "scam_shots"
FONT_DIR = ROOT / "assets" / "fonts"

W, H = 1920, 1080

BG      = (243, 246, 251)
INK     = (12, 20, 36)
INK2    = (51, 64, 90)
MUTED   = (100, 112, 138)
BLUE    = (47, 107, 246)
BLUE_D  = (28, 79, 214)
BLUE_S  = (234, 241, 255)
VIOLET  = (122, 90, 248)
VIO_S   = (240, 236, 255)
GREEN   = (13, 159, 109)
GRN_S   = (230, 248, 241)
RED     = (224, 47, 47)
RED_S   = (253, 236, 236)
AMBER   = (180, 83, 9)
AMB_S   = (253, 243, 229)
WHITE   = (255, 255, 255)
LINE    = (228, 233, 242)

DEVA = str(FONT_DIR / "NotoSansDevanagari-Bold.ttf")
LAT  = str(FONT_DIR / "NotoSans-Regular.ttf")

def font(size, deva=False):
    try:
        f = ImageFont.truetype(DEVA if deva else LAT, size)
        try:
            f.set_variation_by_name("Bold")
        except Exception:
            pass
        return f
    except Exception:
        return ImageFont.load_default()

# ------------------------------------------------------------- primitives --

def rounded(draw, xy, r, fill=None, outline=None, width=1):
    draw.rounded_rectangle(xy, radius=r, fill=fill, outline=outline, width=width)

def shadowed(img, xy, r, fill, blur=26, alpha=60, dy=14):
    sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    sd.rounded_rectangle([xy[0], xy[1] + dy, xy[2], xy[3] + dy], radius=r, fill=(20, 35, 80, alpha))
    sh = sh.filter(ImageFilter.GaussianBlur(blur))
    img.alpha_composite(sh)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle(xy, radius=r, fill=fill)

def paste_rounded(base, path, box, radius=18, border=LINE, bw=2):
    x0, y0, x1, y1 = box
    iw, ih = x1 - x0, y1 - y0
    im = Image.open(path).convert("RGB")
    im = im.resize((iw, ih))
    mask = Image.new("L", (iw, ih), 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle([0, 0, iw - 1, ih - 1], radius=radius, fill=255)
    base.paste(im, (x0, y0), mask)
    d = ImageDraw.Draw(base)
    d.rounded_rectangle([x0, y0, x1, y1], radius=radius, outline=border, width=bw)

def bg_slide():
    im = Image.new("RGBA", (W, H), BG + (255,))
    blob = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    bd = ImageDraw.Draw(blob)
    bd.ellipse([-260, -320, 900, 520], fill=(220, 232, 255, 130))
    bd.ellipse([1280, -220, 2320, 460], fill=(234, 226, 255, 120))
    bd.ellipse([520, 860, 1560, 1360], fill=(216, 244, 234, 100))
    blob = blob.filter(ImageFilter.GaussianBlur(90))
    im.alpha_composite(blob)
    return im

def header(im, kicker, title, title_size=54, deva=False):
    d = ImageDraw.Draw(im)
    kf = font(26, deva=True)
    chip_w = int(d.textlength(kicker, font=kf)) + 56
    rounded(d, [90, 70, 90 + chip_w, 124], 27, fill=BLUE_S)
    d.text((118, 80), kicker, font=kf, fill=BLUE_D)
    d.text((90, 152), title, font=font(title_size, deva=deva), fill=INK)
    d.rounded_rectangle([92, 168 + title_size, 232, 176 + title_size], radius=6, fill=BLUE)

def footer(im, n, total=11):
    d = ImageDraw.Draw(im)
    d.line([90, 1002, 1830, 1002], fill=LINE, width=2)
    d.text((90, 1018), "SatarkPay · सतर्कपे · SANGYAN 2026 · Team SCΛMURΛI", font=font(24, deva=True), fill=MUTED)
    d.text((1740, 1018), f"{n:02d} / {total}", font=font(24), fill=MUTED)

def bullet_block(im, x, y, w, items, size=30, gap=18, deva=True, col=INK2):
    d = ImageDraw.Draw(im)
    f = font(size, deva=deva)
    cy = y
    for it in items:
        d.ellipse([x, cy + 12, x + 12, cy + 24], fill=BLUE)
        # naive wrap
        words = it.split()
        line, lines = "", []
        for wd in words:
            t = (line + " " + wd).strip()
            if d.textlength(t, font=f) > w - 34:
                lines.append(line); line = wd
            else:
                line = t
        if line: lines.append(line)
        for i, ln in enumerate(lines):
            d.text((x + 30, cy), ln, font=f, fill=col)
            cy += size + 12
        cy += gap
    return cy

# ----------------------------------------------------------------- scenes --

def scene(n):
    im = bg_slide()
    d = ImageDraw.Draw(im)
    if n == 1:
        header(im, "SANGYAN 2026 · SEBI × NSDL × IIT-BHU", "SatarkPay  ·  सतर्कपे", 76, deva=True)
        d.text((90, 320), "Fraud detection that runs in the sixty seconds", font=font(44), fill=INK2)
        d.text((90, 382), "BEFORE the PIN — not after the money has gone.", font=font(44), fill=INK2)
        shadowed(im, (90, 500, 1050, 640), 26, WHITE)
        d.text((130, 530), "Web app  ·  Android app  ·  Offline-first  ·  0 bytes uploaded", font=font(34, deva=True), fill=BLUE_D)
        d.text((130, 586), "Team SCΛMURΛI — Nishant Kumar · Prince Singh · Kartik Singh", font=font(30), fill=INK2)
        shield = ROOT / "assets" / "demo" / "slide_shield.png"
        if shield.exists():
            s = Image.open(shield).convert("RGBA").resize((520, 520))
            im.alpha_composite(s, (1330, 300))
    elif n == 2:
        header(im, "THE PROBLEM", "UPI fraud is not a hack — it is a conversation.", 52)
        paste_rounded(im, SCAMS / "01_digital_arrest.png", (90, 300, 510, 830))
        paste_rounded(im, SCAMS / "03_qr_refund.png", (550, 300, 970, 830))
        d.text((1020, 330), "Asli fraud screenshots:", font=font(36, deva=True), fill=INK)
        bullet_block(im, 1020, 400, 800, [
            "“Digital arrest” — fake CBI case over video call",
            "“Scan QR to receive refund” — QR sirf kaatta hai",
            "Har jagah: jaldi, darr, aur galat disha mein paisa",
        ], size=30)
    elif n == 3:
        header(im, "THE PROBLEM", "Ek hi shape — urgency, isolation, pressure.", 52, deva=True)
        paste_rounded(im, SCAMS / "02_kyc_apk.png", (90, 300, 510, 830))
        paste_rounded(im, SCAMS / "04_fake_job.png", (550, 300, 970, 830))
        d.text((1020, 330), "Aur bhi patterns:", font=font(36, deva=True), fill=INK)
        bullet_block(im, 1020, 400, 800, [
            "Fake KYC APK — app hi asla hai, message sirf jaal",
            "Fake job / loan — registration fee se pehle kuch nahi",
            "60-second ka window — yahi protection ki jagah hai",
        ], size=30)
    elif n == 4:
        header(im, "LIVE DEMO · WEB APP", "Offline. No account. No server.", 52)
        paste_rounded(im, WEB / "w1_home.png", (90, 300, 1210, 950))
        bullet_block(im, 1270, 330, 560, [
            "Dashboard — signal chain saamne",
            "Check a payment — 9 sawal",
            "Har signal ka number khula",
            "8/8 pages fully offline",
        ], size=30)
    elif n == 5:
        header(im, "LIVE DEMO · PAYMENT GATE", "Nine signals. Four honest tiers.", 52)
        paste_rounded(im, WEB / "w2_payment.png", (90, 300, 1210, 950))
        shadowed(im, (1270, 320, 1830, 930), 26, WHITE)
        d.text((1310, 350), "Tiers", font=font(40), fill=INK)
        rows = [("LOW 0–2", "pay when ready", GREEN, GRN_S),
                ("CAUTION 3–5", "30s cooling", BLUE_D, BLUE_S),
                ("HIGH 6–9", "60s + alternatives", AMBER, AMB_S),
                ("CRITICAL 10+", "pay withheld", RED, RED_S)]
        yy = 430
        for (a, b, c, cs) in rows:
            rounded(d, [1310, yy, 1790, yy + 100], 18, fill=cs)
            d.text((1338, yy + 14), a, font=font(30, deva=True), fill=c)
            d.text((1338, yy + 56), b, font=font(26, deva=True), fill=INK2)
            yy += 118
    elif n == 6:
        header(im, "LIVE DEMO · MESSAGE CHECKER", "WhatsApp / SMS paste kijiye — 13 patterns.", 52, deva=True)
        paste_rounded(im, WEB / "w3_check.png", (90, 300, 1210, 950))
        bullet_block(im, 1270, 330, 560, [
            "13 fraud patterns + 4 legitimacy markers",
            "False alarm kam, asli warning tez",
            "UPI ID, account, OTP — sab masked",
        ], size=30)
    elif n == 7:
        header(im, "LIVE DEMO · LINKS + LIBRARY", "No blacklist — decomposition instead.", 52)
        paste_rounded(im, WEB / "w4_library.png", (90, 300, 1210, 950))
        bullet_block(im, 1270, 330, 560, [
            "Brand + action word + digits + TLD",
            "Asli domain se compare",
            "10 scams — screenshots ke saath",
        ], size=30)
    elif n == 8:
        header(im, "EMERGENCY MODE", "Paisa chala gaya? 10-minute plan.", 56, deva=True)
        paste_rounded(im, WEB / "w6_emergency.png", (90, 300, 1210, 950))
        shadowed(im, (1270, 320, 1830, 930), 26, WHITE)
        d.text((1310, 350), "7 steps — sahi order", font=font(34, deva=True), fill=INK)
        bullet_block(im, 1310, 420, 480, [
            "1.  1930 — abhi call",
            "2.  cybercrime.gov.in",
            "3.  Bank — freeze",
            "4.  Evidence bachao",
            "5.  Complaint number lo",
            "6.  Family ko batao",
            "7.  Recovery agent ko ₹0",
        ], size=27, gap=10)
    elif n == 9:
        header(im, "SUPPORT", "Complaint packet + Hindi voice mode.", 56, deva=True)
        paste_rounded(im, WEB / "w5_evidence.png", (90, 300, 1210, 950))
        bullet_block(im, 1270, 330, 560, [
            "Structured redacted record — ek click",
            "1930 / bank ko seedha de sakte hain",
            "Senior mode + Hindi awaaz",
            "Har step bina padhe, sun kar",
        ], size=30)
    elif n == 10:
        header(im, "LIVE DEMO · ANDROID APP", "14 screens · Kotlin + Compose · full stack", 52)
        paste_rounded(im, SHOTS / "02_screenshot_radar.png", (90, 300, 700, 760))
        paste_rounded(im, SHOTS / "04_ai_sanchalak.png", (740, 300, 1350, 760))
        paste_rounded(im, SHOTS / "07_auto_report_1930_pack.png", (1390, 300, 1830, 760))
        bullet_block(im, 90, 800, 1740, [
            "Screenshot Radar · Sanchalak AI (Hindi voice) · Wallet AutoPay audit",
            "App-security scanner · Emergency · Grievance ladder · Offline rule engine",
        ], size=32, gap=10)
    elif n == 11:
        header(im, "VERIFIED · HONEST NUMBERS", "Chhote numbers — lekin sach.", 56, deva=True)
        cards = [("92.7 %", "Message classifier F1", BLUE_S, BLUE_D),
                 ("< 12 ms", "Engine latency p50", VIO_S, VIOLET),
                 ("139 / 139", "Tests passing", GRN_S, GREEN),
                 ("8 / 8", "Pages offline", AMB_S, AMBER)]
        x = 90
        for (big, lab, cs, c) in cards:
            shadowed(im, (x, 320, x + 400, 520), 24, WHITE)
            rounded(d, [x, 320, x + 400, 330], 6, fill=c)
            d.text((x + 34, 356), big, font=font(56), fill=c)
            d.text((x + 34, 448), lab, font=font(28, deva=True), fill=INK2)
            x += 445
        shadowed(im, (90, 580, 1830, 800), 26, WHITE)
        d.text((130, 616), "Dhanyavaad! Team SCΛMURΛI — SatarkPay", font=font(48, deva=True), fill=INK)
        d.text((130, 690), "“Paisa bhejne se pehle 60 second, fraud ke baad 60 minute.”", font=font(34, deva=True), fill=BLUE_D)
    footer(im, n)
    return im.convert("RGB")

# ------------------------------------------------------------------ tools --

def ffmpeg_exe():
    import imageio_ffmpeg
    return imageio_ffmpeg.get_ffmpeg_exe()

def audio_duration(path):
    ff = ffmpeg_exe()
    out = subprocess.run([ff, "-i", str(path), "-f", "null", "-"],
                         capture_output=True, text=True)
    m = re.findall(r"time=(\d+):(\d+):(\d+\.?\d*)", out.stderr)
    if not m:
        raise RuntimeError(f"no duration for {path}")
    hh, mm, ss = m[-1]
    return int(hh) * 3600 + int(mm) * 60 + float(ss)

def main():
    cmd = sys.argv[1] if len(sys.argv) > 1 else "slides"
    SLIDES.mkdir(parents=True, exist_ok=True)
    if cmd == "slides":
        # title shield
        shield_src = ROOT / "assets" / "demo" / "slide_shield.png"
        if not shield_src.exists():
            icon = ROOT / "webapp" / "assets" / "icon.svg"
            print("note: place slide_shield.png in assets/demo/ for scene 1")
        for n in range(1, 12):
            im = scene(n)
            im.save(SLIDES / f"s{n:02d}.png")
            print("slide", n)
        return
    if cmd == "assemble":
        ff = ffmpeg_exe()
        VOICE.mkdir(parents=True, exist_ok=True)
        segs = []
        for n in range(1, 12):
            img = SLIDES / f"s{n:02d}.png"
            aud = VOICE / f"s{n:02d}.mp3"
            if not aud.exists():
                print("missing voice", aud); return
            dur = audio_duration(aud) + 1.1
            out = DEMO / f"seg_{n:02d}.mp4"
            fade_out = max(dur - 0.5, 0.5)
            vf = f"scale=1920:1080,format=yuv420p,fade=t=in:st=0:d=0.45,fade=t=out:st={fade_out:.2f}:d=0.45"
            cmdl = [ff, "-y", "-loop", "1", "-i", str(img), "-i", str(aud),
                    "-c:v", "libx264", "-tune", "stillimage", "-preset", "veryfast",
                    "-c:a", "aac", "-b:a", "192k", "-ar", "44100",
                    "-vf", vf, "-pix_fmt", "yuv420p", "-r", "25",
                    "-af", "apad", "-t", f"{dur:.2f}", str(out)]
            subprocess.run(cmdl, capture_output=True, check=True)
            segs.append(out)
            print(f"seg {n:02d}  {dur:.1f}s")
        lst = DEMO / "concat.txt"
        lst.write_text("".join(f"file '{p.name}'\n" for p in segs))
        final = DEMO / "SatarkPay_Demo.mp4"
        subprocess.run([ff, "-y", "-f", "concat", "-safe", "0", "-i", str(lst),
                        "-c:v", "libx264", "-preset", "veryfast", "-crf", "20",
                        "-c:a", "aac", "-b:a", "192k", "-movflags", "+faststart",
                        str(final)], capture_output=True, check=True)
        total = audio_duration(final)
        print("FINAL:", final, f"{total/60:.1f} min")
        return
    print("usage: slides | assemble")

if __name__ == "__main__":
    main()
