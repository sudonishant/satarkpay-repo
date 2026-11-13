// node tools/parity_check.js  -> compares webapp/assets/thehro.js with thehro/engine.py
// (python3 tools/export_parity.py writes the reference file first)
const fs = require("fs"), path = require("path");
const T = require(path.join(__dirname, "../webapp/assets/thehro.js"));
const cards = JSON.parse(fs.readFileSync(path.join(__dirname, "../thehro/playbooks.json"))).cards;
const ref = JSON.parse(fs.readFileSync(process.argv[2] || path.join(__dirname, "../thehro/results/parity_ref.json")));
let bad = 0;
for (const c of ref) {
  const d = T.decide(c.s, cards);
  const mismatches = [];
  if (d.tier !== c.tier) mismatches.push("tier");
  if (d.card_id !== c.card) mismatches.push("card");
  if (Math.abs(d.p - c.p) > 1e-3) mismatches.push("p");
  for (const key of ["action", "reason", "question", "evidence", "floors", "unknown_signals"]) {
    if (JSON.stringify(d[key]) !== JSON.stringify(c[key])) mismatches.push(key);
  }
  if (mismatches.length) {
    bad++;
    console.log("MISMATCH", mismatches.join(","), JSON.stringify(c.s),
      JSON.stringify({ tier: d.tier, card: d.card_id, p: d.p, action: d.action, reason: d.reason,
        question: d.question, evidence: d.evidence, floors: d.floors, unknown_signals: d.unknown_signals }));
  }
}
console.log(`${ref.length - bad}/${ref.length} sessions identical (tier, card, p, action, reason, question, evidence, floors, unknown signals)`);
process.exit(bad ? 1 : 0);
