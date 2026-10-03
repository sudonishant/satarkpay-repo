"""SatarkPay · diagram generator (dark SVG) → diagrams/*.svg + *.png (chromium se render)
Chalane ke liye: python3 build_diagrams.py
"""
import asyncio, pathlib, html

HERE = pathlib.Path(__file__).parent
OUT = HERE / 'diagrams'; OUT.mkdir(exist_ok=True)

BG, PANEL, PANEL2 = '#0B1220', '#121C31', '#16223a'
INK, DIM, ACC, OK, WARN, DANGER = '#E8EEF9', '#9FB0CC', '#4CC9F0', '#3DDC97', '#FFC857', '#FF6B6B'
FONT = "'DejaVu Sans', 'Noto Sans', system-ui, sans-serif"

def svg_open(w, h, title=''):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">'
            f'<rect width="{w}" height="{h}" fill="{BG}"/>'
            f'<style>text{{font-family:{FONT};}} .t{{fill:{INK}}} .d{{fill:{DIM}}} .a{{fill:{ACC}}}'
            f' .k{{fill:{OK}}} .w{{fill:{WARN}}} .r{{fill:{DANGER}}}</style>'
            + (f'<text x="26" y="40" class="t" font-size="22" font-weight="bold">{title}</text>' if title else ''))

def box(x, y, w, h, title, lines=(), stroke=ACC, fill=PANEL, tcol=INK, tsize=15, lsize=11.5, radius=12):
    o = (f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{radius}" fill="{fill}" stroke="{stroke}" stroke-width="1.6"/>'
         f'<text x="{x+14}" y="{y+26}" fill="{tcol}" font-size="{tsize}" font-weight="bold">{html.escape(title)}</text>')
    for i, ln in enumerate(lines):
        o += f'<text x="{x+14}" y="{y+48+i*17}" class="d" font-size="{lsize}">{html.escape(ln)}</text>'
    return o

def arrow(x1, y1, x2, y2, col=DIM, dash='', width=1.8, label='', lsize=11):
    d = f' stroke-dasharray="{dash}"' if dash else ''
    mid = ((x1 + x2) // 2, (y1 + y2) // 2)
    o = (f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{col}" stroke-width="{width}"{d} '
         f'marker-end="url(#ah)"/>')
    if label:
        o += (f'<text x="{mid[0]}" y="{mid[1]-6}" fill="{col}" font-size="{lsize}" text-anchor="middle">{html.escape(label)}</text>')
    return o

DEFS = (f'<defs><marker id="ah" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
        f'<path d="M 0 0 L 10 5 L 0 10 z" fill="{DIM}"/></marker>'
        f'<marker id="ahA" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
        f'<path d="M 0 0 L 10 5 L 0 10 z" fill="{ACC}"/></marker></defs>')

# ---------------------------------------------------------------- diagram 1 · journey
def d_journey():
    W, H = 1420, 720
    o = svg_open(W, H, 'SatarkPay — chat se payment tak, aur fraud ke baad: 7 modules + R38') + DEFS
    # lane labels
    o += f'<text x="26" y="80" class="d" font-size="12">LANE 1 · PRE-PAYMENT (interception — peer me 0%)</text>'
    o += f'<text x="26" y="330" class="d" font-size="12">LANE 2 · SAATH-SAATH CHALNE WALE SHIELDS (parallel guards)</text>'
    o += f'<text x="26" y="555" class="d" font-size="12">LANE 3 · FRAUD KE BAAD (evidence + turant action)</text>'
    y1 = 100
    b = box(30, y1, 170, 96, 'WhatsApp / Telegram', ['chat (naya ya purana', 'contact · group)'], stroke='#3b4a67')
    b += arrow(200, y1 + 48, 236, y1 + 48, ACC, label='')
    b += box(240, y1, 170, 96, 'Chat session timer', ['UsageStats dwell', '(foreground, on-device)'], stroke='#3b4a67')
    b += arrow(410, y1 + 48, 446, y1 + 48, ACC)
    b += box(450, y1, 170, 96, 'UPI app khula', ['payment screen / intent', '≤5 min gap'], stroke='#3b4a67')
    b += arrow(620, y1 + 48, 656, y1 + 48, ACC)
    b += box(660, y1, 250, 96, 'M1 · Chat-Before-Pay Gate', ['contact × session-length matrix', '4 cells → T1 / T2 / T3'], stroke=ACC, fill=PANEL2)
    # T tiers
    b += arrow(910, y1 + 48, 946, y1 + 48, ACC)
    b += box(950, y1 - 34, 360, 74, 'T1 · FAST PATH (saved + chhoti chat)', ['1 min check · ya 1-tap call-confirm → pay'], stroke=OK)
    b += box(950, y1 + 46, 360, 74, 'T2 · COOLING (unknown + chhoti chat)', ['8 min guided: chat dobara padho + 3 sawal'], stroke=WARN)
    b += box(950, y1 + 126, 360, 74, 'T3 · HOLD (unknown + lambi chat / officer)', ['analyst callback ≤5 min · rule R1 attach'], stroke=DANGER)
    o += b
    # lane 2 cards
    y2 = 350
    o += (box(30, y2, 240, 150, 'M2 · Screenshot Radar', ['payment screenshot ka burst', '30 min me 3+ → amber, 5+ → red', 'R22: same payee 3rd txn → cooling']))
    o += (box(282, y2, 240, 150, 'M3 · Domain Trust', ['L1 verified … L4 fake lookalike', 'gateway achha ≠ merchant achha', 'R26: SEBI/NSDL registry check']))
    o += (box(534, y2, 240, 150, 'M4 · AI Sanchalak', ['paste karo → verdict + 3 reasons', 'R27 negation · R28 redaction', 'Hindi voice · senior mode · R37 eval']))
    o += (box(786, y2, 240, 150, 'M5 · Wallet / AutoPay', ['connected apps + mandates', 'daily/unknown mandate = red', 'one-tap revoke']))
    o += (box(1038, y2, 242, 150, 'M6 · Intel Desk (live)', ['crawler: naye scam patterns', 'analyst 15-sec review queue', 'registry push → app OTA']))
    # lane 3
    y3 = 575
    o += (box(30, y3, 250, 120, 'M7 · Auto-Report', ['crop · OCR · QR · redact · hash', 'NCRP text (HI/EN) + annexure + ZIP'], stroke=WARN))
    o += arrow(280, y3 + 60, 320, y3 + 60, WARN)
    o += (box(324, y3, 300, 120, 'R38 · Fraud CONFIRM', ['📧 cyber-cell + bank email draft', '📞 1930 / bank dialer + call script', '⛔ payment-stop request (CFCFRMS)'], stroke=DANGER, fill=PANEL2))
    o += arrow(624, y3 + 60, 664, y3 + 60, DANGER)
    o += (box(668, y3, 250, 120, 'Helpline + bank', ['1930 golden hour · freeze', 'dispute/chargeback · UTR'], stroke=DANGER))
    o += arrow(918, y3 + 60, 958, y3 + 60, WARN)
    o += (box(962, y3, 380, 120, 'SCORES · SMART ODR · Sanchar Saathi', ['investment angle → SEBI SCORES', 'number/SMS → Sanchar Saathi', 'optional: parivaar ko alert'], stroke=WARN))
    # feedback loop
    o += arrow(1150, y3 - 8, 1180, y2 + 152, ACC, dash='6 6')
    o += f'<text x="1132" y="{(y3+y2+152)//2}" class="a" font-size="11">naya pattern → Intel Desk</text>'
    o += '</svg>'
    (OUT / 'svg_journey.svg').write_text(o, encoding='utf-8')

# ---------------------------------------------------------------- diagram 2 · M1 matrix
def d_m1():
    W, H = 1250, 640
    o = svg_open(W, H, 'M1 · Chat-Before-Pay matrix — 4 cells, koi auto-block nahi') + DEFS
    x0, y0, cw, chh = 320, 150, 444, 185
    # column headers
    o += f'<text x="{x0+cw//2}" y="{y0-16}" class="d" font-size="13" text-anchor="middle">chhoti chat (&lt; 8 min)</text>'
    o += f'<text x="{x0+cw+cw//2}" y="{y0-16}" class="d" font-size="13" text-anchor="middle">lambi chat (≥ 8–10 min)</text>'
    o += f'<text x="{x0-14}" y="{y0+chh//2+5}" class="d" font-size="13" text-anchor="end">SAVED contact</text>'
    o += f'<text x="{x0-14}" y="{y0+chh+chh//2+5}" class="d" font-size="13" text-anchor="end">UNKNOWN number</text>'
    o += box(x0, y0, cw, chh, 'T1 · FAST PATH', ['“Saved contact — 1 min check, phir pay”',
        'ya 1-tap: saved number par call-confirm → allow', '',
        'User ke paas: Direct pay · Call-confirm · Cancel'], stroke=OK)
    o += box(x0+cw, y0, cw, chh, 'T1 · NOTIFICATION (3 min)',
        ['“10+ min chat ke baad payment app khula hai —', 'kya koi aapko paisa bhejne ko keh raha hai?”', '',
         'saved number par bhi — account hack + pressure'], stroke=WARN)
    o += box(x0, y0+chh, cw, chh, 'T2 · COOLING (8 min)',
        ['Unknown ko samajhne me time lagta hai:', 'chat dobara padho + 3 sawal', '',
         'Q1: khud bhej rahe ho (kisi ne nahi kaha)?', 'Q2: kisi officer/offer ne kaha?  Q3: live call?'], stroke=WARN)
    o += box(x0+cw, y0+chh, cw, chh, 'T3 · HOLD + CALLBACK',
        ['unknown + lambi chat + turant payment', '= digital-arrest / task-script ka classic shape', '',
         'analyst callback ≤5 min · rule R1 attach'], stroke=DANGER, fill=PANEL2)
    o += box(22, y0, 286, 185, 'Timer kaise chalta hai', ['· sirf foreground dwell (chat screen)', '· app chhod ke 5 s me wapas = credit nahi',
        '· fast path bhi 24h me ek baar (per-payee)', '· “It\'s me” se notification nahi khulta'], lsize=11, stroke='#3b4a67')
    o += box(22, y0+chh+20, 286, 230, 'Kya naya hai (peer me 0%)',
        ['peers 77% “paste msg → score” karte', 'hain — yaani fraud ho jaane ke BAAD.', '',
         'SatarkPay payment se PEHLE intercept', 'karta hai: app-switch + chat-length +', 'contact-tier — sab on-device.', '',
         'Block kuch nahi hota: payment rukta hai,', 'final call user ki.'], lsize=11, stroke=ACC)
    o += '</svg>'
    (OUT / 'svg_m1.svg').write_text(o, encoding='utf-8')

# ---------------------------------------------------------------- diagram 3 · R38 emergency
def d_emergency():
    W, H = 1180, 620
    o = svg_open(W, H, 'R38 · Fraud confirm → 3 kadam (golden hour)') + DEFS
    o += box(30, 110, 230, 120, 'User: “CONFIRM hai”', ['koi auto-detect nahi —', 'user khud confirm karta hai'], stroke=DANGER, fill=PANEL2)
    o += arrow(260, 170, 310, 170, DANGER)
    o += box(314, 100, 210, 140, 'M7 pack ready', ['crop + redact + hash', 'annexure + txn/UTR'], stroke=WARN)
    o += arrow(524, 170, 574, 170, WARN)
    o += box(578, 90, 250, 160, '1 · 📧 Email (1 tap)', ['cyber cell 3 IDs + bank nodal cc', 'subject/body ready (txn, UTR, ₹, annexure)', 'auto-send NAHI — aapka mail app khulta hai'], stroke=ACC)
    o += box(578, 270, 250, 160, '2 · 📞 Helpline call', ['1930 · 1909 · bank/PSP · Bihar cyber cell', '5-line call script screen par', 'golden hour me freeze request isi call par'], stroke=ACC)
    o += box(578, 450, 250, 140, '3 · ⛔ Payment stop', ['txn dispute/chargeback', 'beneficiary par hold (CFCFRMS)', 'account freeze/limit + mandate revoke'], stroke=ACC)
    o += arrow(828, 170, 878, 170, DANGER)
    o += box(882, 90, 270, 500, 'Nateeja (target)', ['· complaint number same din', '· freeze request 10–60 min ke andar', '',
        '· evidence chain-of-custody (SHA-256)', '· agla victim nahi: family/community', '  alert (optional step 4)', '',
        '· NCRP + SCORES / SMART ODR follow-up', '  (investment angle ho to)', '',
        'Honest: recovery guarantee nahi —', 'humein sirf time-optimised steps dete hain.'], stroke=OK, fill=PANEL2)
    o += f'<text x="30" y="300" class="d" font-size="12">Golden-hour clock user ke confirm karne ke waqt se chalta hai — demo me live dikhta hai.</text>'
    o += f'<text x="30" y="324" class="d" font-size="12">Email/call/stop — teeno user ke tap par; app khud nahi bhejti/call karti (privacy).</text>'
    o += '</svg>'
    (OUT / 'svg_emergency.svg').write_text(o, encoding='utf-8')

async def render():
    from playwright.async_api import async_playwright
    async with async_playwright() as pw:
        b = await pw.chromium.launch()
        for f in sorted(OUT.glob('svg_*.svg')):
            import re
            txt = f.read_text(encoding='utf-8')
            w = int(re.search(r'width="(\d+)"', txt).group(1)); h = int(re.search(r'height="(\d+)"', txt).group(1))
            pg = await b.new_page(viewport={'width': w, 'height': h}, device_scale_factor=2)
            await pg.goto('file://' + str(f))
            await pg.wait_for_timeout(250)
            await pg.screenshot(path=str(f.with_suffix('.png')))
            await pg.close(); print('rendered', f.with_suffix('.png').name)
        await b.close()

if __name__ == '__main__':
    d_journey(); d_m1(); d_emergency()
    asyncio.run(render())
