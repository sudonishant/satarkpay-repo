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
    "call_active": False,          # TelephonyCallback / AudioManager mode
    "caller_in_contacts": True,    # number lookup only, contacts never leave the phone
    "call_video_or_voip": False,   # AudioManager MODE_IN_COMMUNICATION
    "call_minutes": 0.0,
    "hour": 14,
    "screen_share": False,         # Play Integrity appAccessRiskVerdict / MediaProjection
    "apk_installed_hours": None,   # newest sideloaded APK with SMS/accessibility permission
    "overlay_service": False,      # unknown enabled accessibility service
    "payee_saved": False,
    "payee_new": True,
    "payee_vpa_age_days": 365,
    "payee_risk": 0.0,             # DoT FRI / bank mule score, 0..1
    "payee_sebi_valid": False,     # '@valid' handle
    "amount": 500.0,
    "baseline_p95": 2000.0,        # user's own 95th-percentile payment
    "credit_claim_recent": False,  # a 'credited' SMS arrived (sender + amount only)
    "real_credit_found": False,    # PSP ledger confirms money actually arrived
    "qr_recent": False,            # QR received / saved in the last 15 min
    "qr_from_gallery": False,
    "qr_receive_intent": False,    # user picked "I am receiving money"
    "recent_fraud_report_days": None,
    "remark": "",                  # UPI note typed for this payment (short)
    "new_payees_7d": 0,
    "rising_amounts": False,       # last 3 outbound amounts strictly increasing
    "small_inbound_unknown_72h": False,
    "unofficial_helpline": False,  # last dialled number not in official registry
    "new_payee_sum_72h": 0.0,      # earlier payments to new payees in 72 h (anti-splitting)
    "held_recently_hours": None,   # hours since this user last saw a T3/T4 hold
}


def eff_amount(s: dict) -> float:
    """Amount used for checks. Payments to new payees are added up over 72 h,
    so splitting ₹4.8 lakh into ten ₹48,000 payments does not dodge the check."""
    return s["amount"] + (s["new_payee_sum_72h"] if s["payee_new"] and not s["payee_saved"] else 0.0)

INVEST_WORDS = ("ipo", "share", "trading", "stock", "invest", "allot")
FEE_WORDS = ("fee", "processing", "release", "refund charge", "recovery")


def _has(remark: str, words) -> bool:
    r = (remark or "").lower()
    return any(w in r for w in words)


PREDICATES = {
    "unknown_caller": lambda s: s["call_active"] and not s["caller_in_contacts"],
    "video_or_voip_call": lambda s: s["call_active"] and s["call_video_or_voip"],
    "long_call": lambda s: s["call_active"] and s["call_minutes"] >= 30,
    "odd_hour": lambda s: s["hour"] < 7 or s["hour"] >= 22,
    "screen_share": lambda s: s["screen_share"],
    "fresh_apk": lambda s: s["apk_installed_hours"] is not None and s["apk_installed_hours"] < 72,
    "overlay_service": lambda s: s["overlay_service"],
    "new_payee": lambda s: s["payee_new"] and not s["payee_saved"],
    "amount_high": lambda s: eff_amount(s) > 3 * s["baseline_p95"],
    "amount_very_high": lambda s: eff_amount(s) > 10 * s["baseline_p95"],
    "fake_credit": lambda s: s["credit_claim_recent"] and not s["real_credit_found"],
    "qr_recent": lambda s: s["qr_recent"],
    "qr_from_gallery": lambda s: s["qr_from_gallery"],
    "qr_receive_intent": lambda s: s["qr_receive_intent"],
    "recent_fraud_report": lambda s: s["recent_fraud_report_days"] is not None and s["recent_fraud_report_days"] <= 90,
    "fee_remark": lambda s: _has(s["remark"], FEE_WORDS),
    "invest_remark": lambda s: _has(s["remark"], INVEST_WORDS),
    "payee_not_sebi_valid": lambda s: _has(s["remark"], INVEST_WORDS) and not s["payee_sebi_valid"],
    "many_new_payees_7d": lambda s: s["new_payees_7d"] >= 3,
    "rising_amounts": lambda s: s["rising_amounts"],
    "small_inbound_credit_unknown": lambda s: s["small_inbound_unknown_72h"],
    "unofficial_helpline": lambda s: s["unofficial_helpline"],
}

# ---------------------------------------------------------------- tiers
TIERS = ["T0", "T1", "T2", "T3", "T4"]
THRESHOLDS = [(0.85, "T4"), (0.60, "T3"), (0.30, "T2"), (0.10, "T1")]
ACTIONS = {
    "T0": "silent",
    "T1": "trust badge on the pay screen",
    "T2": "one neutral question",
    "T3": "scam story + 60 s hold, reconfirm after the call ends",
    "T4": "circuit breaker: hold + guardian alert + 1930 option",
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
    ratio = max(eff_amount(s), 1.0) / max(s["baseline_p95"], 1.0)
    anomaly = min(1.0, max(0.0, math.log10(ratio) / 1.3)) + (0.15 if PREDICATES["odd_hour"](s) else 0.0)
    payee = 0.0
    if not s["payee_saved"]:
        payee = max(s["payee_risk"],
                    (0.35 if s["payee_new"] else 0.0) + (0.3 if s["payee_vpa_age_days"] < 30 else 0.0))
    device = max(1.0 if s["screen_share"] else 0.0,
                 0.8 if PREDICATES["fresh_apk"](s) else 0.0,
                 0.7 if s["overlay_service"] else 0.0)
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
    if best and best.complete and best.card.get("tier_floor"):
        f = best.card["tier_floor"]
        if _tier_index(f) > _tier_index(tier):
            tier = f; floors.append(f"{best.card['id']} complete -> floor {f}")
    if PREDICATES["fake_credit"](s) and _tier_index(tier) < 3:
        tier = "T3"; floors.append("fake credit, no real credit -> T3")
    takeover = s["screen_share"] or (PREDICATES["fresh_apk"](s) and s["overlay_service"])
    if takeover and not s["payee_saved"] and _tier_index(tier) < 4:
        tier = "T4"; floors.append("screen control on pay screen -> T4")
    retry = (s["held_recently_hours"] is not None and s["held_recently_hours"] < 24
             and s["payee_new"] and not s["payee_saved"])
    if retry and _tier_index(tier) < 3:
        tier = "T3"; floors.append("retry to a new payee within 24 h of a hold -> T3")
    if s["payee_saved"] and not takeover and _tier_index(tier) > 1:
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
        reason = "Looks like a normal payment." if tier == "T0" else "New payee. Check the name before you pay."
    else:
        reason = card["reason"] if card else (
            "You were warned about a similar payment today. Scammers often call back and ask again."
            if retry else "Unusual payment pattern.")
    return Decision(round(p, 4), tier, ACTIONS[tier],
                    card["id"] if card else None, card["name"] if card else None,
                    reason, card["question"] if (card and tier == "T2") else None,
                    evidence, floors, budget_note, comp,
                    round((time.perf_counter() - t0) * 1000, 3))


if __name__ == "__main__":
    demo = {"call_active": True, "caller_in_contacts": False, "call_video_or_voip": True,
            "call_minutes": 95, "screen_share": True, "payee_new": True, "payee_vpa_age_days": 4,
            "amount": 480000, "baseline_p95": 12000, "hour": 23}
    print(json.dumps(decide(demo).as_dict(), indent=2, ensure_ascii=False))
