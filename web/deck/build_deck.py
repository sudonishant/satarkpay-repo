"""SatarkPay · deck builder — ek content model se TEEN output:
   deck/satarkpay_deck.pptx  (python-pptx)
   deck/satarkpay_deck.pdf   (HTML → chromium print)
   deck/deck.html            (browser me kholne ke liye)
Chalane ke liye:  python3 build_deck.py
Images: ../demo/screenshots/*, charts/*, diagrams/*
"""
import asyncio, pathlib, html, shutil
from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from PIL import Image
import matplotlib.font_manager  # noqa (font cache warm)

HERE = pathlib.Path(__file__).resolve().parent
SHOTS = HERE / 'screenshots'
CHARTS = HERE / 'charts'
DIAG = HERE / 'diagrams'

INK, DIM, ACC, OK, WARN, DANGER, BG, PANEL = ('#E8EEF9', '#9FB0CC', '#4CC9F0', '#3DDC97',
                                             '#FFC857', '#FF6B6B', '#0B1220', '#121C31')
def R(h): return RGBColor.from_string(h.lstrip('#'))
def hsrc(p):  # HTML <img src> — deck/ se relative (GitHub Pages / kisi bhi machine par chale)
    return pathlib.Path(p).relative_to(HERE).as_posix()

# ============================================================ CONTENT MODEL
def slide(kind, **kw): return dict(kind=kind, **kw)

DECK = [
 slide('cover', title='SatarkPay',
   sub='UPI/wallet fraud se pehle rok do — aur fraud ho jaye to 3 minute me complaint',
   meta=['SANGYAN · SEBI × NSDL × SNTC IIT (BHU) · Investor Resilience',
         'Track A (fraud/scam resilience) + Track B (grievance rights) + Track D (behaviour)',
         '2,443 registrations · 51 repos scan — 71% checker saturation; SatarkPay ke 3 axes ab bhi sabse patle',
         'Live demo: sudonishant.github.io/satarkpay-repo · 7 modules · 1 asli fraud case par chala']),

 slide('bullets', title='Problem — scam ab “padhne” ka nahi, “hone” ka hai',
   kicker='SANGYAN guardrails ke andar: koi tip nahi, koi data harvest nahi — sirf suraksha',
   bullets=['16 crore+ demat accounts · 70% naye Tier-2/3 se · F&O me 9/10 retail traders loss me (problem statement)',
            'Aaj ke scam chat-se-shuru hote hain: WhatsApp/Telegram par baat → phir UPI app → phir paisa',
            'Peer repos ka 77% kaam “message paste karo → score dekho” hai — yaani fraud ke BAAD',
            'Recovery ka golden hour pehla 60 minute hai; usme sahi complaint ho jaye to freeze-trace ka mauka bachta hai',
            'Asli case (humare evidence pack se): ₹5,000 evidence-visible loss, ₹30,625 maanga gaya, 9 scam families'],
   note='Yaani asli problem do hisse ka hai: (1) payment se PEHLE rokna, (2) fraud ke turant baad evidence + complaint.'),

 slide('image_full', title='Solution map — ek journey, saat module',
   img=str(DIAG / 'svg_journey.png'),
   note='Lane 1 interception (peer me 0%) · Lane 2 parallel shields · Lane 3 evidence + turant action (R38).'),

 slide('image_full', title='M1 · Chat-Before-Pay — matrix, block nahi',
   img=str(DIAG / 'svg_m1.png'),
   note='Saved contact ≈1 min (ya call-confirm) · unknown = 8 min guided · 10+ min chat ke baad payment = notification · officer/offer mila to T3 hold.'),

 slide('image_row', title='M1 live demo — teeno haalat',
   imgs=[str(SHOTS/'01a_saved_contact_fastpath.png'), str(SHOTS/'01b_known_12min_notification.png'), str(SHOTS/'01c_unknown_hold_callback.png')],
   captions=['Saved + 2 min chat → 1:00 fast path', 'Saved + 12 min chat → notification', 'Unknown + lambi chat → hold + callback'],
   note='Timer sirf foreground dwell naapta hai; “It\'s me” dabane se notification nahi khulta — sawal ya trusted-person confirm chahiye.'),

 slide('image_row', title='M2 · Screenshot Radar + repeat-payment guard  |  M5 · Wallet & AutoPay',
   imgs=[str(SHOTS/'02_screenshot_radar.png'), str(SHOTS/'05_wallet_autopay_audit.png')],
   captions=['30 min me 3+ payment-screenshot = amber, 5+ = red', 'Daily/unknown mandate = red, one-tap revoke'],
   note='R22: usi payee ko 24h me 3rd payment → cooling (staircase), 4th → hold. Screenshot ki image store nahi hoti — sirf entity + hash.'),

 slide('image_row', title='M3 · Domain Trust ladder  |  M4 · AI Sanchalak + registry check',
   imgs=[str(SHOTS/'03_domain_trust.png'), str(SHOTS/'08_m4_sebi_registry_negation.png')],
   captions=['L1…L4 ladder + gateway≠merchant warning', 'verdict + 3 reason + registry flag (R26)'],
   note='R26: SEBI number ka format (INZ/INH/INA + 9 digits) + official portal domain check — “registered hai” kabhi certify nahi karte. R27: bank ka apna OTP-warning scam nahi ginta. R28: PII device par hi mask.'),

 slide('image_row', title='M6 · Intel Desk (live crawler)  |  M7 · Evidence pack',
   imgs=[str(SHOTS/'06_intel_desk_live.png'), str(SHOTS/'07_auto_report_1930_pack.png')],
   captions=['784 raw → 172 relevant → 63 novel (aaj)', '28 screenshots → crop · OCR · QR · redact · hash'],
   note='M6 auto-publish nahi karta — analyst 15-second review ke baad hi registry update hota hai. M7 me NCRP text (HI+EN) + email + annexure CSV + ZIP, hashes ke saath.'),

 slide('image_full', title='R38 · Fraud CONFIRM — email + helpline + payment stop (3 kadam)',
   img=str(DIAG / 'svg_emergency.png'),
   note='User khud confirm karta hai; app draft/dialer/script ready karti hai — auto-send nahi. Golden-hour clock live chalta hai.'),

 slide('image_row', title='R38 live demo — emergency panel',
   imgs=[str(SHOTS/'11_emergency_confirm_action.png')],
   captions=['3/3 steps · email draft (cyber cell 3 IDs) · 1930 call script · payment-stop (CFCFRMS)'],
   note='Optional step 4: parivaar/mohalla ko alert (masked numbers) — taaki agla victim na bane. Ye step user chaahe to skip kare.'),

 slide('image_side', title='Measured, guess nahi — 33-message eval harness',
   img=str(CHARTS/'chart_eval.png'), side='left',
   bullets=['Wahi engine jo user ke paste par chalta hai, wahi 33 labelled messages (22 scam + 11 legit) par — precision 95% · recall 90% · F1 93%',
            'CAUTION alag bucket (soft catch / legit friction) aur “PAKA NAHI” bhi ek valid jawab — fake confidence nahi',
            'Har miss aur false-alarm table me tag hote hain; set self-authored hai (ceiling, field accuracy nahi)',
            '3 messages jaan-boojh ke rule-library ke bahar rakhe — taki honest buckets live dikhein']),

 slide('image_side', title='Competitive landscape — 51 repo scan (3 Oct 2026)',
   img=str(CHARTS/'chart_landscape.png'), side='left',
   bullets=['77% checker-saturation · 21/31 ke paas live demo hi nahi · registry check sirf 23% me',
            'SatarkPay ke 3 axes peer me 0%: interception (chat→UPI), evidence-grade recovery, measured eval harness',
            'Peer repos me licenses na ke barabar — humne zero code copy kiya, sirf ideas adopt kiye (credit: NOTICE.md)',
            'Purane risks (voice/regional/offline) bhi cover: Hindi voice, senior mode, poora demo offline chalta hai']),

 slide('table', title='Peer ideas → SatarkPay (credit + “kya nahi liya”)',
   headers=['Peer repo (public)', 'Concept', 'SatarkPay me'],
   rows=[['prakshithamalla-art/scam-shield-sangyan', 'negation-aware matching', 'R27 — bank ka OTP-warning scam nahi ginta'],
         ['adityakr-git/sangyan (“Ruko”)', 'client-side redaction + local lists', 'R28 redactPII() · R26 inline registry lists'],
         ['patelsachin9879-gif/Sangyan-Kavach + pyharshcodes', 'registry-number format validator', 'R26 sebiCheck() — format + domain + watch-list'],
         ['anuragtiwari-ux/Sangyan-Suraksha (MIT)', 'measured test set + first-hour recovery', 'R37 eval harness · R30 “pehle 1 ghante”'],
         ['surdish/scamshield-bharat · arcimillion/nivesh', 'senior mode · voice-first', 'senior mode · Hindi voice (Web Speech)'],
         ['Kshitijsawant17/PATIBIMB · Algo-explorer/Grievance-Clock', 'deterministic rules · escalation ladder', 'R31–R36 families · SCORES/SMART ODR rows']],
   note='Zero code copying (30/31 par license nahi, 1 MIT) · koi peer dataset/UI/number quote nahi kiya · pehle jaisa “paste→score” kaam copy nahi kiya — sirf gaps bhare.'),

 slide('bullets', title='Guardrails + privacy (judges ke liye)',
   kicker='SANGYAN ke disqualification rules ka seedha jawab',
   bullets=['Koi stock tip / buy-sell-hold / price prediction / trading algo nahi — R36 sirf “tip ka tareeka” ka risk batata hai',
            'Koi monetisation funnel nahi; koi tip-group promotion nahi',
            'Privacy by design: analysis se pehle client-side PII redaction (R28); demo me network call zero; OCR opt-in, image store nahi (sirf entity + hash)',
            'Koi SMS/OTP/PII harvesting nahi — UsageStats/notification sirf on-device, permission + Play-policy note ke saath (docs/)',
            'Sab verdict deterministic layer deta hai; LLM sirf samjhaata hai (production me 1 env var) — hallucination-safe',
            '“Registered hai” certify karna, ya recovery ki guarantee dena — dono se saaf inkaar (honest limits slide)']),

 slide('bullets', title='Impact → Rubric mapping (30 · 25 · 15 · 15 · 15)',
   bullets=['Resilience & Safety Impact (30): interception + evidence-recovery + registry checks; asli case par end-to-end chala (₹5,000 evidence-visible)',
            'Tier-2/3 Usability (25): Hinglish copy, Hindi voice, senior mode, offline demo, 8-min guided cooling (paisa nahi, samajh)',
            'Guardrail Compliance & Trust (15): privacy by design, no tips/no funnel, honest non-binary verdicts, credit file + limits slide',
            'Technical Execution (15): 7 modules, 66 automated checks (jsdom), live eval harness, crawler (784→172→63), chain-of-custody hashing',
            'Feasibility & Scalability (15): Android parity path (UsageStats + NotificationListener), PSP/bank/telecom partner hooks, on-device-first architecture']),

 slide('bullets', title='Roadmap — 48 ghante me demo se device tak',
   bullets=['h0–4: Kotlin/Compose skeleton — UsageStats listener + MediaStore observer + consent centre',
            'h4–12: M1 matrix + M3 domain engine ko shared module (app + console parity test)',
            'h12–20: M4 real LLM hookup (HI/EN) + library RAG + low-confidence → human queue',
            'h20–30: M5 NotificationListener se mandate parse + revoke flow (synthetic bank notifications)',
            'h30–40: M6 crawler cron + analyst console; M7 pack ko PSP/bank formats me export',
            'h40–48: Red-team (mentor scripts), Hindi copy user-test, 3-min demo rehearsal ×5',
            'Scale: on-device pehle, phir telecom (Sanchar Saathi) + PSP (UPI Help) + exchange (SCORES) hooks']),

 slide('two_col', title='Honest limits + kya hum NAHI karte',
   left_t='Honest limits', left=['Telemetry permissions ke bina features degrade hote hain (iOS par M1/M2/M5 partial)',
        'M1 “padha” nahi naap sakta — sirf “kitni der chat screen par the”',
        'Screenshot ka source-domain 100% reliable nahi — evidence treat karte hain, verdict nahi',
        'Eval set self-authored hai (ceiling); field calibration ke liye PS/bank data chahiye',
        'Recovery ki guarantee nahi — hum sirf time-optimised steps dete hain'],
   right_t='Kya NAHI karte', right=['Koi auto-block nahi — payment rukta hai, final call user ki',
        'WhatsApp automation / READ_SMS / QUERY_ALL_PACKAGES / Accessibility abuse nahi',
        'Koi stock tip, price prediction, ya “guaranteed return” messaging nahi',
        'Koi auto-send email/call nahi — draft + dialer + script dete hain, bhejna user ke tap par',
        'Peer repos se code copy nahi (licenses nahi hain) — sirf ideas, credit ke saath']),

 slide('end', title='Team SCΛMURΛI',
   sub='Nishant Kumar · Prince Singh · Kartik Singh',
   bullets=['Live demo: satarkpay-m2/demo/satarkpay_m2.html (offline, 66/66 checks pass)',
            'Evidence pack: case_evidence/ (28 screenshots, hashes, complaint drafts)',
            'Idea credit: BORROW_LIST.md · OSINT report: OSINT_REPORT.md · rules: rules_R15_R25.json + rules_R26_R38.json',
            'Crawler: scam_intel/intel_crawler.py (aaj: 784 raw → 172 relevant → 63 novel)',
            'SANGYAN submission: 4 Oct, 11:59 PM IST · demo + 3–5 min video + ye deck'],
   note='“Paisa bhejne se pehle 60 second — aur fraud ke baad 60 minute. Dono humare paas hain.”'),
]

# ============================================================ HTML → PDF
CSS = f"""
@page {{ size: 13.333in 7.5in; margin: 0; }}
* {{ box-sizing: border-box; }}
body {{ margin:0; background:{BG}; color:{INK}; font-family:'DejaVu Sans','FreeSans','Noto Sans','Nirmala UI',sans-serif; }}
.slide {{ width:13.333in; height:7.5in; padding:0.5in 0.6in 0.75in; page-break-after:always; position:relative;
          background:linear-gradient(160deg,{BG} 0%,#0e1730 100%); overflow:hidden;
          display:flex; flex-direction:column; }}
.head {{ flex:0 0 auto; }}
.body {{ flex:1 1 auto; display:flex; flex-direction:column; justify-content:center; }}
.body.top {{ justify-content:flex-start; padding-top:0.15in; }}
.kicker {{ color:{ACC}; font-size:13px; letter-spacing:.4px; margin:0 0 6px; }}
h1 {{ font-size:44px; margin:0 0 10px; line-height:1.15; }}
h2 {{ font-size:30px; margin:0 0 14px; }}
.sub {{ color:{DIM}; font-size:17px; margin:0 0 18px; }}
ul {{ margin:0; padding-left:20px; }} li {{ margin:0 0 14px; font-size:17.5px; line-height:1.4; }}
.note {{ position:absolute; left:0.6in; right:0.6in; bottom:0.28in; color:{DIM}; font-size:13px; border-top:1px solid #22304d; padding-top:8px; }}
img.full {{ width:100%; max-height:5.55in; object-fit:contain; display:block; margin:0 auto; }}
.row {{ display:flex; gap:14px; align-items:flex-start; }}
.row figure {{ margin:0; flex:1; }}
.row img {{ width:100%; max-height:5.2in; object-fit:contain; border-radius:10px; border:1px solid #22304d; }}
figcaption {{ color:{DIM}; font-size:12px; margin-top:6px; }}
.side {{ display:flex; gap:18px; align-items:center; }}
.side img {{ width:52%; border-radius:10px; border:1px solid #22304d; }}
.side ul {{ flex:1; }}
table {{ width:100%; border-collapse:collapse; font-size:15px; }}
th {{ text-align:left; color:{ACC}; border-bottom:1px solid #2c3d5f; padding:10px 9px; font-size:14px; }}
td {{ border-bottom:1px solid #1c2740; padding:10px 9px; color:{INK}; }}
td.d {{ color:{DIM}; }}
.meta {{ color:{DIM}; font-size:15px; }} .meta b {{ color:{OK}; }}
.cols {{ display:flex; gap:24px; }} .cols > div {{ flex:1; }}
.cols h3 {{ font-size:16px; color:{ACC}; margin:0 0 8px; }} .cols li {{ font-size:15.5px; margin-bottom:11px; }}
.big {{ font-size:56px; font-weight:bold; }}
"""

def render_html():
    out = [f'<!DOCTYPE html><html><head><meta charset="utf-8"><title>SatarkPay deck</title><style>{CSS}</style></head><body>']
    for s in DECK:
        k = s['kind']
        out.append('<section class="slide">')
        if k == 'cover':
            out.append(f'<h1 style="font-size:52px">{html.escape(s["title"])}</h1><p class="sub">{html.escape(s["sub"])}</p><ul class="meta">')
            out += [f'<li>{html.escape(b)}</li>' for b in s['meta']]
            out.append('</ul></section>'); continue
        out.append('<div class="head">')
        if 'title' in s: out.append(f'<h2>{html.escape(s["title"])}</h2>')
        if s.get('kicker'): out.append(f'<p class="kicker">{html.escape(s["kicker"])}</p>')
        out.append('</div><div class="body' + (' top' if k in ('bullets', 'end') else '') + '">')
        if k in ('bullets', 'end'):
            if s.get('sub'): out.append(f'<p class="sub">{html.escape(s["sub"])}</p>')
            out.append('<ul>'); out += [f'<li>{html.escape(b)}</li>' for b in s['bullets']]; out.append('</ul>')
        elif k == 'image_full':
            out.append(f'<img class="full" src="{hsrc(s["img"])}"/>')
        elif k == 'image_row':
            out.append('<div class="row">')
            for i, im in enumerate(s['imgs']):
                out.append(f'<figure><img src="{hsrc(im)}"/><figcaption>{html.escape(s["captions"][i])}</figcaption></figure>')
            out.append('</div>')
        elif k == 'image_side':
            side = s.get('side', 'left')
            img = f'<img src="{hsrc(s["img"])}"/>'
            ul = '<ul>' + ''.join(f'<li>{html.escape(b)}</li>' for b in s['bullets']) + '</ul>'
            out.append(f'<div class="side">{img + ul if side == "left" else ul + img}</div>')
        elif k == 'table':
            out.append('<table><thead><tr>' + ''.join(f'<th>{html.escape(h)}</th>' for h in s['headers']) + '</tr></thead><tbody>')
            for r in s['rows']:
                out.append('<tr>' + ''.join(f'<td{"" if i==0 else " class=d"}>{html.escape(c)}</td>' for i, c in enumerate(r)) + '</tr>')
            out.append('</tbody></table>')
        elif k == 'two_col':
            out.append(f'<div class="cols"><div><h3>{html.escape(s["left_t"])}</h3><ul>' +
                       ''.join(f'<li>{html.escape(b)}</li>' for b in s['left']) + '</ul></div><div><h3>' +
                       html.escape(s['right_t']) + '</h3><ul>' +
                       ''.join(f'<li>{html.escape(b)}</li>' for b in s['right']) + '</ul></div></div>')
        out.append('</div>')      # body band
        if s.get('note'): out.append(f'<div class="note">{html.escape(s["note"])}</div>')
        out.append('</section>')
    out.append('</body></html>')
    (HERE / 'deck.html').write_text('\n'.join(out), encoding='utf-8')

async def html_to_pdf():
    from playwright.async_api import async_playwright
    async with async_playwright() as pw:
        b = await pw.chromium.launch()
        pg = await b.new_page(viewport={'width': 1280, 'height': 720})
        await pg.goto('file://' + str(HERE / 'deck.html'))
        await pg.wait_for_timeout(1200)
        await pg.pdf(path=str(HERE / 'satarkpay_deck.pdf'), width='13.333in', height='7.5in',
                     print_background=True, margin={'top': '0', 'bottom': '0', 'left': '0', 'right': '0'})
        await b.close()

# ============================================================ PPTX
def fit(path, max_w_in, max_h_in):
    w, h = Image.open(path).size
    r = min(max_w_in / w, max_h_in / h)
    return w * r, h * r

def add_bg(sl):
    bg = sl.shapes.add_shape(1, 0, 0, Inches(13.333), Inches(7.5))
    bg.fill.solid(); bg.fill.fore_color.rgb = R(BG); bg.line.fill.background()
    bg.shadow.inherit = False
    top = sl.shapes.add_shape(1, 0, 0, Inches(13.333), Inches(0.10))
    top.fill.solid(); top.fill.fore_color.rgb = R(ACC); top.line.fill.background(); top.shadow.inherit = False

def tb(sl, x, y, w, h, runs, size=16, color=INK, bold=False, space=8):
    t = sl.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h)); tf = t.text_frame
    tf.word_wrap = True; tf.vertical_anchor = MSO_ANCHOR.TOP
    for i, item in enumerate(runs):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(space); p.line_spacing = 1.12
        txt, sz, col, bd = (item if isinstance(item, tuple) else (item, size, color, bold))
        r = p.add_run(); r.text = txt; r.font.size = Pt(sz); r.font.bold = bd
        r.font.color.rgb = R(col); r.font.name = 'Segoe UI'
    return t

def pic(sl, path, x, y, max_w, max_h):
    w, h = fit(path, max_w, max_h)
    sl.shapes.add_picture(path, Inches(x), Inches(y), Inches(w), Inches(h))
    return w, h

def build_pptx():
    prs = Presentation(); prs.slide_width = Inches(13.333); prs.slide_height = Inches(7.5)
    blank = prs.slide_layouts[6]
    for s in DECK:
        sl = prs.slides.add_slide(blank); add_bg(sl); k = s['kind']
        if k == 'cover':
            tb(sl, 0.7, 1.5, 11.9, 1.0, [('SatarkPay', 54, INK, True)])
            tb(sl, 0.7, 2.6, 11.9, 0.9, [(s['sub'], 22, ACC, False)])
            tb(sl, 0.7, 3.7, 11.9, 2.4, [(b, 14.5, DIM, False) for b in s['meta']])
            continue
        y = 0.45
        if s.get('kicker'):
            tb(sl, 0.7, y, 11.9, 0.35, [(s['kicker'], 12, ACC, False)]); y += 0.34
        tb(sl, 0.7, y, 11.9, 0.7, [(s['title'], 27, INK, True)]); y += 0.85
        if k in ('bullets', 'end'):
            tb(sl, 0.7, max(y, 1.35), 11.9, 5.3, [('•  ' + b, 17, INK if i < 4 else DIM, False) for i, b in enumerate(s['bullets'])], space=14)
        elif k == 'image_full':
            pic(sl, s['img'], 0.7, 1.35, 11.9, 5.35)
        elif k == 'image_row':
            n = len(s['imgs']); gap = 0.25; w = (11.9 - gap * (n - 1)) / n
            for i, im in enumerate(s['imgs']):
                x = 0.7 + i * (w + gap)
                _, h = pic(sl, im, x, 1.45, w, 4.9)
                tb(sl, x, 1.45 + h + 0.08, w, 0.5, [(s['captions'][i], 12.5, DIM, False)])
        elif k == 'image_side':
            side = s.get('side', 'left')
            img_x = 0.7 if side == 'left' else 6.6
            txt_x = 6.6 if side == 'left' else 0.7
            pic(sl, s['img'], img_x, 1.45, 6.05, 5.0)
            tb(sl, txt_x, 1.55, 6.05, 5.0, [('•  ' + b, 15, INK, False) for b in s['bullets']], space=15)
        elif k == 'table':
            rows, cols = len(s['rows']) + 1, len(s['headers'])
            shape = sl.shapes.add_table(rows, cols, Inches(0.7), Inches(max(y,1.35)), Inches(11.9), Inches(4.2))
            table = shape.table
            for j, h in enumerate(s['headers']):
                c = table.cell(0, j); c.text = h
                for p in c.text_frame.paragraphs:
                    for r in p.runs: r.font.size = Pt(12); r.font.bold = True; r.font.color.rgb = R(ACC); r.font.name = 'Segoe UI'
            for i, row in enumerate(s['rows'], start=1):
                for j, val in enumerate(row):
                    c = table.cell(i, j); c.text = val
                    for p in c.text_frame.paragraphs:
                        for r in p.runs:
                            r.font.size = Pt(12.5); r.font.name = 'Segoe UI'
                            r.font.color.rgb = R(INK if j == 0 else DIM)
            for j, wd in enumerate([4.1, 3.1, 4.7]):
                table.columns[j].width = Inches(wd)
        elif k == 'two_col':
            tb(sl, 0.7, 1.45, 5.8, 0.4, [(s['left_t'], 17, ACC, True)])
            tb(sl, 0.7, 1.95, 5.8, 4.6, [('•  ' + b, 15, INK, False) for b in s['left']], space=13)
            tb(sl, 6.9, 1.45, 5.8, 0.4, [(s['right_t'], 17, ACC, True)])
            tb(sl, 6.9, 1.95, 5.8, 4.6, [('•  ' + b, 15, INK, False) for b in s['right']], space=13)
        if s.get('note'):
            ln = sl.shapes.add_shape(1, Inches(0.7), Inches(6.98), Inches(11.9), Emu(9000))
            ln.fill.solid(); ln.fill.fore_color.rgb = R('#22304d'); ln.line.fill.background(); ln.shadow.inherit = False
            tb(sl, 0.7, 7.02, 11.9, 0.42, [(s['note'], 11, DIM, False)], space=0)
    prs.save(HERE / 'satarkpay_deck.pptx')

if __name__ == '__main__':
    render_html()
    build_pptx()
    asyncio.run(html_to_pdf())
    print('deck.html · satarkpay_deck.pptx · satarkpay_deck.pdf — ban gaye')
