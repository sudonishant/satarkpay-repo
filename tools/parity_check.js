// node tools/parity_check.js  -> compares webapp/assets/thehro.js with thehro/engine.py
// (python3 tools/export_parity.py writes the reference file first)
const fs = require("fs"), path = require("path");
const T = require(path.join(__dirname, "../webapp/assets/thehro.js"));
const cards = JSON.parse(fs.readFileSync(path.join(__dirname, "../thehro/playbooks.json"))).cards;
const ref = JSON.parse(fs.readFileSync(process.argv[2] || path.join(__dirname, "../thehro/results/parity_ref.json")));
let bad = 0;
for (const c of ref) {
  const d = T.decide(c.s, cards);
  if (d.tier !== c.tier || d.card_id !== c.card || Math.abs(d.p - c.p) > 1e-3) { bad++; console.log("MISMATCH", c, d.tier, d.p, d.card_id); }
}
console.log(`${ref.length - bad}/${ref.length} sessions identical (tier, card, p)`);
process.exit(bad ? 1 : 0);
