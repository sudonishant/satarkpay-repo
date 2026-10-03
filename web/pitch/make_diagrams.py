#!/usr/bin/env python3
"""SatarkPay · pitch deck diagrams (pure Python — matplotlib).
Premium light theme: cream #FBF6EE · ink #14181F · slate #2C3E50 · gold #B08D57 · sand #E3D3B0
Run: python3 make_diagrams.py   ->  diagrams/*.png
"""
import pathlib
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch, Circle
from matplotlib.lines import Line2D

HERE = pathlib.Path(__file__).resolve().parent
OUT = HERE / 'diagrams'; OUT.mkdir(exist_ok=True)

BG, INK, SLATE, DIM = '#FBF6EE', '#14181F', '#2C3E50', '#8A94A6'
GOLD, SAND, SAGE, CLAY, TEAL = '#B08D57', '#E3D3B0', '#5F7F6F', '#B0654F', '#3E6E78'
F = 'DejaVu Sans'   # पूरा glyph coverage: ₹ ✓ ✗ →

def fig(w, h):
    f = plt.figure(figsize=(w, h), dpi=220); f.patch.set_facecolor(BG)
    return f

def save(f, name):
    p = OUT / name
    f.savefig(p, facecolor=BG, bbox_inches='tight', pad_inches=0.22)
    plt.close(f); print('  ✓', name)

def rbox(ax, x, y, w, h, fc, ec, lw=1.4, r=0.02, alpha=1.0):
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle=f'round,pad=0.004,rounding_size={r}',
                                fc=fc, ec=ec, lw=lw, alpha=alpha, zorder=3))

def arrow(ax, x1, y1, x2, y2, color=GOLD, lw=2.0, style='-|>'):
    ax.add_patch(FancyArrowPatch((x1, y1), (x2, y2), arrowstyle=style, color=color,
                                 lw=lw, mutation_scale=16, zorder=2))

# ============================================================ 1 · 60-second journey
def d_journey():
    f = fig(11, 3.4); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 30); ax.axis('off')

    steps = [
        ('9:41', 'Chat shuru',   'WhatsApp · “HR Priya”\n₹5,000/din ka lalach', SAND,  SLATE),
        ('9:53', 'UPI khulta',   '12 min baat ke baad\npayment screen',        SAND,  SLATE),
        ('9:53', 'GATE',         'M1 · chat-before-pay\nknown? kitni der?',    '#F2E4CD', GOLD),
        ('9:54', 'HOLD',         'bada paisa + unknown\n= 8 min friction\n(cancel user ke haath)', '#F2E4CD', CLAY),
        ('10:41','RECOVERY',     'fraud ho gaya to:\n1930 pack + freeze\n60 minute', '#E7EFEA', SAGE),
    ]
    W, GAP = 17.2, 3.2
    for i, (time, title, sub, fc, ec) in enumerate(steps):
        x = 2 + i * (W + GAP)
        rbox(ax, x, 8, W, 15, fc, ec, lw=2.4 if i == 2 else 1.2)
        ax.text(x + W/2, 20.2, time, ha='center', va='top', fontsize=13, color=GOLD, fontfamily=F, fontweight='bold')
        ax.text(x + W/2, 17.4, title, ha='center', va='top', fontsize=11.5, color=INK, fontfamily=F, fontweight='bold')
        ax.text(x + W/2, 14.6, sub, ha='center', va='top', fontsize=8.4, color=SLATE, fontfamily=F, linespacing=1.45)
        if i < len(steps) - 1:
            arrow(ax, x + W + 0.4, 15.5, x + W + GAP - 0.4, 15.5, GOLD, 1.7)
    ax.text(50, 27.6, 'Ek asli journey — chat se payment tak, aur fraud ke baad tak', ha='center',
            fontsize=12, color=INK, fontfamily=F, fontweight='bold')
    ax.text(50, 25.2, 'SatarkPay chat ke andar nahi jhaankta — sirf timing + payment signals padhta hai (on-device)',
            ha='center', fontsize=8.6, color=DIM, fontfamily=F)
    ax.text(2, 4.4, '60 SECOND  ·  paisa jaane se pehle', fontsize=9.5, color=CLAY, fontfamily=F, fontweight='bold')
    ax.text(98, 4.4, '60 MINUTE  ·  fraud ke baad', fontsize=9.5, color=SAGE, fontfamily=F, fontweight='bold', ha='right')
    save(f, 'd1_journey.png')

# ============================================================ 2 · 3-box architecture
def d_arch():
    f = fig(11, 3.6); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 32); ax.axis('off')
    ax.text(50, 29.6, 'Verdict deterministic hai — LLM sirf samjhata hai (kyun), decide nahi karta', ha='center',
            fontsize=12, color=INK, fontfamily=F, fontweight='bold')

    box = [
        ('1 · SIGNALS (device par)', 'chat dwell · contact known?\npayment burst · domain\nscreenshot entities', SAND, SLATE),
        ('2 · ENGINE (rules)',      'R1–R38 deterministic\nnegation-aware · registry format\nverdict + confidence', '#F2E4CD', GOLD),
        ('3 · EXPLAIN (LLM, optional)', 'Hindi/English me “kyun”\nthreat-library citation\n“pakka nahi” bhi ek jawab', '#E7EFEA', SAGE),
    ]
    W, GAP = 28, 5
    for i, (t, b, fc, ec) in enumerate(box):
        x = 2.5 + i * (W + GAP)
        rbox(ax, x, 8.5, W, 16, fc, ec, lw=2.4 if i == 1 else 1.5)
        ax.text(x + W/2, 22.2, t, ha='center', fontsize=10.6, color=INK, fontfamily=F, fontweight='bold')
        ax.text(x + W/2, 19.6, b, ha='center', va='top', fontsize=8.6, color=SLATE, fontfamily=F, linespacing=1.5)
        if i < 2:
            arrow(ax, x + W + 0.6, 16.5, x + W + GAP - 0.6, 16.5, GOLD, 1.9)
    ax.text(50, 5.6, 'Network zero · inference 0.012 ms (median, 5,000 runs) · offline demo 164 KB single file',
            ha='center', fontsize=9, color=DIM, fontfamily=F)
    save(f, 'd2_architecture.png')

# ============================================================ 3 · proof panel
def d_proof():
    f = fig(11, 3.2); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 28); ax.axis('off')
    ax.text(50, 25.4, 'Verify ho sakta hai — har number ka command repo me hai', ha='center',
            fontsize=12, color=INK, fontfamily=F, fontweight='bold')
    stats = [('66/66', 'automated checks\n(demo par, CI me bhi)', GOLD),
             ('P 95% · R 90%', '33 labelled messages\nF1 92.7%', SAGE),
             ('0.012 ms', 'classify() median\n5,000 runs', TEAL),
             ('28 files', 'asli case → SHA-256\nchain-of-custody pack', CLAY)]
    W, GAP = 21.5, 4
    for i, (big, sub, c) in enumerate(stats):
        x = 2.5 + i * (W + GAP)
        rbox(ax, x, 6, W, 15, '#FFFFFF', SAND, lw=1.3)
        ax.text(x + W/2, 16.6, big, ha='center', fontsize=17, color=c, fontfamily=F, fontweight='bold')
        ax.text(x + W/2, 13.6, sub, ha='center', va='top', fontsize=8.6, color=SLATE, fontfamily=F, linespacing=1.5)
    save(f, 'd3_proof.png')

# ============================================================ 4 · permissions (trust)
def d_perms():
    f = fig(9.5, 3.4); ax = f.add_axes([0, 0, 1, 1]); ax.set_facecolor(BG)
    ax.set_xlim(0, 100); ax.set_ylim(0, 30); ax.axis('off')
    ax.text(50, 27.4, 'Guardrail: hum wo permissions maangte hi nahi jo fraud apps maangte hain', ha='center',
            fontsize=11.4, color=INK, fontfamily=F, fontweight='bold')
    rows = [
        ('READ_SMS / SMS padhna', '✗ SatarkPay', '✓ typical scam apps'),
        ('Notification access',  '✗ SatarkPay', '✓ “wallet audit” apps'),
        ('Contacts / Gallery',   '✗ SatarkPay', '✓ “convenience” apps'),
        ('Internet + audio',     '✓ sirf ye 2 (+2 harmless)', '✓✓✓'),
    ]
    y = 21.5
    for label, ours, theirs in rows:
        ax.text(3, y, label, fontsize=9.4, color=SLATE, fontfamily=F)
        col = SAGE if ours.startswith('✗') else GOLD
        ax.text(52, y, ours, fontsize=9.4, color=col, fontfamily=F, fontweight='bold')
        ax.text(76, y, theirs, fontsize=9.0, color=CLAY, fontfamily=F)
        ax.add_line(Line2D([2, 98], [y - 1.6, y - 1.6], color='#E7DFCD', lw=0.8))
        y -= 4.4
    ax.text(52, 24.4, 'SatarkPay (manifest verified)', fontsize=8.4, color=SAGE, fontfamily=F, fontweight='bold')
    ax.text(76, 24.4, 'industry pattern', fontsize=8.4, color=CLAY, fontfamily=F, fontweight='bold')
    save(f, 'd4_permissions.png')

# ============================================================ 5 · landscape (competitive gap)
def d_landscape():
    f = fig(10.5, 3.6); ax = f.add_axes([0.30, 0.13, 0.66, 0.78]); ax.set_facecolor(BG)
    feats = ['“paste karo → score”', 'regional language', 'voice output', 'offline / on-device',
             'SEBI registry check', 'interception claim', 'SHA-256 evidence chain',
             'live demo', 'measured eval (test set)']
    pct  = [71, 71, 61, 49, 31, 27, 24, 24, 8]
    y = range(len(feats))
    cols = [TEAL if i >= 5 else SAND for i, _ in enumerate(pct)]
    ax.barh(list(y), pct, color=cols, height=0.6, edgecolor=SLATE, lw=0.5)
    for i, p in enumerate(pct):
        lbl = f'{p}%'
        if i >= 7: lbl += '  ← SatarkPay verified'
        ax.text(p + 1.6, i, lbl, va='center', color=(CLAY if i >= 7 else DIM), fontsize=8.8,
                fontweight='bold' if i >= 7 else 'normal', fontfamily=F)
    ax.set_yticks(list(y)); ax.set_yticklabels(feats, fontsize=9, color=SLATE, fontfamily=F)
    ax.set_xlim(0, 100); ax.set_xticks([0, 25, 50, 75, 100])
    ax.set_xticklabels(['0', '25', '50', '75', '100%'], fontsize=8.5, color=DIM, fontfamily=F)
    for s in ax.spines.values(): s.set_color('#E7DFCD')
    ax.grid(axis='x', color='#EFE7D6', lw=0.7); ax.set_axisbelow(True)
    ax.set_title('51-repo scan (3 Oct 2026): jo sab karte hain vs jo koi nahi karta', color=INK,
                 fontsize=10.5, pad=9, fontfamily=F, loc='left')
    save(f, 'd5_landscape.png')

if __name__ == '__main__':
    print('diagrams ban rahe hain:')
    d_journey(); d_arch(); d_proof(); d_perms(); d_landscape()
    print('done →', OUT)
