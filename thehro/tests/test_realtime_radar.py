import unittest

from thehro.engine import decide
from thehro.radar import Radar
from thehro.realtime import SessionTracker

T0 = 1_760_000_000.0


class RealtimeTests(unittest.TestCase):
    def test_digital_arrest_stream_escalates_with_real_clock(self):
        t = SessionTracker(baseline_p95=12000)
        tiers = [t.on("call_start", T0, video=True, in_contacts=False).tier]
        tiers.append(t.on("tick", T0 + 10 * 60).tier)               # 10 min: still quiet
        tiers.append(t.on("screen_share_on", T0 + 40 * 60).tier)    # 40 min + screen share
        d = t.on("amount_entered", T0 + 95 * 60, amount=480000, payee_new=True, payee_vpa_age_days=4)
        self.assertTrue(all(x in ("T0", "T1") for x in tiers[:2]))   # no interruption yet
        self.assertEqual(d.tier, "T4")
        self.assertAlmostEqual(t.decide(T0 + 95 * 60).components["device"], 1.0)

    def test_call_minutes_come_from_clock(self):
        t = SessionTracker(baseline_p95=12000)
        t.on("call_start", T0, video=True, in_contacts=False)
        d = t.on("amount_entered", T0 + 31 * 60, amount=150000, payee_new=True, payee_vpa_age_days=3)
        self.assertIn("long_call", d.evidence)

    def test_call_end_resets(self):
        t = SessionTracker()
        t.on("call_start", T0, in_contacts=False)
        d = t.on("call_end", T0 + 60)
        self.assertNotIn("unknown_caller", d.evidence)

    def test_split_payments_are_added_up(self):
        t = SessionTracker(baseline_p95=10000)
        for i in range(4):
            t.on("amount_entered", T0 + i * 3600, amount=30000, payee_new=True, payee_vpa_age_days=5)
            t.on("payment_done", T0 + i * 3600 + 5)
        d = t.decide(T0 + 5 * 3600)
        self.assertIn("amount_very_high", d.evidence)   # 30k now + 120k earlier > 10x baseline

    def test_retry_after_hold_is_t3(self):
        d = decide({"payee_new": True, "payee_vpa_age_days": 200, "amount": 2000, "held_recently_hours": 3})
        self.assertEqual(d.tier, "T3")
        self.assertIn("warned", d.reason)

    def test_unknown_event_ignored(self):
        t = SessionTracker()
        self.assertEqual(t.on("some_future_event", T0).tier, "T0")

    def test_audit_log_has_no_content(self):
        t = SessionTracker()
        t.on("call_start", T0, in_contacts=False)
        self.assertEqual(t.log[0][1:], ("call_start", "T0"))


class RadarTests(unittest.TestCase):
    PRED = ["unknown_caller", "qr_from_gallery", "unofficial_helpline", "new_payee"]

    def test_surge_drafts_candidate_for_analyst(self):
        r = Radar()
        for i in range(8):
            r.add(self.PRED, "10k-50k", f"user{i}", T0 + i * 600)
        c = r.surges(T0 + 3 * 3600)
        self.assertEqual(len(c), 1)
        self.assertEqual(c[0].status, "needs_analyst_approval")
        self.assertTrue(c[0].draft_card["id"].startswith("PB-NEW-"))

    def test_one_user_spamming_is_not_a_surge(self):
        r = Radar()
        for i in range(20):
            r.add(self.PRED, "10k-50k", "same_user", T0 + i)
        self.assertEqual(r.surges(T0 + 100), [])

    def test_steady_signature_is_not_emerging(self):
        r = Radar()
        for d in range(1, 8):
            for i in range(6):
                r.add(self.PRED, "10k-50k", f"u{d}{i}", T0 - d * 86400 + i)
        for i in range(6):
            r.add(self.PRED, "10k-50k", f"n{i}", T0 + i)
        self.assertEqual(r.surges(T0 + 100), [])

    def test_generic_signals_alone_are_ignored(self):
        r = Radar()
        self.assertIsNone(r.add(["new_payee", "odd_hour"], "1k-10k", "u", T0))

    def test_signature_is_deterministic(self):
        a = Radar.signature(["b", "a", "new_payee"], "x")
        self.assertEqual(a, ("a", "b", "band:x"))


if __name__ == "__main__":
    unittest.main()
