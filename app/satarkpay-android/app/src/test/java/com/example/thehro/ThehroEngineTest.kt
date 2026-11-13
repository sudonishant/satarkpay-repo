package com.example.thehro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Same cases as thehro/tests/test_engine.py, so the Kotlin port cannot drift. */
class ThehroEngineTest {
    private val engine = ThehroEngine()
    private val digitalArrest = PaymentSession(
        callActive = true, callerInContacts = false, callVideoOrVoip = true, callMinutes = 95.0,
        screenShare = true, payeeNew = true, payeeVpaAgeDays = 4, amount = 480000.0,
        baselineP95 = 12000.0, hour = 23,
    )

    @Test fun digitalArrestIsT4() {
        val d = engine.decide(digitalArrest)
        assertEquals("T4", d.tier); assertEquals("PB-B1", d.cardId)
        assertEquals(0.9894, d.p, 1e-3)
    }

    @Test fun digitalArrestWithoutScreenShareStillT4() =
        assertEquals("T4", engine.decide(digitalArrest.copy(screenShare = false)).tier)

    @Test fun savedPayeeNeverAboveT1() {
        val d = engine.decide(PaymentSession(payeeSaved = true, payeeNew = false, amount = 90000.0,
            baselineP95 = 3000.0, callActive = true, callerInContacts = false))
        assertTrue(ThehroEngine.TIERS.indexOf(d.tier) <= 1)
    }

    @Test fun screenControlBeatsCap() =
        assertEquals("T4", engine.decide(PaymentSession(screenShare = true, amount = 10.0)).tier)

    @Test fun unknownControlAndInstalledRiskAppDoNotImplyTakeover() {
        val d = engine.decide(PaymentSession(activeScreenControl = null, screenShare = null,
            highRiskAppInstalled = true, amount = 10.0))
        assertFalse(d.tier == "T4")
        assertTrue("active_screen_control" in d.unknownSignals)
        assertEquals(0.35, d.components.device, 1e-6)
    }

    @Test fun unknownCreditVerificationDoesNotTriggerFakeCreditFloor() {
        val d = engine.decide(PaymentSession(creditClaimRecent = true, realCreditFound = null, amount = 2000.0))
        assertTrue(ThehroEngine.TIERS.indexOf(d.tier) < 3)
    }

    @Test fun unknownP95DoesNotCreateAnAmountAnomaly() {
        val d = engine.decide(PaymentSession(amount = 10_000_000.0, baselineP95 = null))
        assertEquals(0.0, d.components.anomaly, 1e-6)
        assertTrue("baseline_p95" in d.unknownSignals)
    }

    @Test fun explicitTrustDoesNotSuppressFakeCreditFloor() {
        val d = engine.decide(PaymentSession(payeeSaved = true, payeeNew = false,
            creditClaimRecent = true, realCreditFound = false, amount = 100.0))
        assertEquals("T3", d.tier)
        assertTrue("fake credit, no real credit -> T3" in d.floors)
    }

    @Test fun paymentGateOnlyAllowsT0T1OrExplicitlyConfirmedT2() {
        val t0 = engine.decide(PaymentSession())
        assertTrue(t0.permitsPayment())
        val t2 = Decision(0.4, "T2", "one neutral question", null, null, "Check?", "Check?",
            emptyList(), emptyList(), null, Components(0.0, 0.0, 0.0, 0.0), emptyList())
        assertFalse(t2.permitsPayment())
        assertTrue(t2.permitsPayment(questionConfirmed = true))
        val t3 = t2.copy(tier = "T3")
        val t4 = t2.copy(tier = "T4")
        assertFalse(t3.permitsPayment(questionConfirmed = true))
        assertFalse(t4.permitsPayment(questionConfirmed = true))
    }

    @Test fun personalBaselineNeedsTwentySamplesAndUsesNearestRankP95() {
        assertNull(PersonalBaseline.p95((1L..19L).toList()))
        assertEquals(19.0, PersonalBaseline.p95((1L..20L).toList()) ?: -1.0, 0.0)
        assertEquals(95.0, PersonalBaseline.p95((1L..100L).toList()) ?: -1.0, 0.0)
    }

    @Test fun fakeCreditFloor() {
        val d = engine.decide(PaymentSession(creditClaimRecent = true, realCreditFound = false, amount = 2000.0))
        assertTrue(ThehroEngine.TIERS.indexOf(d.tier) >= 3)
    }

    @Test fun smallMerchantSilent() =
        assertEquals("T0", engine.decide(PaymentSession(payeeNew = false, payeeVpaAgeDays = 900, amount = 120.0)).tier)

    @Test fun budgetNeverSuppressesT4() {
        val b = AttentionBudget(1)
        val q = PaymentSession(payeeNew = true, payeeVpaAgeDays = 10, amount = 30000.0, baselineP95 = 3000.0)
        val first = engine.decide(q, b, 0); val second = engine.decide(q, b, 60_000)
        if (first.tier == "T2") { assertEquals("T1", second.tier); assertNotNull(second.budgetNote) }
        assertEquals("T4", engine.decide(digitalArrest, b, 120_000).tier)
    }
}
