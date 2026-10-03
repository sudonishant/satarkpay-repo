/* SatarkPay · rule-engine latency benchmark
   Wahi classify() engine jo demo me chalta hai, wahi 33 eval messages.
   Run:  cd eval && NODE_PATH=../node_modules node bench_latency.js
   (ya: NODE_PATH=$(npm root -g) node bench_latency.js)                       */
const fs = require('fs'), path = require('path');
const { JSDOM, VirtualConsole } = require('jsdom');
const HTML = process.env.HTML_PATH || path.join(__dirname, '..', 'web', 'satarkpay_m2.html');
const dom = new JSDOM(fs.readFileSync(HTML, 'utf8'),
  { runScripts: 'dangerously', pretendToBeVisual: true, virtualConsole: new VirtualConsole() });
const { window } = dom;
window.EVAL_TEXTS = window.eval('EVAL_SET').map(c => c.t);
const r = window.eval(`(() => {
  const T = EVAL_TEXTS.slice();
  for (let w = 0; w < 200; w++) classify(T[w % T.length]);        // warm-up
  const N = 5000, ts = [];
  for (let i = 0; i < N; i++) {
    const t = T[i % T.length];
    const s = performance.now(); classify(t); ts.push(performance.now() - s);
  }
  ts.sort((a, b) => a - b);
  return { n: N, mean: ts.reduce((a,b)=>a+b,0)/N, median: ts[N>>1],
           p95: ts[Math.floor(N*0.95)], p99: ts[Math.floor(N*0.99)], max: ts[N-1] };
})()`);
const f = x => (+x).toFixed(4);
console.log(`classify() latency · ${r.n} runs over ${window.EVAL_TEXTS.length} eval messages`);
console.log(`  mean ${f(r.mean)} ms · median ${f(r.median)} ms · p95 ${f(r.p95)} ms · p99 ${f(r.p99)} ms · max ${f(r.max)} ms`);
console.log('  NOTE: Node/jsdom par measured (deterministic engine, no network). Low-end phone par zyada hoga.');
