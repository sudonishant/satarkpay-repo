"""Emerging-workflow radar ("emerging" in PS 02).

Playbook cards only know yesterday's scams. The radar looks for *new* ones:
sessions that looked risky (T1+ or a user report) but matched no complete card.
Each such session is reduced to a content-free signature: the sorted set of
predicates that fired, plus an amount band. No user ID, no payee, no text.

If one signature surges (many distinct reporters in 24 h compared with its
7-day baseline), the radar drafts a candidate playbook card. A human analyst
must approve it (and in production sign it) before it reaches any phone.

    r = Radar()
    r.add(predicates, amount_band, reporter_hash, ts)
    r.surges(now) -> [Candidate(...)]
"""
from __future__ import annotations

import hashlib
from collections import defaultdict
from dataclasses import dataclass, field


@dataclass
class Candidate:
    signature: tuple
    count_24h: int
    reporters_24h: int
    baseline_per_day: float
    draft_card: dict
    status: str = "needs_analyst_approval"


@dataclass
class Radar:
    min_reporters: int = 5          # distinct reporters in 24 h
    surge_factor: float = 3.0       # vs 7-day daily average
    events: dict = field(default_factory=lambda: defaultdict(list))   # sig -> [(ts, reporter)]

    @staticmethod
    def signature(predicates, amount_band: str) -> tuple:
        generic = {"new_payee", "odd_hour"}
        core = tuple(sorted(p for p in set(predicates) if p not in generic))
        return core + (f"band:{amount_band}",)

    def add(self, predicates, amount_band: str, reporter_hash: str, ts: float):
        sig = self.signature(predicates, amount_band)
        if len(sig) < 3:             # too generic to learn from
            return None
        self.events[sig].append((ts, reporter_hash))
        return sig

    def surges(self, now: float) -> list[Candidate]:
        out = []
        for sig, ev in self.events.items():
            last = [(t, r) for t, r in ev if now - t < 86400]
            week = [(t, r) for t, r in ev if 86400 <= now - t < 8 * 86400]
            reporters = len({r for _, r in last})
            base = len(week) / 7.0
            if reporters >= self.min_reporters and len(last) >= self.surge_factor * max(base, 1.0):
                stages = [p for p in sig if not p.startswith("band:")]
                out.append(Candidate(sig, len(last), reporters, round(base, 2), {
                    "id": "PB-NEW-" + hashlib.sha1("|".join(sig).encode()).hexdigest()[:6],
                    "name": "Candidate workflow (radar draft)",
                    "stages": stages + ["new_payee"],
                    "min_match": max(2, len(stages)),
                    "anchor": stages[0],
                    "tier_floor": None,
                    "reason": "Many people were asked to pay in this exact way today. Please check before you pay.",
                    "question": "Is someone guiding you through this payment right now?",
                }))
        out.sort(key=lambda c: c.reporters_24h, reverse=True)
        return out
