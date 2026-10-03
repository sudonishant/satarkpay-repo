# Repo map — kaunsi cheez kahan hai

```
satarkpay/
├── README.md                  project intro, quickstart, numbers + limits
├── CHANGELOG.md               kya-kya kab bana (v0.9 → v1.0)
├── LICENSE                    MIT (sirf hamara original code)
├── NOTICE.md                  peer repo idea-credits + license position (zero code copying)
├── CONTRIBUTING / CODE_OF_CONDUCT / SECURITY / Makefile / publish.sh
├── .github/                   CI (66 checks + eval + JSON lint), issue/PR templates
│
├── web/                       ★ ASLI DELIVERABLE (offline demo)
│   ├── satarkpay_m2.html      ek file — browser me kholo, kuch install nahi
│   ├── _base.html · _part_m3m6.html · _part_app.js · _intel.json    (source)
│   ├── build.py               parts → HTML assemble
│   ├── smoke_test.js · run_smoke.sh     66 automated checks
│   ├── shoot_new_shots.py     demo ke screenshots (playwright)
│   └── deck/                  presentation + assets
│       ├── satarkpay_deck.pdf / .pptx / deck.html   ← 18 slides (light theme)
│       ├── build_deck.py · build_charts.py · build_diagrams.py
│       ├── images/            3 AI-generated illustrations
│       ├── diagrams/          journey · M1 matrix · R38 emergency (SVG + PNG)
│       ├── charts/            eval confusion · 31-repo coverage
│       └── screenshots/       12 demo screenshots
│
├── app/                       Android (finale ke liye)
│   └── android-skeleton/      Kotlin: GateEngine · VerdictEngine · DomainEngine ·
│                              EvidenceBuilder · EmergencyActions(R38) · signals · consent · Room
│                              + AndroidManifest (sirf 3 optional permissions) + strings.xml
│
├── eval/                      measurement
│   ├── eval_set.json          33 labelled messages (22 scam + 11 legit)
│   ├── run_eval.js / .py      live P/R/F + miss/false-alarm tags
│   └── latest_results.json    aakhri run ka poora table
│
├── rules/
│   ├── rules_R15_R25.json     core modules ka pack (M1..M7)
│   └── rules_R26_R38.json     registry · negation · redaction · naye families · eval · emergency
│
├── tools/                     scripts
│   ├── intel_crawler.py       M6 crawler (RSS + Google News, keyless)
│   ├── make_demo_snapshot.py  crawl → demo snapshot
│   ├── crop_tool.py           M7 crop + redact + QR decode
│   ├── ocr_text.py            M7 offline OCR (RapidOCR)
│   ├── build_report.py        M7 complaint + email + annexure + zip
│   ├── sources.json · threat_feed.json · requirements.txt
│
├── data/                      snapshots (privacy-safe)
│   ├── intel_snapshot.json    aaj ka crawl (784 → 172 → 63 novel)
│   ├── OSINT_REPORT.md · repo_matrix.csv/.json · repo_licenses.json · problem_statement.pdf/.txt
│   └── case_pack/             asli case ke MASKED documents (annexure, complaint, email, summary)
│
└── docs/                      sab likhit
    ├── UI_SPEC.md             ★ screen-by-screen (S0–S11 + C1–C4), wireframes, copy, flows
    ├── ARCHITECTURE.md        layers, engine pseudocode, failure modes
    ├── PRIVACY.md             DPDP checklist + kya collect hota hai/nahi
    ├── RULES.md               R1–R38 human-readable + schema
    ├── DEMO_SCRIPT.md         3-min demo beats + judges ke 6 sawal ke jawab
    ├── PLAY_STORE_NOTES.md    permission declarations + risk register
    ├── ROADMAP.md             phase 1 (48h) · pilot · scale + metrics
    ├── SUBMISSION.md          SANGYAN submission checklist
    ├── OSINT_SUMMARY.md       31-repo landscape + event facts
    ├── VERIFICATION.md        har claim ka command + output
    └── REPO_MAP.md            ye file
```

## “Mujhe kya chahiye” → kahan jayein

| Chahiye | Kahan |
|---|---|
| Demo chala ke dekhna | `web/satarkpay_m2.html` |
| Judges ko deck bhejna | `web/deck/satarkpay_deck.pdf` |
| Deck edit karna | `web/deck/satarkpay_deck.pptx` ya source (`build_deck.py`) |
| App banane ke liye blueprint | `docs/UI_SPEC.md` + `app/android-skeleton/` |
| Rules ka JSON | `rules/` |
| Numbers verify karna | `docs/VERIFICATION.md` + `eval/latest_results.json` |
| Scam intel chalana | `tools/intel_crawler.py` |
| Naya rule/scam add karna | `CONTRIBUTING.md` + `rules/RULES.md` schema |
| Privacy kya hai | `docs/PRIVACY.md` |
| Peer repos ka credit | `NOTICE.md` + `data/repo_licenses.json` |
