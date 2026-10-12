"""Synthetic scenario generator + evaluation for the THEHRO engine.

IMPORTANT: these are *simulated* sessions written by the team from public case
reports (see data/ and the 50-scam catalogue). They test that the logic does
what we claim; they are NOT real-world accuracy. Real numbers come from the
finale field test (docs/RAKSHAM.md, section "Proof plan").

Run:  python -m thehro.simulate            (prints a table, writes results JSON)
"""
from __future__ import annotations

import json
import random
import statistics
import sys
from pathlib import Path

from .engine import AttentionBudget, decide, load_cards

OUT = Path(__file__).resolve().parent / "results" / "sim_results.json"


def _base(r: random.Random) -> dict:
    return {"baseline_p95": r.choice([1500, 3000, 6000, 12000, 25000]), "hour": r.randint(8, 21)}


def scam_sessions(r: random.Random) -> list[dict]:
    """50 scam sessions, about 4 variants per P1 card. Some variants drop
    signals on purpose (e.g. no screen share, amount only 4x) so the test is
    not a copy of the card."""
    S = []

    def add(card, d, note):
        b = _base(r); b.update(d); S.append({"label": "scam", "card": card, "note": note, "s": b})

    for i in range(5):  # PB-B1 digital arrest
        bl = _base(r)["baseline_p95"]
        add("PB-B1", {"call_active": True, "caller_in_contacts": False, "call_video_or_voip": True,
                      "call_minutes": r.choice([45, 90, 140]), "screen_share": i % 2 == 0,
                      "payee_new": True, "payee_vpa_age_days": r.randint(1, 20), "baseline_p95": bl,
                      "amount": bl * r.choice([12, 25, 40]), "hour": r.choice([10, 15, 22])},
            "video call, long, new payee, big amount" + (" + screen share" if i % 2 == 0 else ""))
    for i in range(4):  # PB-B2 recovery agent
        add("PB-B2", {"recent_fraud_report_days": r.randint(3, 60), "call_active": i != 3,
                      "caller_in_contacts": False, "payee_new": True, "remark": r.choice(["processing fee", "recovery charges", "release fee"]),
                      "amount": r.choice([4999, 9999, 15000])}, "re-victimisation fee")
    for i in range(4):  # PB-A1 family emergency
        bl = _base(r)["baseline_p95"]
        add("PB-A1", {"call_active": True, "caller_in_contacts": False, "payee_new": True,
                      "baseline_p95": bl, "amount": bl * r.choice([4, 8, 15]), "qr_recent": i != 2,
                      "payee_vpa_age_days": r.randint(2, 200)}, "unknown caller, urgent QR")
    for i in range(4):  # PB-A3 video friend
        bl = _base(r)["baseline_p95"]
        add("PB-A3", {"call_active": True, "caller_in_contacts": False, "call_video_or_voip": True,
                      "payee_new": True, "baseline_p95": bl, "amount": bl * r.choice([3.5, 6, 10]),
                      "payee_vpa_age_days": r.randint(5, 400)}, "video call from new number")
    for i in range(4):  # PB-A4 fake credit
        add("PB-A4", {"credit_claim_recent": True, "real_credit_found": False, "payee_new": True,
                      "qr_recent": i % 2 == 0, "amount": r.choice([2000, 5000, 10000]),
                      "call_active": i < 2, "caller_in_contacts": False}, "fake 'credited' SMS")
    for i in range(4):  # PB-C1 deepfake investment ad
        bl = _base(r)["baseline_p95"]
        add("PB-C1", {"remark": ["invest", "trading profit", "", "stock tips"][i], "new_payees_7d": r.randint(3, 6), "rising_amounts": True, "small_inbound_unknown_72h": i != 1,
                      "payee_new": True, "baseline_p95": bl, "amount": bl * r.choice([2, 5, 9]),
                      "payee_vpa_age_days": r.randint(3, 60)}, "rising deposits after small profit")
    for i in range(4):  # PB-C2 fake IPO
        bl = _base(r)["baseline_p95"]
        add("PB-C2", {"remark": r.choice(["IPO allotment", "trading account", "pre-IPO shares"]), "payee_sebi_valid": False,
                      "payee_new": True, "baseline_p95": bl, "amount": bl * r.choice([4, 10, 20])}, "IPO to non-@valid")
    for i in range(4):  # PB-C3 task scam
        bl = _base(r)["baseline_p95"]
        add("PB-C3", {"small_inbound_unknown_72h": True, "new_payees_7d": r.randint(3, 5), "rising_amounts": True,
                      "payee_new": True, "baseline_p95": bl, "amount": bl * r.choice([1.5, 3, 6]),
                      "payee_vpa_age_days": r.randint(1, 30)}, "task scam deposit")
    for i in range(4):  # PB-D1 APK
        add("PB-D1", {"apk_installed_hours": r.choice([2, 20, 60]), "overlay_service": i != 3,
                      "payee_new": True, "amount": r.choice([3000, 20000, 49000])}, "fresh APK with control")
    for i in range(5):  # PB-D2 screen share KYC
        add("PB-D2", {"screen_share": True, "call_active": True, "caller_in_contacts": False,
                      "payee_new": i != 4, "amount": r.choice([1, 5000, 25000])}, "screen share during call")
    for i in range(4):  # PB-E1 inverted QR
        add("PB-E1", {"qr_from_gallery": True, "qr_receive_intent": i != 1, "call_active": i % 2 == 0,
                      "caller_in_contacts": False, "payee_new": True, "amount": r.choice([5000, 12000, 30000])},
            "QR to 'receive' money")
    for i in range(4):  # PB-G1 fake helpline
        bl = _base(r)["baseline_p95"]
        add("PB-G1", {"unofficial_helpline": True, "call_active": True, "caller_in_contacts": False,
                      "screen_share": i == 0, "payee_new": True, "baseline_p95": bl,
                      "amount": bl * r.choice([1, 4, 8])}, "helpline number from search")
    return S


def normal_sessions(r: random.Random, n: int = 200) -> list[dict]:
    """Everyday payments, including hard benign cases: long calls with family,
    first-time big merchant payments, new payees while on a known call."""
    N = []
    kinds = ["merchant_small", "saved_family", "rent_saved", "new_shop", "call_known_new_payee",
             "big_first_time", "late_night_food", "sip_valid", "friend_split", "refund_real"]
    for i in range(n):
        k = kinds[i % len(kinds)]
        b = _base(r); bl = b["baseline_p95"]
        if k == "merchant_small":
            b.update(payee_new=r.random() < 0.3, payee_vpa_age_days=r.randint(200, 2000), amount=r.randint(20, 800))
        elif k == "saved_family":
            b.update(payee_saved=True, payee_new=False, amount=bl * r.choice([0.5, 2, 6]),
                     call_active=r.random() < 0.5, caller_in_contacts=True, call_minutes=r.choice([5, 40]))
        elif k == "rent_saved":
            b.update(payee_saved=True, payee_new=False, amount=bl * r.choice([4, 8]))
        elif k == "new_shop":
            b.update(payee_new=True, payee_vpa_age_days=r.randint(30, 1500), amount=bl * r.uniform(0.2, 1.5))
        elif k == "call_known_new_payee":
            b.update(call_active=True, caller_in_contacts=True, payee_new=True,
                     payee_vpa_age_days=r.randint(60, 900), amount=bl * r.uniform(0.3, 2.5))
        elif k == "big_first_time":   # e.g. new laptop, hospital deposit
            b.update(payee_new=True, payee_vpa_age_days=r.randint(90, 3000), amount=bl * r.choice([4, 8, 12]))
        elif k == "late_night_food":
            b.update(hour=r.choice([22, 23, 0, 1]), payee_new=r.random() < 0.5, payee_vpa_age_days=900, amount=r.randint(150, 900))
        elif k == "sip_valid":
            b.update(remark="SIP mutual fund invest", payee_sebi_valid=True, payee_new=r.random() < 0.3,
                     payee_vpa_age_days=1500, amount=bl * r.choice([0.5, 2]))
        elif k == "friend_split":
            b.update(payee_new=r.random() < 0.4, payee_vpa_age_days=r.randint(20, 900), amount=r.randint(200, 3000),
                     call_active=r.random() < 0.2, caller_in_contacts=True)
        elif k == "refund_real":
            b.update(credit_claim_recent=True, real_credit_found=True, payee_new=True,
                     payee_vpa_age_days=r.randint(30, 900), amount=r.randint(500, 5000))
        N.append({"label": "normal", "card": None, "note": k, "s": b})
    return N


def evaluate(seed: int = 7) -> dict:
    r = random.Random(seed)
    cards = load_cards()
    scams, normals = scam_sessions(r), normal_sessions(r)
    rows, lat = [], []
    budget = AttentionBudget(per_30_days=3)
    day = 0.0
    for case in scams + normals:
        d = decide(case["s"], cards, budget if case["label"] == "normal" else None, now=day)
        day += 86400 * 30 / 100   # ~100 payments a month for the normal user
        lat.append(d.ms)
        rows.append({**{k: case[k] for k in ("label", "card", "note")}, "tier": d.tier, "p": d.p,
                     "matched": d.card_id, "floors": d.floors})
    sc = [x for x in rows if x["label"] == "scam"]
    no = [x for x in rows if x["label"] == "normal"]
    ge = lambda x, t: int(x["tier"][1]) >= t
    caught_t2 = sum(ge(x, 2) for x in sc)
    caught_t3 = sum(ge(x, 3) for x in sc)
    right_card = sum(x["matched"] == x["card"] for x in sc)
    fp_t3 = sum(ge(x, 3) for x in no)
    interrupts = sum(ge(x, 2) for x in no)
    tp_t3 = caught_t3
    res = {
        "simulated": True, "seed": seed,
        "scam_sessions": len(sc), "normal_sessions": len(no),
        "scams_caught_T2_plus": caught_t2, "scams_caught_T3_plus": caught_t3,
        "correct_playbook": right_card,
        "precision_T3_plus": round(tp_t3 / max(tp_t3 + fp_t3, 1), 3),
        "interruptions_per_100_normal": round(100 * interrupts / len(no), 2),
        "normal_tier_mix": {t: sum(x["tier"] == t for x in no) for t in ("T0", "T1", "T2", "T3", "T4")},
        "decision_ms_median": round(statistics.median(lat), 3),
        "decision_ms_p95": round(sorted(lat)[int(0.95 * len(lat)) - 1], 3),
        "missed": [x for x in sc if not ge(x, 2)],
        "false_alarms_T3_plus": [x for x in no if ge(x, 3)],
        "rows": rows,
    }
    return res


def main():
    res = evaluate()
    OUT.parent.mkdir(exist_ok=True)
    OUT.write_text(json.dumps(res, indent=2, ensure_ascii=False), encoding="utf-8")
    print("THEHRO simulated evaluation (synthetic sessions, NOT real-world accuracy)")
    for k in ("scam_sessions", "normal_sessions", "scams_caught_T2_plus", "scams_caught_T3_plus",
              "correct_playbook", "precision_T3_plus", "interruptions_per_100_normal",
              "normal_tier_mix", "decision_ms_median", "decision_ms_p95"):
        print(f"  {k:32s} {res[k]}")
    for m in res["missed"]:
        print("  MISSED:", m["card"], m["note"], m["tier"], m["p"])
    for m in res["false_alarms_T3_plus"]:
        print("  FALSE ALARM:", m["note"], m["tier"], m["p"], m["floors"])
    print(f"  written: {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
