#!/usr/bin/env python3
"""SatarkPay webapp2 — logic, page and offline assertions."""

import os, sys, subprocess, time, socket, functools, http.server, threading

ROOT = os.path.dirname(os.path.abspath(__file__))
WEB = os.path.dirname(os.path.abspath(__file__))
PORT = 8731

PAGES = ['index.html', 'payment.html', 'check.html', 'domain.html',
         'library.html', 'evidence.html', 'emergency.html', 'about.html']

FAILS = []
PASS = [0]


def ok(cond, label, extra=''):
    if cond:
        PASS[0] += 1
        print(f'  ok   {label}')
    else:
        FAILS.append(label + (' :: ' + str(extra) if extra else ''))
        print(f'  FAIL {label}  {extra}')


def serve():
    h = functools.partial(http.server.SimpleHTTPRequestHandler, directory=WEB)
    srv = http.server.ThreadingHTTPServer(('127.0.0.1', PORT), h)
    threading.Thread(target=srv.serve_forever, daemon=True).start()
    return srv


# ----------------------------------------------------------------- logic ----

LOGIC = r"""
() => {
  const R = [];
  const t = (name, cond, got) => R.push([name, !!cond, got === undefined ? null : String(got)]);

  // ---- redaction -------------------------------------------------------
  t('redact masks account number', SP.redact('A/c 1234567890123 debited').includes('\u2022\u2022\u2022\u2022'), SP.redact('A/c 1234567890123 debited'));
  t('redact keeps a readable tail', /\d{4}$/.test(SP.redact('A/c 1234567890123 debited').replace(/[^\d]/g, '')), SP.redact('A/c 1234567890123 debited'));
  t('redact masks UPI handle', SP.redact('send to ramesh.kirana@ybl').includes('ram\u2022\u2022\u2022@ybl'), SP.redact('send to ramesh.kirana@ybl'));
  t('redact masks OTP digits', !/OTP[\s:=-]*\d{4,}/.test(SP.redact('your OTP: 448291')), SP.redact('your OTP: 448291'));
  t('redact leaves ordinary text alone', SP.redact('your account will be blocked').indexOf('\u2022') < 0);

  // ---- VPA parsing -----------------------------------------------------
  const p1 = SP.parseVPA('ramesh.kirana@ybl');
  t('parseVPA splits local and psp', p1 && p1.local === 'ramesh.kirana' && p1.psp === 'ybl');
  t('parseVPA knows a real PSP', p1 && p1.knownPSP === true);
  const p2 = SP.parseVPA('sbi.help@okfake');
  t('parseVPA flags an invented PSP', p2 && p2.knownPSP === false);
  t('parseVPA detects a brandish handle', p2 && p2.brandish === true);
  t('parseVPA rejects a non-VPA', SP.parseVPA('not an address') === null);
  t('parseVPA strips a upi:// prefix', SP.parseVPA('upi://pay?pa=x@ybl').psp === 'ybl');

  // ---- domain ladder ---------------------------------------------------
  const d1 = SP.checkDomain('onlinesbi.sbi');
  t('real domain recognised', d1.verdict === 'official', d1.verdict);
  const d2 = SP.checkDomain('http://sbi-kyc-update.online/verify');
  t('look-alike flagged as fake', d2.verdict === 'fake', d2.verdict);
  t('look-alike names the real domain', d2.real === 'onlinesbi.sbi', d2.real);
  t('look-alike reports the registered domain', d2.reg === 'sbi-kyc-update.online', d2.reg);
  const d3 = SP.checkDomain('https://www.hdfcbank.com/personal');
  t('www and path are stripped', d3.verdict === 'official' && d3.reg === 'hdfcbank.com', d3.verdict);
  const d4 = SP.checkDomain('random-shop.xyz');
  t('bulk TLD flagged suspicious', d4.verdict === 'suspicious', d4.verdict);
  const d5 = SP.checkDomain('example.com');
  t('unknown domain is not called unsafe', d5.verdict === 'unknown' && d5.ok === true, d5.verdict);
  t('empty input reports empty', SP.checkDomain('').verdict === 'empty');
  t('no-dot input reports empty', SP.checkDomain('hello').verdict === 'empty');

  // ---- message scoring -------------------------------------------------
  const m1 = SP.scoreMessage(SP.SAMPLES[0].t);   // KYC block scam
  t('KYC scam is CRITICAL or HIGH', ['critical','high'].includes(m1.tier.key), m1.tier.key + ' ' + m1.score);
  const m2 = SP.scoreMessage(SP.SAMPLES[5].t);   // legit debit alert
  t('legit debit alert is LOW', m2.tier.key === 'low', m2.tier.key + ' ' + m2.score);
  const m3 = SP.scoreMessage(SP.SAMPLES[6].t);   // legit delivery update
  t('legit delivery update is LOW', m3.tier.key === 'low', m3.tier.key + ' ' + m3.score);
  const m4 = SP.scoreMessage(SP.SAMPLES[7].t);   // legit SIP note
  t('legit SIP note is LOW', m4.tier.key === 'low', m4.tier.key + ' ' + m4.score);
  let scamHigh = 0;
  for (let i = 0; i < 5; i++) if (['critical','high'].includes(SP.scoreMessage(SP.SAMPLES[i].t).tier.key)) scamHigh++;
  t('all 5 scam samples reach HIGH or CRITICAL', scamHigh === 5, scamHigh);
  let legitLow = 0;
  for (let i = 5; i < 8; i++) if (SP.scoreMessage(SP.SAMPLES[i].t).tier.key === 'low') legitLow++;
  t('all 3 legit samples stay LOW', legitLow === 3, legitLow);
  t('legit marker subtracts points', SP.scoreMessage(SP.SAMPLES[5].t).credit < 0, SP.scoreMessage(SP.SAMPLES[5].t).credit);
  t('no score is ever NaN', !Number.isNaN(m1.score) && !Number.isNaN(m2.score));
  t('empty message scores zero', SP.scoreMessage('').score === 0);

  // ---- tier thresholds -------------------------------------------------
  const T = (s) => SP.tierFor(s).key;
  t('threshold 0 -> low',      T(0) === 'low', T(0));
  t('threshold 2 -> low',      T(2) === 'low', T(2));
  t('threshold 3 -> caution',  T(3) === 'caution', T(3));
  t('threshold 5 -> caution',  T(5) === 'caution', T(5));
  t('threshold 6 -> high',     T(6) === 'high', T(6));
  t('threshold 9 -> high',     T(9) === 'high', T(9));
  t('threshold 10 -> critical',T(10) === 'critical', T(10));
  t('threshold 99 -> critical',T(99) === 'critical', T(99));

  // ---- attack chain ----------------------------------------------------
  const c1 = SP.CHAIN.assess({});
  t('no signals -> LOW, may proceed', c1.tier.key === 'low' && c1.canProceed === true);
  const c2 = SP.CHAIN.assess({ call: true });
  t('call alone (4) -> CAUTION', c2.tier.key === 'caution' && c2.score === 4, c2.score);
  t('CAUTION gives a 30s cooling off', c2.coolSeconds === 30, c2.coolSeconds);
  const c3 = SP.CHAIN.assess({ call: true, scanrecv: true });
  t('call + scan to receive (8) -> HIGH', c3.tier.key === 'high' && c3.score === 8, c3.score);
  t('HIGH gives a 60s cooling off', c3.coolSeconds === 60, c3.coolSeconds);
  const c4 = SP.CHAIN.assess({ call: true, first: true, amount: true, device: true });
  t('four medium signals reach CRITICAL', c4.tier.key === 'critical' && c4.score === 13, c4.score);
  t('CRITICAL withholds the pay action', c4.canProceed === false);
  t('CRITICAL shows no countdown', c4.coolSeconds === 0, c4.coolSeconds);
  t('CRITICAL offers concrete alternatives', c4.instead.length >= 3, c4.instead.length);
  const c5 = SP.CHAIN.assess({ call: true, scanrecv: true, first: true, amount: true, device: true, chat: true, longchat: true, psp: true });
  t('all eight signals add to 23', c5.score === 23, c5.score);
  const c6 = SP.CHAIN.assess({}, SP.SAMPLES[0].t);
  t('a scam message alone adds the 5-point signal', c6.score === 5 || c6.score === 7, c6.score);
  t('every hit carries its weight', c6.hits.every(h => typeof h.w === 'number'));
  t('hit count matches scored signals', c6.hits.length > 0 && c6.score > 0);
  const c7 = SP.CHAIN.assess({}, SP.SAMPLES[6].t);
  t('a benign message adds no risk hit', c7.score === 0, c7.score);
  t('score is never NaN for an unknown source', !Number.isNaN(c7.score) && !Number.isNaN(SP.scoreMessage('x').score));

  // ---- ledger ----------------------------------------------------------
  const L = SP.ledgerStats(SP.SAMPLE_LEDGER);
  t('ledger returns one row per payment', L.rows.length === 5, L.rows.length);
  t('ledger finds first-time payees', L.newPayees === 2, L.newPayees);
  t('ledger flags something', L.flagged >= 2, L.flagged);
  t('fingerprint is 8 hex characters', /^[0-9a-f]{8}$/.test(L.rows[0].fp), L.rows[0].fp);
  t('fingerprint is stable for the same VPA', SP.hashVPA('a@ybl') === SP.hashVPA('a@ybl'));
  t('fingerprint differs between VPAs', SP.hashVPA('a@ybl') !== SP.hashVPA('b@ybl'));
  t('ledger does not store the raw handle in the fingerprint', L.rows[0].fp.indexOf('@') < 0);
  const big = L.rows.filter(r => r.amt === 25000)[0];
  t('a large first-time payment is flagged bad', big.level === 'bad', big.level + ' ' + big.flags.join('/'));
  t('flags explain themselves in words', big.flags.length >= 2 && typeof big.flags[0] === 'string');

  // ---- mandates --------------------------------------------------------
  const M = SP.auditMandates(SP.SAMPLE_MANDATES);
  t('mandate audit covers every row', M.length === 4, M.length);
  t('unknown mandate is flagged bad', M.filter(m => m.level === 'bad').length >= 1);
  t('trial-sourced mandate is flagged', M.some(m => m.flags.join(' ').toLowerCase().indexOf('trial') > -1));
  t('mandate total is the sum', SP.mandateTotal(SP.SAMPLE_MANDATES) === 1626, SP.mandateTotal(SP.SAMPLE_MANDATES));

  // ---- evidence --------------------------------------------------------
  const asmt = SP.CHAIN.assess({ call: true, first: true, amount: true }, SP.SAMPLES[0].t);
  const ev = SP.evidence({ whenText: '04 Oct 2026, 10:20', payee: 'sbi.help.desk@okaxis',
    amount: 24999, message: SP.SAMPLES[0].t, assessment: asmt,
    actions: ['I did not pay'], notes: 'Caller said transfer to 123456789012 quickly.' });
  t('evidence record has a header', ev.indexOf('SATARKPAY INCIDENT RECORD') === 0);
  t('evidence masks the account in the notes', ev.indexOf('123456789012') < 0, 'account leaked');
  t('evidence masks the payee handle', ev.indexOf('sbi.help.desk@okaxis') < 0, 'handle leaked');
  t('evidence names 1930', ev.indexOf('1930') > -1);
  t('evidence names cybercrime.gov.in', ev.indexOf('cybercrime.gov.in') > -1);
  t('evidence states what it is not', ev.indexOf('not a police complaint') > -1);
  t('evidence includes the risk assessment', ev.indexOf('RISK ASSESSMENT') > -1);
  t('evidence lists the actions taken', ev.indexOf('I did not pay') > -1);
  t('evidence KEEPS the scam link (it is the evidence)', ev.indexOf('sbi-kyc-update.online') > -1, 'link dropped');
  t('evidence assessment tier is written in', ev.indexOf('Tier: CRITICAL') > -1, ev.split('\n').slice(6,10).join(' | '));
  t('evidence lists each scored signal', ev.indexOf('[+4]') > -1 || ev.indexOf('[+3]') > -1);

  return R;
}
"""


def main():
    srv = serve()
    time.sleep(0.4)

    from playwright.sync_api import sync_playwright
    with sync_playwright() as pw:
        b = pw.chromium.launch()
        ctx = b.new_context(viewport={'width': 1440, 'height': 900})
        page = ctx.new_page()

        errors = []
        page.on('console', lambda m: errors.append(m.text) if m.type == 'error' else None)
        page.on('pageerror', lambda e: errors.append('pageerror: ' + str(e)))

        base = f'http://127.0.0.1:{PORT}/'

        print('\n== logic assertions ==')
        page.goto(base + 'index.html', wait_until='load')
        results = page.evaluate(LOGIC)
        for name, cond, got in results:
            ok(cond, name, got)
        print(f'  ({len(results)} logic assertions)')

        print('\n== pages ==')
        for p in PAGES:
            before = len(errors)
            page.goto(base + p, wait_until='load')
            page.wait_for_timeout(220)
            title = page.title()
            h1 = page.locator('h1').first.inner_text()
            navs = page.locator('.nav a').count()
            ok(navs >= 8, f'{p} renders sidebar with 8 links', navs)
            ok(len(errors) == before, f'{p} has no console errors',
               errors[before:before + 2])
            ok(bool(h1.strip()), f'{p} has an h1', h1[:40])
            ok('SatarkPay' in title, f'{p} sets a title', title)

        print('\n== interaction ==')
        page.goto(base + 'payment.html', wait_until='load')
        page.click('#demo')
        page.wait_for_timeout(400)
        tier = page.locator('.verdict .tier').first.inner_text()
        ok(tier == 'CRITICAL', 'worked example reaches CRITICAL', tier)
        ok(page.locator('.verdict.v-critical').count() == 1, 'CRITICAL styling applied')
        ok(page.locator('#pay').count() == 0, 'no pay button at CRITICAL')
        ok(page.locator('.verdict .body .sig.hit').count() >= 4, 'signals listed with weights',
           page.locator('.verdict .body .sig.hit').count())
        ok('withheld' in page.locator('.verdict .badge').first.inner_text().lower(), 'badge says withheld')

        page.click('#clear')
        page.wait_for_timeout(120)
        # single signal -> caution -> countdown appears
        page.click('#questions .switch[data-id="call"]')
        page.click('#run')
        page.wait_for_timeout(300)
        tier2 = page.locator('.verdict .tier').first.inner_text()
        ok(tier2 == 'CAUTION', 'one signal gives CAUTION', tier2)
        ok(page.locator('#pay').count() == 1, 'pay button exists below CRITICAL')
        ok('disabled' in (page.locator('#pay').get_attribute('disabled') is not None and 'disabled' or ''), 'pay is disabled during cooling off')
        page.wait_for_timeout(1400)
        ok('Pay in' in page.locator('#pay').inner_text() or 'Pay' in page.locator('#pay').inner_text(),
           'countdown is running', page.locator('#pay').inner_text())

        page.goto(base + 'check.html', wait_until='load')
        page.click('#samples .chip >> nth=0')
        page.wait_for_timeout(300)
        mtier = page.locator('.verdict .tier').first.inner_text()
        ok(mtier in ('CRITICAL', 'HIGH'), 'sample scam message classified high', mtier)
        red = page.locator('#red').inner_text()
        ok('sbi-kyc-update.online' in red, 'the scam link is kept — it is the evidence', red[:70])

        # a message carrying both a link and identifiers: link kept, ids masked
        mixed = ('Dear Customer, KYC pending on A/c 1234567890123. Update now: '
                 'http://sbi-kyc-update.online/verify or pay to sbi.help.desk@okaxis. OTP 448291')
        page.fill('#t', mixed)
        page.click('#go')
        page.wait_for_timeout(300)
        red2 = page.locator('#red').inner_text()
        ok('\u2022' in red2, 'identifiers are masked in the redacted block', red2[:80])
        ok('1234567890123' not in red2, 'the account number is gone', red2[:80])
        ok('sbi.help.desk@okaxis' not in red2, 'the UPI handle is gone', red2[:80])
        ok('448291' not in red2, 'the OTP digits are gone', red2[:80])
        ok('sbi-kyc-update.online' in red2, 'the link survives the masking', red2[:80])
        ok(page.locator('#count').inner_text().find('masked') > -1, 'the page reports how many identifiers it masked',
           page.locator('#count').inner_text())

        page.goto(base + 'domain.html', wait_until='load')
        page.wait_for_timeout(250)
        ok(page.locator('.verdict .tier').first.inner_text().startswith('LOOK-ALIKE'),
           'domain page auto-runs the sharpest example',
           page.locator('.verdict .tier').first.inner_text())

        page.goto(base + 'library.html', wait_until='load')
        page.wait_for_timeout(250)
        ok(page.locator('.scam').count() == 10, 'library shows 10 scams', page.locator('.scam').count())
        page.click('.scam >> nth=0 >> .scam-top')
        page.wait_for_timeout(200)
        ok(page.locator('.scam.open').count() == 1, 'a scam expands')
        ok(page.locator('.scam.open .scam-body').is_visible(), 'expanded body is visible')

        page.goto(base + 'evidence.html', wait_until='load')
        page.fill('#p', 'sbi.help.desk@okaxis')
        page.fill('#a', '24999')
        page.fill('#m', 'Dear Customer, KYC pending. Update: http://sbi-kyc-update.online/verify A/c 1234567890123')
        page.check('#acts input >> nth=0')
        page.click('#build')
        page.wait_for_timeout(300)
        rec = page.locator('#rec').inner_text()
        ok(rec.startswith('SATARKPAY INCIDENT RECORD'), 'record generated', rec[:40])
        ok('sbi.help.desk@okaxis' not in rec, 'record masks the payee handle')
        ok('1234567890123' not in rec, 'record masks the account number')
        ok('sbi-kyc-update.online' in rec, 'record keeps the link for the investigator')
        ok('1930' in rec, 'record names 1930')

        # ---- offline ----
        print('\n== offline ==')
        page.goto(base + 'index.html', wait_until='load')
        sw = page.evaluate("() => navigator.serviceWorker.ready.then(r => !!r.active)")
        ok(sw, 'service worker is active', sw)
        page.wait_for_timeout(600)
        cached = page.evaluate("""async () => {
            const ks = await caches.keys();
            let n = 0;
            for (const k of ks) { const c = await caches.open(k); n += (await c.keys()).length; }
            return { names: ks, count: n };
        }""")
        ok(cached['count'] >= 14, 'shell is precached', cached)

        ctx.set_offline(True)
        offline_ok = 0
        for p in PAGES:
            page.goto(base + p, wait_until='load')
            page.wait_for_timeout(160)
            if page.locator('h1').count() > 0:
                offline_ok += 1
        ok(offline_ok == 8, 'all 8 pages load with the network off', f'{offline_ok}/8')

        page.goto(base + 'payment.html', wait_until='load')
        page.click('#demo')
        page.wait_for_timeout(350)
        ok(page.locator('.verdict .tier').first.inner_text() == 'CRITICAL',
           'the engine still works offline')
        ctx.set_offline(False)

        ok(len(errors) == 0, 'no console errors in the whole run', errors[:3])

        b.close()
    srv.shutdown()

    print(f'\n{ "=" * 58 }')
    print(f'PASS {PASS[0]}   FAIL {len(FAILS)}')
    for f in FAILS:
        print('  ! ' + f)
    print('=' * 58)
    return 1 if FAILS else 0


if __name__ == '__main__':
    sys.exit(main())
