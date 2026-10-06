# SatarkPay 

**Fraud detection that runs in the sixty seconds before the PIN, not after the loss.**

A UPI payment is approved with one six-digit PIN. The people who take money
through UPI do not break encryption — they talk to you. A call, a new payee, a
handle that is not a real PSP code, an amount larger than anything you normally
send, all converging in one moment where you are being told to hurry.

Any one of those alone is survivable. Two or three together is the shape of an
active attack, and that shape is visible **on the phone, offline, before the
money moves**. That is the entire idea this project is built on.

**[▶ Open the live app](https://sudonishant.github.io/satarkpay-repo/webapp/)** —
no install, no account, works with the network switched off.

**Also in this repo:** [the 10-slide hackathon deck](SatarkPay_SANGYAN_Final.pptx) ·
the demo video (`assets/demo/SatarkPay_Demo.mp4`) ·
[the native Android app](app/satarkpay-android/) ·
[the offline single-file demo](web/satarkpay_m2.html)

---

## What it does

| | |
|---|---|
| **Eight pages** | Dashboard · Check a payment · Check a message · Check a link · Scam library · Evidence pack · Money already gone · What works, what cannot |
| **Nine attack-chain signals** | Each with a whole-number weight, added in the open. You can do the arithmetic yourself and reach the same tier the app shows. |
| **Four tiers** | `LOW 0–2` · `CAUTION 3–5` · `HIGH 6–9` · `CRITICAL 10+`. Cooling-off is 30 s at CAUTION, 60 s at HIGH. At CRITICAL the pay action is withheld and **no countdown is shown** — a countdown would imply the payment becomes available. |
| **Thirteen message patterns** | Plus four *legitimacy* markers that subtract points, so a real debit alert is never flagged for containing the word "OTP". |
| **Link analysis without a blacklist** | A blacklist is stale the day a domain is registered. Instead the string is decomposed: brand + action word, digits in a brand, bulk TLD, punycode — and compared against the real registrable domain it is imitating. |
| **Redaction that runs first** | Account numbers, card numbers, UPI handles, emails and OTP/PIN digits are masked before they reach the screen, the clipboard or a saved file. Scam **links are deliberately kept** — they are the most useful thing you can hand an investigator. |
| **Evidence pack** | Turns what happened into a structured, redacted record you can paste into cybercrime.gov.in or hand to your bank. |
| **Emergency mode** | Money already gone: seven steps in the order that changes the outcome, and the two things that will happen next — including the "recovery agent" who will call you within days. |
| **0 bytes uploaded** | No account, no server, no analytics. The whole app is static files running a rules engine in your browser. |

---

## The image that explains it

```
  THE 60 SECONDS BEFORE THE PIN

   you are on a call ................ +4   ┐
   "scan this to receive money" ..... +4   │
   first time paying this payee ..... +3   ├─►  score 23  ─►  CRITICAL
   amount ≫ your usual .............. +3   │    10+          pay action
   screen sharing active ............ +3   │                  withheld
   instructions came over chat ...... +2   │
   long build-up before the ask ..... +2   │
   handle is not a real PSP code .... +2   │
   message matches a known script ... +5   ┘
```

Every weight is a plain number. There is no model, no scoring curve, and no
hidden layer. **The arithmetic is the explanation** — which is the only way a
warning is ever going to be believed by the person it is stopping.

---

## Verified, measured, and labelled as such

These are the numbers from running the code in this repository. They are
small-sample and self-authored, and they are described that way on purpose.

| Claim | Value | How |
|---|---|---|
| Message classifier | **P 95.0 % · R 90.5 % · F1 92.7 %** | 33 hand-authored messages — TP 19 · FP 1 · FN 2 · TN 9 |
| Engine latency (5,000 runs) | **p50 11.7 ms · p95 31.5 ms · mean 25.2 ms** | `eval/bench_latency.js` |
| Attack-chain assertions | **139 / 139 passing, 0 console errors** | `test_app.py` (logic + UI + offline in a real browser) |
| Offline operation | **8 / 8 pages served with the network cut** | service worker precache, verified in-test by setting the browser offline |
| Web smoke assertions | **66** | `web/smoke_test.js` |

The two misses in the classifier are messages whose fraud is carried by a phone
number and a context a text-only rule cannot see. That is the honest boundary of
pattern matching, and it is exactly the gap the on-device signal chain exists to
close.

Two real bugs found by these assertions, kept in the record:

1. A missing default in a weight map produced `score: NaN` whenever the message
   source was unknown — which silently pushed **every** verdict to LOW.
2. A countdown was shown at CRITICAL, contradicting the rule that the pay action
   is withheld. A user could have read it as "available in 60 seconds".

Neither was visible without running the assertions against a real browser.

---

## The UI

Premium light, calm, one accent family. Nothing decorative that does not carry
meaning: `#f3f6fb` canvas, `#2f6bf6 → #7a5af8` accent family, `#0c1424` ink,
glass topbar and sidebar, hairline borders, a 3 px gradient progress bar on
navigation, a hero with a live phone mockup of a CRITICAL verdict, count-up on
the numbers that are the point, and a filled arc gauge that puts the risk score
next to three threshold ticks so the reader can see *how far past HIGH* a score
is. Real fraud screenshots from `web/deck/scam_shots/` ship inside the library.

**Two accessibility features, on by choice:** a **Hindi voice mode** (the
browser reads verdicts and emergency steps aloud, `hi-IN`) and a **senior
mode** (type scaled up, targets large) — both toggled from the topbar and
remembered for the session. This is the "voice service for the elderly" idea,
running fully on-device.

No framework. No build step. No dependencies. Three scripts:

```
webapp/
├── index.html          dashboard — the idea, the tiers, the eight pages
├── payment.html        the attack chain: questions → score → gate
├── check.html          message patterns + redaction, side by side
├── domain.html         link decomposition, no blacklist
├── library.html        ten scams: what they want, what makes it impossible
├── evidence.html       structured incident record for 1930 / the bank
├── emergency.html      money already gone — order of action
├── about.html          13 capabilities × status × why not live × how to make real
├── assets/
│   ├── app.css         design system
│   ├── app.js          shell, icons, gauge, session memory, service worker
│   ├── engine.js       redaction, VPA parsing, domain ladder, 13 rules + 4 markers
│   ├── chain.js        nine signals → score → tier → gate → evidence text
│   └── data.js         domains, PSP list, ten scams, samples, advisories
├── assets/scams/       four real fraud screenshots (library gallery)
├── sw.js               precache the shell (v6)
└── manifest.webmanifest
```

### The demo pack

```
SatarkPay_SANGYAN_Final.pptx    10-slide hackathon deck (scam shots + demo shots)
assets/demo/SatarkPay_Demo.mp4  ~7 min guided demo — Hindi narration, intro → walkthrough → outro
assets/demo/slides/             the 11 video slides (rebuildable)
docs/DEMO_VIDEO_SCRIPT_HI.md    the narration script, beat by beat
tools/generate_final_deck.py    rebuild the deck
tools/make_demo_video.py        rebuild slides + assemble the video
```

### Run it

```bash
cd webapp && python3 -m http.server 8000
# open http://localhost:8000
```

Or open `index.html` directly from the filesystem — everything except the
service worker works from `file://` too.

---

## The honest part

A prototype is only worth something if it separates the part that runs from the
part that is a plan. The app carries a full capability matrix on
`about.html`; the summary is:

**Running on the device (6):** attack-chain scoring · message pattern matching ·
link structure analysis · identifier redaction · structured incident record ·
full offline operation.

**Need manual input (2):** payee history (the sample ledger is generated
locally) · AutoPay mandate audit (mandates live inside the PSP app).

**Need a partner API (4):** reading incoming SMS and notifications automatically
(a web page cannot — full stop) · domain reputation and age · **payee name
verification against the bank record** · live updates across users.

**Would need a backend (1):** signed, versioned pattern updates and anonymous
redacted scam reports — and the non-negotiable constraint that it must never
receive an unredacted identifier.

The single highest-value integration available is **payee name verification**.
The most useful check in Indian payments is whether the name your app displays
matches the name you were told. That lookup belongs to the PSP and is not
available to an independent app. Without it, "verify the name yourself" stays a
manual step — which is why the app keeps saying it.

On SEBI's own advice: checking that an entity is registered does **not** by
itself establish that a particular transaction or product is legitimate.
Registration is a starting filter, not a verdict.

---

## Built for

**SANGYAN 2026** — SEBI × NSDL × SNTC, IIT-BHU
**Track A** (Fraud Resilience) · **Track B** (Awareness & Grievance Rights) · **Track D** (Habits & Behavioral Security)

**Team SCΛMURΛI** — Nishant Kumar · Prince Singh · Kartik Singh

---

## Sources the content is built on

Only public, verifiable material — no invented statistics anywhere in the app:

- NPCI — UPI handle (PSP) code list and the rule that a QR scan can only debit
- RBI — customer liability for unauthorised electronic transactions; report promptly
- I4C / cybercrime.gov.in — 1930 and the National Cyber Crime Reporting Portal
- DoT Sanchar Saathi (1909) — spam reporting and connections issued in your name
- SEBI — registered-intermediary lookups, and the caveat that registration alone
  does not establish legitimacy

## Licence

MIT — see [LICENSE](LICENSE).
