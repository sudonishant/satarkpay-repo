/* SatarkPay M2 · smoke test
   Chalane ke liye:  npm i jsdom && node smoke_test.js
   Path: satarkpay_m2.html isi folder me hona chahiye (ya neeche HTML_PATH set karo).
   Note: jsdom 'scrollTo' ko implement nahi karta — woh ek harmless stub warning hai,
   asli browser (Chromium) me page errors 0 hain. */
const fs=require('fs');
const {JSDOM}=require('jsdom');
const html=fs.readFileSync(process.env.HTML_PATH||require('path').join(__dirname,'satarkpay_m2.html'),'utf8');
const errors=[];
const dom=new JSDOM(html,{runScripts:'dangerously',pretendToBeVisual:true,
  virtualConsole:new (require('jsdom').VirtualConsole)().on('jsdomError',e=>errors.push('jsdomError: '+e.message))
    .on('error',(...a)=>errors.push('console.error: '+a.join(' ')))});
const {window}=dom; const {document}=window;
const $=s=>document.querySelector(s), $$=s=>[...document.querySelectorAll(s)];
const ev=code=>window.eval(code);
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
let pass=0,fail=0;
const t=(name,cond,extra='')=>{cond?(pass++,console.log('  ✓',name)):(fail++,console.log('  ✗',name,extra));};

(async()=>{
 console.log('== load ==');
 t('no load errors', errors.length===0, errors.join(' | '));
 t('7 modules present', $$('.module').length===7, $$('.module').length);
 t('intel list rendered', $$('#m6list .item').length>0, $$('#m6list .item').length);
 t('m6 stats from crawl', $('#m6raw').textContent==='784' && $('#m6rel').textContent==='172');

 console.log('== tabs ==');
 $('#tabs button[data-m="m4"]').click();
 t('tab switch', $('#m4').classList.contains('on') && !$('#m1').classList.contains('on'));

 console.log('== M1 matrix (known/unknown × chat length) ==');
 $('#tabs button[data-m="m1"]').click();
 const runSc=(i,code)=>{ $$('#m1 .chip[data-m1sc]')[i].click(); ev(code); };
 // 1) saved contact + 2 min chat -> QUICK, 1 min
 runSc(0, 'm1.dwell=0;m1Ring();');
 t('known+short -> tier QUICK', ev('m1.tier')==='QUICK', ev('m1.tier'));
 t('signals include chat session', $('#m1sigs').textContent.includes('Chat session'));
 t('quick shows 1:00 countdown', $('#m1clock').textContent==='01:00', $('#m1clock').textContent);
 t('no notification for quick', $('#m1notif').innerHTML==='');
 ev('m1.dwell=61;m1Tick();');
 t('quick clears after 1 min', !!$('#m1qs') && $$('#m1qs button[data-q]').length===2);
 t('call-confirm button present', !!$('#m1call'));
 $('#m1call').click();
 t('call confirm -> pay allowed', $('#m1final').textContent.includes('Call confirm ho gaya'));
 ev('clearInterval(m1.timer); m1.timer=null;');
 // 2) saved contact + 12 min chat -> NOTIFY
 runSc(1, 'm1.dwell=0;m1Tick();');
 t('known+long -> tier NOTIFY', ev('m1.tier')==='NOTIFY', ev('m1.tier'));
 t('user gets NOTIFICATION', $('#m1notif').textContent.includes('10+ minute chat'), $('#m1notif').textContent.slice(0,60));
 t('notify is T1 (3 min, not a block)', $('#m1gateUI').textContent.includes('Notification'));
 ev('m1.dwell=181;m1Tick();');
 t('notify clears -> 2 sawal', $$('#m1qs button[data-q]').length===4);
 ev('clearInterval(m1.timer); m1.timer=null;');
 // 3) unknown + 5 min chat -> COOLING 8 min
 runSc(2, 'm1.dwell=0;m1Ring();');
 t('unknown+short -> COOLING', ev('m1.tier')==='COOLING', ev('m1.tier'));
 t('8:00 on the clock', $('#m1clock').textContent==='08:00', $('#m1clock').textContent);
 ev('m1.dwell=470;m1Tick();');   // +3s tick => 473
 t('not cleared at ~473s', !$('#m1qs'));
 ev('m1.dwell=482;m1Tick();');
 t('clears -> 3 sawal', $$('#m1qs button[data-q]').length===6);
 ev('clearInterval(m1.timer); m1.timer=null;');
 // 4) unknown + 10 min chat -> HOLD
 runSc(3, 'm1.dwell=0;m1Tick();');
 t('unknown+long -> HOLD', ev('m1.tier')==='HOLD', ev('m1.tier'));
 t('hold notification', $('#m1notif').textContent.includes('10 minute chat'));
 ev('m1.dwell=601;m1Tick();');
 const qb=$$('#m1qs button[data-q]'); qb[0].click(); qb[2].click(); qb[5].click();
 t('hold -> Tier-3 callback card', $('#m1final').textContent.includes('analyst callback'), $('#m1final').textContent.slice(0,50));
 ev('clearInterval(m1.timer); m1.timer=null;');
 // 5) no chat
 runSc(4, '');
 t('no chat -> nudge only', $('#m1gateUI').textContent.includes('Nudge'));

 console.log('== M2 screenshot radar ==');
 $('#tabs button[data-m="m2"]').click();
 $$('#m2 .chip[data-m2sc]')[1].click();   // burst of 4
 t('4 shots rendered', $$('#m2gal .shot').length===4);
 t('RED severity', $('#m2sev').textContent==='RED' && $('#m2sev').className.includes('bad'));
 t('burst banner', $('#m2banner').textContent.includes('burst'));

 console.log('== M2b repeat-payment guard (R22) ==');
 t('staircase has 3 payments initially', $$('#m2bchart .sig').length===3, $$('#m2bchart .sig').length);
 t('3rd = cooling banner', $('#m2bwarn').textContent.includes('COOLING'));
 $('#m2bnext').click();
 t('4th payment -> HOLD', $$('#m2bchart .sig').length===4 && $('#m2bwarn').textContent.includes('HOLD'), $('#m2bwarn').textContent.slice(0,40));

 console.log('== M3 domain trust ==');
 $('#tabs button[data-m="m3"]').click();
 $('#m3url').value='https://rbi-nodal-verify.xyz/refund?amt=4999'; $('#m3go').click();
 t('L4 on fake rbi domain', $('#m3out').textContent.includes('L4') && $('#m3out').textContent.includes('Lookalike'));
 $$('#m3 .chip[data-u]')[5].click();  // unknown-but-clean hospital
 t('L3 amber on unverified-clean', $('#m3out').textContent.includes('L3') && $('#m3out').textContent.includes('Unverified'));
 $$('#m3 .chip[data-u]')[2].click();  // razorpay w/ unverified merchant
 t('gateway-merchant warning', $('#m3out').textContent.includes('merchant') && $('#m3out').textContent.includes('quickearn-pro'));
 $$('#m3 .chip[data-u]')[6].click();  // onlinesbi.sbi
 t('L1 verified bank', $('#m3out').textContent.includes('L1'));

 console.log('== M4 assistant ==');
 $('#tabs button[data-m="m4"]').click();
 $('#m4 .chip[data-e="1"]').click();  // telegram task (index ke bajaye data-e se)
 await sleep(1100);
 let last=$$('#m4chat .m').slice(-1)[0].textContent;
 t('telegram task -> SCAM LIKELY', last.includes('SCAM LIKELY') && last.includes('F3'), last.slice(0,70));
 $('#m4 .chip[data-e="4"]').click();  // legit amazon refund
 await sleep(1100);
 last=($$('#m4chat .m').slice(-1)[0]||{}).textContent||'';
 t('legit refund -> SEEMS OK', last.includes('SEEMS OK'), last.slice(0,70));
 $('#m4 .chip[data-e="2"]').click();  // KYC apk with link
 await sleep(1100);
 last=($$('#m4chat .m').slice(-1)[0]||{}).textContent||'';
 t('kyc+link -> SCAM LIKELY + Link check block', last.includes('SCAM LIKELY') && last.includes('Link check'), last.slice(0,80));
 // naye engine ke checks (R26 registry, R27 negation, R28 redaction, eval harness)
 $('#m4 .chip[data-e="5"]').click();  // fake SEBI number + guaranteed return
 await sleep(1100);
 last=$$('#m4chat .m').slice(-1)[0].textContent;
 t('fake SEBI number -> SCAM LIKELY + registry flag', last.includes('SCAM LIKELY') && /registry|R26/i.test(last), last.slice(0,80));
 $('#m4 .chip[data-e="6"]').click();  // bank ka apna OTP warning
 await sleep(1100);
 last=$$('#m4chat .m').slice(-1)[0].textContent;
 t('bank OTP safety-warning -> SEEMS OK (negation R27)', last.includes('SEEMS OK'), last.slice(0,80));
 $('#m4in').value='Mera number 9876543210 hai, UPI vipadvisor@oksbi se paisa gaya.';
 ev('m4Ask()');
 await sleep(1100);
 t('R28 client-side redaction note', $$('#m4chat .m').slice(-1)[0].textContent.includes('redaction') || $$('#m4chat .m')[$$('#m4chat .m').length-2].textContent.includes('redaction'));
 ev('evalRun()');
 t('eval harness runs (30 messages)', $('#evP').textContent.includes('%') && $('#evR').textContent.includes('%'), $('#evP').textContent+'/'+$('#evR').textContent);
 t('eval note honest buckets', /miss\(FN\) \d+/.test($('#evNote').textContent) && $('#evNote').textContent.includes('CAUTION bucket'));
 $('#m4human').click();
 t('human queue case', $$('#m4chat .m').slice(-1)[0].textContent.includes('SP-2271'));

 console.log('== M5 wallet audit ==');
 $('#tabs button[data-m="m5"]').click();
 t('6 apps listed', $$('#m5apps .item').length===6);
 t('5 mandates', $$('#m5mand .item').length===5);
 t('autopay red alert', $('#m5warn').textContent.includes('DAILY'));
 const before=$$('#m5mand .item').length;
 $$('#m5mand button[data-rev]')[0].click();
 t('revoke works', $$('#m5mand .item').length===before-1);

 console.log('== M6 intel desk ==');
 $('#tabs button[data-m="m6"]').click();
 const all=$$('#m6list .item').length;
 $$('#m6 .chip[data-f]')[2].click();  // Telegram filter
 t('platform filter works', $$('#m6list .item').length<=all && $$('#m6list .item').length>0);
 $$('#m6 .chip[data-f]')[0].click();
 t('review queue rendered', $$('#m6queue .item').length>0, $$('#m6queue .item').length);
 const appr=$$('#m6queue button[data-push]')[0]; appr.click();
 t('approve push works', appr.disabled===true);

 console.log('== M7 auto-report ==');
 $('#tabs button[data-m="m7"]').click();
 t('M7 masked UPI by default', $('#m7upi').textContent.includes('••••'));
 $('#m7mask').click();
 t('M7 mask toggle works', $('#m7upi').textContent.includes('7349045416'));
 $('#m7build').click();
 await sleep(3600);
 t('M7 pack ready', $('#m7out').textContent.includes('Pack ready'), $('#m7out').textContent.slice(0,60));
 t('M7 annexure hashes', $$('#m7annex tbody tr').length>=4, $$('#m7annex tbody tr').length);

 console.log('== M7b emergency (R38: confirm -> email + call + payment stop) ==');
 t('emergency card hidden by default', window.getComputedStyle($('#emCard')).display==='none' || $('#emCard').style.display==='none');
 $('#m7confirm').click();
 await sleep(1200);
 t('confirm -> emergency panel khul gaya', $('#emCard').style.display!=='none');
 t('golden-hour clock chalu', /\+\d{2}:\d{2}/.test($('#emClock').textContent), $('#emClock').textContent);
 $('#emMail').click();
 t('email draft ready (cyber cell + bank)', $('#emMailTag').textContent.includes('done') && $('#emMailPrev').textContent.includes('patnacyberpps-bih@gov.in'), $('#emMailTag').textContent);
 $('#emCall1930').click();
 t('1930 helpline call + script', $('#emCallTag').textContent.includes('done') && $('#emOut').textContent.includes('1930'), $('#emCallTag').textContent);
 $('#emStop').click();
 t('payment-stop request bani', $('#emStopTag').textContent.includes('done') && $('#emOut').textContent.includes('CFCFRMS'), $('#emStopTag').textContent);
 t('steps counter 3/3', $('#emDone').textContent==='3 / 3', $('#emDone').textContent);
 t('teeno-step completion note', $('#emOut').textContent.includes('Teeno step ho gaye'));
 $('#emFam').click();
 t('optional family/community alert (step 4)', $('#emOut').textContent.includes('Family/community alert ready') && $('#emFamTag').textContent.includes('optional'), $('#emFamTag').textContent);

 console.log('== demo script + speed ==');
 $('#demoBtn').click();
 t('demo script visible', $$('#dsList .step').length>=7, $$('#dsList .step').length);
 $('#speedBtn').click();
 t('speed toggles to real time', $('#speedBtn').textContent.includes('real time'));
 const real=errors.filter(e=>!e.includes('scrollTo'));
 t('no runtime errors during full run (jsdom scrollTo stub ignore)', real.length===0, real.join(' | '));
 console.log(`\nRESULT: ${pass} passed, ${fail} failed`);
 process.exit(fail?1:0);
})();
