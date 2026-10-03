"""SatarkPay · deck charts (dark theme) → charts/*.png
Chalane ke liye:  python3 build_charts.py
Numbers ka source: demo eval harness (33 messages, live chalaya gaya) + sangyan_osint/repo_matrix.csv (31 repos).
"""
import json, pathlib
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

BG, PANEL, INK, DIM = '#0B1220', '#121C31', '#E8EEF9', '#9FB0CC'
ACC, OK, WARN, DANGER = '#4CC9F0', '#3DDC97', '#FFC857', '#FF6B6B'

HERE = pathlib.Path(__file__).parent
OUT = HERE / 'charts'; OUT.mkdir(exist_ok=True)
plt.rcParams.update({'font.family': 'DejaVu Sans', 'text.color': INK,
                     'axes.labelcolor': INK, 'xtick.color': DIM, 'ytick.color': DIM})

def fig(w, h):
    f = plt.figure(figsize=(w, h), dpi=200)
    f.patch.set_facecolor(BG)
    return f

def save(f, name):
    f.savefig(OUT / name, facecolor=BG, bbox_inches='tight', pad_inches=0.25)
    plt.close(f); print('saved', name)

# ---------------------------------------------------------------- 1 · eval confusion matrix
def chart_eval():
    f = fig(6.4, 5.0)
    ax = f.add_axes([0.12, 0.16, 0.56, 0.72]); ax.set_facecolor(PANEL)
    m = [[19, 2], [1, 9]]          # rows: asli scam / asli legit ; cols: engine SCAM LIKELY / engine SAFE
    cols, rows = ['engine: SCAM LIKELY', 'engine: SAFE / CAUTION'], ['asli: scam (22)', 'asli: legit (11)']
    ax.imshow(m, cmap=matplotlib.colors.LinearSegmentedColormap.from_list('sp', [PANEL, '#1b3a5c', ACC]))
    for i in range(2):
        for j in range(2):
            good = (i == j)
            ax.text(j, i, str(m[i][j]), ha='center', va='center', fontsize=26,
                    color=(('#08301f' if i == j == 0 else OK) if good else DANGER), fontweight='bold')
            # i=0 -> asli scam ; i=1 -> asli legit
            tag = {0: 'true positive', 1: 'MISS (FN)'}.get(j) if i == 0 else {0: 'false alarm', 1: 'sahi chhoda'}.get(j)
            ax.text(j, i + 0.34, tag, ha='center', va='center', fontsize=9,
                    color=('#08301f' if i == j == 0 else DIM), fontweight=('bold' if i == j == 0 else 'normal'))
    ax.set_xticks([0, 1]); ax.set_xticklabels(cols, fontsize=10)
    ax.set_yticks([0, 1]); ax.set_yticklabels(rows, fontsize=10)
    ax.set_xticks([], minor=True); ax.tick_params(length=0)
    for s in ax.spines.values(): s.set_color('#22304d')
    ax.set_title('33-message labelled set · live confusion', color=INK, fontsize=12, pad=12)

    bx = f.add_axes([0.72, 0.16, 0.26, 0.72]); bx.set_facecolor(BG); bx.axis('off')
    bx.text(0.0, 1.0, 'buckets jab “paka nahi”', color=DIM, fontsize=9.5, transform=bx.transAxes)
    bars = [('CAUTION · scam (soft)', 1, WARN), ('CAUTION · legit (friction)', 1, WARN),
            ('UNCERTAIN · scam (guess nahi)', 2, DIM), ('legit · SEEMS OK', 2, OK)]
    y = 0.84
    for lab, v, c in bars:
        bx.add_patch(FancyBboxPatch((0.0, y - 0.055), 0.86 * v / 2, 0.085, transform=bx.transAxes,
                                    boxstyle='round,pad=0.004', fc=c, ec='none', alpha=0.85))
        bx.text(0.0, y - 0.1, f'{lab} — {v}', color=INK, fontsize=9, transform=bx.transAxes)
        y -= 0.19
    bx.text(0.0, y - 0.02, 'P 95% · R 90% · F1 93%\n(self-authored set = ceiling,\nfield accuracy nahi)', color=ACC,
            fontsize=11.5, fontweight='bold', transform=bx.transAxes, va='top')
    save(f, 'chart_eval.png')

# ---------------------------------------------------------------- 2 · peer landscape
def chart_landscape():
    feats = ['checker ("paste msg → score")', 'regional language', 'voice output', 'LLM explanation',
             'live demo hai', 'offline / on-device', 'SEBI registry check', 'interception (chat→UPI)',
             'evidence-grade recovery', 'measured eval harness']
    pct = [77, 71, 52, 35, 32, 26, 23, 0, 0, 0]      # % of 31 peer repos
    f = fig(7.2, 4.6)
    ax = f.add_axes([0.30, 0.10, 0.66, 0.84]); ax.set_facecolor(BG)
    y = range(len(feats))
    cols = [ACC if p == 0 else '#3b4a67' for p in pct]
    ax.barh(list(y), pct, color=cols, height=0.62)
    for i, p in enumerate(pct):
        ax.text(p + 1.5, i, (f'{p}%' if p else 'SatarkPay — peer me 0%'), va='center',
                color=(ACC if p == 0 else DIM), fontsize=10, fontweight='bold' if p == 0 else 'normal')
    ax.set_yticks(list(y)); ax.set_yticklabels(feats, fontsize=10.5)
    ax.set_xlim(0, 100); ax.set_xticks([0, 25, 50, 75, 100]); ax.set_xticklabels(['0', '25', '50', '75', '100%'])
    for s in ax.spines.values(): s.set_color('#22304d')
    ax.set_title('31 peer repos (SANGYAN landscape, 3 Oct) me feature coverage', color=INK, fontsize=12, pad=10)
    ax.grid(axis='x', color='#1c2740', lw=0.7)
    ax.set_axisbelow(True)
    save(f, 'chart_landscape.png')

if __name__ == '__main__':
    chart_eval(); chart_landscape()
