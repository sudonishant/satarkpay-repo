/* SatarkPay · eval harness (node + jsdom)
   Wahi engine chalata hai jo demo me user ke paste par chalta hai.
   Chalane ke liye:  node run_eval.js            (HTML_PATH env se custom demo file)
   Output:          console table + latest_results.json                        */
const fs = require('fs'), path = require('path');
const { JSDOM, VirtualConsole } = require('jsdom');
const HTML = process.env.HTML_PATH || path.join(__dirname, '..', 'web', 'satarkpay_m2.html');
const html = fs.readFileSync(HTML, 'utf8');
const vc = new VirtualConsole();
const dom = new JSDOM(html, { runScripts: 'dangerously', pretendToBeVisual: true, virtualConsole: vc });
const { window } = dom;

const set = window.eval('EVAL_SET');
const rows = set.map(c => {
  const r = window.eval(`(()=>{const r=classify(${JSON.stringify(c.t)});
    return {v:r.verdict, fam:r.hit?r.hit.fam:null, red:r.S.flags.filter(f=>f[0]==='red').length, dom:r.dom?r.dom.level:null};})()`);
  const strict = r.v === 'SCAM LIKELY', soft = r.v === 'CAUTION';
  const tag = c.y === 'scam' ? (!strict && !soft ? 'miss' : (soft ? 'soft' : '')) : (strict ? 'false-alarm' : (soft ? 'soft' : ''));
  return { label: c.y, verdict: r.v, family: r.fam, reds: r.red, domLevel: r.dom, tag, text: c.t };
});
const n = rows.length, nScam = rows.filter(r => r.label === 'scam').length;
const tp = rows.filter(r => r.label === 'scam' && r.verdict === 'SCAM LIKELY').length;
const fp = rows.filter(r => r.label === 'legit' && r.verdict === 'SCAM LIKELY').length;
const fn = rows.filter(r => r.label === 'scam' && r.verdict !== 'SCAM LIKELY' && r.verdict !== 'CAUTION').length;
const tn = rows.filter(r => r.label === 'legit' && r.verdict !== 'SCAM LIKELY' && r.verdict !== 'CAUTION').length;
const P = tp / (tp + fp), R = tp / (tp + fn), F1 = 2 * P * R / (P + R);
const pad = (s, w) => String(s).padEnd(w).slice(0, w);

console.log(`\nSatarkPay eval · ${n} messages (${nScam} scam / ${n - nScam} legit) · positive = SCAM LIKELY\n`);
console.log('  ' + pad('label', 7) + pad('verdict', 15) + pad('tag', 13) + pad('family', 8) + 'text');
for (const r of rows) console.log('  ' + pad(r.label, 7) + pad(r.verdict, 15) + pad(r.tag || '-', 13) + pad(r.family || '-', 8) + r.text.slice(0, 46) + '…');
console.log(`\n  precision ${(P * 100).toFixed(1)}%  ·  recall ${(R * 100).toFixed(1)}%  ·  F1 ${(F1 * 100).toFixed(1)}%`);
console.log(`  TP ${tp} · FP ${fp} · miss(FN) ${fn} · sahi chhoda(TN) ${tn}`);
console.log('  NOTE: set self-authored hai (ceiling) — 3 messages jaan-boojh ke outside-library hain.\n');

fs.writeFileSync(path.join(__dirname, 'latest_results.json'),
  JSON.stringify({ generated: new Date().toISOString(), source: path.relative(path.join(__dirname, '..'), HTML),
    n, nScam, tp, fp, fn, tn, precision: +P.toFixed(4), recall: +R.toFixed(4), f1: +F1.toFixed(4), rows }, null, 2));
console.log('  → latest_results.json likh diya\n');
