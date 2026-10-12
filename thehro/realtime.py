"""Real-time session tracker for THEHRO.

The phone emits small events as they happen (call started, screen share on,
payee opened, amount typed ...). The tracker keeps one live session per
payment journey and can answer "what should happen if the user paid now?"
at any moment, with call length computed from the real clock.

    t = SessionTracker(baseline_p95=12000)
    t.on("call_start", ts, video=True, in_contacts=False)
    t.on("screen_share_on", ts + 1800)
    d = t.on("amount_entered", ts + 5400, amount=480000, payee_new=True, payee_vpa_age_days=4)
    d.tier  -> "T4"

Events carry metadata only. Unknown event names are ignored (forward-compatible).
"""
from __future__ import annotations

import time

from .engine import AttentionBudget, Decision, decide, load_cards


class SessionTracker:
    def __init__(self, baseline_p95: float = 2000.0, budget: AttentionBudget | None = None, cards=None):
        self.cards = cards if cards is not None else load_cards()
        self.budget = budget
        self.s: dict = {"baseline_p95": baseline_p95}
        self.call_started: float | None = None
        self.payments_72h: list[tuple[float, float]] = []   # (ts, amount) to new payees
        self.last_hold: float | None = None
        self.log: list[tuple[float, str, str]] = []          # (ts, event, tier) audit trail, no content

    # -- event handlers ---------------------------------------------------
    def on(self, event: str, ts: float | None = None, **d) -> Decision:
        ts = time.time() if ts is None else ts
        s = self.s
        if event == "call_start":
            self.call_started = ts
            s.update(call_active=True, caller_in_contacts=d.get("in_contacts", False),
                     call_video_or_voip=d.get("video", False))
        elif event == "call_end":
            self.call_started = None
            s.update(call_active=False, call_minutes=0.0)
        elif event == "screen_share_on":
            s["screen_share"] = True
        elif event == "screen_share_off":
            s["screen_share"] = False
        elif event == "apk_installed":
            s["apk_installed_at"] = ts
            s["overlay_service"] = d.get("accessibility", False)
        elif event == "sms_credit_claim":
            s["credit_claim_recent"] = True
        elif event == "credit_confirmed":
            s["real_credit_found"] = True
        elif event == "qr_scanned":
            s.update(qr_recent=True, qr_from_gallery=d.get("from_gallery", False),
                     qr_receive_intent=d.get("receive_intent", False))
        elif event in ("payee_opened", "amount_entered"):
            for k in ("payee_new", "payee_saved", "payee_vpa_age_days", "payee_risk",
                      "payee_sebi_valid", "amount", "remark"):
                if k in d:
                    s[k] = d[k]
        elif event == "payment_done":
            if s.get("payee_new", True) and not s.get("payee_saved", False):
                self.payments_72h.append((ts, float(s.get("amount", 0))))
        dec = self.decide(ts)
        if dec.tier in ("T3", "T4") and event in ("amount_entered", "pay_tapped"):
            self.last_hold = ts
        self.log.append((ts, event, dec.tier))
        return dec

    # -- decision at time ts ------------------------------------------------
    def decide(self, ts: float | None = None) -> Decision:
        ts = time.time() if ts is None else ts
        s = dict(self.s)
        if self.call_started is not None:
            s["call_minutes"] = (ts - self.call_started) / 60.0
        if "apk_installed_at" in s:
            s["apk_installed_hours"] = (ts - s.pop("apk_installed_at")) / 3600.0
        self.payments_72h = [(t, a) for t, a in self.payments_72h if ts - t < 72 * 3600]
        s["new_payee_sum_72h"] = sum(a for _, a in self.payments_72h)
        if self.last_hold is not None:
            s["held_recently_hours"] = (ts - self.last_hold) / 3600.0
        return decide(s, self.cards, self.budget, now=ts)
