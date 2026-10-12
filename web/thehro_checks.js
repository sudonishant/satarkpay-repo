// THEHRO v3 checks (RAKSHAM PS 02). Runs after smoke_test.js in run_smoke.sh.
// Covers: engine behaviour per playbook card, floors, alert-fatigue guards,
// anti-splitting, retry-after-hold, playbook schema, the live demo page,
// every v2 web page, and the offline cache list.
const fs = require("fs"), path = require("path");
const { JSDOM, VirtualConsole } = require("jsdom");
const ROOT = path.join(__dirname, "..");
const WEB = path.join(ROOT, "webapp");
const T = require(path.join(WEB, "assets/thehro.js"));
const pb = JSON.parse(fs.readFileSync(path.join(WEB, "assets/playbooks.json"), "utf8"));
const cards = pb.cards;
let pass = 0, fail = 0;
const t = (name, ok, info = "") => { ok ? pass++ : fail++; console.log(`  ${ok ? "✓" : "✗"} ${name}${ok ? "" : "  → " + info}`); };
const lvl = d => +d.tier[1];
const D = s => T.decide(s, cards);

console.log("== playbook cards: schema ==");
t("12 P1 cards", cards.length === 12, cards.length);
t("card ids unique", new Set(cards.map(c => c.id)).size === cards.length);
for (const c of cards) {
  const ok = c.stages.every(x => x in T.PREDICATES) && c.stages.includes(c.anchor) &&
    c.min_match >= 2 && c.min_match <= c.stages.length && c.reason.length > 20 && c.question.endsWith("?");
  t(`${c.id}: stages known, anchor in stages, reason + question`, ok, JSON.stringify(c).slice(0, 120));
}
t("web copy of playbooks.json == engine copy",
  fs.readFileSync(path.join(ROOT, "thehro/playbooks.json"), "utf8") === fs.readFileSync(path.join(WEB, "assets/playbooks.json"), "utf8"));

console.log("== every card fires on its own workflow ==");
const canon = {
  "PB-B1": { call_active: true, caller_in_contacts: false, call_video_or_voip: true, call_minutes: 90, payee_new: true, amount: 300000, baseline_p95: 10000 },
  "PB-B2": { recent_fraud_report_days: 10, call_active: true, caller_in_contacts: false, payee_new: true, remark: "processing fee", amount: 9999 },
  "PB-A1": { call_active: true, caller_in_contacts: false, payee_new: true, amount: 40000, baseline_p95: 5000, qr_recent: true },
  "PB-A3": { call_active: true, caller_in_contacts: false, call_video_or_voip: true, payee_new: true, amount: 40000, baseline_p95: 5000 },
  "PB-A4": { credit_claim_recent: true, real_credit_found: false, payee_new: true, qr_recent: true, amount: 5000 },
  "PB-C1": { remark: "invest", new_payees_7d: 4, rising_amounts: true, small_inbound_unknown_72h: true, payee_new: true, amount: 20000 },
  "PB-C2": { remark: "IPO allotment", payee_sebi_valid: false, payee_new: true, amount: 60000, baseline_p95: 6000 },
  "PB-C3": { small_inbound_unknown_72h: true, new_payees_7d: 4, rising_amounts: true, payee_new: true, amount: 8000 },
  "PB-D1": { apk_installed_hours: 5, overlay_service: true, payee_new: true, amount: 20000 },
  "PB-D2": { screen_share: true, call_active: true, caller_in_contacts: false, payee_new: true, amount: 5000 },
  "PB-E1": { qr_from_gallery: true, qr_receive_intent: true, payee_new: true, amount: 12000 },
  "PB-G1": { unofficial_helpline: true, call_active: true, caller_in_contacts: false, screen_share: true, payee_new: true, amount: 20000 },
};
for (const c of cards) {
  const d = D(canon[c.id]);
  const okTier = lvl(d) >= 2 && (!c.tier_floor || lvl(d) >= +c.tier_floor[1]);
  t(`${c.id} → alert at T2+ and respects floor (${d.tier}, ${d.card_id})`, okTier && d.card_id !== null, JSON.stringify(d).slice(0, 160));
  t(`${c.id} → alert has a plain reason`, typeof d.reason === "string" && d.reason.length > 15);
}

console.log("== alert fatigue: normal payments stay quiet ==");
const normals = [
  ["chai ₹40 new shop", { payee_new: true, payee_vpa_age_days: 700, amount: 40, baseline_p95: 2500 }],
  ["rent to saved landlord, 4.5× usual", { payee_saved: true, payee_new: false, amount: 18000, baseline_p95: 4000 }],
  ["saved family, unknown caller, big amount", { payee_saved: true, payee_new: false, amount: 90000, baseline_p95: 3000, call_active: true, caller_in_contacts: false }],
  ["talking to mother (saved contact) + new payee", { call_active: true, caller_in_contacts: true, call_minutes: 40, payee_new: true, payee_vpa_age_days: 400, amount: 1500 }],
  ["late-night food order", { hour: 23, payee_new: true, payee_vpa_age_days: 900, amount: 450 }],
  ["SIP to @valid handle", { remark: "SIP mutual fund invest", payee_sebi_valid: true, payee_new: false, payee_vpa_age_days: 1500, amount: 2000, baseline_p95: 3000 }],
  ["real refund confirmed by ledger", { credit_claim_recent: true, real_credit_found: true, payee_new: false, amount: 2000 }],
  ["empty session", {}],
];
for (const [n, s] of normals) { const d = D(s); t(`${n} → no interruption (${d.tier})`, lvl(d) <= 1, JSON.stringify(d.floors)); }

console.log("== deterministic floors and guards ==");
t("screen control on non-saved payee → T4 even for ₹1", D({ screen_share: true, amount: 1 }).tier === "T4");
t("fake credit with no real credit → at least T3", lvl(D({ credit_claim_recent: true, amount: 100 })) >= 3);
t("saved payee capped at T1 without takeover", lvl(D({ payee_saved: true, payee_new: false, amount: 1e6, baseline_p95: 1000, call_active: true, caller_in_contacts: false, call_minutes: 120 })) <= 1);
t("generic unknown call + new payee does not complete severe cards", !["PB-D2", "PB-D1", "PB-B1"].includes(D({ call_active: true, caller_in_contacts: false, payee_new: true, amount: 500 }).card_id));
t("split payments are added up (anti-splitting)", D({ payee_new: true, amount: 30000, baseline_p95: 10000, new_payee_sum_72h: 120000 }).evidence.includes("amount_very_high"));
t("split sum ignored for saved payee", !D({ payee_saved: true, payee_new: false, amount: 30000, baseline_p95: 10000, new_payee_sum_72h: 120000 }).evidence.includes("amount_very_high"));
const r = D({ payee_new: true, payee_vpa_age_days: 200, amount: 2000, held_recently_hours: 3 });
t("retry to new payee within 24 h of a hold → T3 with its own reason", r.tier === "T3" && /warned/.test(r.reason), r.reason);
t("retry after 30 h is not forced", lvl(D({ payee_new: true, payee_vpa_age_days: 200, amount: 200, held_recently_hours: 30 })) <= 1);
t("p is between 0 and 1", [canon["PB-B1"], {}, canon["PB-D2"]].every(s => { const p = D(s).p; return p >= 0 && p <= 1; }));
t("decision is deterministic", JSON.stringify(D(canon["PB-B1"]).evidence) === JSON.stringify(D(canon["PB-B1"]).evidence));
t("unknown stage name never fires", (() => { try { return T.decide({}, [{ id: "X", name: "x", stages: ["future_signal"], min_match: 1, anchor: "future_signal", tier_floor: "T4", reason: "r", question: "q?" }]).tier === "T0"; } catch (e) { return false; } })());
let worst = 0; for (let i = 0; i < 2000; i++) { const a = Date.now(); D(canon["PB-B1"]); worst = Math.max(worst, Date.now() - a); }
t(`2,000 decisions, slowest ${worst} ms (< 150 ms target)`, worst < 150);

console.log("== live demo page + v2 pages ==");
(async () => {
  const pages = fs.readdirSync(WEB).filter(f => f.endsWith(".html"));
  for (const p of pages) {
    const errs = []; const vc = new VirtualConsole(); vc.on("jsdomError", e => errs.push(e.message.slice(0, 120)));
    const dom = await JSDOM.fromFile(path.join(WEB, p), { runScripts: "dangerously", resources: "usable", pretendToBeVisual: true, virtualConsole: vc,
      beforeParse(w) { w.fetch = u => Promise.resolve({ ok: true, json: () => Promise.resolve(JSON.parse(fs.readFileSync(path.join(WEB, u), "utf8"))) }); w.scrollTo = () => {}; w.matchMedia = w.matchMedia || (() => ({ matches: false, addListener() {}, removeListener() {}, addEventListener() {} })); } });
    await new Promise(r => setTimeout(r, 900));
    t(`${p} loads with no script errors`, errs.length === 0, errs.join(" | "));
    if (p === "thehro.html") {
      const d = dom.window.document;
      const want = { "Digital arrest": "T4", "Screen-share 'KYC'": "T4", "Fake 'credited' SMS": "T3", "Normal: rent to saved landlord": "T0", "Normal: new chai shop": "T0" };
      const n = d.querySelectorAll("#tabs button").length;
      t("demo has 5 sessions", n === 5, n);
      for (let j = 0; j < n; j++) {
        const b = d.querySelectorAll("#tabs button")[j]; const name = b.textContent; b.click();
        for (let i = 0; i < 8; i++) d.getElementById("next").click();
        const on = [...d.querySelectorAll("#ladder div")].find(x => x.getAttribute("style"));
        const tier = on ? on.textContent.slice(0, 2) : "?";
        t(`demo "${name}" ends at ${want[name]}`, tier === want[name], tier);
      }
      const live = d.getElementById("live");
      t("demo has a real-time Live button", !!live);
      t("demo phone shows an alert box at the end of digital arrest", (() => { d.querySelectorAll("#tabs button")[0].click(); for (let i = 0; i < 8; i++) d.getElementById("next").click(); return /hold/i.test(d.getElementById("alert").textContent); })());
    }
    dom.window.close();
  }
  console.log("== offline cache ==");
  const sw = fs.readFileSync(path.join(WEB, "sw.js"), "utf8");
  const assets = (sw.split("var ASSETS")[1].split("]")[0].match(/'([^']+)'/g) || []).map(x => x.slice(1, -1)).filter(x => x !== "./");
  t("every precached file exists", assets.every(a => fs.existsSync(path.join(WEB, a))), assets.filter(a => !fs.existsSync(path.join(WEB, a))).join(","));
  t("THEHRO files are precached", ["thehro.html", "assets/thehro.js", "assets/playbooks.json"].every(a => assets.includes(a)));
  console.log(`\nTHEHRO RESULT: ${pass} passed, ${fail} failed`);
  process.exit(fail ? 1 : 0);
})();
