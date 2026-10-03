import asyncio, pathlib
from playwright.async_api import async_playwright

HTML = 'file:///home/user/satarkpay-m2/demo/satarkpay_m2.html'
OUT = pathlib.Path('/home/user/satarkpay-m2/demo/screenshots')
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
