package com.example.thehro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
