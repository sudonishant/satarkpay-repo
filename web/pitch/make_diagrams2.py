#!/usr/bin/env python3
"""SatarkPay · pitch deck diagrams — part 2 (pure Python, matplotlib).
Premium light theme (same as make_diagrams.py).
Run: python3 make_diagrams2.py  ->  diagrams/d6..d11
"""
import pathlib, json
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch
from matplotlib.lines import Line2D

HERE = pathlib.Path(__file__).resolve().parent
OUT = HERE / 'diagrams'; OUT.mkdir(exist_ok=True)
BG, INK, SLATE, DIM = '#FBF6EE', '#14181F', '#2C3E50', '#8A94A6'
GOLD, SAND, SAGE, CLAY, TEAL = '#B08D57', '#E3D3B0', '#5F7F6F', '#B0654F', '#3E6E78'
F = 'DejaVu Sans'

def fig(w, h):
    f = plt.figure(figsize=(w, h), dpi=220); f.patch.set_facecolor(BG); return f
def save(f, name):
    p = OUT / name; f.savefig(p, facecolor=BG, bbox_inches='tight', pad_inches=0.22)
    plt.close(f); print('  ✓', name)
def rbox(ax, x, y, w, h, fc, ec, lw=1.3, r=0.02):
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle=f'round,pad=0.004,rounding_size={r}',
                                fc=fc, ec=ec, lw=lw, zorder=3))

# ============================================================ d6 · eval + confusion
def d_eval():
    f = fig(11, 3.3); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 30); ax.axis('off')
    ax.text(50, 28, '33 labelled messages — aur hum apne miss/FP khud dikhate hain', ha='center',
            fontsize=11.6, color=INK, fontfamily=F, fontweight='bold')
    # left: metrics bars
    ax.text(6, 24.4, 'Metrics', fontsize=10, color=GOLD, fontfamily=F, fontweight='bold')
    for i, (lab, v, c) in enumerate([('Precision', 95.0, SAGE), ('Recall', 90.5, TEAL), ('F1', 92.7, SLATE)]):
        y = 21.4 - i * 3.4
        ax.add_patch(FancyBboxPatch((17, y), 26, 1.7, boxstyle='round,pad=0.002,rounding_size=0.28',
                                    fc='#EFE7D6', ec='none', zorder=2))
        ax.add_patch(FancyBboxPatch((17, y), 26 * v / 100, 1.7, boxstyle='round,pad=0.002,rounding_size=0.28',
                                    fc=c, ec='none', zorder=3))
        ax.text(15.6, y + 0.82, lab, ha='right', va='center', fontsize=9.6, color=SLATE, fontfamily=F)
        ax.text(44.4, y + 0.82, f'{v}%', va='center', fontsize=10.4, color=INK, fontfamily=F, fontweight='bold')
    # right: confusion 2x2
    ax.text(58, 24.4, 'Confusion (33 msgs)', fontsize=10, color=GOLD, fontfamily=F, fontweight='bold')
    cells = [(58, 17.8, 'TP', 19, '#EFF4EF', SAGE), (73.5, 17.8, 'FP', 1, '#F7ECE6', CLAY),
             (58, 12.6, 'miss (FN)', 2, '#F7ECE6', CLAY), (73.5, 12.6, 'TN', 9, '#EFF4EF', SAGE)]
    for x, y, lab, n, fc, ec in cells:
        rbox(ax, x, y, 14, 4.6, fc, ec, 1.2)
        ax.text(x + 7, y + 3.1, str(n), ha='center', fontsize=17, color=ec, fontfamily=F, fontweight='bold')
        ax.text(x + 7, y + 1.3, lab, ha='center', fontsize=9, color=SLATE, fontfamily=F)
    ax.text(50, 7.6, '“PAKKA NAHI BATA SAKTA” ek valid jawab hai — isliye 9 legit messages ko humne chhoda, guess nahi kiya.',
            ha='center', fontsize=9.4, color=SLATE, fontfamily=F)
    ax.text(50, 4.9, 'Set self-authored hai (ceiling, field accuracy nahi) — ye hum likhte hain, chhupate nahi.',
            ha='center', fontsize=8.8, color=DIM, fontfamily=F, style='italic')
    ax.text(50, 1.9, 'Command: python3 eval/run_eval.py  ·  latest_results.json repo me', ha='center',
            fontsize=8.4, color=GOLD, fontfamily=F, fontweight='bold')
    save(f, 'd6_eval_confusion.png')

# ============================================================ d7 · latency histogram (REAL data)
def d_latency():
    raw = json.loads((HERE / 'latency_raw.json').read_text())
    ts = sorted(raw['ts']); n = len(ts)
    med = ts[n // 2]; p95 = ts[int(n * 0.95)]; p99 = ts[int(n * 0.99)]
    f = fig(10.5, 3.2); ax = f.add_axes([0.08, 0.16, 0.88, 0.68]); ax.set_facecolor(BG)
    bins = [0.0005, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.5, 5]
    import numpy as np
    counts, edges = [], bins
    for i in range(len(edges) - 1):
        counts.append(sum(1 for t in ts if edges[i] <= t < edges[i + 1]))
    labels = ['<2µs', '2–5µs', '5–10µs', '10–20µs', '20–50µs', '50–100µs', '0.1–0.5ms', '0.5–5ms']
    xs = range(len(counts))
    cols = [GOLD if (0.01 <= e < 0.02) else (SAND if e < 0.01 or e < 0.05 else '#D8CBB4') for e in edges[:-1]]
    ax.bar(xs, counts, color=cols, edgecolor=SLATE, lw=0.5, width=0.72)
    for x, c in zip(xs, counts):
        if c: ax.text(x, c + n * 0.012, str(c), ha='center', fontsize=7.8, color=SLATE, fontfamily=F)
    ax.set_xticks(list(xs)); ax.set_xticklabels(labels, fontsize=8.2, color=SLATE, fontfamily=F, rotation=18)
    ax.set_ylabel('runs (of 5,000)', fontsize=8.6, color=DIM, fontfamily=F)
    ax.set_title(f'classify() latency · 5,000 runs · median {med:.4f} ms  ·  p95 {p95:.4f} ms  ·  p99 {p99:.4f} ms',
                 color=INK, fontsize=9.6, pad=8, fontfamily=F, loc='left')
    for s in ax.spines.values(): s.set_color('#E7DFCD')
    ax.grid(axis='y', color='#EFE7D6', lw=0.7); ax.set_axisbelow(True)
    save(f, 'd7_latency_hist.png')

# ============================================================ d8 · module map M1-M7
def d_modules():
    f = fig(11, 3.6); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 32); ax.axis('off')
    ax.text(50, 29.8, 'Saat modules + emergency layer — poora chain: pehle roko, baad me likho', ha='center',
            fontsize=11.8, color=INK, fontfamily=F, fontweight='bold')
    mods = [('M1', 'Chat-Before-Pay', 'known/unknown ×\nchat length', GOLD),
            ('M2', 'Screenshot Radar', 'payment burst +\nprovenance', SLATE),
            ('M3', 'Domain Trust', 'gateway ≠\nmerchant', SLATE),
            ('M4', 'AI Sanchalak', 'verdict + registry\nformat check', GOLD),
            ('M5', 'Wallet Audit', 'autopay/UPI\nmandates', SLATE),
            ('M6', 'Intel Desk', 'live threat\nfeed', SLATE),
            ('M7', 'Auto-Report', '1930 pack +\nchain of custody', SAGE)]
    W, GAP = 12.6, 1.05
    x0 = (100 - (7 * W + 6 * GAP)) / 2
    for i, (m, name, sub, c) in enumerate(mods):
        x = x0 + i * (W + GAP)
        rbox(ax, x, 12.6, W, 13.4, '#FFFFFF', c, 1.6)
        ax.text(x + W/2, 23.4, m, ha='center', fontsize=13, color=c, fontfamily=F, fontweight='bold')
        ax.text(x + W/2, 20.9, name, ha='center', fontsize=8.6, color=INK, fontfamily=F, fontweight='bold')
        ax.text(x + W/2, 18.2, sub, ha='center', va='top', fontsize=7.6, color=SLATE, fontfamily=F, linespacing=1.4)
    rbox(ax, x0, 4.6, 100 - 2*x0, 5.6, '#F2E4CD', CLAY, 1.8)
    ax.text(x0 + (100-2*x0)/2, 8.3, 'EMERGENCY · R38 golden hour', ha='center', fontsize=10, color=CLAY, fontfamily=F, fontweight='bold')
    ax.text(x0 + (100-2*x0)/2, 6.2, 'email (cyber cell + bank)  ·  call script 1930  ·  payment stop (dispute + CFCFRMS + mandate revoke)',
            ha='center', fontsize=8.4, color=SLATE, fontfamily=F)
    ax.text(50, 1.8, 'Emergency layer sirf tabs par nahi — M1 ke HOLD card se bhi ek tap me khulta hai.', ha='center',
            fontsize=8.2, color=DIM, fontfamily=F)
    save(f, 'd8_modules.png')

# ============================================================ d9 · privacy/data flow
def d_privacy():
    f = fig(10.5, 3.3); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 30); ax.axis('off')
    ax.text(50, 27.8, 'Data kahan jaata hai — phone se bahar kya nikalta hai', ha='center',
            fontsize=11.6, color=INK, fontfamily=F, fontweight='bold')
    # device box
    rbox(ax, 4, 6.5, 44, 17.5, '#FFFFFF', SLATE, 1.6)
    ax.text(26, 21.6, 'USER KA PHONE  ·  ON-DEVICE', ha='center', fontsize=10.4, color=INK, fontfamily=F, fontweight='bold')
    ax.text(26, 18.7, '·  chat timing + payment signals\n·  screenshot OCR (opt-in)\n·  rule engine (0.012 ms)\n·  evidence pack + SHA-256',
            ha='center', va='top', fontsize=8.6, color=SLATE, fontfamily=F, linespacing=1.55)
    ax.text(26, 8.2, 'message text, OTP, PIN — device chhodte hi nahi', ha='center', fontsize=8.4,
            color=SAGE, fontfamily=F, fontweight='bold')
    # arrow
    ax.annotate('', xy=(60.5, 15), xytext=(48.5, 15),
                arrowprops=dict(arrowstyle='-|>', color=GOLD, lw=2.2, mutation_scale=17))
    ax.text(54.5, 16.4, 'sirf ye (opt-in)', ha='center', fontsize=7.8, color=GOLD, fontfamily=F, fontweight='bold')
    # server box
    rbox(ax, 61, 6.5, 35, 17.5, '#F5F0E4', SAND, 1.6)
    ax.text(78.5, 21.6, 'SERVER  ·  OPTIONAL', ha='center', fontsize=10.4, color=INK, fontfamily=F, fontweight='bold')
    ax.text(78.5, 18.7, '·  verdict code (SCAM_LIKELY…)\n·  entity ka HASH (phone number\ndomain) — plaintext nahi\n·  kuch bhi na — default OFF',
            ha='center', va='top', fontsize=8.6, color=SLATE, fontfamily=F, linespacing=1.55)
    ax.text(78.5, 8.2, 'DPDP Act 2023 aligned · purpose-limited', ha='center', fontsize=8.4,
            color=CLAY, fontfamily=F, fontweight='bold')
    ax.text(50, 3.2, 'Judges note: yahi wajah hai ki manifest me READ_SMS / Notification access / Contacts nahi hain.',
            ha='center', fontsize=8.8, color=DIM, fontfamily=F)
    save(f, 'd9_privacy.png')

# ============================================================ d10 · 7 scam families coverage
def d_families():
    f = fig(11, 3.2); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 28); ax.axis('off')
    ax.text(50, 25.9, '7 scam families — kaun module pakadta hai', ha='center',
            fontsize=11.8, color=INK, fontfamily=F, fontweight='bold')
    fam = [('Digital Arrest', 'M1 + R38', 0.86), ('Task Scam', 'M1 + M6', 0.78), ('Utility Bill', 'M4 + R26', 0.71),
           ('Sextortion', 'M1 + M2', 0.64), ('Fake Refund', 'M2 + M3', 0.57), ('APK / QR Swap', 'M3 + R22', 0.50),
           ('SIP / Trading Tip', 'M4 + registry', 0.43)]
    for i, (name, mods, conf) in enumerate(fam):
        y = 21.2 - i * 2.9
        ax.text(3, y + 0.9, name, va='center', fontsize=9.4, color=INK, fontfamily=F, fontweight='bold')
        ax.add_patch(FancyBboxPatch((24, y), 44, 1.8, boxstyle='round,pad=0.002,rounding_size=0.3',
                                    fc='#EFE7D6', ec='none', zorder=2))
        c = GOLD if conf >= 0.78 else (TEAL if conf >= 0.6 else SAGE)
        ax.add_patch(FancyBboxPatch((24, y), 44 * conf, 1.8, boxstyle='round,pad=0.002,rounding_size=0.3',
                                    fc=c, ec='none', zorder=3))
        ax.text(70, y + 0.9, f'confidence target  {int(conf*100)}%', va='center', fontsize=8.4, color=DIM, fontfamily=F)
        ax.text(24 + 44 * conf + 0.8, y + 0.9, mods, va='center', fontsize=8.6, color=CLAY, fontfamily=F, fontweight='bold')
    ax.text(50, 1.7, 'Confidence target = rule design goal (red-team se calibrate) — koi claim nahi; live numbers eval + field me.',
            ha='center', fontsize=8, color=DIM, fontfamily=F)
    save(f, 'd10_families.png')

# ============================================================ d11 · roadmap (what happens Monday)
def d_roadmap():
    f = fig(11, 3.0); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 26); ax.axis('off')
    ax.text(50, 23.9, 'Aaj ke baad kya — “Monday morning” plan', ha='center',
            fontsize=11.8, color=INK, fontfamily=F, fontweight='bold')
    ax.add_line(Line2D([6, 94], [13.2, 13.2], color=SAND, lw=3))
    phases = [('Aaj', 'working demo + evidence pack\n+ measured eval', GOLD),
              ('30 din', 'field calibration (asli labelled data)\n+ Play Store internal track', SAGE),
              ('90 din', 'Bhashini voice 12 languages\n+ SEBI registry OTA sync', TEAL),
              ('6 mahine', '2-college + 1 cyber-cell pilot\n(Patna) · SCORES/SMART-ODR draft', CLAY)]
    for i, (t, b, c) in enumerate(phases):
        x = 12 + i * 25.3
        ax.add_patch(plt.Circle((x, 13.2), 1.15, fc=BG, ec=c, lw=2.4, zorder=3))
        ax.add_patch(plt.Circle((x, 13.2), 0.55, fc=c, ec='none', zorder=4))
        ax.text(x, 16.4, t, ha='center', fontsize=9.8, color=c, fontfamily=F, fontweight='bold')
        ax.text(x, 18.9, b, ha='center', va='bottom', fontsize=8.2, color=SLATE, fontfamily=F, linespacing=1.45)
    ax.text(50, 6.4, 'Zero-cost rails hi use karenge: GitHub Pages (demo), Play internal track (app), OTA rules JSON (updates).',
            ha='center', fontsize=8.8, color=SLATE, fontfamily=F)
    ax.text(50, 4.2, 'Asli proof chahiye to hum 30 din me field accuracy dobara measure karke public karenge — ceiling nahi, sach.',
            ha='center', fontsize=8.4, color=DIM, fontfamily=F, style='italic')
    save(f, 'd11_roadmap.png')

if __name__ == '__main__':
    print('part-2 diagrams:')
    d_eval(); d_latency(); d_modules(); d_privacy(); d_families(); d_roadmap()
    print('done →', OUT)
