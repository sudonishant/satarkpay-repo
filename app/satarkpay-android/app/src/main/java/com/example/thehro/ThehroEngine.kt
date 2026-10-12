package com.example.thehro

import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

/**
 * THEHRO engine, Kotlin port of thehro/engine.py (SatarkPay v3, RAKSHAM 2026, PS 02).
 *
 * Input is metadata only. No SMS body, chat text or call audio is read.
 * Output is one decision per payment attempt: a tier T0..T4, a plain reason,
 * and the evidence behind it. Pure Kotlin: no Android imports, unit-testable on the JVM.
 */
data class PaymentSession(
    val callActive: Boolean = false,
    val callerInContacts: Boolean = true,
    val callVideoOrVoip: Boolean = false,
    val callMinutes: Double = 0.0,
    val hour: Int = 14,
    val screenShare: Boolean = false,
    val apkInstalledHours: Double? = null,
    val overlayService: Boolean = false,
    val payeeSaved: Boolean = false,
    val payeeNew: Boolean = true,
    val payeeVpaAgeDays: Int = 365,
    val payeeRisk: Double = 0.0,
    val payeeSebiValid: Boolean = false,
    val amount: Double = 500.0,
    val baselineP95: Double = 2000.0,
    val creditClaimRecent: Boolean = false,
    val realCreditFound: Boolean = false,
    val qrRecent: Boolean = false,
    val qrFromGallery: Boolean = false,
    val qrReceiveIntent: Boolean = false,
    val recentFraudReportDays: Int? = null,
    val remark: String = "",
    val newPayees7d: Int = 0,
    val risingAmounts: Boolean = false,
    val smallInboundUnknown72h: Boolean = false,
    val unofficialHelpline: Boolean = false,
)

data class Components(val stage: Double, val anomaly: Double, val payee: Double, val device: Double)

data class Decision(
    val p: Double,
    val tier: String,
    val action: String,
    val cardId: String?,
    val cardName: String?,
    val reason: String,
    val question: String?,
    val evidence: List<String>,
    val floors: List<String>,
    val budgetNote: String?,
    val components: Components,
)

/** Per-user interruption budget. Only T2 questions spend it; T3/T4 are never suppressed. */
class AttentionBudget(private val per30Days: Int = 3) {
    private val used = ArrayDeque<Long>()
    fun remaining(nowMs: Long): Int {
        while (used.isNotEmpty() && nowMs - used.first() >= 30L * 86_400_000L) used.removeFirst()
        return per30Days - used.size
    }
    fun spend(nowMs: Long) { used.addLast(nowMs) }
}

class ThehroEngine(private val cards: List<PlaybookCard> = PlaybookCards.P1) {

    private data class Match(val card: PlaybookCard, val hits: List<String>, val progress: Double,
                             val complete: Boolean, val anchorHit: Boolean)

    private val invest = listOf("ipo", "share", "trading", "stock", "invest", "allot")
    private val fee = listOf("fee", "processing", "release", "refund charge", "recovery")
    private fun has(r: String, w: List<String>) = w.any { r.lowercase().contains(it) }

    fun predicate(name: String, s: PaymentSession): Boolean = when (name) {
        "unknown_caller" -> s.callActive && !s.callerInContacts
        "video_or_voip_call" -> s.callActive && s.callVideoOrVoip
        "long_call" -> s.callActive && s.callMinutes >= 30
        "odd_hour" -> s.hour < 7 || s.hour >= 22
        "screen_share" -> s.screenShare
        "fresh_apk" -> s.apkInstalledHours != null && s.apkInstalledHours < 72
        "overlay_service" -> s.overlayService
        "new_payee" -> s.payeeNew && !s.payeeSaved
        "amount_high" -> s.amount > 3 * s.baselineP95
        "amount_very_high" -> s.amount > 10 * s.baselineP95
        "fake_credit" -> s.creditClaimRecent && !s.realCreditFound
        "qr_recent" -> s.qrRecent
        "qr_from_gallery" -> s.qrFromGallery
        "qr_receive_intent" -> s.qrReceiveIntent
        "recent_fraud_report" -> s.recentFraudReportDays != null && s.recentFraudReportDays <= 90
        "fee_remark" -> has(s.remark, fee)
        "invest_remark" -> has(s.remark, invest)
        "payee_not_sebi_valid" -> has(s.remark, invest) && !s.payeeSebiValid
        "many_new_payees_7d" -> s.newPayees7d >= 3
        "rising_amounts" -> s.risingAmounts
        "small_inbound_credit_unknown" -> s.smallInboundUnknown72h
        "unofficial_helpline" -> s.unofficialHelpline
        else -> false   // unknown stage from a newer card: never fires
    }

    private fun match(s: PaymentSession): List<Match> = cards.map { c ->
        val hits = c.stages.filter { predicate(it, s) }
        var wsum = 0.0; var got = 0.0
        c.stages.forEachIndexed { i, x -> val w = 1 + 0.25 * i; wsum += w; if (x in hits) got += w }
        val anchorHit = c.anchor in hits
        Match(c, hits, got / wsum, hits.size >= c.minMatch && anchorHit, anchorHit)
    }.sortedWith(compareByDescending<Match> { it.complete }
        .thenByDescending { it.anchorHit }
        .thenByDescending { it.card.tierFloor != null }
        .thenByDescending { it.hits.size }
        .thenByDescending { it.progress })

    private fun r3(x: Double) = Math.round(x * 1000) / 1000.0

    private fun components(s: PaymentSession, best: Match?): Components {
        val stage = if (best == null) 0.0 else if (best.complete) best.progress else best.progress * 0.5
        val ratio = max(s.amount, 1.0) / max(s.baselineP95, 1.0)
        val anomaly = min(1.0, max(0.0, log10(ratio) / 1.3)) + if (predicate("odd_hour", s)) 0.15 else 0.0
        val payee = if (s.payeeSaved) 0.0 else
            max(s.payeeRisk, (if (s.payeeNew) 0.35 else 0.0) + (if (s.payeeVpaAgeDays < 30) 0.3 else 0.0))
        val device = maxOf(if (s.screenShare) 1.0 else 0.0,
                           if (predicate("fresh_apk", s)) 0.8 else 0.0,
                           if (s.overlayService) 0.7 else 0.0)
        return Components(r3(stage), r3(min(anomaly, 1.0)), r3(min(payee, 1.0)), r3(device))
    }

    fun decide(s: PaymentSession, budget: AttentionBudget? = null,
               nowMs: Long = System.currentTimeMillis()): Decision {
        val ms = match(s)
        val best = ms.firstOrNull()
        val c = components(s, best)
        val z = W_BIAS + W_STAGE * c.stage + W_ANOMALY * c.anomaly + W_PAYEE * c.payee + W_DEVICE * c.device
        val p = 1 / (1 + exp(-z))
        var tier = THRESHOLDS.firstOrNull { p >= it.first }?.second ?: "T0"
        val floors = mutableListOf<String>()
        best?.card?.tierFloor?.let { f ->
            if (best.complete && idx(f) > idx(tier)) { tier = f; floors += "${best.card.id} complete -> floor $f" }
        }
        if (predicate("fake_credit", s) && idx(tier) < 3) { tier = "T3"; floors += "fake credit, no real credit -> T3" }
        val takeover = s.screenShare || (predicate("fresh_apk", s) && s.overlayService)
        if (takeover && !s.payeeSaved && idx(tier) < 4) { tier = "T4"; floors += "screen control on pay screen -> T4" }
        if (s.payeeSaved && !takeover && idx(tier) > 1) { tier = "T1"; floors += "saved / recurring payee -> never above T1" }
        var budgetNote: String? = null
        if (tier == "T2" && budget != null) {
            if (budget.remaining(nowMs) <= 0) { tier = "T1"; budgetNote = "attention budget used up -> badge instead of question" }
            else budget.spend(nowMs)
        }
        val card = if (best != null && (best.complete || tier != "T0")) best.card else null
        val reason = when (tier) {
            "T0" -> "Looks like a normal payment."
            "T1" -> "New payee. Check the name before you pay."
            else -> card?.reason ?: "Unusual payment pattern."
        }
        return Decision(Math.round(p * 1e4) / 1e4, tier, ACTIONS.getValue(tier), card?.id, card?.name, reason,
            if (card != null && tier == "T2") card.question else null,
            best?.hits ?: emptyList(), floors, budgetNote, c)
    }

    companion object {
        val TIERS = listOf("T0", "T1", "T2", "T3", "T4")
        val THRESHOLDS = listOf(0.85 to "T4", 0.60 to "T3", 0.30 to "T2", 0.10 to "T1")
        val ACTIONS = mapOf(
            "T0" to "silent",
            "T1" to "trust badge on the pay screen",
            "T2" to "one neutral question",
            "T3" to "scam story + 60 s hold, reconfirm after the call ends",
            "T4" to "circuit breaker: hold + guardian alert + 1930 option",
        )
        // v3 hand-set weights, same as thehro/engine.py; refit on finale data
        const val W_BIAS = -4.6
        const val W_STAGE = 4.2
        const val W_ANOMALY = 1.5
        const val W_PAYEE = 1.6
        const val W_DEVICE = 2.4
        private fun idx(t: String) = TIERS.indexOf(t)
    }
}
