"""THEHRO reference engine (SatarkPay v3, RAKSHAM 2026, PS 02).

Detects AI-enabled scam *workflows* before the UPI PIN, from metadata only,
and picks the lowest alert that will work so users do not get alert fatigue.

Pipeline:  signals -> predicates -> kill-chain match per playbook card
           -> risk fusion p(scam) -> deterministic floors -> tier T0..T4
           -> attention budget -> one alert (or silence) + plain reason.

Pure Python standard library. The Android SDK (Kotlin) and the web demo (JS)
are ports of this file; tests/test_engine.py keeps their behaviour pinned.
"""
from __future__ import annotations

import json
import math
import time
from dataclasses import dataclass, field
from pathlib import Path

HERE = Path(__file__).resolve().parent

# ---------------------------------------------------------------- signals
# Everything below is metadata the phone or the bank already has.
# No SMS body, chat text or call audio is ever read.
DEFAULT_SESSION = {
    "call_active": None,           # None = not verifiable on this device
    "caller_in_contacts": None,    # contacts are not requested by this app
    "call_video_or_voip": None,
    "call_minutes": None,
    "hour": None,
    "active_screen_control": None, # never inferred from an installed app
    "active_screen_control_confidence": None,
    "screen_share": None,          # deprecated input alias for older scenarios
    "high_risk_app_installed": None,
    "accessibility_service_enabled": None,
    "apk_installed_hours": None,
    "overlay_service": None,
    "payee_saved": False,
    "payee_new": None,
    "payee_vpa_age_days": None,
    "payee_risk": None,
    "payee_sebi_valid": None,
    "amount": 500.0,
    "baseline_p95": None,
    "credit_claim_recent": None,
    "real_credit_found": None,
    "qr_recent": None,
    "qr_from_gallery": None,
    "qr_receive_intent": None,
    "recent_fraud_report_days": None,
    "remark": "",
    "new_payees_7d": None,
    "rising_amounts": None,
    "small_inbound_unknown_72h": None,
    "unofficial_helpline": None,
    "new_payee_sum_72h": None,
    "held_recently_hours": None,
}

UNKNOWN_FIELDS = (
    "call_active", "caller_in_contacts", "call_video_or_voip", "call_minutes", "hour",
    "active_screen_control_confidence", "high_risk_app_installed", "accessibility_service_enabled",
    "apk_installed_hours", "overlay_service", "payee_new", "payee_vpa_age_days", "payee_risk",
    "payee_sebi_valid", "baseline_p95", "credit_claim_recent", "real_credit_found", "qr_recent",
    "qr_from_gallery", "qr_receive_intent", "recent_fraud_report_days", "new_payees_7d",
    "rising_amounts", "small_inbound_unknown_72h", "unofficial_helpline", "new_payee_sum_72h",
    "held_recently_hours",
)


def _screen_control(s: dict):
    """Prefer the explicit field; accept screen_share only as a legacy scenario alias."""
    return s["active_screen_control"] if s["active_screen_control"] is not None else s["screen_share"]


def unknown_signals(s: dict) -> list[str]:
    unknown = [key for key in UNKNOWN_FIELDS if s.get(key) is None]
    if s["active_screen_control"] is None and s["screen_share"] is None:
        unknown.append("active_screen_control")
    return unknown


def eff_amount(s: dict) -> float:
    """Amount used for checks. Payments to new payees are added up over 72 h,
    so splitting ₹4.8 lakh into ten ₹48,000 payments does not dodge the check."""
    earlier = s["new_payee_sum_72h"]
    if s["payee_new"] is True and not s["payee_saved"] and earlier is not None:
        return s["amount"] + earlier
    return s["amount"]

INVEST_WORDS = ("ipo", "share", "trading", "stock", "invest", "allot")
FEE_WORDS = ("fee", "processing", "release", "refund charge", "recovery")


def _has(remark: str, words) -> bool:
    r = (remark or "").lower()
    return any(w in r for w in words)


PREDICATES = {
    "unknown_caller": lambda s: s["call_active"] is True and s["caller_in_contacts"] is False,
    "video_or_voip_call": lambda s: s["call_active"] is True and s["call_video_or_voip"] is True,
    "long_call": lambda s: s["call_active"] is True and s["call_minutes"] is not None and s["call_minutes"] >= 30,
    "odd_hour": lambda s: s["hour"] is not None and (s["hour"] < 7 or s["hour"] >= 22),
    "screen_share": lambda s: _screen_control(s) is True,
    "fresh_apk": lambda s: s["apk_installed_hours"] is not None and s["apk_installed_hours"] < 72,
    "overlay_service": lambda s: s["overlay_service"] is True,
    "new_payee": lambda s: s["payee_new"] is True and not s["payee_saved"],
    "amount_high": lambda s: s["baseline_p95"] is not None and s["baseline_p95"] > 0 and eff_amount(s) > 3 * s["baseline_p95"],
    "amount_very_high": lambda s: s["baseline_p95"] is not None and s["baseline_p95"] > 0 and eff_amount(s) > 10 * s["baseline_p95"],
    "fake_credit": lambda s: s["credit_claim_recent"] is True and s["real_credit_found"] is False,
    "qr_recent": lambda s: s["qr_recent"] is True,
    "qr_from_gallery": lambda s: s["qr_from_gallery"] is True,
    "qr_receive_intent": lambda s: s["qr_receive_intent"] is True,
    "recent_fraud_report": lambda s: s["recent_fraud_report_days"] is not None and s["recent_fraud_report_days"] <= 90,
    "fee_remark": lambda s: _has(s["remark"], FEE_WORDS),
    "invest_remark": lambda s: _has(s["remark"], INVEST_WORDS),
    "payee_not_sebi_valid": lambda s: _has(s["remark"], INVEST_WORDS) and s["payee_sebi_valid"] is False,
    "many_new_payees_7d": lambda s: s["new_payees_7d"] is not None and s["new_payees_7d"] >= 3,
    "rising_amounts": lambda s: s["rising_amounts"] is True,
    "small_inbound_credit_unknown": lambda s: s["small_inbound_unknown_72h"] is True,
    "unofficial_helpline": lambda s: s["unofficial_helpline"] is True,
}

# ---------------------------------------------------------------- tiers
TIERS = ["T0", "T1", "T2", "T3", "T4"]
THRESHOLDS = [(0.85, "T4"), (0.60, "T3"), (0.30, "T2"), (0.10, "T1")]
ACTIONS = {
    "T0": "silent",
    "T1": "caution badge; recipient is not verified",
    "T2": "one neutral question",
    "T3": "scam story + 60 s hold, reconfirm after the call ends",
    "T4": "payment hold; offer trusted-contact and 1930 options",
}
# fusion weights (hand-set for v3, refit on finale data; see docs/RAKSHAM.md)
W = {"bias": -4.6, "stage": 4.2, "anomaly": 1.5, "payee": 1.6, "device": 2.4}


def _tier_index(t: str) -> int:
    return TIERS.index(t)


def load_cards(path: Path | None = None) -> list[dict]:
    path = path or HERE / "playbooks.json"
    return json.loads(Path(path).read_text(encoding="utf-8"))["cards"]


@dataclass
class Match:
    card: dict
    hits: list[str]
    progress: float          # 0..1, how far along the card's kill chain
    complete: bool           # min_match reached


@dataclass
class Decision:
    p: float
    tier: str
    action: str
    card_id: str | None
    card_name: str | None
    reason: str
    question: str | None
    evidence: list[str]
    floors: list[str]
    budget_note: str | None
    components: dict
    unknown_signals: list[str] = field(default_factory=list)
    ms: float = 0.0
    def as_dict(self):
        return self.__dict__.copy()


def match_cards(s: dict, cards: list[dict]) -> list[Match]:
    """Kill-chain match. Each card is a left-to-right chain of stages; a stage
    counts once its predicate is true; the card's 'anchor' stage is mandatory. Progress weights later stages slightly
    more, because reaching 'instruction' or 'extraction' matters more than
    'contact'. (Simplified left-to-right HMM; the full HMM is a finale task.)"""
    out = []
    for c in cards:
        st = c["stages"]
        hits = [x for x in st if PREDICATES.get(x, lambda _s: False)(s)]  # unknown stage never fires
        wsum = sum(1 + 0.25 * i for i in range(len(st)))
        prog = sum(1 + 0.25 * i for i, x in enumerate(st) if x in hits) / wsum
        complete = len(hits) >= c["min_match"] and c.get("anchor", st[0]) in hits
        out.append(Match(c, hits, prog, complete))
    # complete cards first, then cards whose anchor was seen, then cards with
    # a safety floor (their anchor is a hard signal), then most stages hit
    out.sort(key=lambda m: (m.complete, m.card.get("anchor", m.card["stages"][0]) in m.hits,
                            bool(m.card.get("tier_floor")), len(m.hits), m.progress), reverse=True)
    return out


def components(s: dict, best: Match | None) -> dict:
    stage = best.progress if best and best.complete else (best.progress * 0.5 if best else 0.0)
    baseline = s["baseline_p95"]
    if baseline is None or baseline <= 0:
        anomaly = 0.0
    else:
        ratio = max(eff_amount(s), 1.0) / max(baseline, 1.0)
        anomaly = min(1.0, max(0.0, math.log10(ratio) / 1.3))
    anomaly += 0.15 if PREDICATES["odd_hour"](s) else 0.0
    payee = 0.0
    if not s["payee_saved"]:
        age_is_new = s["payee_vpa_age_days"] is not None and s["payee_vpa_age_days"] < 30
        payee = max(s["payee_risk"] or 0.0,
                    (0.35 if s["payee_new"] is True else 0.0) + (0.3 if age_is_new else 0.0))
    device = max(1.0 if _screen_control(s) is True else 0.0,
                 0.8 if PREDICATES["fresh_apk"](s) else 0.0,
                 0.7 if s["overlay_service"] is True else 0.0,
                 0.45 if s["accessibility_service_enabled"] is True else 0.0,
                 0.35 if s["high_risk_app_installed"] is True else 0.0)
    return {"stage": round(stage, 3), "anomaly": round(min(anomaly, 1.0), 3),
            "payee": round(min(payee, 1.0), 3), "device": round(device, 3)}


def fuse(comp: dict) -> float:
    z = W["bias"] + sum(W[k] * comp[k] for k in ("stage", "anomaly", "payee", "device"))
    return 1 / (1 + math.exp(-z))


class AttentionBudget:
    """Per-user interruption budget. Only T2 questions spend it; T3/T4 are
    never suppressed (safety first), but they are rare by construction."""
    def __init__(self, per_30_days: int = 3):
        self.limit = per_30_days
        self.used: list[float] = []
    def remaining(self, now: float) -> int:
        self.used = [t for t in self.used if now - t < 30 * 86400]
        return self.limit - len(self.used)
    def spend(self, now: float):
        self.used.append(now)


def decide(session: dict, cards: list[dict] | None = None,
           budget: AttentionBudget | None = None, now: float | None = None) -> Decision:
    t0 = time.perf_counter()
    s = {**DEFAULT_SESSION, **session}
    cards = cards if cards is not None else load_cards()
    now = time.time() if now is None else now
    ms = match_cards(s, cards)
    best = ms[0] if ms else None
    comp = components(s, best)
    p = fuse(comp)
    tier = "T0"
    for th, t in THRESHOLDS:
        if p >= th:
            tier = t
            break
    floors = []
    # deterministic floors: rules a model is never allowed to override
    hard_floor = bool(best and best.complete and best.card.get("tier_floor"))
    if best and best.complete and best.card.get("tier_floor"):
        f = best.card["tier_floor"]
        if _tier_index(f) > _tier_index(tier):
            tier = f; floors.append(f"{best.card['id']} complete -> floor {f}")
    if PREDICATES["fake_credit"](s) and _tier_index(tier) < 3:
        tier = "T3"; floors.append("fake credit, no real credit -> T3")
    if PREDICATES["fake_credit"](s):
        hard_floor = True
    takeover = _screen_control(s) is True or (PREDICATES["fresh_apk"](s) and s["overlay_service"] is True)
    if takeover and _tier_index(tier) < 4:
        tier = "T4"; floors.append("active screen control or fresh APK plus overlay -> T4")
    if takeover:
        hard_floor = True
    retry = (s["held_recently_hours"] is not None and s["held_recently_hours"] < 24
             and s["payee_new"] is True and not s["payee_saved"])
    if retry and _tier_index(tier) < 3:
        tier = "T3"; floors.append("retry to a new payee within 24 h of a hold -> T3")
    if retry:
        hard_floor = True
    if s["payee_saved"] and not hard_floor and not takeover and _tier_index(tier) > 1:
        tier = "T1"; floors.append("saved / recurring payee -> never above T1")
    budget_note = None
    if tier == "T2" and budget is not None:
        if budget.remaining(now) <= 0:
            tier = "T1"; budget_note = "attention budget used up -> badge instead of question"
        else:
            budget.spend(now)
    anchor_hit = bool(best) and best.card.get("anchor", best.card["stages"][0]) in best.hits
    card = best.card if best and (best.complete or (tier != "T0" and anchor_hit)) else None
    evidence = best.hits if best else []
    if tier in ("T0", "T1"):
        reason = ("No high-risk pattern is confirmed from the available signals." if tier == "T0"
                  else "Caution: check the recipient name independently before paying.")
    else:
        reason = card["reason"] if card else (
            "You were warned about a similar payment today. Scammers often call back and ask again."
            if retry else "Unusual payment pattern.")
    return Decision(round(p, 4), tier, ACTIONS[tier],
                    card["id"] if card else None, card["name"] if card else None,
                    reason, card["question"] if (card and tier == "T2") else None,
                    evidence, floors, budget_note, comp, unknown_signals(s),
                    round((time.perf_counter() - t0) * 1000, 3))


if __name__ == "__main__":
    demo = {"call_active": True, "caller_in_contacts": False, "call_video_or_voip": True,
            "call_minutes": 95, "screen_share": True, "payee_new": True, "payee_vpa_age_days": 4,
            "amount": 480000, "baseline_p95": 12000, "hour": 23}
    print(json.dumps(decide(demo).as_dict(), indent=2, ensure_ascii=False))
