"""Run: python -m unittest discover -s thehro/tests -t ."""
import unittest

from thehro.engine import AttentionBudget, decide
from thehro.simulate import evaluate

DIGITAL_ARREST = {"call_active": True, "caller_in_contacts": False, "call_video_or_voip": True,
                  "call_minutes": 95, "screen_share": True, "payee_new": True, "payee_vpa_age_days": 4,
                  "amount": 480000, "baseline_p95": 12000, "hour": 23}


class EngineTests(unittest.TestCase):
    def test_digital_arrest_is_t4(self):
        d = decide(DIGITAL_ARREST)
        self.assertEqual(d.tier, "T4")
        self.assertEqual(d.card_id, "PB-B1")

    def test_digital_arrest_without_screen_share_still_floor_t4(self):
        d = decide({**DIGITAL_ARREST, "screen_share": False})
        self.assertEqual(d.tier, "T4")

    def test_saved_payee_never_above_t1(self):
        d = decide({"payee_saved": True, "payee_new": False, "amount": 90000, "baseline_p95": 3000,
                    "call_active": True, "caller_in_contacts": False})
        self.assertLessEqual(int(d.tier[1]), 1)

    def test_screen_control_beats_saved_payee_cap(self):
        d = decide({"payee_saved": False, "screen_share": True, "amount": 10})
        self.assertEqual(d.tier, "T4")

    def test_fake_credit_floor(self):
        d = decide({"credit_claim_recent": True, "real_credit_found": False, "amount": 2000})
        self.assertGreaterEqual(int(d.tier[1]), 3)

    def test_real_refund_is_quiet(self):
        d = decide({"credit_claim_recent": True, "real_credit_found": True, "payee_new": False,
                    "payee_saved": True, "amount": 2000})
        self.assertLessEqual(int(d.tier[1]), 1)

    def test_small_merchant_payment_silent(self):
        d = decide({"payee_new": False, "payee_vpa_age_days": 900, "amount": 120})
        self.assertEqual(d.tier, "T0")

    def test_budget_downgrades_questions_only(self):
        b = AttentionBudget(per_30_days=1)
        q = {"payee_new": True, "payee_vpa_age_days": 10, "amount": 30000, "baseline_p95": 3000}
        first = decide(q, budget=b, now=0)
        second = decide(q, budget=b, now=60)
        if first.tier == "T2":
            self.assertEqual(second.tier, "T1")
            self.assertIsNotNone(second.budget_note)
        # T4 is never suppressed by the budget
        self.assertEqual(decide(DIGITAL_ARREST, budget=b, now=120).tier, "T4")

    def test_no_text_or_audio_fields_needed(self):
        d = decide({})
        self.assertEqual(d.tier, "T0")

    def test_unknown_stage_never_fires(self):
        card = {"id": "X", "name": "x", "stages": ["future_signal"], "min_match": 1,
                "anchor": "future_signal", "tier_floor": "T4", "reason": "r", "question": "q?"}
        self.assertEqual(decide({}, [card]).tier, "T0")

    def test_simulation_targets(self):
        r = evaluate()
        self.assertGreaterEqual(r["scams_caught_T3_plus"], 45)
        self.assertGreaterEqual(r["precision_T3_plus"], 0.9)
        self.assertLessEqual(r["interruptions_per_100_normal"], 3.0)


if __name__ == "__main__":
    unittest.main()
