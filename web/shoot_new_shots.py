import asyncio, pathlib
from playwright.async_api import async_playwright

HERE = pathlib.Path(__file__).resolve().parent
HTML = (HERE / 'satarkpay_m2.html').as_uri()
OUT = HERE / 'deck' / 'screenshots'
OUT.mkdir(parents=True, exist_ok=True)

async def main():
    async with async_playwright() as pw:
        b = await pw.chromium.launch()
        pg = await b.new_page(viewport={'width': 1500, 'height': 1050}, device_scale_factor=2)
        await pg.goto(HTML)
        await pg.wait_for_timeout(1500)

        # ---- M4: fake SEBI number case (R26 registry + F28 family) ----
        await pg.click('#tabs button[data-m="m4"]')
        await pg.wait_for_timeout(400)
        await pg.click('#m4 .chip[data-e="5"]')
        await pg.wait_for_timeout(2200)
        m4 = await pg.query_selector('#m4')
        await m4.screenshot(path=str(OUT / '08_m4_sebi_registry_negation.png'))

        # ---- eval harness: miss / false-alarm tags ke saath table ----
        await pg.click('#evRun')
        await pg.wait_for_timeout(900)
        card = pg.locator('xpath=//div[contains(@class,"card")][.//b[@id="evP"]]')
        await card.first.screenshot(path=str(OUT / '09_eval_harness_33msgs.png'))

        # ---- senior mode + Hindi voice buttons ----
        await pg.click('#m4 .chip[data-e="1"]')
        await pg.wait_for_timeout(1600)
        await pg.click('#seniorBtn')
        await pg.click('#voiceBtn')
        await pg.wait_for_timeout(600)
        await pg.screenshot(path=str(OUT / '10_senior_mode_voice.png'))

        # ---- M7b: fraud confirm -> emergency action panel (R38) ----
        await pg.click('#tabs button[data-m="m7"]')
        await pg.wait_for_timeout(500)
        await pg.click('#m7build')
        await pg.wait_for_timeout(4200)
        await pg.click('#m7confirm')
        await pg.wait_for_timeout(900)
        await pg.click('#emMail')
        await pg.wait_for_timeout(500)
        await pg.click('#emCall1930')
        await pg.wait_for_timeout(500)
        await pg.click('#emStop')
        await pg.wait_for_timeout(700)
        panel = await pg.query_selector('#emCard')
        await panel.screenshot(path=str(OUT / '11_emergency_confirm_action.png'))
        await b.close()

asyncio.run(main())
print('shots done')

# ============================================================
# Full deck set (01a–07) — naya UI. Run: python3 shoot_new_shots.py
# ============================================================
async def full_set():
    async with async_playwright() as pw:
        b = await pw.chromium.launch()
        pg = await b.new_page(viewport={'width': 1500, 'height': 1050}, device_scale_factor=1)
        await pg.goto(HTML)
        await pg.wait_for_timeout(1800)

        async def m1_shot(chip_text, fname):
            await pg.click('#tabs button[data-m="m1"]')
            await pg.click('#m1reset')
            await pg.wait_for_timeout(250)
            await pg.click(f'#m1 .chip:has-text("{chip_text}")')
            await pg.click('#m1run')
            await pg.wait_for_timeout(2600)
            await pg.screenshot(path=str(OUT / fname))

        await m1_shot('Saved contact · 2 min chat',  '01a_saved_contact_fastpath.png')
        await m1_shot('Saved contact · 12 min chat', '01b_known_12min_notification.png')
        await m1_shot('Unknown · 10 min chat',       '01c_unknown_hold_callback.png')

        await pg.click('#tabs button[data-m="m2"]'); await pg.wait_for_timeout(300)
        await pg.click('#m2 .chip[data-m2sc="burst"]'); await pg.wait_for_timeout(1200)
        await pg.screenshot(path=str(OUT / '02_screenshot_radar.png'))

        await pg.click('#tabs button[data-m="m3"]'); await pg.wait_for_timeout(1000)
        await pg.screenshot(path=str(OUT / '03_domain_trust.png'))

        await pg.click('#tabs button[data-m="m4"]'); await pg.wait_for_timeout(300)
        await pg.click('#m4 .chip[data-e="1"]'); await pg.wait_for_timeout(2200)
        await pg.screenshot(path=str(OUT / '04_ai_sanchalak.png'))

        await pg.click('#tabs button[data-m="m5"]'); await pg.wait_for_timeout(1000)
        await pg.screenshot(path=str(OUT / '05_wallet_autopay_audit.png'))

        await pg.click('#tabs button[data-m="m6"]'); await pg.wait_for_timeout(1200)
        await pg.screenshot(path=str(OUT / '06_intel_desk_live.png'))

        await pg.click('#tabs button[data-m="m7"]'); await pg.wait_for_timeout(300)
        await pg.click('#m7build'); await pg.wait_for_timeout(4500)
        await pg.screenshot(path=str(OUT / '07_auto_report_1930_pack.png'))

        await b.close()
    print('full set done')

asyncio.run(full_set())
