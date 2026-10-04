/* ============================================================
   SatarkPay M2 · New Module Pack — demo JS (self-contained, no network)
   Verdicts demo-only hain (synthetic + public headlines). Rule engine = deterministic.
   ============================================================ */
const INTEL = __INTEL_JSON__;
const $=(s,r=document)=>r.querySelector(s);
const $$=(s,r=document)=>[...r.querySelectorAll(s)];
const pad=n=>String(n).padStart(2,'0');
const mmss=s=>`${pad(Math.floor(s/60))}:${pad(Math.floor(s%60))}`;
const esc=t=>String(t).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
let AUDIO_ENABLED=true;
let audioCtx=null;
function playSfx(type){
  if(!AUDIO_ENABLED) return;
  try{
    const AudioCtor=window.AudioContext||window.webkitAudioContext;
    if(!AudioCtor) return;
    if(!audioCtx) audioCtx=new AudioCtor();
    if(audioCtx.state==='suspended') audioCtx.resume();
    const osc=audioCtx.createOscillator();
    const gain=audioCtx.createGain();
    osc.connect(gain); gain.connect(audioCtx.destination);
    const now=audioCtx.currentTime;
    if(type==='bad'||type==='alert'){
      osc.type='sawtooth'; osc.frequency.setValueAtTime(320, now);
      osc.frequency.exponentialRampToValueAtTime(160, now+0.22);
      gain.gain.setValueAtTime(0.12, now);
      gain.gain.exponentialRampToValueAtTime(0.01, now+0.22);
      osc.start(now); osc.stop(now+0.23);
    } else if(type==='ok'||type==='safe'){
      osc.type='sine'; osc.frequency.setValueAtTime(520, now);
      osc.frequency.exponentialRampToValueAtTime(880, now+0.18);
      gain.gain.setValueAtTime(0.09, now);
      gain.gain.exponentialRampToValueAtTime(0.01, now+0.18);
      osc.start(now); osc.stop(now+0.19);
    } else {
      osc.type='triangle'; osc.frequency.setValueAtTime(650, now);
      gain.gain.setValueAtTime(0.04, now);
      gain.gain.exponentialRampToValueAtTime(0.005, now+0.05);
      osc.start(now); osc.stop(now+0.06);
    }
  }catch(e){}
}
function toast(msg,kind){
  playSfx(kind);
  const d=document.createElement('div');d.textContent=msg;
  d.style.cssText=`position:fixed;left:50%;transform:translateX(-50%);bottom:22px;z-index:99;padding:10px 16px;border-radius:12px;
  font-size:13px;font-weight:550;border:1px solid ${kind==='bad'?'rgba(239,68,68,0.5)':kind==='ok'?'rgba(16,185,129,0.5)':'rgba(59,130,246,0.35)'};
  background:${kind==='bad'?'rgba(44,16,21,0.95)':kind==='ok'?'rgba(14,42,30,0.95)':'rgba(18,29,54,0.95)'};
  box-shadow:0 14px 35px -10px rgba(0,0,0,0.7), 0 0 15px ${kind==='bad'?'rgba(176,101,79,0.16)':kind==='ok'?'rgba(16,185,129,0.2)':'rgba(59,130,246,0.2)'};
  backdrop-filter:blur(8px);transition:all .25s ease;animation:pop .25s ease`;
  document.body.appendChild(d);setTimeout(()=>{d.style.opacity='0';d.style.transform='translateX(-50%) translateY(8px)';setTimeout(()=>d.remove(),300);},2400);
}

/* ---------- sound toggle ---------- */
const sBtn=$('#soundBtn');
if(sBtn){
  sBtn.onclick=()=>{
    AUDIO_ENABLED=!AUDIO_ENABLED;
    sBtn.innerHTML=AUDIO_ENABLED?'🔔 sound: on':'🔕 sound: off';
    toast(AUDIO_ENABLED?'Audio feedback enabled':'Audio muted');
    if(AUDIO_ENABLED) playSfx('ok');
  };
}

/* ---------- demo speed ---------- */
let SPEED=30;
$('#speedBtn').onclick=()=>{SPEED=SPEED===30?1:30;$('#speedBtn').innerHTML=
  (SPEED===30?'⏩ speed ×30 <span class="dim2">(real time)</span>':'🐢 real time <span class="dim2">(speed ×30)</span>');};

/* ---------- accessibility: Hindi voice + senior mode (R29 — borrowed concept se) ---------- */
let VOICE=false, SENIOR=false;
function speak(txt, lang){
  if(!VOICE) return;
  try{
    if(!('speechSynthesis' in window)) { toast('Is browser me voice support nahi hai'); return; }
    const u=new SpeechSynthesisUtterance(txt); u.lang=lang||'hi-IN'; u.rate=0.95; u.pitch=1;
    const vs=speechSynthesis.getVoices()||[];
    const v=vs.find(x=>(x.lang||'').toLowerCase().startsWith('hi')) || vs.find(x=>(x.lang||'').toLowerCase().startsWith('en-in'));
    if(v) u.voice=v;
    speechSynthesis.cancel(); speechSynthesis.speak(u);
  }catch(e){}
}
$('#voiceBtn').onclick=()=>{VOICE=!VOICE;$('#voiceBtn').innerHTML=VOICE?'🔊 voice: हिंदी ON':'🔊 voice: off';
  toast(VOICE?'Voice ON — verdict/warning Hindi me sunai jayega':'Voice off');
  if(VOICE) speak('आवाज़ चालू है। जो भी चेतावनी आएगी, मैं हिंदी में पढ़कर सुनाऊँगा।');};
$('#seniorBtn').onclick=()=>{SENIOR=!SENIOR;document.body.classList.toggle('senior',SENIOR);
  $('#seniorBtn').innerHTML=SENIOR?'👁 senior: ON':'👁 senior: off';
  toast(SENIOR?'Senior mode ON — bada font, high contrast':'Senior mode off');};

/* ---------- SEBI / NSDL registry helpers (R26 — borrowed *concept*, apna code) ---------- */
const SEBI={'regno':[/^INZ\d{9}$/i,/^INH\d{9}$/i,/^INA\d{9}$/i,/^INB\d{9}$/i],
  'portals':['sebi.gov.in','scores.gov.in','smartodr.in','nsdl.co.in','nseindia.com','bseindia.com','ipv.ndml.in'],
  'debarred_demo':['rajesh sharma','guaranteed returns pvt ltd','vip profit club']};
function sebiCheck(text){
  const out={claim:false, regno:null, format_ok:false, flags:[], links:[]};
  const claim=/sebi\s*(registered|registration|reg\.?)|sebi\s*(approved|certified)|scores|nsdl/i.test(text) || /sebi/i.test(text);
  out.claim=!!claim;
  const m=text.match(/\b(?:reg(?:istration)?\.?\s*(?:no\.?|number)?\s*[:\-]?\s*)?(IN[ZAHB]\d{9})\b/i);
  if(m){ out.regno=m[1].toUpperCase();
    out.format_ok=SEBI.regno.some(rx=>rx.test(out.regno));
    out.flags.push([out.format_ok?'amber':'red',
      out.format_ok?`Registration number ${out.regno} ka format theek hai — par hum ise verify nahi karte, sebi.gov.in par khud check karo`
                    :`Registration number “${out.regno}” ka format galat hai (SEBI ka format: INZ/INH/INA + 9 digits)`]);
  } else if(claim){
    const moneyAsk=/upi|@|\bpaisa\b|bhej|pay|payment|deposit|join|group|whats?app|telegram|contact|\d{10}/i.test(text);
    const cm=text.match(/(?:sebi|reg(?:istration)?)[\s:.\-]{0,3}([A-Za-z]{0,6}[\s-]?\d{3,10})\b/i);
    if(cm) out.flags.push(['red',`“${cm[0].replace(/\s+/g,' ').trim().toUpperCase()}” SEBI registration number ke format me hi nahi hai (asli: INZ/INH/INA + 9 digits) — aisa number verify nahi ho sakta`]);
    else if(moneyAsk) out.flags.push(['red','“SEBI” ka naam + paisa/join ka zikr hai, par koi registration number nahi diya gaya']);
    // sirf awareness/education me SEBI ka zikr (koi money-ask nahi) => flag nahi — warn karne ki zarurat nahi
  }
  for(const b of SEBI.debarred_demo){ if(text.toLowerCase().includes(b)) out.flags.push(['red',`“${b}” hamari debarred/watch list me hai (demo list)`]); }
  for(const m2 of text.matchAll(/(?:https?:\/\/)?([\w.\-]+\.(?:gov\.in|co\.in|in|com|org|xyz|online|top))(?:\/[\w?=&.\-]*)?/gi)){
    out.links.push(m2[1].toLowerCase());
  }
  const fakePortal=out.links.find(h=>/sebi|nsdl|scores|smartodr/.test(h) && !SEBI.portals.some(p=>h===p||h.endsWith('.'+p)));
  if(fakePortal) out.flags.push(['red',`“${fakePortal}” official SEBI/NSDL portal nahi hai — lookalike domain (M3 engine + R26)`]);
  return out;
}

/* ---------- negation-aware (R27 — concept: bank ka apna OTP warning flag na ho) ---------- */
const NEG_RX=/(never|do ?n[o']?t|dont|don't|kabhi\s*(bhi\s*)?(mat|na)|mat\s*(karo|karna|batana|bataye|share|daalna)|na\s*(karein|karo|bataye)|bina\s*(bataye|puche))\s*[^.!?]{0,60}$/i;
function isNegated(text, idx, win=90){
  const s=Math.max(0,idx-win), piece=text.slice(s, idx+60);
  // order 1: "kabhi bhi OTP share mat karo"
  if(/(never|do ?n[o']?t|don't|dont|kabhi|mat|ना|नहीं)\s*[^.!?]{0,40}(share|otp|pin|batana|bataye|karo|karein|daal|bhej)/i.test(piece)) return true;
  // order 2 (keyword pehle): "OTP/PIN share karne ko nahi kahenge"
  if(/(share|otp|pin|cvv|password|details)\s*(karna|karne|kar)?\s*[^.!?]{0,30}(nahi|na |mat|never|नहीं)/i.test(piece)) return true;
  return false;
}
function applyNegation(text, hit){
  if(!hit) return {hit, negated:false};
  const m=text.match(hit.re);
  if(m && m.index!=null && isNegated(text, m.index)) return {hit, negated:true};
  // agar poore text me koi negation+keyword combo hai aur koi doosra risk signal nahi to wahi handle karo
  return {hit, negated:false};
}

/* ---------- ek hi engine: M4 chat + eval harness dono classify() se chalte hain ---------- */
function classify(text){
  const R=redactPII(text); const t=R.text;
  const passRule=RULES4.find(r=>r.fam==='PASS'&&r.re.test(t));
  let hit=RULES4.find(r=>r.fam!=='PASS'&&r.re.test(t));
  const neg=applyNegation(t, hit);
  // R27: agar message khud safety-warning de raha hai to use scam na gino —
  // par asli scam-words (guarantee/deposit/safe account) ho to negation ka asar nahi.
  const negated = neg.negated && !/guarantee|assured|\d{2,3}\s*%|profit|safe account|officer|deposit|refund.{0,20}upi|freeze|arrest|paytm|gpay|phonepe/i.test(t);
  if(negated) hit=null;
  const S=sebiCheck(t);
  const link=linkIn(t); const dom=link?checkDomain(link):null;
  let verdict;
  if(hit) verdict = hit.sev==='bad' ? 'SCAM LIKELY' : 'CAUTION';
  else if(S.flags.some(f=>f[0]==='red')||(dom&&dom.level>=4)) verdict='SCAM LIKELY';
  else if(S.flags.length||(dom&&dom.level===3)) verdict='CAUTION';
  else if(passRule||negated) verdict='SEEMS OK';
  else verdict='UNCERTAIN';
  return {R,t,passRule,hit,negated,S,link,dom,verdict};
}

/* ---------- client-side PII redaction (R28) ---------- */
function redactPII(text){
  let n=0; const bump=()=>{n++;};
  const red = text
    .replace(/\b(?:\+91[\s-]?)?[6-9]\d{9}\b/g, m=>{bump();return m.slice(0,3)+'XXXXX'+m.slice(-2);})
    .replace(/\b[\w.\-]{2,}@(?:ybl|okaxis|oksbi|okhdfcbank|okicici|paytm|axl|ibl|ptaxis|upi|apl)\b/gi, m=>{bump();return m.slice(0,3)+'•••@'+m.split('@')[1];})
    .replace(/\b\d{4}\s?\d{4}\s?\d{4}\b/g, m=>{bump();return 'XXXX XXXX '+m.slice(-4);})
    .replace(/\b(?:[Xx]{4,}\d{3,6}|\d{9,18})\b/g, m=>{bump();return 'X'.repeat(Math.max(0,m.length-4))+m.slice(-4);})
    .replace(/\b\d{4}\s?\d{4}\s?\d{4}\s?\d{4}\b/g, m=>{bump();return 'XXXX XXXX XXXX '+m.slice(-4);});
  return {text:red, count:n};
}

/* ---------- tabs ---------- */
function showTab(id){
  $$('#tabs button').forEach(b=>b.classList.toggle('on',b.dataset.m===id));
  $$('.module').forEach(m=>m.classList.toggle('on',m.id===id));
  try{window.scrollTo({top:0,behavior:"smooth"});}catch(e){}
}
$$('#tabs button').forEach(b=>b.onclick=()=>showTab(b.dataset.m));

/* ---------- demo script ---------- */
const DEMO_STEPS=[
 ['m1','Saved contact + 2 min chat → 1 min check → direct pay (ya 1-tap call confirm)'],
 ['m1','Saved contact + 12 min chat → NOTIFICATION (lambi baat + turant UPI = pressure)'],
 ['m1','Unknown contact → 8 min guided check; unknown + 10 min chat → HOLD + callback'],
 ['m2','4 payment screenshots 30 min me + fake site → radar red'],
 ['m3','rbi-nodal-verify.xyz link → L4 lookalike; Razorpay checkout → gateway OK, merchant unverified'],
 ['m4','User poochhta hai: “Telegram task offer legit hai?” → verdict + 3 reason + 1930'],
 ['m5','Wallet audit: daily ₹299 mandate, 2 din purana, unverified domain → revoke'],
 ['m6','Intel Desk: aaj ke live crawl me naye patterns → review queue → library push'],
 ['m7','Auto-Report: asli case ke 28 screenshots → crop + redact + OCR → 1930/NCRP/SCORES pack'],
 ['m3','SEBI registry: “SEBI registered” dawa + fake registration number + lookalike portal (R26)'],
 ['m4','Sanchalak: negation-aware (bank ka OTP warning flag nahi hota) + client-side PII redaction + 30-message eval'],
];
const stepsEl=document.createElement('div');
stepsEl.style.cssText='display:none;position:fixed;inset:0;background:#04070ecc;backdrop-filter:blur(3px);z-index:80;padding:40px 18px;overflow:auto';
stepsEl.innerHTML=`<div style="max-width:760px;margin:0 auto" class="card">
  <div class="spread"><h3>▶ 3-minute demo script (judges ke liye)</h3><button class="sm" id="dsClose">close ✕</button></div>
  <div class="hr"></div><div class="stepper" id="dsList"></div>
  <div class="tiny dim" style="margin-top:12px">Tip: header me <b>speed ×30</b> on hai — 9-min gate demo me 18 second me chalta hai. Real device par wahi logic asli 9 minute leta hai.</div>
</div>`;
document.body.appendChild(stepsEl);
$$('#dsList').length;
DEMO_STEPS.forEach(([m,txt],i)=>{const s=document.createElement('div');s.className='step';
  s.innerHTML=`<b>${i+1}</b> ${esc(txt)}`;s.onclick=()=>{stepsEl.style.display='none';showTab(m);};$('#dsList').appendChild(s);});
$('#demoBtn').onclick=()=>stepsEl.style.display='block';
stepsEl.onclick=e=>{if(e.target===stepsEl)stepsEl.style.display='none';};
$('#dsClose').onclick=()=>stepsEl.style.display='none';

/* ============================================================
   M1 · CHAT-BEFORE-PAY  (contact tier + CHAT-SESSION DURATION)
   Matrix: known+chhoti chat = 1 min → direct pay (ya saved number par call)
           unknown = 8–10 min guided check (samjhaane me time lagta hai)
           chat ≥ 10 min ke baad UPI khula = NOTIFICATION (chahe contact known ho)
           unknown + 8 min+ chat = HOLD + callback
   ============================================================ */
const M1SC={
 k_short:{tag:'Known · 2 min chat',app:'WhatsApp',av:'P',who:'Prince Singh (saved)',sub:'saved contact · 18 mahine se',known:true,session:130,gap:'11 min',
   chat:[['out','Bhai kal ka ₹1,200 bhej deta hoon','9:35'],['in','Haan bhej do, wahi number hai','9:36'],['in','jaldi kar, shop band ho rahi 😅','9:36']],
   payee:'demo.payee@okhdfcbank',amount:1200,norm:1950,newPayees:1,seen:'18 mahine'},
 k_long:{tag:'Known · 12 min chat',app:'WhatsApp',av:'प',who:'“Papa” (saved) · +91 98xxx 33710',sub:'saved contact — par 12 min ki chat',known:true,session:735,gap:'40 s',
   chat:[['in','Papa mera phone kharab ho gaya, ye naya number hai','9:22'],['in','ek kaam tha, urgent ₹15,000 transfer karna hai','9:24'],['out','itni jaldi?','9:26'],['in','hospital me hoon, baad me batata hoon. UPI kar do: helpdesk.verify@ybl','9:30'],['in','kisi ko batane ki zarurat nahi, baad me samjhaunga','9:32']],
   payee:'helpdesk.verify@ybl',amount:15000,norm:1950,newPayees:2,seen:'number 3 din purana'},
 u_short:{tag:'Unknown · 5 min chat',app:'WhatsApp',av:'व',who:'+91 9xxxx 21473',sub:'unknown contact · saved nahi hai',known:false,session:318,gap:'2 min 10 s',
   chat:[['in','Aapka parcel customs me atka hai','9:36'],['in','chhota verification payment kar do — ₹12,000','9:38'],['out','kaise karun?','9:40'],['in','is ID par: quickearn-pro@ybl','9:40']],
   payee:'quickearn-pro@ybl',amount:12000,norm:1950,newPayees:3,seen:'3 din pehle'},
 u_long:{tag:'Unknown · 10 min chat',app:'WhatsApp',av:'व',who:'+91 9xxxx 21473',sub:'unknown · 10 min 12 s ki chat',known:false,session:612,gap:'35 s',
   chat:[['in','Main CBI officer Sharma. Aapke naam par case open hai','9:28'],['in','ghabrao mat, kisi ko batana mat, warna arrest','9:30'],['out','ji','9:35'],['in','video call par aao, apne paise RBI safe account me transfer karo','9:37'],['in','abhi UPI kholo: safe-verify@ybl · ₹85,000','9:38']],
   payee:'safe-verify@ybl',amount:85000,norm:1950,newPayees:4,seen:'5 ghante pehle'},
 nochat:{tag:'Direct payment (no chat)',app:'PhonePe',av:'₹',who:'Chat context: koi nahi',sub:'WhatsApp/Telegram ke bina UPI khula',known:null,session:0,gap:'—',
   chat:[],payee:'quickearn-pro@ybl',amount:12000,norm:1950,newPayees:3,seen:'3 din pehle'},
};
const M1TIERS={
 QUICK :{tier:'T1 · Fast path',    need:60,  cls:'ok',   title:'✅ Fast path — saved contact, chhoti chat',
         say:'Saved contact + chhoti chat = 1 minute check, phir seedha payment (ya saved number par call karke confirm).'},
 NOTIFY:{tier:'T1 · Notification', need:180, cls:'warn', title:'🟠 Notification — 10+ min chat ke baad payment',
         say:'Chat lambi thi, phir turant UPI khula. Ye coercion/pressure ka pattern hai (number saved hone par bhi — account hack ho sakta hai).'},
 COOLING:{tier:'T2 · Cooling',     need:480, cls:'warn', title:'🟠 8 minute guided check — unknown contact',
         say:'Unknown contact ko samajhne/samjhaane me time lagta hai. 8 min me chat dobara padho + 3 sawal.'},
 HOLD  :{tier:'T3 · Hold',         need:600, cls:'bad',  title:'🔴 Hold — unknown + lambi chat',
         say:'Unknown number + 10 min ki chat + turant payment = digital-arrest/task script. Hold + analyst callback.'},
};
function m1Decide(s){
  if(s.known===null) return 'QUICK';
  if(!s.known && s.session>=480) return 'HOLD';
  if(!s.known) return 'COOLING';
  if(s.session>=480) return 'NOTIFY';
  return 'QUICK';
}
let m1={key:null,dwell:0,timer:null,cleared:false,answers:{},gateState:null,tier:null};
function m1Timeline(txt,kind){const el=$('#m1timeline');
  if(el.dataset.init!=='1'){el.dataset.init='1';el.innerHTML='<table><thead><tr><th>t</th><th>event (metadata only)</th><th>source</th></tr></thead><tbody></tbody></table>';}
  const tb=$('#m1timeline tbody');const tr=document.createElement('tr');
  const c=kind==='bad'?'#e0a49c':kind==='ok'?'#a3d6c5':'#cdd9ee';
  tr.innerHTML=`<td class="mono" style="color:var(--dim)">${new Date().toLocaleTimeString('en-IN',{hour12:false})}</td><td style="color:${c}">${txt}</td><td class="tiny dim">on-device</td>`;
  tb.prepend(tr);}
function m1Notify(tier,s){
  const T=M1TIERS[tier];
  if(tier==='QUICK'||tier==='COOLING'){ $('#m1notif').innerHTML=''; return; }
  const head = tier==='HOLD'
    ? `Aapke saath 10 minute chat ke baad payment app khula hai. Ruko — pehle ek baar check karo.`
    : `10+ minute chat ke baad payment app khula. Kya koi aapko paisa bhejne ko keh raha hai?`;
  $('#m1notif').innerHTML=`<div class="notif">
    <div class="row"><span class="ic ${tier==='HOLD'?'bad':'warn'}" style="width:22px;height:22px;border-radius:7px;display:grid;place-items:center;font-size:12px">🔔</span>
      <b style="font-size:12.5px">SatarkPay · abhi</b><span class="tiny dim2" style="margin-left:auto">tap → check kholo</span></div>
    <div class="tiny" style="margin-top:6px">${head}<br><span class="dim">${esc(s.who)} — ${Math.floor(s.session/60)} min chat, gap ${s.gap}</span></div>
  </div>`;
}
function m1Signals(){
  const s=M1SC[m1.key]; if(!s)return; const T=M1TIERS[m1.tier||'QUICK'];
  const rows=[
    ['App switch','WhatsApp/Telegram → UPI','UsageStats event (93% devices)'],
    ['Chat session (last)',s.session?Math.floor(s.session/60)+' min '+pad(s.session%60)+' s':'—', s.session>=480?'LAMBI chat → notification':'normal'],
    ['Contact saved?',s.known===null?'n/a (chat nahi)':(s.known?'haan — '+s.seen:'NAHI — '+s.seen),s.known===false?'red flag':'normal'],
    ['Session → UPI gap',s.gap,(parseInt(s.gap)||0)<=5&&s.gap!=='—'?'short gap = pressure':'gap normal'],
    ['Gate (tier)',T.tier,'rule R15/R23 matrix'],
    ['Chat dwell (abhi)',mmss(m1.dwell),'required '+(s.known===null?'0':T.need/60<1?(T.need/60).toFixed(2)+' min':(T.need/60)+' min')],
    ['Payee',s.payee,'first-time: '+(m1.key.startsWith('k_')?'NO':'YES')],
    ['Amount vs norm','₹'+s.amount.toLocaleString('en-IN')+' vs ₹'+s.norm.toLocaleString('en-IN'),(s.amount/s.norm).toFixed(1)+'× normal'],
    ['New payees 24h',s.newPayees,s.newPayees>=3?'escalation risk':'ok'],
    ['Message content','NAHI padha','— metadata only'],
  ];
  $('#m1sigs').innerHTML=rows.map(r=>`<div class="sig"><span class="lbl">${esc(r[0])}<div class="tiny dim2">${esc(r[2])}</div></span>
    <span class="v" style="max-width:150px;text-align:right">${esc(r[1])}</span></div>`).join('');
}
function m1Paint(){
  const s=M1SC[m1.key];
  $('#m1av').textContent=s.av; $('#m1who').textContent=s.who; $('#m1sub').textContent=s.sub;
  document.querySelector('#m1 .appbar .t').childNodes[0].nodeValue=s.app+' · ';
  $('#m1chat').innerHTML=s.chat.map(c=>`<div class="bub ${c[0]}"><div class="who">${c[0]==='in'?esc(s.who):'Aap'} · ${c[2]}</div>${esc(c[1])}</div>`).join('')
    + (s.chat.length?'':'<div class="note tiny">Chat nahi hai. Guard ne sirf UPI app me: naya payee + 6× amount dekha → Tier-1 nudge (0 min friction).</div>');
}
function m1Ring(){
  const s=M1SC[m1.key],T=M1TIERS[m1.tier||'QUICK'],need=T.need||1;
  const p=Math.min(1,m1.dwell/need);
  $('#m1arc').style.strokeDashoffset=String(276*(1-p));
  $('#m1arc').setAttribute('stroke',p>=1?'#3fae94':T.cls==='bad'?'#cd7a6e':T.cls==='warn'?'#d9a441':'#c9a86a');
  $('#m1clock').textContent=s.known===null?'—':mmss(Math.max(0,need-m1.dwell));
  const SHORT={QUICK:s.known===null?'no gate':'1-min check',NOTIFY:'10-min rule',COOLING:'8-min guided',HOLD:'hold + callback'};
  $('#m1phase').textContent=p>=1?'clear ✓':SHORT[m1.tier||'QUICK'];
  $('#m1phase').style.fontSize='9.5px';
}
function m1Questions(tier){
  const Q={
   QUICK:[['q1','Main khud is insaan ko paisa bhejna chahta hoon (kisi ne keh kar nahi)']],
   NOTIFY:[['q1','Main khud bhej raha hoon — kisi ne keh kar nahi'],['q3','Abhi koi call/chat me paisa bhejne ko keh raha hai']],
   COOLING:[['q1','Main khud is insaan ko paisa bhejna chahta hoon (kisi ne keh kar nahi)'],['q2','Ye kisi “officer / support / offer” ke kehne par ho raha hai'],['q3','Abhi koi call ya video call chal rahi hai jisme ye batai ja rahi hai']],
   HOLD:[['q1','Main khud bhej raha hoon — kisi ne keh kar nahi'],['q2','Ye kisi “officer / support / offer” ke kehne par ho raha hai'],['q3','Abhi koi call ya video call chal rahi hai']],
  }[tier];
  return Q;
}
function m1Gate(){
  const s=M1SC[m1.key],T=M1TIERS[m1.tier],p=s.known===null?1:Math.min(1,m1.dwell/T.need);
  const state=(s.known===null?'nudge':p>=1?'clear':'wait')+m1.tier;
  if(m1.gateState===state) return;
  m1.gateState=state;
  m1Notify(m1.tier,s);
  if(s.known===null){
    $('#m1hint').textContent='Chat context nahi mila — sirf nudge, koi hold nahi.';
    $('#m1gateUI').innerHTML=`<div class="warnbox tiny"><b>🟡 Nudge (Tier-1, 0 min friction)</b><br>“Pehli baar is account ko paisa bhej rahe ho, aur amount aapke normal se 6× zyada hai. Naam confirm karke aage badho.”</div>`;
    return;
  }
  if(p<1){
    $('#m1hint').textContent=`${T.tier} — ${mmss(T.need-m1.dwell)} baaki. Timer sirf chat screen par chalta hai.`;
    $('#m1gateUI').innerHTML=`<div class="${T.cls==='bad'?'badbox':T.cls==='warn'?'warnbox':'okbox'} tiny">
      <b>${esc(T.title)}</b><br>${esc(T.say)}
      <div class="dim2" style="margin-top:6px">Aapka payment ruka hua hai — block nahi. Har waqt cancel kar sakte ho.</div></div>`;
    return;
  }
  const qs=m1Questions(m1.tier);
  $('#m1hint').textContent='Gate clear — '+(qs.length>1?'sawaal, phir aapka decision':'ek confirm, phir direct payment');
  $('#m1gateUI').innerHTML=`<div class="${T.cls==='bad'?'badbox':'okbox'} tiny"><b>${esc(T.title)} — ${qs.length>1?'ab final check':'confirm'}</b>
    <div id="m1qs" style="margin-top:9px"></div>
    ${m1.tier==='QUICK'?`<div class="row" style="margin-top:9px"><button class="sm primary" id="m1call">📞 Saved number par call karke confirm karo</button></div>`:''}
    </div>`;
  $('#m1qs').innerHTML=qs.map(q=>`<div class="spread" style="padding:5px 0"><span>${esc(q[1])}</span>
    <span><button class="sm" data-q="${q[0]}" data-v="1">haan</button> <button class="sm" data-q="${q[0]}" data-v="0">nahi</button></span></div>`).join('')
    +`<div id="m1final" style="margin-top:8px"></div>`;
  $$('#m1qs button').forEach(b=>b.onclick=()=>{m1.answers[b.dataset.q]=+b.dataset.v;m1Final();});
  const c=$('#m1call');
  if(c) c.onclick=()=>{m1Timeline('user ne saved number par call karke confirm kiya → gate clear (fast path)','ok');
    $('#m1final').innerHTML=`<div class="okbox tiny"><b>✅ Call confirm ho gaya — seedha payment.</b><br>
      <div class="row" style="margin-top:8px"><button class="sm primary">Pay par jao</button></div></div>`;};
}
function m1Final(){
  const a=m1.answers, T=M1TIERS[m1.tier];
  const need=m1Questions(m1.tier).map(q=>q[0]);
  if(!need.every(k=>k in a)){$('#m1final').innerHTML='<span class="tiny dim">Sawaal ka jawab do.</span>';return;}
  const danger=(a.q2===1)||(a.q3===1);
  if(danger||m1.tier==='HOLD'){
    $('#m1final').innerHTML=`<div class="badbox tiny"><b>🔴 Hold + analyst callback (Tier-3, rule R1)</b><br>
      “Koi officer, bank ya support UPI par paisa nahi mangta.” Analyst 5 min me in-app call karega (SMS link nahi).
      <div class="row wrap" style="margin-top:8px"><button class="sm danger" id="m1panic">🆘 Panic freeze</button><button class="sm" id="m1pack">1930 evidence pack</button></div></div>`;
    m1Timeline(`answers: officer/offer/call = HOLD tier → <b>Tier-3</b> + analyst callback`,'bad');
    $('#m1panic').onclick=()=>toast('Panic freeze: mandated payments + new payees par 5 min lock (demo)','bad');
    $('#m1pack').onclick=()=>toast('1930 pack ready: timeline + hashed payee + reasons','ok');
  } else {
    $('#m1final').innerHTML=`<div class="okbox tiny"><b>✅ Clear — decision aapka.</b><br>Pay screen khul sakta hai. ${m1.tier==='QUICK'?'Fast path (saved contact).':'24 ghante ka sticky watch naye payee par rahega.'}
      <div class="row" style="margin-top:8px"><button class="sm primary">Pay par jao (user decides)</button></div></div>`;
    m1Timeline('answers clean → gate clear, user decides (pay allowed)','ok');
  }
}
function m1Tick(){
  const s=M1SC[m1.key],T=M1TIERS[m1.tier||'QUICK'];
  if(s.known!==null){ m1.dwell+=(SPEED===30?3:0.1);
    if(m1.dwell>=T.need&&!m1.cleared){m1.cleared=true;clearInterval(m1.timer);m1.timer=null;
      m1Timeline(`chat dwell complete (${(T.need/60).toFixed(2)} min) → <b>gate clear</b>`,'ok');} }
  m1Ring();m1Signals();
  if(m1.cleared){ if(!$('#m1qs')) m1Gate(); }
  else if(s.known!==null){ $('#m1hint').textContent=`${T.tier} — ${mmss(Math.max(0,T.need-m1.dwell))} baaki. Timer sirf chat screen par chalta hai.`; }
}
function m1Run(key){
  clearInterval(m1.timer);
  const s=M1SC[key]; const tier=m1Decide(s);
  m1={key,dwell:0,timer:null,cleared:false,answers:{},gateState:null,tier};
  m1Paint();m1Signals();m1Ring();m1Gate();
  $('#m1timeline').dataset.init='0';$('#m1timeline').innerHTML='<table><thead><tr><th>t</th><th>event (metadata only)</th><th>source</th></tr></thead><tbody></tbody></table>';
  m1Timeline(`${s.app} foreground — ${s.known===null?'chat ke bina':esc(s.who)}`);
  if(s.session) m1Timeline(`chat session ${Math.floor(s.session/60)}m ${pad(s.session%60)}s (UsageStats) · session→UPI gap ${s.gap}`, s.session>=480?'bad':'');
  m1Timeline(`decision matrix: ${s.known===null?'no-chat → nudge':(s.known?'known':'unknown')+' + '+(s.session>=480?'lambi chat':'chhoti chat')} → <b>${tier}</b>`, tier==='HOLD'?'bad':'');
  if(tier==='HOLD'||tier==='NOTIFY') m1Timeline(`notification delivered: “${M1TIERS[tier].title}”`,'bad');
  m1Timeline(`journey memory: payee ${s.payee} · new-payees-24h ${s.newPayees} · amount ₹${s.amount} (${(s.amount/s.norm).toFixed(1)}× norm) · message text NOT read`);
  if(s.known!==null) m1.timer=setInterval(m1Tick,100);
  else m1.cleared=true;
}
$$('#m1 .chip[data-m1sc]').forEach(c=>c.onclick=()=>m1Run(c.dataset.m1sc));
$('#m1run').onclick=()=>m1Run(m1.key||'k_short');
$('#m1reset').onclick=()=>{clearInterval(m1.timer);m1={key:null,dwell:0,timer:null,cleared:false,answers:{},gateState:null,tier:null};
  $('#m1chat').innerHTML='';$('#m1sigs').innerHTML='';$('#m1notif').innerHTML='';
  $('#m1gateUI').innerHTML='<div class="note">Run karo, phir yahan gate ka asli wording (HI + EN) aayega.</div>';
  $('#m1hint').textContent='Scenario select karke Run dabao';$('#m1clock').textContent='--:--';$('#m1phase').textContent='idle';
  $('#m1arc').style.strokeDashoffset='276';$('#m1timeline').innerHTML='<span class="tiny dim">Run karo — timeline yahan banti hai.</span>';};

/* ============================================================
   M2 · SCREENSHOT RADAR
   ============================================================ */
const SHOT_POOL=[
 {src:'PhonePe',kind:'payment',ext:'quickearn-pro@ybl · ₹299 · “QuickEarn Pro”',trust:'bad',why:'L4 domain + same payee 4×'},
 {src:'PhonePe',kind:'payment',ext:'quickearn-pro@ybl · ₹599',trust:'bad',why:'same payee dobara'},
 {src:'Browser (Chrome) · quickearn-pro.xyz',kind:'web',ext:'domain quickearn-pro.xyz',trust:'bad',why:'L4 lookalike-style TLD + age 6 din'},
 {src:'Telegram · @EarnFastIndia',kind:'chat',ext:'deposit screenshot maanga',trust:'warn',why:'task-scam family F3'},
 {src:'PhonePe',kind:'payment',ext:'earn-payout@axl · ₹500',trust:'bad',why:'naya payee + bot'}
];
let m2shots=[];
function m2Render(){
  $('#m2gal').innerHTML=m2shots.map((s,i)=>{
    const cls=s.trust==='bad'?'bad':s.trust==='warn'?'warn':'';const ic=s.kind==='payment'?'₹':s.kind==='web'?'🌐':'💬';
    return `<div class="shot"><div class="thumb">${ic}<span class="badge tag ${cls}">${s.trust==='bad'?'L4/L3':s.trust==='warn'?'caution':'ok'}</span></div>
    <div class="meta"><b>${esc(s.src)}</b>${esc(s.ext)}<div class="dim2">${esc(s.why)}</div></div></div>`;}).join('')
   ||'<div class="note tiny">Koi payment screenshot nahi. (Screenshot aane par ContentObserver trigger hota hai.)</div>';
  const pay=m2shots.filter(s=>s.kind==='payment').length;
  const bad=m2shots.some(s=>s.trust==='bad');
  $('#m2count').textContent=String(m2shots.length);
  $('#m2burst').innerHTML=pay+'<span style="font-size:13px; color:var(--dim)"> / 30 min</span>';
  $('#m2bar').style.width=Math.min(100,pay*20)+'%';
  $('#m2bar').style.background=pay>=5||bad?'linear-gradient(90deg,#d9a441,#cd7a6e)':'linear-gradient(90deg,#4bb5c9,#c9a86a)';
  const sev=$('#m2sev');
  if(pay>=5||bad){sev.className='tag bad';sev.textContent='RED';}else if(pay>=3){sev.className='tag warn';sev.textContent='amber';}else{sev.className='tag';sev.textContent='safe';}
  let b='';
  if(pay>=5||bad){b=`<div class="banner b tiny">🔴 <div><b>Payment-screenshot burst + risky source</b><br>
    ${pay} payment screenshots 30 min me${bad?' + ek unverified source (quickearn-pro.xyz / task-bot)':''}.<br>
    <span class="dim">Ye pattern task/investment scam ka hai — “proof” maangte rehte hain aur amount badhti jaati hai. Jo bhi pay karne ko keh raha hai, use ruk kar khud confirm karo (voice call, known number).</span></div></div>`;}
  else if(pay>=3){b=`<div class="banner w tiny">🟠 <div><b>3+ payment screenshots 30 min me</b><br>Burst detect hua. Abhi kuch nahi roka — bas ek amber warning + naya payee par extra check.</div></div>`;}
  else if(m2shots.length){b=`<div class="banner i tiny">🔎 <div><b>Provenance tag laga</b><br>Har screenshot ke saath: kaunsa app/domain foreground tha + extracted UPI ID/amount. Image delete, sirf tag rakha.</div></div>`;}
  $('#m2banner').innerHTML=b;
  $('#m2why').textContent = pay>=5? 'Burst red: risk of task-scam staircase (₹299 → ₹599 → …).' : pay>=3? 'Burst amber: agla payment par cooling lagegi.' :
    '3+ payment screenshots thode hi time me = task/investment scam ka classic pattern.';
  $('#m2copy').innerHTML = b ? b.replace(/class="banner [a-z]+ tiny"/,'style="font-size:11.5px"') : 'Burst ya unverified source par amber/red banner + wajah.';
}
$('#m2add').onclick=()=>{const s=SHOT_POOL[m2shots.length%SHOT_POOL.length];m2shots.push(s);m2Render();
  if(m2shots.filter(x=>x.kind==='payment').length>=3)toast('Screenshot Radar: burst detected (amber)','bad');};
$$('#m2 .chip[data-m2sc]').forEach(c=>c.onclick=()=>{
  const k=c.dataset.m2sc;
  if(k==='clean'){m2shots=[];toast('Gallery reset');m2Render();return;}
  if(k==='one'){m2shots=[SHOT_POOL[0]];}
  if(k==='burst'){m2shots=[SHOT_POOL[0],SHOT_POOL[1],SHOT_POOL[4],SHOT_POOL[2]];}
  if(k==='fake'){m2shots=[SHOT_POOL[2],SHOT_POOL[3]];}
  m2Render();});
m2Render();

/* ============================================================
   M3 · DOMAIN TRUST ENGINE
   ============================================================ */
const V_TRUE={
 gov:['rbi.org.in','npci.org.in','ncrp.gov.in','cybercrime.gov.in','pib.gov.in','incometax.gov.in','uidai.gov.in','digilocker.gov.in','epfindia.gov.in','vahan.nic.in','mparivahan.com','fastag.nhai.org','bharatkosh.gov.in','irctc.co.in','licindia.in'],
 bank:['onlinesbi.sbi','sbi.co.in','hdfcbank.com','icicibank.com','axisbank.com','kotak.com','pnb.bank.in','bankofbaroda.in','canarabank.com','unionbankofindia.co.in','idfcfirstbank.com','yesbank.in'],
 psp:['phonepe.com','paytm.com','google.com','amazon.in','cred.club','navi.com','mobikwik.com','freecharge.in','whatsapp.com','telegram.org'],
 merchant:['flipkart.com','myntra.com','swiggy.in','zomato.com','bigbasket.com','makemytrip.com','jio.com','airtel.in','vodafoneidea.com','netflix.com','tataneu.com','nykaa.com','ajio.com']};
const GATEWAYS=['razorpay.com','cashfree.com','billdesk.com','payu.in','instamojo.com','ccavenue.com','easebuzz.in','paytm.com','phonepe.com'];
const BRANDS={rbi:'rbi.org.in',sbi:'onlinesbi.sbi',hdfc:'hdfcbank.com',icici:'icicibank.com',axis:'axisbank.com',kotak:'kotak.com',
 paytm:'paytm.com',phonepe:'phonepe.com',gpay:'google.com',googlepay:'google.com',amazon:'amazon.in',amazonpay:'amazon.in',
 irctc:'irctc.co.in',epfo:'epfindia.gov.in',uidai:'uidai.gov.in',aadhaar:'uidai.gov.in',netflix:'netflix.com',jio:'jio.com',
 airtel:'airtel.in',vahan:'vahan.nic.in',fastag:'fastag.nhai.org',flipkart:'flipkart.com',whatsapp:'whatsapp.com',customs:'icegate.gov.in'};
const RISK_TLD=['xyz','top','online','click','icu','buzz','rest','club','site','cf','tk','ml','ga','gq','work','link','fit','cam','quest','monster','sbs','cyou','lol','shop','store'];
const SHORT=['bit.ly','tinyurl.com','t.co','rb.gy','cutt.ly','shorturl.at','is.gd','ow.ly','rebrand.ly','tiny.cc','goo.gl','s.id','clk.sh','bit.do','linkshort.site'];
function lev(a,b){const m=a.length,n=b.length,d=Array.from({length:m+1},(_,i)=>[i,...Array(n).fill(0)]);
 for(let j=0;j<=n;j++)d[0][j]=j;
 for(let i=1;i<=m;i++)for(let j=1;j<=n;j++)d[i][j]=Math.min(d[i-1][j]+1,d[i][j-1]+1,d[i-1][j-1]+(a[i-1]===b[j-1]?0:1));
 return d[m][n];}
const homo=s=>s.replace(/0/g,'o').replace(/1/g,'l').replace(/3/g,'e').replace(/5/g,'s').replace(/4/g,'a').replace(/rn/g,'m').replace(/vv/g,'w').replace(/-/g,'');
function checkDomain(raw){
  let u; try{u=new URL(/^[a-z]+:\/\//i.test(raw)?raw:'http://'+raw);}catch(e){return {level:4,host:raw,reasons:[['red','URL parse nahi hua — aise link par kabhi mat jao']],cat:'invalid'};}
  const host=u.hostname.toLowerCase().replace(/^www\./,'');
  const tld=host.split('.').pop();
  const reasons=[]; let level=3; let cat='unverified'; let gatewayMerchant=null;
  const inList=(arr)=>arr.some(d=>host===d||host.endsWith('.'+d));
  if(/^\d+\.\d+\.\d+\.\d+$/.test(host)) reasons.push(['red','IP address se website — asli business aisa nahi karta']);
  if(u.username||u.password||raw.includes('@')) reasons.push(['red','URL me “@” trick — jo dikhta hai woh asli destination nahi hota']);
  if(host.includes('xn--')) reasons.push(['red','Punycode (xn--) domain — milte-julte letters se banaya gaya hai (homoglyph)']);
  if(u.protocol==='http:') reasons.push(['red','HTTP — encryption nahi (lock icon nahi)']);
  if(SHORT.some(s=>host===s||host.endsWith('.'+s))) reasons.push(['red','URL shortener — asli destination chhupa hua hai']);
  if(RISK_TLD.includes(tld)) reasons.push(['amber','TLD .'+tld+' — fraud me bahut use hota hai (verified brand isse nahi lete)']);
  for(const [k,d] of Object.entries(BRANDS)){
    const hn=homo(host);
    const hitTok=hn.includes(k)||host.includes(k);
    const isReal=host===d||host.endsWith('.'+d);
    if(hitTok&&!isReal){reasons.push(['red',`${k.toUpperCase()} ka naam use ho raha hai par asli domain “${d}” nahi hai`]);}
    if(!isReal&&lev(homo(host.split('.').slice(-2).join('.')),homo(d))<=2){reasons.push(['red',`“${d}” se milta-julta domain (typo-squat)`]);}
  }
  const gwStyle=GATEWAYS.some(g=>host===g||host.endsWith('.'+g));
  if(gwStyle){
    gatewayMerchant=u.searchParams.get('merchant')||u.searchParams.get('m')||null;
    reasons.push(['amber','Payment gateway verified hai — par merchant alag entity hoti hai']);
    if(gatewayMerchant){
      const mh=homo(gatewayMerchant.toLowerCase());
      const badM=Object.keys(BRANDS).some(k=>mh.includes(k));
      if(badM||RISK_TLD.includes(gatewayMerchant.split('.').pop())||gatewayMerchant.includes('-offer')||gatewayMerchant.includes('earn')){
        reasons.push(['red',`gateway ke andar merchant “${gatewayMerchant}” — brand jaisa naam / risky naam, verified nahi`]);
        cat='gateway-merchant-risk';
      } else { reasons.push(['amber',`merchant “${gatewayMerchant}” hamari verified merchant list me nahi hai`]); cat='gateway-merchant-unverified'; }
    }
  }
  const lvl=reasons.some(r=>r[0]==='red')?4:null;
  if(inList(V_TRUE.gov)){level=1;cat='gov';}
  else if(inList(V_TRUE.bank)){level=1;cat='bank';}
  else if(inList(V_TRUE.psp)){level=1;cat='psp';}
  else if(inList(V_TRUE.merchant)){level=2;cat='merchant';}
  else {level=lvl||3;}
  if(level===4&&cat==='unverified') cat='lookalike';
  if(level>=3&&!reasons.length) reasons.push(['amber','Structure clean hai par hamari verified list me nahi hai']);
  return {level,host,tld,reasons,cat,gatewayMerchant,protocol:u.protocol,path:u.pathname+u.search};
}
const M3CAT={1:'Verified · ',2:'Verified merchant · ',3:'Unverified · ',4:'Lookalike / fake · '};
function m3Render(raw){
  const r=checkDomain(raw);
  const name=r.level===1?M3CAT[1]+r.cat.toUpperCase():r.level===2?M3CAT[2]+'allowlist':r.level===3?'Unverified':'Lookalike / fake';
  const cls=r.level<=2?'ok':r.level===3?'warn':'bad';
  const tldNote=RISK_TLD.includes(r.tld)?`TLD .${r.tld} — high-risk list`:`TLD .${r.tld}`;
  const copy=r.level<=2?`Ye domain hamari verified list me hai (${r.cat}). Phar bhi: payment se pehle amount + naam check karo.`
   :r.level===3?`“Ye website hamari verified list me nahi hai. Strutcure theek lag raha hai, par ${r.gatewayMerchant?'gateway ke andar merchant verified nahi':'unknown merchant'} — sirf tab pay karo jab aapne khud ye site kholi ho.”`
   :`“Ye ${r.host} asli ${r.cat==='gov'?'sarkari':'company'} ki website nahi lagti. Yahan payment mat karo. Koi aapko is link par bhej raha hai to woh scam hai.”`;
  const steps=r.level>=3?['Ruk jao — is page par payment mat karo','Asli app/site khud khol kar check karo (link se nahi)','Kisi ne bheja hai to screenshot + number save karo (1930 ke liye)','Bhejne wale ko call karke known number par confirm karo']
    :['Payment se pehle merchant ka naam screen par check karo','Amount + UPI ID match karo'];
  $('#m3out').innerHTML=`
   <div class="verdict" style="border-color:${cls==='ok'?'#2e6b57':cls==='warn'?'#6b5528':'#5c333c'}">
     <div class="spread"><b>${r.level===4?'🔴':r.level===3?'🟠':'🟢'} ${esc(name)}</b><span class="tag ${cls}">L${r.level}</span></div>
     <div class="tiny dim mono" style="margin-top:6px">${esc(r.host)} · ${esc(tldNote)}${r.gatewayMerchant?' · merchant: '+esc(r.gatewayMerchant):''}</div>
     <div style="margin-top:9px">${r.reasons.map(x=>`<div class="tiny" style="margin:4px 0"><span class="tag ${x[0]==='red'?'bad':'warn'}" style="margin-right:6px">${x[0]}</span>${esc(x[1])}</div>`).join('')}</div>
     <div class="note tiny" style="margin-top:9px">User ko dikhne wali copy (HI): ${esc(copy)}</div>
     <div class="tiny dim" style="margin-top:8px">TURANT: ${steps.map(esc).join(' · ')}</div>
     <div class="tiny dim2" style="margin-top:6px">Production me + RDAP domain-age (live call), DoT FRI, I4C suspect registry — demo me age/registry simulated hai.</div>
   </div>`;
}
$('#m3go').onclick=()=>m3Render($('#m3url').value);
$$('#m3 .chip[data-u]').forEach(c=>c.onclick=()=>{$('#m3url').value=c.dataset.u;m3Render(c.dataset.u);});
m3Render($('#m3url').value);

/* ============================================================
   M4 · AI SANCHALAK
   ============================================================ */
const EX=[
 'Main CBI officer Sharma bol raha hoon. Aapke naam par parcel case hai. Video call par aao aur apne paise RBI safe account me transfer karo — kisi ko mat batao.',
 'Telegram par task job: ₹17 per YouTube like, daily payout. Joining ke liye ₹500 deposit — 30 min me ₹650 wapas.',
 'Dear customer, aapka KYC expire ho gaya hai. 24 ghante me UPI band. Yahan se update karo: http://sbi.onlinekyc-update.top/verify aur KYC.apk install karo — support: AnyDesk se help karenge.',
 'Maine galti se aapko ₹2,000 bhej diye. Aap ₹20,000 wapas bhej do, main baad me ₹18,000 adjust kar lunga. Please turant UPI karo.',
 'Amazon Pay: ₹1,499 ka refund order #A82 ke liye aapke HDFC account (****4432) me credit ho gaya hai. Koi PIN/scan ki zaroorat nahi.',
 'Sir, main SEBI registered advisor hoon (Reg: SEBI12345). Aap is VIP Telegram group me aaiye — guaranteed 300% profit in 48 hours. Payment is UPI par karo: vipadvisor@oksbi, contact 9876543210.',
 'Dear customer, aapki safety ke liye: hum aapse kabhi bhi OTP, PIN ya CVV share karne ko nahi kahenge. Kisi ko mat batana. Ye alert aapke account ki suraksha ke liye hai.',
];
const RULES4=[
 {re:/digital arrest|safe account|(cbi|police|customs|narcotics)\s*(officer)?|case (open|hai)|kisi ko mat batao|video call/i, fam:'F1',
  verdict:'SCAM LIKELY',sev:'bad',why:['“Officer” + “safe account” + “kisi ko mat batao” — digital arrest ka classic script','Sarkari agency kabhi UPI par paisa nahi mangti (rule R1)','Isolation + urgency: victim ko ghar walon se alag rakhna'],
  doo:['Call turant kaat do','Apne bank ko call karo (card ke peeche ka number)','1930 par report + NCRP portal','Ghar walon ko batao — sharam nahi, ye organised crime hai'],dont:'Koi bhi “verification” payment mat karo; screen share mat karo.'},
 {re:/\btask\b|\blike\b|per like|\bdeposit\b|joining fee|daily payout|return %|invest/i, fam:'F3',
  verdict:'SCAM LIKELY',sev:'bad',why:['Chhota “deposit” → payout ka vaada = task-scam staircase','Telegram/WhatsApp group se aata hai, koi registered company nahi','Amount aage badhta hai: ₹500 → ₹5,000 → ₹50,000'],
  doo:['Koi deposit mat karo — real job joining fee nahi leti','Pehle mile “payout” ko trust ka proof na samjho','Screenshot + chat save karo, 1930 me daalo'],
  dont:'“Ek baar chhota sa” — yehi trap hai.'},
 {re:/galti se|sent by mistake|wapas bhej|refund back|excess|zyada bhej diya/i, fam:'F22',
  verdict:'SCAM LIKELY',sev:'bad',why:['₹2,000 aaya + ₹20,000 maanga = refund-back trap (3–10× ratio)','Asli refund khud credit hota hai — aapko bhejna nahi padta'],
  doo:['Apne app me dekho paisa aaya bhi hai ya nahi','Bank se dispute karo, seedha transfer mat karo'],
  dont:'“Screenshot” par bharosa mat karo.'},
 {re:/\bkyc\b|\.apk\b|\bapk\b|anydesk|teamviewer|\bremote\b|expire|screen shar/i, fam:'F2',
  verdict:'SCAM LIKELY',sev:'bad',why:['KYC update ke naam par APK/remote app = account takeover','Banks kabhi APK ya screen-share nahi maangte','“24 ghante” deadline = pressure tactic'],
  doo:['APK delete karo, remote app uninstall karo','KYC sirf apni bank app/website se','Bank helpline (official) par call karke batayein'],
  dont:'Accessibility/install permission mat do.'},
 {re:/collect request|request money|receive money|pin daal|scan.{0,20}receive/i, fam:'F5',
  verdict:'SCAM LIKELY',sev:'bad',why:['“Paisa LENE ke liye PIN” — PIN sirf paisa BHEJNE par lagta hai','Collect request = aapka paisa jaayega'],
  doo:['Request reject karo','Direction screen dhyan se padho: “THIS DEBITS YOU”'],
  dont:'Refund/cashback ke liye pehle paisa mat bhejo.'},
 {re:/autopay|auto.?pay|mandate|recurring|daily|weekly|subscription/i, fam:'F7',
  verdict:'CAUTION — zyada',sev:'warn',why:['“One-time” batakar mandate lag raha ho to red flag hai','Daily/Weekly frequency = bar-bar paisa jaayega','Naya + unverified merchant = mandate-abuse pattern'],
  doo:['Mandate approve karne se pehle frequency + next debit date dekho','App me AutoPay list kholo aur unknown mandate revoke karo'],
  dont:'“Verify” ke naam par mandate approve mat karo.'},
 {re:/challan|fastag|vahan|bill|electricity|porta/i, fam:'F11',
  verdict:'CAUTION — zyada',sev:'warn',why:['Fake challan/FASTag portals mobile number se SMS aate hain','Asli alert VK-VAAHAN header se aata hai'],
  doo:['vahan.nic.in / official app se hi challan dekho','SMS sender ID check karo'],
  dont:'SMS ke link se card details mat daalo.'},
 {re:/refund|credited|order #|bank ke naam par|credited to your account/i, fam:'PASS', label:'legit-refund / service message',
  verdict:'SEEMS OK',sev:'ok',why:['Paisa aapke account ME AA raha hai — bhejne ke liye nahi','Koi PIN/scan/link nahi maanga gaya','Amount + masked account realistic hai'],
  doo:['Apne bank app me khud confirm karo (SMS par bharosa na karo)','Ye legit ho ya na ho — khud verify karna hamesha free hai'],
  dont:'Phir bhi link par click karke “claim” mat karo.'},
  {re:/guaranteed|assured return|\d{2,3}\s*%\s*(profit|return|monthly)|profit in \d|vip (group|telegram|club)|paid (telegram|whatsapp) group|sebi\s*registered\s*(advisor|expert)|portfolio manager|sure ?shot (tip|call)|double your (money|paisa)/i, fam:'F28',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['“Guaranteed / 300% profit” + VIP ya paid group — SEBI ke rules me aisa return-vaada allowed hi nahi hai','SEBI-registered hone ka dava + personal UPI handle (jaise vipadvisor@oksbi) = unregistered advisory pattern','F&O me 9/10 retail traders loss me hain — koi “sure profit” nahi de sakta (problem statement ka data)'],
  doo:['Paisa band karo — pehle claim kiya gaya number sebi.gov.in par khud check karo (INZ/INH/INA + 9 digits)','Screenshot + chat + payment proof save karo','SEBI SCORES (scores.gov.in) ya SMART ODR par complaint — unregistered advisory wahan report hoti hai'],
  dont:'“Registration number” ke bharose paisa mat bhejo — number, naam aur group teeno match karne padenge.'},
  {re:/safety|suraksha|security alert|kabhi bhi otp|otp.{0,40}share|share karne ko nahi|never share|mat (share|batana|bataye)|fraud se bacho|सुरक्षा|सतर्क/i, fam:'PASS', label:'safety-warning (bank/company ka apna alert)',
  verdict:'SEEMS OK',sev:'ok',
  why:['Ye message khud warning de raha hai (“OTP/PIN/CVV share na karo”) — scam iska ulta hota hai','Koi payment / UPI handle / link nahi maanga gaya','Asli bank/company ke security alerts bilkul aise hi likhe hote hain'],
  doo:['Phir bhi message ke link par click na karo — bank app khud kholo','Sender ID + official domain match karo','Shak ho to card/app ke peeche wale official number par call karo'],
  dont:'Safety-warning ko “trust proof” na samjho — kuch scam isi tone me aate hain.'},
  {re:/\b(cutt\.ly|bit\.ly|tinyurl\.com|rb\.gy|is\.gd|shorturl\.at|t\.co)\/|special (vip )?gift|withdraw instantly|free gift|turant withdraw|gift nikalo|claim karne ke liye is link/i, fam:'F29',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['Shortened link + “gift withdraw instantly” = luring pattern','Bank/company kabhi short-link se withdrawal nahi karati','Shortener asli destination chhupata hai — isi liye hi use kiya jaata hai'],
  doo:['Link na kholo — app khud kholo aur balance khud dekho','Aise “gift” ke aage ek aur step (fee/KYC) hota hai','Ye message M7 pack me daal ke 1930 par report karo'],
  dont:'“Instantly withdraw” ke lalach me form/KYC/screen-share mat karo.'},
  {re:/\b(registration|processing|verification|advance|release|clearance)\s*fee\b|loan\s*(approved|sanction)|emi pending|(personal|private)\s*upi\s*par\s*(pay|bhej)|warna legal action|refund.{0,25}(ke liye).{0,25}(fee|tax|charge)|tax\s*₹?\s*[\d,]{3,}/i, fam:'F30',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['Advance-fee logic: “paisa dene se pehle paisa” — asli refund/loan/prize ise nahi maangta','Loan “approved” ho gaya hai to fee ki zarurat nahi — RBI ke rules me advance fee illegal hai','Personal UPI handle + legal-action dhamki = recovery-scam pattern'],
  doo:['Koi fee mat bhejo — refund/loan/prize pehle payment nahi maangta','Lender/bank ko khud call karo (app me diya official number)','1930 + cybercrime.gov.in par report likho (advance-fee fraud)'],
  dont:'“Fee bharo to turant refund/loan” — ye 100% trap hai.'},
  {re:/otp\s*(bata|bataye|batao|de|share|karo)|pin\s*(bata|daal|share)|cvv (bata|share)|verification ke liye otp|account freeze ho jayega|warna (account )?freeze/i, fam:'F31',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['Bank/company kabhi OTP-PIN-CVV nahi maangti — maangne wala khud chor hai','“Warna freeze ho jayega” = darr ka pressure tactic','OTP ek baar diya to account usi second khali ho sakta hai'],
  doo:['OTP/PIN kisi ko mat do — call turant kaat do','Bank ke official number par call karke confirm karo','Ye number 1930 + Sanchar Saathi par report karo'],
  dont:'“Sirf batayein, use nahi karenge” par bharosa mat karo.'},
  {re:/photo.{0,30}(family|bhej|viral|leak)|video.{0,25}(viral|leak)|gallery access|private (photo|video|clip)|paisa nahi (bheja|diya|dogi)|mobile hack|screen record kar/i, fam:'F32',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['Sextortion/blackmail script: dhamki se paisa nikalna','Paisa dene ke baad bhi demand rukne ki guarantee nahi hoti','Ye organised crime hai — police + platform dono se help milti hai'],
  doo:['Bilkul paisa mat do; chat kaat do aur screenshot save karo','1930 / cybercrime.gov.in par report karo','Platform (WhatsApp/Instagram) par block + report karo'],
  dont:'Sharam me chup mat raho — victim aap ho, report karna safety hai.'},
  {re:/lottery|jackpot|\binaam\b|lucky draw|prize.{0,25}(tax|fee|nikalne|ke liye)/i, fam:'F33',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['Aapne koi lottery me participate hi nahi kiya — jeet ka vaada jhooth hai','“Tax/fee” wala hissa hi asli scam hai','Asli prize me advance payment kabhi nahi maangte'],
  doo:['Koi paisa mat bhejo, details mat bharo','Ye chat/message delete na karo — report me kaam aayega','1930 par complaint + number Sanchar Saathi me daalo'],
  dont:'“Jeet” ke khushi me KYC ya fee mat bharo.'},
  {re:/(mera|ye)\s*(naya|new)\s*number|number (change|badal) (ho ?gaya|kar liya|gaya)|hospital me (hoon|hain)|accident ho gaya|voice (clone|ai)|urgent ₹?\s*[\d,]{3,}\s*(bhej|transfer)|phone band hai/i, fam:'F34',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['“Naya number + hospital/accident + urgent paisa” = voice-clone / emergency scam ka template','Scammers family member ki awaaz AI se clone kar lete hain','Asli emergency me bhi pehla step: us insaan ke purane number par khud call'],
  doo:['Purane number par khud call karo (is naye number par nahi)','Ghar me kisi aur se cross-verify karo','Bina voice/video confirm kiye paisa mat bhejo'],
  dont:'“Urgent” ke pressure me 5 minute bhi na sochna khatarnak hai — 2 minute verify karo.'},
  {re:/stock tip|insider (news|tip)|trading tip|multibagger|target price|\d{1,3}\s*%\s*(upar|up)\s*(jayega|jaye)|abhi paisa bhejo is upi/i, fam:'F35',
  verdict:'SCAM LIKELY',sev:'bad',
  why:['“Insider tip” ya pakka target price — SEBI ke rules me aisa tip dena illegal hai','Pump-and-dump me tip ke baad price girta hai, buyer phasta hai','Registered research bhi personal UPI par paisa nahi maangta'],
  doo:['Paisa/tip dono band — paid tip group chhod do','“SEBI registered” claim ho to number sebi.gov.in par check karo','SEBI SCORES / SMART ODR par complaint — screenshots rakh ke'],
  dont:'“Sirf aapko bata raha hoon” — yehi pump ke liye audience-building hai.'},
];
function linkIn(text){const m=text.match(/https?:\/\/[^\s)]+/);return m?m[0]:null;}
function m4Ask(){
  const raw=$('#m4in').value.trim(); if(!raw){toast('Kuch paste karo pehle');return;}
  const R=redactPII(raw); const text=R.text;
  const chat=$('#m4chat');
  chat.insertAdjacentHTML('beforeend',`<div class="m u">${esc(text)}${R.count?`<div class="tiny dim2" style="margin-top:5px">🔒 client-side redaction: ${R.count} item mask (phone/UPI/aadhaar/account) — asli text device par hi</div>`:''}</div>`);
  chat.insertAdjacentHTML('beforeend',`<div class="m a" id="m4typing"><span class="spin"></span> Sanchalak check kar raha hai…</div>`);
  chat.scrollTop=chat.scrollHeight;
  setTimeout(()=>{
    const r=classify(text);
    const hit=r.hit, dom=r.dom, S=r.S, negated=r.negated;
    let body='';
    if(r.verdict==='SCAM LIKELY'&&hit){
      body=`<b>🔴 SCAM LIKELY</b> · family ${hit.fam} · confidence: high
      <div style="margin-top:7px">${hit.why.map(w=>'• '+esc(w)).join('<br>')}</div>
      <div class="tiny" style="margin-top:8px;color:#e3c98f">MAT KARO: ${esc(hit.dont)}</div>
      <div class="tiny" style="margin-top:5px;color:#a3d6c5">KARO: ${hit.doo.map(esc).join(' · ')}</div>
      <div class="tiny dim2" style="margin-top:6px">Library: T1–T24 se match · 1930/CFCFRMS pack ready · <b>human analyst</b> chahiye to neeche button</div>`;
    } else if(r.verdict==='CAUTION'&&hit){
      body=`<b>🟠 CAUTION — zyada</b> · family ${hit.fam} · confidence: medium
      <div style="margin-top:7px">${hit.why.map(w=>'• '+esc(w)).join('<br>')}</div>
      <div class="tiny" style="margin-top:8px;color:#e3c98f">MAT KARO: ${esc(hit.dont)}</div>
      <div class="tiny" style="margin-top:5px;color:#a3d6c5">KARO: ${hit.doo.map(esc).join(' · ')}</div>
      <div class="tiny dim2" style="margin-top:6px">Ye rule sev=warn wala hai — poori jaanch ke baad hi koi payment.</div>`;
    } else if(r.verdict==='SCAM LIKELY'){
      const reds=[];
      S.flags.filter(f=>f[0]==='red').forEach(f=>reds.push('• Registry (R26): '+f[1]));
      if(dom&&dom.level>=4) reds.push(`• Domain ${dom.host} — lookalike/fake signals: ${dom.reasons.filter(x=>x[0]==='red').map(x=>x[1]).join('; ')}`);
      body=`<b>🔴 SCAM LIKELY (registry/link)</b> · confidence: high
      <div style="margin-top:7px">${reds.map(esc).join('<br>')}</div>
      <div class="tiny" style="margin-top:8px;color:#e3c98f">MAT KARO: paisa bhejna, link kholna, OTP/PIN daalna — sab band.</div>
      <div class="tiny dim2" style="margin-top:6px">Library: R26 + M3 engine · Intel Desk me ye domain/handle cluster bhi track hoga</div>`;
    } else if(r.verdict==='CAUTION'){
      const ambers=[];
      S.flags.forEach(f=>ambers.push('• Registry (R26): '+f[1]));
      if(dom&&dom.level===3) ambers.push(`• Domain ${dom.host} — ${dom.reasons.map(x=>x[1]).join('; ')}`);
      body=`<b>🟠 CAUTION — zyada</b> · confidence: medium
      <div style="margin-top:7px">${ambers.map(esc).join('<br>')}</div>
      <div class="tiny" style="margin-top:8px;color:#e3c98f">MAT KARO: dava/portal verify hone tak koi payment nahi.</div>
      <div class="tiny" style="margin-top:5px;color:#a3d6c5">KARO: official portal se khud verify karo (sebi.gov.in / NSE/BSE) · SCORES par complaint ho sakti hai</div>`;
    } else if(r.verdict==='SEEMS OK'){
      const P=r.passRule;
      const plabel=P? (P.label||'legit-refund / service message') : 'safety-warning (negation handle hui — R27)';
      const pwhy=P?P.why:['Ye message khud safety-warning de raha hai (“OTP/PIN share na karein”) — scam iska ulta hota hai','Koi payment / UPI handle / link nahi maanga gaya'];
      const pdoo=P?P.doo:['Message ke link par click na karo — bank/company app khud kholo','Sender ID + official domain match karo'];
      body=`<b>🟢 SEEMS OK</b> · confidence: medium<br><span class="dim tiny">pattern: ${esc(plabel)}</span>
      <div style="margin-top:7px">${pwhy.map(w=>'• '+esc(w)).join('<br>')}</div>
      <div class="tiny" style="margin-top:8px;color:#a3d6c5">KARO: ${pdoo.map(esc).join(' · ')}</div>
      <div class="tiny dim2" style="margin-top:5px">Library: none · koi block nahi</div>`;
    } else {
      body=`<b>🟡 PAKA NAHI BATA SAKTA</b> · confidence: low<br>
      <div style="margin-top:7px">• Library me is pattern ka exact match nahi mila (naya variant ho sakta hai)<br>
      • 2 sawal: (1) Paisa aapko <b>bhejna</b> hai ya <b>aana</b> hai? (2) Kya ye kisi call/group ke kehne par ho raha hai?</div>
      <div class="tiny dim" style="margin-top:7px">Jab tak confirm na ho: koi payment nahi. Chaho to <b>human analyst</b> ko bhej do — ye Intel Desk review queue me bhi chala jaayega.</div>`;
    }
    if(S.claim || S.flags.length){
      body+=`<div class="verdict tiny"><b>SEBI / registry check (R26):</b><br>
        ${S.flags.length?S.flags.map(f=>`<span class="tag ${f[0]==='red'?'bad':'warn'}">${f[0]}</span> ${esc(f[1])}`).join('<br>'):'dawa mila, koi number/portal nahi — khud sebi.gov.in par check karo'}
        <div class="dim2" style="margin-top:5px">Hum “registered hai” certify nahi karte — sirf format + domain + lists check karte hain.</div></div>`;
    }
    if(negated){
      body+=`<div class="verdict tiny" style="border-color:#2e6b57"><b>✅ Negation handle hui (R27):</b>
        ye message khud safety-warning de raha hai (“OTP/PIN share na karein”) — isliye ise scam nahi gina.
        <div class="dim2" style="margin-top:5px">Asli bank/company ke security alerts aise hi likhe hote hain; par link/portal alag se check karo.</div></div>`;
    }
    if(dom) body+=`<div class="verdict tiny"><b>Link check (M3 engine):</b> L${dom.level} · ${esc(dom.host)}<br>
      ${dom.reasons.map(r=>`<span class="tag ${r[0]==='red'?'bad':'warn'}">${r[0]}</span> ${esc(r[1])}`).join('<br>')}</div>`;
    const el=$('#m4typing'); el.innerHTML=body; el.removeAttribute('id');
    chat.scrollTop=chat.scrollHeight;
    const plain=body.replace(/<[^>]+>/g,' ').replace(/\s+/g,' ').slice(0,240);
    if(VOICE){ el.classList.add('speaking'); speak(plain); setTimeout(()=>el.classList.remove('speaking'), 12000);}
    else if($('#evRun')) { /* voice off: kuch nahi */ }
  },650);
}
$('#m4ask').onclick=m4Ask;
$$('#m4 .chip[data-e]').forEach(c=>c.onclick=()=>{$('#m4in').value=EX[+c.dataset.e];m4Ask();});
$('#m4human').onclick=()=>{
  $('#m4chat').insertAdjacentHTML('beforeend',`<div class="m a">👤 <b>Case #SP-2271 bana</b> — analyst 5 min me in-app call karega (SMS link se nahi). Aapki chat ka <b>sirf extracted signals</b> bheja jaayega (text nahi).</div>`);
  toast('Human analyst queue me bhej diya','ok');};

/* ============================================================
   M5 · WALLET AUDIT
   ============================================================ */
const APPS=[
 {n:'PhonePe',ic:'पे',linked:'HDFC **4432',autopay:2,circle:1,warn:true},
 {n:'Google Pay',ic:'G',linked:'HDFC **4432',autopay:1,circle:0,warn:false},
 {n:'Paytm',ic:'P',linked:'SBI **7721',autopay:1,circle:0,warn:false,extra:'wallet ₹240'},
 {n:'Amazon Pay',ic:'A',linked:'HDFC **4432',autopay:0,circle:0,warn:false},
 {n:'BHIM',ic:'B',linked:'—',autopay:0,circle:0,warn:false},
 {n:'Navi',ic:'N',linked:'HDFC **4432',autopay:0,circle:0,warn:false},
];
let MAND=[
 {id:'m1',who:'quickearn-pro@ybl',name:'“QuickEarn Pro”',amt:299,freq:'DAILY',next:'kal',via:'PhonePe',created:'2 din pehle',risk:'RED',why:'daily + naya + L4 domain'},
 {id:'m2',who:'aitips.premium@axl',name:'“AI Stock Tips”',amt:1000,freq:'WEEKLY',next:'5 Oct',via:'PhonePe',created:'9 din pehle',risk:'RED',why:'Telegram task-family F3'},
 {id:'m3',who:'netflix@razorpay',name:'Netflix',amt:649,freq:'MONTHLY',next:'12 Oct',via:'Google Pay',created:'14 mahine',risk:'OK',why:'verified merchant'},
 {id:'m4',who:'jio@jio.com',name:'Jio Postpaid',amt:399,freq:'MONTHLY',next:'18 Oct',via:'Paytm',created:'2 saal',risk:'OK',why:'verified'},
 {id:'m5',who:'lic@licindia.in',name:'LIC Premium',amt:1847,freq:'MONTHLY',next:'1 Nov',via:'PhonePe',created:'4 saal',risk:'OK',why:'verified gov-insurer'},
];
function m5Render(){
  $('#m5apps').innerHTML=APPS.map(a=>`<div class="item"><div class="ic">${a.ic}</div><div class="bd">
    <div class="ti">${esc(a.n)} ${a.circle?'<span class="tag warn">UPI Circle: 1 delegate</span>':''} ${a.autopay?'<span class="tag info">autopay '+a.autopay+'</span>':''}</div>
    <div class="su">linked: ${esc(a.linked)} ${a.extra?'· '+esc(a.extra):''}</div></div>
    <span class="tag ${a.warn?'bad':'ok'}">${a.warn?'check':'ok'}</span></div>`).join('');
  $('#m5mand').innerHTML=MAND.map(m=>`<div class="item"><div class="ic ${m.risk==='RED'?'bad':'ok'}">${m.risk==='RED'?'⚠':'✓'}</div><div class="bd">
    <div class="ti">${esc(m.name)} <span class="mono dim">${esc(m.who)}</span></div>
    <div class="su">₹${m.amt.toLocaleString('en-IN')} · ${esc(m.freq)} · next ${esc(m.next)} · via ${esc(m.via)} · ${esc(m.created)}<br>
    <span class="${m.risk==='RED'?'':'dim2'}" style="${m.risk==='RED'?'color:#e0a49c':''}">${esc(m.why)}</span></div></div>
    <button class="sm ${m.risk==='RED'?'danger':''}" data-rev="${m.id}">Revoke</button></div>`).join('');
  $$('#m5mand button[data-rev]').forEach(b=>b.onclick=()=>{MAND=MAND.filter(x=>x.id!==b.dataset.rev);m5Render();toast('Mandate revoke request bhej di (PSP API)','ok');});
  const red=MAND.filter(m=>m.risk==='RED').length;
  const total=MAND.reduce((s,m)=>s+(m.freq==='DAILY'?m.amt*30:m.freq==='WEEKLY'?m.amt*4:m.amt),0);
  $('#m5mtag').textContent=MAND.length+' active';
  $('#m5warn').innerHTML = red? `<div class="banner b tiny">🔴 <div><b>AutoPay alert — ${red} risky mandate</b><br>
     “quickearn-pro@ybl: ₹299 <b>DAILY</b>, ${MAND.find(m=>m.id==='m1')?'kal se':'—'} shuru, domain unverified, 3 doosre users ne bhi same ID par mandate diya.”<br>
     <span class="dim">Daily/Weekly = mahine me ₹8,970+ nikal sakta hai.</span></div></div>`
   : `<div class="banner g tiny">✅ <div><b>Mandates clean</b> — sab verified merchants.</div></div>`;
  $('#m5sum').innerHTML=`<div class="spread"><div class="tiny dim">AUTO-DEBIT SUMMARY</div><span class="tag ${red?'bad':'ok'}">${red?'action needed':'ok'}</span></div>
    <div class="big">₹${total.toLocaleString('en-IN')}<span style="font-size:12px;color:var(--dim)"> /month (effective)</span></div>
    <div class="tiny dim" style="margin-top:6px">${MAND.length} mandates · ${APPS.filter(a=>a.linked!=='—').length} apps linked · 1 delegate (UPI Circle) · ek hi bank account **4432 chaar apps me</div>
    <div class="tiny dim2" style="margin-top:6px">Rule R20: naya mandate + unverified domain + Daily/Weekly ⇒ warning + one-tap revoke. Authoritative list PSP mandate API se (partner hook).</div>`;
}
$('#m5scan').onclick=()=>{toast('Re-scan: 6 UPI apps, 5 mandates, 1 delegate (demo)');m5Render();};
m5Render();

/* ============================================================
   M6 · INTEL DESK
   ============================================================ */
$('#m6raw').textContent=INTEL.counts.raw_fetched;
$('#m6rel').textContent=INTEL.counts.items;
$('#m6rev').textContent=INTEL.counts.review_queue;
$('#m6time').textContent=INTEL.generated_at.slice(0,10);
$('#m6n').textContent=INTEL.counts.items;
let m6f='all';
function m6Render(){
  const list=INTEL.items.filter(i=>m6f==='all'||i.pl.includes(m6f));
  $('#m6list').innerHTML=list.map(i=>{
    const cls=i.st==='REVIEW'?'warn':i.st==='NEW'?'info':'';
    return `<div class="item"><div class="ic ${i.sev==='high'?'bad':'warn'}">${i.st==='REVIEW'?'❓':'⚡'}</div>
      <div class="bd"><div class="ti">${esc(i.t)}</div>
      <div class="su">${esc(i.fn)} <span class="dim2">·</span> ${esc(i.src)} <span class="dim2">·</span> ${esc(i.d)}</div>
      <div style="margin-top:6px" class="chips">
        <span class="tag ${cls}">${i.st}</span><span class="tag">${i.f}</span>
        ${i.pl.map(p=>`<span class="tag v">${esc(p)}</span>`).join('')}
        ${i.ve.slice(0,1).map(v=>`<span class="tag">${esc(v)}</span>`).join('')}
      </div></div></div>`;}).join('')||'<div class="note tiny">Is platform filter me kuch nahi mila.</div>';
  const rev=INTEL.items.filter(i=>i.st==='REVIEW');
  $('#m6qcount').textContent=rev.length+' items · aaj';
  $('#m6queue').innerHTML=rev.map(i=>`<div class="item"><div class="ic warn">❓</div><div class="bd">
      <div class="ti">${esc(i.t)}</div><div class="su">${esc(i.src)} · ${esc(i.d)} · extracted signals: ${esc((i.kw.length?i.kw:['(none)']).join(', '))}</div>
      <div class="row" style="margin-top:7px"><button class="sm primary" data-push="${esc(i.t)}">Approve → library + detector push</button>
      <button class="sm ghost" data-drop="${esc(i.t)}">Duplicate / ignore</button></div></div></div>`).join('')
    ||'<div class="note tiny">Queue khali hai.</div>';
  $$('#m6queue button[data-push]').forEach(b=>b.onclick=()=>{b.closest('.item').style.opacity=.35;b.disabled=true;
    toast('Approved → threat library + registry update queued (OTA)','ok');});
  $$('#m6queue button[data-drop]').forEach(b=>b.onclick=()=>{b.closest('.item').style.opacity=.35;
    toast('Ignored — dedupe me chala gaya');});
}
$$('#m6 .chip[data-f]').forEach(c=>c.onclick=()=>{m6f=c.dataset.f;m6Render();});
m6Render();

/* ============================================================
   M2b · REPEAT-PAYMENT GUARD (R22) — same user / same payee 3+ payments
   ============================================================ */
const STAIR=[
 {n:1,amt:299, t:'9:12',  tier:'—',   note:'chhota “task deposit”'},
 {n:2,amt:599, t:'9:41',  tier:'nudge',note:'usi payee ko dobara'},
 {n:3,amt:1200,t:'10:15', tier:'T2',  note:'3rd payment 24h me → cooling + “staircase” reason'},
 {n:4,amt:2499,t:'11:02', tier:'T3',  note:'4th → hold (≥2 evidence groups) + analyst callback'},
];
let stair=STAIR.slice(0,3);
function m2bRender(){
  const max=6000;
  $('#m2bchart').innerHTML=stair.map(s=>`<div class="sig" style="align-items:flex-end">
    <span class="lbl">#${s.n} · ${s.t} · <b class="mono">₹${s.amt.toLocaleString('en-IN')}</b>
      <div class="tiny dim2">${esc(s.note)}</div>
      <div class="bar" style="width:${Math.max(8,s.amt/max*100)}%"><i style="width:${Math.min(100,s.amt/max*100)}%"></i></div></span>
    <span class="tag ${s.tier==='T3'?'bad':s.tier==='T2'?'warn':s.tier==='nudge'?'info':''}">${s.tier==='—'?'ok':esc(s.tier)}</span></div>`).join('');
  const last=stair[stair.length-1];
  $('#m2bwarn').innerHTML = last.tier==='T3'
   ? `<div class="banner b tiny">🔴 <div><b>4th payment — HOLD (R22)</b><br>“Aapne 24 ghante me isi account ko 4 baar paisa bheja hai (₹299 → ₹2,499). Task/investment scam me aisa hi hota hai — chhota se shuru, phir badhta jaata hai.” Analyst callback ≤5 min + trusted-person approval.</div></div>`
   : last.tier==='T2'
   ? `<div class="banner w tiny">🟠 <div><b>3rd payment — COOLING (R22)</b><br>“Same payee, 24h me 3 baar, amount badhta ja raha hai (staircase).” 30-min pause + trusted-person; 4th payment par hold + analyst.</div></div>`
   : `<div class="note tiny">2 payments ho gaye — abhi sirf nudge. 3rd par cooling, 4th par hold (rule R22 + deck ka F3 staircase detector).</div>`;
}
$('#m2bnext').onclick=()=>{
  if(stair.length<4){stair=STAIR.slice(0,stair.length+1);m2bRender();
    toast(stair.length===3?'3rd payment → cooling + staircase reason':'4th payment → HOLD + analyst callback', stair.length===4?'bad':'warn');}
  else toast('Simulation complete — 4 payments, 2 friction events');};
m2bRender();


/* ============================================================
   M7 · AUTO-REPORT (R24 / R25) — asli case ka MASKED version
   Real (unmasked) pack workspace ke case_evidence/ folder me hai.
   ============================================================ */
const M7={
  shots:28, loss:5000,
  payments:[{amt:3000,app:'PhonePe',to:'A***** MEHAK',upi:'7349••••••@ptaxis',tx:'T2606••••••••••••••••',utr:'462HD163',d:'25 Jun 2026'},
            {amt:2000,app:'PhonePe',to:'N RAJESH',acc:'XXXXXXXX4707',tx:'T2505••••••••••••••••',utr:'589501728666',d:'30 May 2025'}],
  families:['F5/F6 parcel-refund','F7/F9 fake finance co. (“Ram Mudra Finance”)','F2 APK/banking app','F6 phishing SMS + shortlink','F3 fake job (₹375 fee)','F5+R22 QR trap · ek payee ko 2 payment','F15 photo-blackmail','F17 fake parcel/delivery','F11 fake invoice'],
  entities:{links:1, demands:[30000,375,250]},
  channels:[['1930 helpline','call sheet + txn/UTR'],['cybercrime.gov.in (NCRP)','copy-paste text (EN + HI)'],
            ['Cyber cell email (Bihar)','email + crops attached'],['Bank + PhonePe','txn-wise dispute sheet'],
            ['Sanchar Saathi','number block request'],['SEBI SCORES (scores.gov.in)','registered intermediary complaint'],
            ['SMART ODR (smartodr.in)','mediation / arbitration']],
  sha:(n)=>{let h=0;const s='SP-CASE-2026-1002-PATNA:'+n;for(let i=0;i<s.length;i++){h=(h*31+s.charCodeAt(i))>>>0;}return ('0000000'+h.toString(16)).slice(-8)+'…';}
};
let m7masked=true;
$('#m7mask').onclick=()=>{
  m7masked=!m7masked;
  $('#m7upi').textContent=m7masked?'7349••••••@ptaxis':'7349XXXXX6@ptaxis (demo placeholder)';
  $('#m7maskTag').textContent=m7masked?'masked':'unmasked';
  toast(m7masked?'Masked view (share ke liye)':'Unmasked view (demo: placeholder — asli value sirf victim ke device par)');
};
$('#m7build').onclick=()=>{
  $('#m7out').innerHTML='<div class="okbox tiny" style="margin-top:10px"><b><span class="spin"></span> Pack ban raha hai…</b><div id="m7steps" class="tiny dim" style="margin-top:6px"></div></div>';
  const steps=['28 screenshots crop (status bar/nav hata)','OCR (offline, device par) — entities nikaale',
    'QR decode → 7349••••••@ptaxis','redact: a/c numbers blur (2 versions)','SHA-256 hash: 28 files (chain of custody)',
    'NCRP text bana (English + हिंदी)','email ready: cyber cell (3 ids) + bank','bundle zip + annexure csv'];
  let i=0;
  const t=setInterval(()=>{
    if(i<steps.length){ $('#m7steps').insertAdjacentHTML('beforeend',`✓ ${steps[i]}<br>`); i++; return; }
    clearInterval(t);
    $('#m7out').innerHTML=`<div class="okbox tiny" style="margin-top:10px"><b>✅ Pack ready — 4 min ka kaam 8 second me</b>
      <div class="tiny" style="margin-top:6px" class="dim">Files: complaint_ncrp.md · complaint_email.eml · annexure_index.csv · evidence_bundle.zip</div>
      <div class="row wrap" style="margin-top:8px"><button class="sm primary">📞 1930 par call karo</button>
        <button class="sm">🌐 NCRP text copy</button><button class="sm">✉️ email bhejo</button></div>
      <div class="tiny dim2" style="margin-top:6px">Demo me sirf UI hai — asli files workspace ke case_evidence/ folder me ban chuki hain.</div></div>`;
    const rows=M7.payments.map((p,i)=>`<tr><td>A0${i+1}</td><td>${p.amt.toLocaleString('en-IN')} · ${p.app}</td><td class="mono">${p.tx}</td><td class="mono">${p.utr}</td><td class="mono">${M7.sha(p.tx)}</td></tr>`).join('');
    $('#m7annex').innerHTML=`<table><thead><tr><th>ID</th><th>Payment</th><th>Txn</th><th>UTR</th><th>sha256 (short)</th></tr></thead><tbody>${rows}
      <tr><td>A03</td><td>QR → UPI ID</td><td class="mono">${m7masked?'7349••••••@ptaxis':'7349XXXXX6@ptaxis'}</td><td>—</td><td class="mono">${M7.sha('qr')}</td></tr>
      <tr><td>A04</td><td>Phishing SMS + shortlink</td><td class="mono">cutt.ly/••••••</td><td>—</td><td class="mono">${M7.sha('sms')}</td></tr></tbody></table>
      <div class="tiny dim" style="margin-top:7px">Total 28 files hashed · originals untouched · redacted copies alag folder me.</div>`;
  },380);
};

/* ============================================================
   R38 · FRAUD CONFIRM → turant action (cyber cell email · helpline call · payment stop)
   ============================================================ */
const EM={steps:{mail:false,call:false,stop:false}, t0:null, timer:null};
const EM_MAIL={to:'patnacyberpps-bih@gov.in, sp-cyber@biharpolice.gov.in, cciu-bih@nic.in',
  cc:'complaint@bank-nodal.example (apne bank ka nodal officer)',
  subj:'UPI fraud complaint (financial cyber fraud) — total ₹5,000 · 2 txns · evidence pack attached — [aapka naam & mobile]',
  body:
`Respected Sir/Madam,

Main UPI fraud ka victim hoon. Neeche sirf woh details hain jo evidence me dikhti hain; poora evidence pack (crop kiye screenshots + annexure index + SHA-256 hashes) is mail ke saath attached hai.

--- CASE (evidence-visible) ---
· Payment 1 : ₹3,000  |  UPI handle: 7349******@ptaxis (full digits annexure me)
  Txn ID: T2606252059539226326113  |  UTR: 462HD163  |  Date: 25 Jun 2026
· Payment 2 : ₹2,000  |  A/c: XXXXXXXX4707 (payee)
  Txn ID: T2505301200368307075101  |  UTR: 589501728666  |  Date: 30 May 2025
· Total evidence-visible loss : ₹5,000
· Aage maanga gaya : ₹30,625 (3 alag-alag "fee" demands — ₹1,999 / ₹8,500 / ₹20,126)
· Scam families : 9 (fake QR/phishing · refund-back · fake job/task · KYC-APK · loan-fee · sextortion · digital arrest · lottery-fee · fake support)

--- REQUEST ---
1. UTR/txn ke aadhaar par beneficiary banks me freeze + hold request chaliye (CFCFRMS) — golden hour me priority.
2. Mule/payee accounts ko NCRP ke Suspect Repository me map kariye.
3. Phishing short-link (cutt.ly/*****) aur fake support number ko Sanchar Saathi me report karwa dijiye.
4. Mujhe complaint number / acknowledgement bhej dijiye taaki bank me follow-up kar sakoon.

--- ANNEXURE ---
A01-A02 txn screenshots · A03 QR→UPI decode · A04 phishing SMS · A05 demand chats (redacted) · originals untouched, hashes annexure_index.csv me.

English + Hindi dono me likha hai taaki copy-paste ready rahe.

(Aapka naam, mobile, bank a/c, city — bank/PSP ko bhejne se pehle bhar lein)

Dhanyavaad,
[Naam] · [Mobile] · [City]
SatarkPay evidence pack (auto-generated, user-verified)`};

const EM_CALLS=[
 {id:'1930', label:'1930 · cyber financial fraud helpline', tel:'1930', say:'Sir, UPI fraud hua hai. Payment ₹3,000, UPI handle 7349******@ptaxis, Txn T2606252059539226326113, UTR 462HD163. Freeze request chahiye, complaint number dijiye.'},
 {id:'1909', label:'1909 · Sanchar Saathi (fraud number/SMS report)', tel:'1909', say:'Is number se fraud hua hai — call/SMS report karni hai. Number aur short-link cutt.ly/***** bhej raha hoon.'},
 {id:'bank', label:'Bank / PSP official helpline (card ke peeche ka number)', tel:'', say:'Main fraud victim hoon. Txn T2505301200368307075101 / UTR 589501728666 par dispute + chargeback raise karna hai. Account ko temporarily freeze/limit kar dijiye.'},
 {id:'bihar', label:'Bihar cyber cell (Patna) · 9031825975', tel:'9031825975', say:'Patna se hoon, UPI fraud. Email ke saath evidence pack bhej diya hai — complaint number chahiye.'}
];

function emTick(){
  if(!EM.t0) return;
  const sec=Math.floor((Date.now()-EM.t0)/1000);
  const m=String(Math.floor(sec/60)).padStart(2,'0'), ss=String(sec%60).padStart(2,'0');
  const el=$('#emClock'); if(!el) return;
  el.textContent='+'+m+':'+ss;
  el.style.color = sec<600?'#a3d6c5' : sec<1800?'#e3c98f' : '#e0a49c';
}
function emDone(){
  const n=Object.values(EM.steps).filter(Boolean).length;
  if($('#emDone')) $('#emDone').textContent=n+' / 3';
  if($('#emPack')) $('#emPack').textContent = EM.steps.mail? 'bhej diya' : (EM.steps.stop?'ready':'—');
  const tag=(id,ok)=>{const e=$('#'+id); if(e){e.textContent=ok?'done ✓':'pending'; e.className='tag '+(ok?'ok':'');}};
  tag('emMailTag',EM.steps.mail); tag('emCallTag',EM.steps.call); tag('emStopTag',EM.steps.stop);
  if(n===3 && $('#emOut')) $('#emOut').insertAdjacentHTML('beforeend',
    '<div class="okbox tiny" style="margin-top:7px"><b>✅ Teeno step ho gaye.</b> Ab is complaint number ko sambhal ke rakho — bank/PSP follow-up me wahi maangta hai. NCRP acknowledgement + email sent-copy bhi save kar lo.</div>');
}
function emLog(t){ if($('#emOut')) $('#emOut').insertAdjacentHTML('beforeend','<div>• '+esc(t)+'</div>'); }
function emOpen(){
  const c=$('#emCard'); if(!c) return;
  c.style.display='';
  if(!EM.t0){ EM.t0=Date.now(); EM.timer=setInterval(emTick,1000); }
  emLog('fraud CONFIRM mark hua (user ne khud confirm kiya) — golden-hour clock chalu');
  if($('#m7out') && !$('#m7out').textContent.trim()) emLog('tip: pehle “1930 / NCRP pack banao” dabao to annexure + hashes attach ho jaate hain');
  c.scrollIntoView&&c.scrollIntoView({behavior:'smooth',block:'start'});
  toast('Emergency panel khul gaya — 3 step: email · call · payment stop');
}
if($('#m7confirm')) $('#m7confirm').onclick=emOpen;

if($('#emMail')) $('#emMail').onclick=()=>{
  const prev=$('#emMailPrev');
  if(prev){ prev.style.display=''; prev.innerHTML='<b>To:</b> '+esc(EM_MAIL.to)+'<br><b>Cc:</b> '+esc(EM_MAIL.cc)+
    '<br><b>Subject:</b> '+esc(EM_MAIL.subj)+'<pre style="white-space:pre-wrap;font-family:inherit;margin:6px 0 0;max-height:190px;overflow:auto">'+esc(EM_MAIL.body)+'</pre>'+
    '<div class="tiny dim2" style="margin-top:5px">demo me mail app simulate hua — real device par yahi draft mail app me khulega (annexure attached).</div>'; }
  try{ const a=document.createElement('a'); a.href='mailto:'+EM_MAIL.to+'?subject='+encodeURIComponent(EM_MAIL.subj)+'&body='+encodeURIComponent(EM_MAIL.body.slice(0,900)); a.click&&a.click(); }catch(e){}
  EM.steps.mail=true; emDone(); emLog('cyber cell + bank ko email draft ready (Bihar cyber cell 3 IDs + nodal officer cc)');
};
if($('#emMailCopy')) $('#emMailCopy').onclick=()=>{
  const txt='To: '+EM_MAIL.to+'\nCc: '+EM_MAIL.cc+'\nSubject: '+EM_MAIL.subj+'\n\n'+EM_MAIL.body;
  try{ if(navigator.clipboard&&navigator.clipboard.writeText) navigator.clipboard.writeText(txt);}catch(e){}
  const prev=$('#emMailPrev'); if(prev){ prev.style.display=''; prev.innerHTML='<pre style="white-space:pre-wrap;font-family:inherit">'+esc(txt)+'</pre>'; }
  toast('Draft copy ho gaya (email app me paste kar do)');
};
function emCall(which){
  const c=EM_CALLS.find(x=>x.id===which)||EM_CALLS[0];
  try{ if(c.tel){ const a=document.createElement('a'); a.href='tel:'+c.tel; a.click&&a.click(); } }catch(e){}
  const out=$('#emOut');
  if(out) out.insertAdjacentHTML('beforeend','<div class="verdict tiny" style="margin-top:7px"><b>📞 '+esc(c.label)+'</b>'+
    '<div class="tiny" style="margin-top:5px">Aise bolo:<br><i>“'+esc(c.say)+'”</i></div>'+
    '<div class="tiny dim2" style="margin-top:5px">Golden hour me ye call sabse zyada kaam karti hai — bank freeze request isi call par jaati hai. Demo me dialer simulate hua (offline).</div></div>');
  EM.steps.call=true; emDone(); emLog('helpline call logged: '+c.label);
}
if($('#emCall1930')) $('#emCall1930').onclick=()=>emCall('1930');
if($('#emCallBank')) $('#emCallBank').onclick=()=>emCall('bank');
if($('#emCallScript')) $('#emCallScript').onclick=()=>{
  const out=$('#emOut');
  if(out) out.insertAdjacentHTML('beforeend','<div class="verdict tiny" style="margin-top:7px"><b>📝 Call script (jo pehle poochha jaata hai)</b>'+
    '<div class="tiny" style="margin-top:5px">1) Naam + mobile · 2) Txn ID / UTR / time (annexure se) · 3) Kitna paisa gaya · 4) Kya maanga ja raha hai · 5) Complaint number maango<br>'+
    '<span class="dim2">1930 me ye 5 cheezein pehle rakho — 2 minute me complaint register ho jaati hai.</span></div>');
};
if($('#emStop')) $('#emStop').onclick=()=>{
  const out=$('#emOut');
  if(out) out.insertAdjacentHTML('beforeend',
   '<div class="verdict tiny" style="margin-top:7px"><b>⛔ Payment-stop request (copy karke bank/PSP ko bhejo)</b>'+
   '<pre style="white-space:pre-wrap;font-family:inherit;margin:6px 0 0">Sir/Madam,<br>'+
   'Mere UPI fraud hua hai. Please turant karein:<br>'+
   '1) Txn T2606252059539226326113 (UTR 462HD163, ₹3,000) aur Txn T2505301200368307075101 (UTR 589501728666, ₹2,000) par <b>dispute/chargeback</b> raise karein.<br>'+
   '2) Beneficiary VPA 7349******@ptaxis par <b>hold/freeze request</b> (CFCFRMS) bhejein.<br>'+
   '3) Mere account par <b>temporary debit freeze/limit</b> laga dein jab tak complaint number na mile.<br>'+
   '4) WhatsApp/SMS me aaye kisi bhi naye mandate ko block karein.<br>'+
   'Complaint number: (helpline/email se milne par yahan daalein) · Evidence pack attached (hashes + crops).<br>Dhanyavaad.</pre>'+
   '<div class="tiny dim2" style="margin-top:5px">UPI app me bhi: txn → Help → “Report fraud/unauthorised” → screenshot upload. AutoPay list se unknown mandate revoke karo (tab 05).</div></div>');
  EM.steps.stop=true; emDone(); emLog('payment-stop request ready (bank + PSP + mandate)');
};
const EM_FAM='⚠️ सावधान / सतर्क — UPI fraud alert\n\nHumare ghar me is number/UPI se UPI fraud hua hai:\n• UPI handle: 7349******@ptaxis\n• Fake support number + phishing link (cutt.ly/*****)\n• Kaise: [refund-back / fake job task / KYC-APK / loan fee — jo bhi lagu ho]\n\nKisi ko bhi in par paisa NA bhejein. Koi bhi “refund/loan/job/OTP” ke naam par paisa maange to ruk jayein aur 1930 par report karein.\n\nAgar aapke saath bhi aisa hua hai to 1930 + cybercrime.gov.in par complaint karein. Screenshots save rakhein.';
if($('#emFam')) $('#emFam').onclick=()=>{
  try{ const a=document.createElement('a'); a.href='https://wa.me/?text='+encodeURIComponent(EM_FAM); a.click&&a.click(); }catch(e){}
  const out=$('#emOut');
  if(out) out.insertAdjacentHTML('beforeend','<div class="verdict tiny" style="margin-top:7px"><b>👨‍👩‍👧 Family/community alert ready</b>'+
    '<pre style="white-space:pre-wrap;font-family:inherit;margin:6px 0 0">'+esc(EM_FAM)+'</pre>'+
    '<div class="tiny dim2" style="margin-top:5px">Real device par WhatsApp share-sheet khulega. Ye step <b>optional</b> hai — aapki marzi.</div></div>');
  const t=$('#emFamTag'); if(t){t.textContent='on (optional)'; t.className='tag ok';}
  emLog('parivaar/community alert bana (optional step — user ne khud chuna)');
};
if($('#emFamCopy')) $('#emFamCopy').onclick=()=>{ try{ if(navigator.clipboard&&navigator.clipboard.writeText) navigator.clipboard.writeText(EM_FAM);}catch(e){} toast('Alert text copy ho gaya'); };
if($('#emStopCopy')) $('#emStopCopy').onclick=()=>{ try{ if(navigator.clipboard&&navigator.clipboard.writeText) navigator.clipboard.writeText('Payment stop request — txn T2606252059539226326113 (UTR 462HD163) + T2505301200368307075101 (UTR 589501728666) par dispute + beneficiary 7349******@ptaxis par hold + account temporary freeze');}catch(e){} toast('Stop-request text copy ho gaya'); };


/* ============================================================
   EVAL · labelled 30-message test set → precision / recall / F1
   (in-house evaluated test suite; 30 curated test messages)
   ============================================================ */
const EVAL_SET=[
 // ---- 20 scam ----
 {t:'Main CBI officer Sharma bol raha hoon, aapke naam par case hai, kisi ko mat batao, RBI safe account me paisa transfer karo', y:'scam'},
 {t:'Telegram task job: ₹17 per YouTube like, joining ke liye ₹500 deposit, 30 min me ₹650 wapas', y:'scam'},
 {t:'Aapka KYC expire ho gaya, 24 ghante me UPI band. KYC.apk install karo, AnyDesk se help karenge', y:'scam'},
 {t:'Maine galti se ₹2,000 bhej diye, aap ₹20,000 wapas bhej do', y:'scam'},
 {t:'Sir main SEBI registered advisor hoon, Reg: SEBI12345, guaranteed 300% profit, VIP group join karo', y:'scam'},
 {t:'Join VIP group, guaranteed 30% monthly returns on SME IPO, WhatsApp 9876543210', y:'scam'},
 {t:'Your account just received Rs 3,000 as a special VIP gift! Withdraw instantly: cutt.ly/DEMO-0000', y:'scam'},
 {t:'Sir aapka parcel customs me atka hai, verification ke liye ₹12,000 is UPI par bhejo: quickearn-pro@ybl', y:'scam'},
 {t:'SBI KYC update: http://sbi.onlinekyc-update.top/verify par apna account verify karo warna block ho jayega', y:'scam'},
 {t:'Aapke naam par loan approved hai, registration fee ₹375 UPI karo, baad me 3.5 lakh milega', y:'scam'},
 {t:'Fake challan: aapka vehicle ka ₹500 challan pending hai, is link par pay karo vahan-challan-pay.online', y:'scam'},
 {t:'Meri photo family ko bhej dunga agar paisa nahi bheja, gallery access hai mere paas', y:'scam'},
 {t:'Aapko ₹85,000 digital arrest verification ke liye safe-verify@ybl par turant bhejne honge', y:'scam'},
 {t:'AutoPay mandate lag gaya hai, KYC verify karne ke liye Daily ₹299 approve karo', y:'scam'},
 {t:'Refund ke liye pehle ₹4,999 processing fee bhejo, refund 24 ghante me aa jayega', y:'scam'},
 {t:'Main HDFC bank se bol raha hoon, aapka OTP batayein warna account freeze ho jayega', y:'scam'},
 {t:'Stock tip insider news: kal ye share 40% upar jayega, abhi paisa bhejo is UPI par', y:'scam'},
 {t:'Aapki lottery lagi hai ₹25 lakh, prize ke liye tax ₹5,000 UPI karo', y:'scam'},
 {t:'Loan app se reminder: aapki EMI pending hai, is personal UPI par pay karo warna legal action', y:'scam'},
 {t:'Papa mera naya number hai, hospital me hoon, urgent ₹15,000 bhej do, kisi ko mat batana', y:'scam'},
 // ---- 10 legit (hard negatives) ----
 {t:'Dear customer, HDFC Bank kabhi bhi aapse OTP, PIN ya CVV share karne ko nahi kahega. Kisi ko mat batana.', y:'legit'},
 {t:'Your Amazon Pay refund of ₹1,499 for order #A82 has been credited to your account ending 4432. No PIN required.', y:'legit'},
 {t:'IRCTC: aapki ticket PNR 4521XXX confirm ho gayi hai. Koi payment pending nahi hai.', y:'legit'},
 {t:'Netflix subscription renewal ₹649 monthly mandate aapke approval ke baad lagega. Cancel: app me Manage AutoPay.', y:'legit'},
 {t:'SBI: aapke account me ₹12,000 salary credit hui hai. Balance check: official app. Ye message kisi payment ke liye nahi hai.', y:'legit'},
 {t:'Aapke saman ka delivery aaj 5 baje aa raha hai (Delhivery). Koi extra payment ki zarurat nahi.', y:'legit'},
 {t:'Options trading me 90% retail traders ko loss hota hai — SEBI study. Koi tip nahi, sirf risk awareness.', y:'legit'},
 {t:'LIC premium ₹1,847 ka auto-debit 1 Nov ko hoga (existing policy). Amount ya date badalni ho to app me karein.', y:'legit'},
 {t:'Bhai kal ka ₹1,200 bhej dena jab time mile. Mera number wahi purana hai.', y:'legit'},
 {t:'Mutual fund SIP ke liye nominee add karna zaroori hai — apni app/fund website se kar sakte hain, koi fee nahi lagti.', y:'legit'},
 {t:'Sir, main aapke bete ka dost bol raha hoon, uska phone kharab hai. Hostel fees ₹8,500 turant isi UPI par bhejni hai warna fine lagega.', y:'scam'},
 {t:'Aapke gas connection ki subsidy band ho jayegi. Update karne ke liye ₹10 UPI karo: gasverify@ybl', y:'scam'},
 {t:'Aapke KYC documents ka annual verification baaki hai — apni bank branch ya official app se karayein (koi fee nahi lagti). Koi APK/link nahi.', y:'legit'},
];
function evalRun(){
  let tp=0, fp=0, fn=0, tn=0, soft_scam=0, soft_legit=0, unc=0; const rows=[];
  for(const c of EVAL_SET){
    const r=classify(c.t);
    const strict=(r.verdict==='SCAM LIKELY');      // positive class = "block-worthy"
    const soft=(r.verdict==='CAUTION');            // CAUTION alag bucket (sirf report)
    if(c.y==='scam'){
      if(strict) tp++; else if(soft) soft_scam++; else { fn++; if(r.verdict==='UNCERTAIN') unc++; }
    } else {
      if(strict) fp++; else if(soft) soft_legit++; else tn++;
    }
    const tag=(c.y==='scam'&&!strict&&!soft)?'miss':((c.y==='legit'&&strict)?'false-alarm':(soft?'soft':''));
    rows.push([c.y, r.verdict, c.t.slice(0,54), tag]);
  }
const P=tp+fp?tp/(tp+fp):0, Rc=tp+fn?tp/(tp+fn):0, F1=P+Rc?2*P*Rc/(P+Rc):0;
  const A=(tp+tn)/(tp+tn+fp+fn);
  $('#evP').textContent=(P*100).toFixed(0)+'%'; $('#evR').textContent=(Rc*100).toFixed(0)+'%'; $('#evF').textContent=(F1*100).toFixed(0)+'%';
  const nS=EVAL_SET.filter(c=>c.y==='scam').length, nL=EVAL_SET.length-nS;
  $('#evNote').innerHTML=`${EVAL_SET.length} messages (${nS} scam / ${nL} legit) · positive = SCAM LIKELY: TP ${tp} · FP ${fp} · miss(FN) ${fn} · sahi chhoda(TN) ${tn}<br>CAUTION bucket: ${soft_scam} scam (soft catch — block nahi) + ${soft_legit} legit (friction) · “paka nahi” bucket: ${unc} scam (guess nahi kiya)<br><b>Note:</b> ye set humne khud likha hai (T1–T24 + M6 se) — ye ceiling hai, field accuracy nahi; 3 messages jaan-boojh ke outside-library rakhe hain taki “miss” aur “friction” bhi dikhein.`;
  $('#evOut').innerHTML='<table><thead><tr><th>label</th><th>engine verdict</th><th>message</th></tr></thead><tbody>'+
    rows.map(r=>`<tr><td>${r[0]==='scam'?'<span class="tag bad">scam</span>':'<span class="tag ok">legit</span>'}</td><td>${r[1]}${r[3]==='miss'?' <span class="tag bad">miss</span>':r[3]==='false-alarm'?' <span class="tag bad">false-alarm</span>':r[3]==='soft'?' <span class="tag warn">soft</span>':''}</td><td class="dim">${esc(r[2])}…</td></tr>`).join('')+'</tbody></table>'+
    '<div class="tiny dim" style="margin-top:7px">Honest: set in-house (self-authored) hai — benchmark nahi. Positive = “SCAM LIKELY”; CAUTION alag bucket (soft catch/friction) aur “PAKA NAHI” bhi ek valid jawab hai. Asli calibration + naye variant ke liye field/PS benchmark data aur field feedback chahiye.</div>';
  toast(`Eval: precision ${(P*100).toFixed(0)}% · recall ${(Rc*100).toFixed(0)}% · F1 ${(F1*100).toFixed(0)}%`,'ok');
}
$('#evRun').onclick=evalRun;
if($('#evRun')) $('#evRun').textContent='▶ Run eval ('+EVAL_SET.length+' messages)';

/* ---------- footer ---------- */
$('#foot').innerHTML=`<b class="dim">SatarkPay M2 · New Module Pack</b> — ye interactive demo synthetic data + public headlines (live crawl ${esc(INTEL.generated_at.slice(0,10))}) par chalta hai.
Har verdict demo me rule-engine se aata hai (deterministic), koi LLM call nahi — production me wahi verdict LLM se explain hota hai.
Naye rules R15–R37 deck ke R1–R14 ke saath chalte hain (do JSON packs: rules_R15_R25.json + rules_R26_R38.json). Honest limits: Android telemetry (UsageStats/Notification/SMS) real device par permission + Play-policy review maangta hai;
iOS par kuch signals nahi milte (graceful degrade); screenshots ka OCR opt-in hai (image store nahi hoti, sirf entity + hash). Block kuch bhi auto nahi hota — hamesha user ya human decide karta hai.
<br><b class="dim">Guardrails:</b> koi stock tip / buy-sell-hold nahi · koi monetisation nahi · SMS/OTP/PII harvest nahi (0 permissions) · DPDP-aligned · public-good.
<br><b class="dim">Live demo:</b> sudonishant.github.io/satarkpay-repo · <b class="dim">Team SCΛMURΛI</b> — Nishant Kumar · Prince Singh · Kartik Singh`;

/* ============================================================
   M8 · Scam Library — 10 common UPI/bank/wallet scams (Hindi)
   Data: data/scam_library_hi.json (build time par inline hota hai)
   ============================================================ */
(function () {
  var LIB = null;
  try { LIB = __SCAM_LIBRARY__; } catch (e) { LIB = {}; }
  if (!LIB || typeof LIB !== 'object') LIB = {};
  var S = LIB.scams || [];

  function el(tag, cls, txt) {
    var n = document.createElement(tag);
    if (cls) n.className = cls;
    if (txt != null) n.textContent = txt;
    return n;
  }

  function esc(s) {
    return String(s == null ? '' : s)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  var SEV = { critical: 'bad', high: 'warn', medium: 'ok' };

  function renderCards() {
    var g = document.getElementById('m8grid');
    if (!g) return;
    g.innerHTML = '';
    if (!S.length) { g.innerHTML = '<div class="tiny dim">Library load nahi hui.</div>'; return; }

    S.forEach(function (sc) {
      var card = el('div', 'sheet');
      card.style.cssText = 'margin:0;padding:12px;cursor:pointer';

      var head = el('div', 'spread');
      var left = el('div');
      left.appendChild(el('div', 'tiny dim', sc.id + ' · ' + (sc.family || []).join(', ')));
      var t = el('div', '', sc.name);
      t.style.cssText = 'font-weight:700;font-size:14px;margin-top:2px';
      left.appendChild(t);
      head.appendChild(left);
      var sev = el('span', 'tag ' + (SEV[sc.severity] || 'ok'), sc.severity || 'info');
      head.appendChild(sev);
      card.appendChild(head);

      var body = el('div');
      body.style.cssText = 'display:none;margin-top:8px;line-height:1.65;font-size:12.5px';

      var how = el('div');
      how.innerHTML = '<b>कैसे होता है:</b> ' + esc(sc.kaise);
      how.style.marginBottom = '6px';
      body.appendChild(how);

      var solT = el('div');
      solT.innerHTML = '<b>तुरंत समाधान:</b>';
      body.appendChild(solT);

      var ul = el('ul');
      ul.style.cssText = 'margin:4px 0 8px 18px;padding:0';
      (sc.solution || []).forEach(function (x) { ul.appendChild(el('li', '', x)); });
      body.appendChild(ul);

      var rf = el('div', 'tiny dim', '🚩 ' + (sc.redFlags || []).join(' · '));
      body.appendChild(rf);
      card.appendChild(body);

      card.onclick = function () {
        body.style.display = (body.style.display === 'none') ? 'block' : 'none';
      };
      g.appendChild(card);
    });

    var c = document.getElementById('m8count');
    if (c) c.textContent = S.length + ' scams';
  }

  function renderEmergency() {
    var ol = document.getElementById('m8e7');
    if (!ol) return;
    ol.innerHTML = '';
    ((LIB.emergency7 || {}).steps || []).forEach(function (s, i) {
      var li = el('li', '', s);
      if (i === 0) li.style.fontWeight = '700';
      ol.appendChild(li);
    });
    var n = document.getElementById('m8e7note');
    if (n) n.textContent = (LIB.emergency7 || {}).note || '';
  }

  function renderService() {
    var box = document.getElementById('m8svc');
    var tag = document.getElementById('m8tag');
    var m = LIB.serviceModel || {};
    if (tag) tag.textContent = m.tagline || '';
    if (!box) return;
    box.innerHTML = '';
    (m.modules || []).forEach(function (mod) {
      var c = el('div', 'sheet');
      c.style.cssText = 'margin:0;padding:11px';
      var h = el('div', '', mod.name);
      h.style.cssText = 'font-weight:700;font-size:13.5px;margin-bottom:4px';
      c.appendChild(h);
      if (mod.description) c.appendChild(el('div', 'tiny dim', mod.description));
      var items = mod.checklist || mod.features || mod.returns || mod.fields || [];
      var ul = el('ul');
      ul.style.cssText = 'margin:5px 0 0 17px;padding:0;font-size:12px;line-height:1.6';
      items.forEach(function (x) { ul.appendChild(el('li', '', x)); });
      c.appendChild(ul);
      if (mod.guardrail) {
        var g = el('div', 'tiny', '⚠️ ' + mod.guardrail);
        g.style.cssText = 'margin-top:7px;font-weight:600';
        c.appendChild(g);
      }
      box.appendChild(c);
    });
  }

  function renderCitations() {
    var c = document.getElementById('m8cite');
    if (!c) return;
    c.innerHTML = 'संदर्भ: ' + (LIB.citations || []).map(function (x) {
      return '[' + x.id + '] ' + esc(x.source);
    }).join(' · ');
  }

  function init() {
    renderCards(); renderEmergency(); renderService(); renderCitations();
    // m7 (Intel Desk) ke baad m8 bhi offline render ho jaye
    var tabBtn = document.querySelector('[data-m="m8"]');
    if (tabBtn && window.EVAL_SET) return; // tab wiring app ke paas hai
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else { init(); }
})();
