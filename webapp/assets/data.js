/* ==========================================================================
   SatarkPay — data.js
   All knowledge the engine uses. No network calls anywhere in this app.
   Only verified public information: NPCI/UPI handle rules, RBI and SEBI
   public advisories, and government cyber-fraud reporting routes.
   ========================================================================== */

window.SP = window.SP || {};

/* --------------------------------------------------------------- domains --
   The ladder: what a VPA / link domain actually belongs to, and what an
   attacker types instead. `official` = the real registrable domain.
   ------------------------------------------------------------------------ */

SP.DOMAINS = {
  official: [
    { d: 'onlinesbi.sbi',        who: 'State Bank of India — net banking',        yes: true },
    { d: 'sbi.co.in',            who: 'State Bank of India — corporate',          yes: true },
    { d: 'hdfcbank.com',         who: 'HDFC Bank',                                yes: true },
    { d: 'icicibank.com',        who: 'ICICI Bank',                               yes: true },
    { d: 'axisbank.com',         who: 'Axis Bank',                                yes: true },
    { d: 'kotak.com',            who: 'Kotak Mahindra Bank',                      yes: true },
    { d: 'npci.org.in',          who: 'NPCI — runs UPI (UPI ka operator)',        yes: true },
    { d: 'rbi.org.in',           who: 'Reserve Bank of India',                    yes: true },
    { d: 'sebi.gov.in',          who: 'SEBI — securities regulator',              yes: true },
    { d: 'nsdl.co.in',           who: 'NSDL — depository',                        yes: true },
    { d: 'incometax.gov.in',     who: 'Income Tax Department',                    yes: true },
    { d: 'uidai.gov.in',         who: 'UIDAI — Aadhaar',                          yes: true },
    { d: 'cybercrime.gov.in',    who: 'National Cyber Crime Reporting Portal',    yes: true },
    { d: 'sancharsaathi.gov.in', who: 'Sanchar Saathi — DoT',                     yes: true },
    { d: 'digilocker.gov.in',    who: 'DigiLocker',                               yes: true }
  ],

  /* Patterns an attacker uses. Checked in order; first match wins. */
  lures: [
    { re: /-kyc|-update|-verify|-login|-secure|-support|-help|-care|-refund/i,
      tag: 'Brand + action word', why: 'Real banks do not put "-kyc" or "-login" in the domain. The hyphen itself is the tell.' },
    { re: /\d{2,}/,
      tag: 'Digits in brand name', why: 'A number glued onto a bank name (sbi24, hdfc2026) is almost always a copy.' },
    { re: /[a-z]{2,}\.(xyz|top|club|live|online|site|icu|cyou|click|shop|store|info|buzz)$/i,
      tag: 'Cheap bulk TLD', why: 'These TLDs cost almost nothing and are bought in thousands for short-lived scam pages.' },
    { re: /^xn--/,
      tag: 'Punycode (xn--)', why: 'Punycode lets a domain use look-alike foreign letters that render like Latin ones.' }
  ],

  /* Handle rules that are actually enforced by NPCI. */
  psp: [
    'okaxis', 'okhdfcbank', 'okicici', 'oksbi', 'ybl', 'ibl', 'axl',
    'paytm', 'ptyes', 'ptsbi', 'pthdfc', 'paytmbank',
    'apl', 'yapl', 'airtel',
    'upi', 'freecharge', 'fbl', 'jio', 'timecosmos', 'waaxis', 'wahdfcbank',
    'waicici', 'wasbi', 'pingpay', 'abfspay', 'naviaxis', 'slice', 'fam'
  ]
};

/* ------------------------------------------------------------- 10 scams --
   Field names are stable — library.html and the engine both read them.
   `ask` = what the scammer wants you to do. `tell` = the physical fact that
   makes it impossible. `do` = the correct action, in order.
   ------------------------------------------------------------------------ */

SP.SCAMS = [
  {
    id: 'collect',
    no: '01',
    ti: 'Collect request — "paisa aa raha hai, PIN daalo"',
    freq: 'Most common',
    ask: 'Approves a UPI collect request by entering your UPI PIN.',
    line: 'Scammer says money is coming IN. But a PIN is only ever needed to send money OUT.',
    tell: 'A UPI PIN authorises a debit. Receiving money never asks for a PIN, never asks you to "scan to receive", and never asks for a screenshot of the request.',
    do: [
      'Decline the request. Do not enter the PIN to "check" it.',
      'The request text shows the direction: "Pay" means money leaves, always.',
      'If you already entered the PIN, call your bank now and file at cybercrime.gov.in within 24 hours — the fastest window for a hold.'
    ],
    signs: ['PIN asked while receiving', '"scan this to receive"', 'Request amount differs from what was promised'],
    hi: 'Paisa aa raha hai — par PIN sirf paisa jaane ke liye lagta hai. Ulta kabhi nahi.'
  },
  {
    id: 'qr',
    no: '02',
    ti: 'Receive-money QR code',
    freq: 'Very common',
    ask: 'Scans a QR code that a "buyer" sent, believing it will credit money.',
    line: 'Every UPI QR code is a payment instruction in disguise. Scanning it can only send.',
    tell: 'A QR code carries a payee address and an amount. There is no UPI construct that credits you when you scan. The "receive QR" does not exist.',
    do: [
      'Never scan a code someone sent you to receive money for a sale.',
      'To receive, share your own VPA or your own static QR — never scan.',
      'Selling online? Take the payment on delivery or ask the buyer to pay first and confirm in your own app.'
    ],
    signs: ['QR sent by a stranger', 'Buyer refuses cash on delivery', 'Amount pre-filled in the code'],
    hi: 'Receive QR naam ki cheez UPI me hoti hi nahi. Scan ka matlab — paisa jaana.'
  },
  {
    id: 'kyc',
    no: '03',
    ti: 'KYC / account-block panic',
    freq: 'Very common',
    ask: 'Clicks a link and enters net-banking or UPI credentials to "stop the block".',
    line: 'The deadline is fabricated. The link is not your bank. The block is not real.',
    tell: 'Banks freeze accounts through their own app or branch, never through a link in an SMS/SMS-like message. A real deadline is never hours away.',
    do: [
      'Do not click. Open your bank app separately and check the account yourself.',
      'Type the bank domain yourself instead of trusting a link.',
      'Report the SMS: forward to 1909 (DoT spam reporting) and block the sender.'
    ],
    signs: ['24-hour deadline', '"Account will be blocked today"', 'Link with -kyc / -verify'],
    hi: 'Asli bank kabhi link se account block nahi karta. Deadline nakli hoti hai.'
  },
  {
    id: 'job',
    no: '04',
    ti: 'Task / part-time job prepaid scam',
    freq: 'Rising fast',
    ask: 'Pays a "joining fee" or "deposit" to unlock higher-paying tasks.',
    line: 'First two payouts are real. That is the product being sold to you — your own trust.',
    tell: 'A genuine employer never takes money from an employee. The first small payouts exist only to make the big deposit feel safe.',
    do: [
      'Any job that asks you to pay first is a scam, regardless of the payout screenshots.',
      'Screenshots of other people getting paid are produced in a minute and prove nothing.',
      'Never move to Telegram for "task assignment" with money involved.'
    ],
    signs: ['Pay to start', 'Task groups on Telegram', 'Escalating deposit tiers'],
    hi: 'Naukri me paisa tumhe milta hai, tumse liya nahi jaata. Jo join fee maange — scam.'
  },
  {
    id: 'invest',
    no: '05',
    ti: 'Guaranteed-return investment / trading app',
    freq: 'Highest loss value',
    ask: 'Installs a "trading" APK shared over chat and transfers money to it.',
    line: 'The app is a spreadsheet with a login screen. The returns are typed in by the operator.',
    tell: 'No SEBI-registered intermediary guarantees returns in writing. A profit number inside an app the scammer built is not a balance — it is text on their server.',
    do: [
      'Check the intermediary on the SEBI website before transferring any money.',
      'Never install an APK file sent over WhatsApp/Telegram. It is not on any app store because it would be rejected.',
      'Withdrawals being blocked behind "tax" or "fees" fees is the point where the scam usually ends.'
    ],
    signs: ['APK file shared over chat', '"Guaranteed" profit', 'Withdrawal blocked behind a fee'],
    hi: 'Jo app WhatsApp pe APK ban ke aati hai, wo store se reject hoti hai — sochne wali baat hai.'
  },
  {
    id: 'otp',
    no: '06',
    ti: 'OTP / remote-access takeover',
    freq: 'High value',
    ask: 'Reads out an OTP or installs a screen-sharing app.',
    line: 'Somebody watching your screen can see every balance and authorise payments with you.',
    tell: 'No bank, wallet, or support desk ever needs an OTP, a PIN, or a screen-share. An OTP authorises a specific transaction — read it out loud and you authorised it.',
    do: [
      'Never share an OTP. Never install AnyDesk/TeamViewer/QuickSupport at a caller s request.',
      'Hang up and call the number printed on your card or the bank site.',
      'If you installed a screen-share app: uninstall it, change your password, then call the bank.'
    ],
    signs: ['OTP requested on call', 'Screen-share app requested', 'Caller claims to be bank/RBI/CBI'],
    hi: 'OTP kisi ko nahi batana. Screen share kar diya to unhe apna hi phone dikh raha hai.'
  },
  {
    id: 'courier',
    no: '07',
    ti: 'Courier / parcel "narcotics found" call',
    freq: 'Common',
    ask: 'Terrifies you into transferring a "verification deposit" to avoid arrest.',
    line: 'It is the oldest fear trick: no due process has ever been conducted over a video call.',
    tell: 'No Indian agency takes a deposit over UPI, asks you to stay on a video call, or forbids you from telling your family. Real notice arrives on paper or through the official portal.',
    do: [
      'Disconnect. Police never arrests over a courier call.',
      'Do not stay on the call, do not "stay on the line while we verify".',
      'Tell one family member immediately — isolation is the mechanism.'
    ],
    signs: ['Fake FIR / fake notice on WhatsApp', 'Video call from a "police station"', 'Stay-on-call instruction'],
    hi: 'Asli FIR video call pe nahi hoti. Jo caller aapko akele me rakhna chahe — wahi khatra hai.'
  },
  {
    id: 'army',
    no: '08',
    ti: 'Fake buyer — "army officer" marketplace fraud',
    freq: 'Common',
    ask: 'Sends a payment screenshot, asks you to release goods before money arrives.',
    line: 'A screenshot of a payment is not a payment. The bank reference is the only proof.',
    tell: 'Payment screenshots are edited in seconds and UTR numbers can be fake or from a different, cancelled transaction. Money is real only when it shows in your own balance.',
    do: [
      'Open your bank app and check the balance — not the screenshot.',
      'Do not ship or hand over goods until the credit is visible as "uncleared/cleared" in your own account.',
      'A "payment sent" screenshot with a UTR that does not appear in your statement is a scam.'
    ],
    signs: ['Buyer in a hurry', 'Screenshot instead of a bank credit', 'Requests to use a friend s account'],
    hi: 'Screenshot se paisa nahi aata. Apni app me balance dekho — bas wahi sach hai.'
  },
  {
    id: 'loan',
    no: '09',
    ti: 'Instant-loan app / illegal recovery',
    freq: 'Hidden harm',
    ask: 'Installs a small loan app with broad permissions; later faces extortion.',
    line: 'The loan is small. What you hand over is your contact list and your photo gallery.',
    tell: 'These apps ask for contacts, gallery and SMS permissions because the repayment pressure comes from calling your contacts. That is the business model, not a bug.',
    do: [
      'Never grant contacts/gallery/SMS access to any loan app.',
      'Check whether the lender is on the RBI list of registered NBFCs before installing.',
      'If threatened: do not pay the extortion fee. Collect the app name and file at cybercrime.gov.in.'
    ],
    signs: ['Contacts + gallery permission', 'Loan in 5 minutes, no documents', 'Threat calls to family'],
    hi: 'Loan chhota hota hai — jo data jaata hai wo bada hota hai. Contacts permission mat do.'
  },
  {
    id: 'autopay',
    no: '10',
    ti: 'Silent AutoPay mandate / free-trial trap',
    freq: 'Quiet, recurring',
    ask: 'Approves a recurring mandate once; it keeps debiting quietly.',
    line: 'One approval, every month, forever — and nobody sends you a fresh warning each time.',
    tell: 'UPI AutoPay mandates are standing instructions. Once approved, they execute without asking again. Free trials are the usual way the approval is harvested.',
    do: [
      'Check your active mandates in your UPI app — most apps list them under AutoPay/Mandates.',
      'Revoke anything you do not recognise. Revoking is one tap and does not require the merchant s permission.',
      'Before any "₹1 trial", check what the renewal amount and period are.'
    ],
    signs: ['₹1 / free trial', 'Mandate approved in a hurry', 'Small repeated debits you cannot place'],
    hi: 'Ek baar mandate approve kar diya — phir har mahine chupchap kate. List check karte raho.'
  }
];

/* --------------------------------------------------------------- samples --
   Realistic message shapes used by the "check a message" tool. Deliberately
   include both scam and legitimate examples so the result is not a wall of red.
   ------------------------------------------------------------------------ */

SP.SAMPLES = [
  { k: 'Scam — KYC block threat',
    t: 'Dear Customer, your SBI YONO account will be BLOCKED today as KYC is pending. Update immediately: http://sbi-kyc-update.online/verify\n- SBI Team' },
  { k: 'Scam — job deposit',
    t: 'Congratulations! Selected for Amazon online task job. Earn ₹3000 daily. Only joining deposit ₹499 refundable. WhatsApp 98xxxxxxxx for task group. Hurry only 4 seats left!' },
  { k: 'Scam — lottery',
    t: 'Your number has won KBC Lucky Draw 25 Lakh! Send your Aadhaar and pay ₹4,999 processing charge to claim. Contact: kbc.winner2026@ybl' },
  { k: 'Scam — electricity bill',
    t: 'Electricity bill update: your power will be disconnected tonight 9:30pm because previous month bill not updated. Pay immediately on this link: bit.ly/eb-pay-now' },
  { k: 'Scam — parcel/customs',
    t: 'Your parcel is held at customs, containing illegal items. A case has been registered. Stay on call with our officer and do not inform family. Pay verification deposit ₹25,000 for clearance.' },
  { k: 'Legit — bank debit alert',
    t: 'Rs.450.00 debited from A/c XX4471 on 04-10-26 to VPA paytm.qr1234@paytm. Balance 12,304.22. Not you? Call 18001234.' },
  { k: 'Legit — delivery update',
    t: 'Your Amazon order 402-xxxxx has been delivered to the security desk. Track in the app. Do not share OTP with anyone.' },
  { k: 'Legit — mutual fund NAV note',
    t: 'Your SIP of Rs.2000 in Axis Bluechip Fund is processed. Units allotted as per today NAV. View statement in the app.' }
];

/* ----------------------------------------------------------- intel desk --
   Short, factual, verifiable advisories. No invented statistics.
   ------------------------------------------------------------------------ */

SP.INTEL = [
  { d: '04 Oct', sev: 'high', ti: 'Fake "electricity disconnect" links on the rise',
    b: 'Shortened links (bit.ly, tinyurl) posing as state electricity boards. The page asks for a card or UPI PIN. Never pay a bill through a link in a message — use the board s own app or site.' },
  { d: '02 Oct', sev: 'high', ti: 'WhatsApp APK "trading" groups',
    b: 'Groups promising 8-10% weekly returns push an APK file. The app is not on Play Store because it would fail review. Profits shown inside are typed by the operator.' },
  { d: '29 Sep', sev: 'med', ti: 'AutoPay mandate harvesting in free trials',
    b: 'A ₹1 trial approves a recurring mandate. Renewal amounts are set far higher and debited silently. Check your UPI app s AutoPay list and revoke unknown mandates.' },
  { d: '26 Sep', sev: 'med', ti: 'Task-job scams moving to Telegram',
    b: 'Small payouts first, then larger deposits to "unlock levels". Reports to 1930 involving this pattern are rising. Any job that takes money from you is a scam.' },
  { d: '21 Sep', sev: 'low', ti: 'Courier "narcotics" calls now using video',
    b: 'Callers stage a fake police set-up on video call and demand a clearance deposit. No Indian agency conducts interrogation or takes deposits over a video call.' }
];

/* ---------------------------------------------------------- sample payee --
   Used by the ledger/autopay screens for a realistic, fully local demo.
   ------------------------------------------------------------------------ */

SP.SAMPLE_LEDGER = [
  { vpa: 'ramesh.kirana@ybl',      amt: 240,   when: 'Today, 08:12',   known: true,  n: 'Kirana — regular' },
  { vpa: 'blu.dart.courier@paytm', amt: 1890,  when: 'Yesterday, 19:04', known: false, n: 'First-time payee' },
  { vpa: 'electricity.bihar@icici',amt: 1420,  when: '02 Oct, 10:31', known: true,  n: 'BBPS biller' },
  { vpa: 'tradepro.help@okaxis',   amt: 25000, when: '01 Oct, 21:47',  known: false, n: 'First-time payee' },
  { vpa: 'sip.axis@icici',         amt: 2000,  when: '01 Oct, 06:00',  known: true,  n: 'SIP mandate' }
];

SP.SAMPLE_MANDATES = [
  { m: 'StreamMax Plus',        amt: 149,  per: 'monthly', next: '07 Oct', src: '₹1 trial, 12 Aug', risk: 'ok' },
  { m: 'FitPro Premium',        amt: 899,  per: 'monthly', next: '11 Oct', src: 'Free trial, 02 Sep', risk: 'warn' },
  { m: 'CloudBackup 2TB',       amt: 79,   per: 'monthly', next: '19 Oct', src: 'Direct signup', risk: 'ok' },
  { m: 'UNKNOWN-PSP-COLLECT',   amt: 499,  per: 'monthly', next: '05 Oct', src: 'Mandate approved 03 Oct', risk: 'bad' }
];
