/* THEHRO engine, browser/Node port of thehro/engine.py (keep in sync;
   tools/parity_check.js compares both on the simulated sessions). */
(function (root) {
  "use strict";
  const DEFAULT_SESSION = {
    call_active: false, caller_in_contacts: true, call_video_or_voip: false, call_minutes: 0,
    hour: 14, screen_share: false, apk_installed_hours: null, overlay_service: false,
    payee_saved: false, payee_new: true, payee_vpa_age_days: 365, payee_risk: 0,
    payee_sebi_valid: false, amount: 500, baseline_p95: 2000, credit_claim_recent: false,
    real_credit_found: false, qr_recent: false, qr_from_gallery: false, qr_receive_intent: false,
    recent_fraud_report_days: null, remark: "", new_payees_7d: 0, rising_amounts: false,
    small_inbound_unknown_72h: false, unofficial_helpline: false,
    new_payee_sum_72h: 0, held_recently_hours: null
  };
  const effAmount = s => s.amount + (s.payee_new && !s.payee_saved ? s.new_payee_sum_72h : 0);
  const INVEST = ["ipo", "share", "trading", "stock", "invest", "allot"];
  const FEE = ["fee", "processing", "release", "refund charge", "recovery"];
  const has = (r, w) => w.some(x => (r || "").toLowerCase().includes(x));
  const P = {
    unknown_caller: s => s.call_active && !s.caller_in_contacts,
    video_or_voip_call: s => s.call_active && s.call_video_or_voip,
    long_call: s => s.call_active && s.call_minutes >= 30,
    odd_hour: s => s.hour < 7 || s.hour >= 22,
    screen_share: s => !!s.screen_share,
    fresh_apk: s => s.apk_installed_hours !== null && s.apk_installed_hours < 72,
    overlay_service: s => !!s.overlay_service,
    new_payee: s => s.payee_new && !s.payee_saved,
    amount_high: s => effAmount(s) > 3 * s.baseline_p95,
    amount_very_high: s => effAmount(s) > 10 * s.baseline_p95,
    fake_credit: s => s.credit_claim_recent && !s.real_credit_found,
    qr_recent: s => !!s.qr_recent,
    qr_from_gallery: s => !!s.qr_from_gallery,
    qr_receive_intent: s => !!s.qr_receive_intent,
    recent_fraud_report: s => s.recent_fraud_report_days !== null && s.recent_fraud_report_days <= 90,
    fee_remark: s => has(s.remark, FEE),
    invest_remark: s => has(s.remark, INVEST),
    payee_not_sebi_valid: s => has(s.remark, INVEST) && !s.payee_sebi_valid,
    many_new_payees_7d: s => s.new_payees_7d >= 3,
    rising_amounts: s => !!s.rising_amounts,
    small_inbound_credit_unknown: s => !!s.small_inbound_unknown_72h,
    unofficial_helpline: s => !!s.unofficial_helpline
  };
  const TIERS = ["T0", "T1", "T2", "T3", "T4"];
  const TH = [[0.85, "T4"], [0.60, "T3"], [0.30, "T2"], [0.10, "T1"]];
  const ACTIONS = {
    T0: "silent", T1: "trust badge on the pay screen", T2: "one neutral question",
    T3: "scam story + 60 s hold, reconfirm after the call ends",
    T4: "circuit breaker: hold + guardian alert + 1930 option"
  };
  const W = { bias: -4.6, stage: 4.2, anomaly: 1.5, payee: 1.6, device: 2.4 };
  const ti = t => TIERS.indexOf(t);
  const r3 = x => Math.round(x * 1000) / 1000;

  function matchCards(s, cards) {
    const out = cards.map(c => {
      const st = c.stages, hits = st.filter(x => (P[x] ? P[x](s) : false));  // unknown stage from a newer card never fires
      let wsum = 0, got = 0;
      st.forEach((x, i) => { const w = 1 + 0.25 * i; wsum += w; if (hits.includes(x)) got += w; });
      const complete = hits.length >= c.min_match && hits.includes(c.anchor || st[0]);
      return { card: c, hits, progress: got / wsum, complete, anchorHit: hits.includes(c.anchor || st[0]) };
    });
    out.sort((a, b) => (b.complete - a.complete) || (b.anchorHit - a.anchorHit)
      || (!!b.card.tier_floor - !!a.card.tier_floor) || (b.hits.length - a.hits.length) || (b.progress - a.progress));
    return out;
  }
  function components(s, best) {
    const stage = best ? (best.complete ? best.progress : best.progress * 0.5) : 0;
    const ratio = Math.max(effAmount(s), 1) / Math.max(s.baseline_p95, 1);
    let anomaly = Math.min(1, Math.max(0, Math.log10(ratio) / 1.3)) + (P.odd_hour(s) ? 0.15 : 0);
    let payee = 0;
    if (!s.payee_saved) payee = Math.max(s.payee_risk, (s.payee_new ? 0.35 : 0) + (s.payee_vpa_age_days < 30 ? 0.3 : 0));
    const device = Math.max(s.screen_share ? 1 : 0, P.fresh_apk(s) ? 0.8 : 0, s.overlay_service ? 0.7 : 0);
    return { stage: r3(stage), anomaly: r3(Math.min(anomaly, 1)), payee: r3(Math.min(payee, 1)), device: r3(device) };
  }
  const fuse = c => 1 / (1 + Math.exp(-(W.bias + W.stage * c.stage + W.anomaly * c.anomaly + W.payee * c.payee + W.device * c.device)));

  function decide(session, cards) {
    const t0 = (typeof performance !== "undefined" ? performance.now() : Date.now());
    const s = Object.assign({}, DEFAULT_SESSION, session);
    const ms = matchCards(s, cards), best = ms[0] || null;
    const comp = components(s, best), p = fuse(comp);
    let tier = "T0";
    for (const [th, t] of TH) { if (p >= th) { tier = t; break; } }
    const floors = [];
    if (best && best.complete && best.card.tier_floor && ti(best.card.tier_floor) > ti(tier)) {
      tier = best.card.tier_floor; floors.push(best.card.id + " complete -> floor " + tier);
    }
    if (P.fake_credit(s) && ti(tier) < 3) { tier = "T3"; floors.push("fake credit, no real credit -> T3"); }
    const takeover = s.screen_share || (P.fresh_apk(s) && s.overlay_service);
    if (takeover && !s.payee_saved && ti(tier) < 4) { tier = "T4"; floors.push("screen control on pay screen -> T4"); }
    const retry = s.held_recently_hours !== null && s.held_recently_hours < 24 && s.payee_new && !s.payee_saved;
    if (retry && ti(tier) < 3) { tier = "T3"; floors.push("retry to a new payee within 24 h of a hold -> T3"); }
    if (s.payee_saved && !takeover && ti(tier) > 1) { tier = "T1"; floors.push("saved / recurring payee -> never above T1"); }
    const card = best && (best.complete || (tier !== "T0" && best.anchorHit)) ? best.card : null;
    const reason = tier === "T0" ? "Looks like a normal payment." : tier === "T1" ? "New payee. Check the name before you pay."
      : (card ? card.reason : (retry ? "You were warned about a similar payment today. Scammers often call back and ask again." : "Unusual payment pattern."));
    const t1 = (typeof performance !== "undefined" ? performance.now() : Date.now());
    return {
      p: Math.round(p * 1e4) / 1e4, tier, action: ACTIONS[tier], card_id: card ? card.id : null,
      card_name: card ? card.name : null, reason, question: card && tier === "T2" ? card.question : null,
      evidence: best ? best.hits : [], floors, components: comp, ms: Math.round((t1 - t0) * 1000) / 1000
    };
  }
  const api = { decide, matchCards, DEFAULT_SESSION, PREDICATES: P, ACTIONS };
  if (typeof module !== "undefined" && module.exports) module.exports = api; else root.THEHRO = api;
})(this);
