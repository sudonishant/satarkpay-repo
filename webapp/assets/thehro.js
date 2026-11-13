/* THEHRO engine, browser/Node port of thehro/engine.py (keep in sync;
   tools/parity_check.js compares both on the simulated sessions). */
(function (root) {
  "use strict";
  const DEFAULT_SESSION = {
    call_active: null, caller_in_contacts: null, call_video_or_voip: null, call_minutes: null,
    hour: null, active_screen_control: null, active_screen_control_confidence: null, screen_share: null,
    high_risk_app_installed: null, accessibility_service_enabled: null, apk_installed_hours: null,
    overlay_service: null, payee_saved: false, payee_new: null, payee_vpa_age_days: null, payee_risk: null,
    payee_sebi_valid: null, amount: 500, baseline_p95: null, credit_claim_recent: null,
    real_credit_found: null, qr_recent: null, qr_from_gallery: null, qr_receive_intent: null,
    recent_fraud_report_days: null, remark: "", new_payees_7d: null, rising_amounts: null,
    small_inbound_unknown_72h: null, unofficial_helpline: null,
    new_payee_sum_72h: null, held_recently_hours: null
  };
  const UNKNOWN_FIELDS = ["call_active", "caller_in_contacts", "call_video_or_voip", "call_minutes", "hour",
    "active_screen_control_confidence", "high_risk_app_installed", "accessibility_service_enabled",
    "apk_installed_hours", "overlay_service", "payee_new", "payee_vpa_age_days", "payee_risk",
    "payee_sebi_valid", "baseline_p95", "credit_claim_recent", "real_credit_found", "qr_recent",
    "qr_from_gallery", "qr_receive_intent", "recent_fraud_report_days", "new_payees_7d", "rising_amounts",
    "small_inbound_unknown_72h", "unofficial_helpline", "new_payee_sum_72h", "held_recently_hours"];
  const screenControl = s => s.active_screen_control != null ? s.active_screen_control : s.screen_share;
  const unknownSignals = s => UNKNOWN_FIELDS.filter(k => s[k] == null).concat(
    s.active_screen_control == null && s.screen_share == null ? ["active_screen_control"] : []);
  const effAmount = s => s.amount + (s.payee_new === true && !s.payee_saved && s.new_payee_sum_72h != null ? s.new_payee_sum_72h : 0);
  const INVEST = ["ipo", "share", "trading", "stock", "invest", "allot"];
  const FEE = ["fee", "processing", "release", "refund charge", "recovery"];
  const has = (r, w) => w.some(x => (r || "").toLowerCase().includes(x));
  const P = {
    unknown_caller: s => s.call_active === true && s.caller_in_contacts === false,
    video_or_voip_call: s => s.call_active === true && s.call_video_or_voip === true,
    long_call: s => s.call_active === true && s.call_minutes != null && s.call_minutes >= 30,
    odd_hour: s => s.hour != null && (s.hour < 7 || s.hour >= 22),
    screen_share: s => screenControl(s) === true,
    fresh_apk: s => s.apk_installed_hours !== null && s.apk_installed_hours < 72,
    overlay_service: s => s.overlay_service === true,
    new_payee: s => s.payee_new === true && !s.payee_saved,
    amount_high: s => s.baseline_p95 != null && s.baseline_p95 > 0 && effAmount(s) > 3 * s.baseline_p95,
    amount_very_high: s => s.baseline_p95 != null && s.baseline_p95 > 0 && effAmount(s) > 10 * s.baseline_p95,
    fake_credit: s => s.credit_claim_recent === true && s.real_credit_found === false,
    qr_recent: s => !!s.qr_recent,
    qr_from_gallery: s => !!s.qr_from_gallery,
    qr_receive_intent: s => !!s.qr_receive_intent,
    recent_fraud_report: s => s.recent_fraud_report_days !== null && s.recent_fraud_report_days <= 90,
    fee_remark: s => has(s.remark, FEE),
    invest_remark: s => has(s.remark, INVEST),
    payee_not_sebi_valid: s => has(s.remark, INVEST) && s.payee_sebi_valid === false,
    many_new_payees_7d: s => s.new_payees_7d != null && s.new_payees_7d >= 3,
    rising_amounts: s => s.rising_amounts === true,
    small_inbound_credit_unknown: s => s.small_inbound_unknown_72h === true,
    unofficial_helpline: s => s.unofficial_helpline === true
  };
  const TIERS = ["T0", "T1", "T2", "T3", "T4"];
  const TH = [[0.85, "T4"], [0.60, "T3"], [0.30, "T2"], [0.10, "T1"]];
  const ACTIONS = {
    T0: "silent", T1: "caution badge; recipient is not verified", T2: "one neutral question",
    T3: "scam story + 60 s hold, reconfirm after the call ends",
    T4: "payment hold; offer trusted-contact and 1930 options"
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
    let anomaly = 0;
    if (s.baseline_p95 != null && s.baseline_p95 > 0) {
      const ratio = Math.max(effAmount(s), 1) / Math.max(s.baseline_p95, 1);
      anomaly = Math.min(1, Math.max(0, Math.log10(ratio) / 1.3));
    }
    anomaly += P.odd_hour(s) ? 0.15 : 0;
    let payee = 0;
    if (!s.payee_saved) payee = Math.max(s.payee_risk || 0,
      (s.payee_new === true ? 0.35 : 0) + (s.payee_vpa_age_days != null && s.payee_vpa_age_days < 30 ? 0.3 : 0));
    const device = Math.max(screenControl(s) === true ? 1 : 0, P.fresh_apk(s) ? 0.8 : 0,
      s.overlay_service === true ? 0.7 : 0, s.accessibility_service_enabled === true ? 0.45 : 0,
      s.high_risk_app_installed === true ? 0.35 : 0);
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
    let hardFloor = !!(best && best.complete && best.card.tier_floor);
    if (best && best.complete && best.card.tier_floor && ti(best.card.tier_floor) > ti(tier)) {
      tier = best.card.tier_floor; floors.push(best.card.id + " complete -> floor " + tier);
    }
    if (P.fake_credit(s) && ti(tier) < 3) { tier = "T3"; floors.push("fake credit, no real credit -> T3"); }
    if (P.fake_credit(s)) hardFloor = true;
    const takeover = screenControl(s) === true || (P.fresh_apk(s) && s.overlay_service === true);
    if (takeover && ti(tier) < 4) { tier = "T4"; floors.push("active screen control or fresh APK plus overlay -> T4"); }
    if (takeover) hardFloor = true;
    const retry = s.held_recently_hours !== null && s.held_recently_hours < 24 && s.payee_new && !s.payee_saved;
    if (retry && ti(tier) < 3) { tier = "T3"; floors.push("retry to a new payee within 24 h of a hold -> T3"); }
    if (retry) hardFloor = true;
    if (s.payee_saved && !hardFloor && !takeover && ti(tier) > 1) { tier = "T1"; floors.push("saved / recurring payee -> never above T1"); }
    const card = best && (best.complete || (tier !== "T0" && best.anchorHit)) ? best.card : null;
    const reason = tier === "T0" ? "No high-risk pattern is confirmed from the available signals." : tier === "T1" ? "Caution: check the recipient name independently before paying."
      : (card ? card.reason : (retry ? "You were warned about a similar payment today. Scammers often call back and ask again." : "Unusual payment pattern."));
    const t1 = (typeof performance !== "undefined" ? performance.now() : Date.now());
    return {
      p: Math.round(p * 1e4) / 1e4, tier, action: ACTIONS[tier], card_id: card ? card.id : null,
      card_name: card ? card.name : null, reason, question: card && tier === "T2" ? card.question : null,
      evidence: best ? best.hits : [], floors, unknown_signals: unknownSignals(s), components: comp, ms: Math.round((t1 - t0) * 1000) / 1000
    };
  }
  const api = { decide, matchCards, DEFAULT_SESSION, PREDICATES: P, ACTIONS };
  if (typeof module !== "undefined" && module.exports) module.exports = api; else root.THEHRO = api;
})(this);
