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
    val callActive: Boolean? = null,
    val callerInContacts: Boolean? = null,
    val callVideoOrVoip: Boolean? = null,
    val callMinutes: Double? = null,
    val hour: Int? = null,
    val activeScreenControl: Boolean? = null,
    val activeScreenControlConfidence: Double? = null,
    val screenShare: Boolean? = null, // legacy input alias; never infer from installed apps
    val highRiskAppInstalled: Boolean? = null,
    val accessibilityServiceEnabled: Boolean? = null,
    val apkInstalledHours: Double? = null,
    val overlayService: Boolean? = null,
    val payeeSaved: Boolean = false,
    val payeeNew: Boolean? = null,
    val payeeVpaAgeDays: Int? = null,
    val payeeRisk: Double? = null,
    val payeeSebiValid: Boolean? = null,
    val amount: Double = 500.0,
    val baselineP95: Double? = null,
    val creditClaimRecent: Boolean? = null,
    val realCreditFound: Boolean? = null,
    val qrRecent: Boolean? = null,
    val qrFromGallery: Boolean? = null,
    val qrReceiveIntent: Boolean? = null,
    val recentFraudReportDays: Int? = null,
    val remark: String = "",
    val newPayees7d: Int? = null,
    val risingAmounts: Boolean? = null,
    val smallInboundUnknown72h: Boolean? = null,
    val unofficialHelpline: Boolean? = null,
    val newPayeeSum72h: Double? = null,    // anti-splitting history is unknown until sourced
    val heldRecentlyHours: Double? = null, // hours since last T3/T4 hold
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
    val unknownSignals: List<String>,
)

fun Decision.permitsPayment(questionConfirmed: Boolean = false): Boolean = when (tier) {
    "T0", "T1" -> true
    "T2" -> questionConfirmed
    else -> false
}

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
    private fun screenControl(s: PaymentSession): Boolean? = s.activeScreenControl ?: s.screenShare
    private fun eff(s: PaymentSession) = s.amount +
        if (s.payeeNew == true && !s.payeeSaved) (s.newPayeeSum72h ?: 0.0) else 0.0

    private fun unknownSignals(s: PaymentSession): List<String> = buildList {
        if (s.callActive == null) add("call_active")
        if (s.callerInContacts == null) add("caller_in_contacts")
        if (s.callVideoOrVoip == null) add("call_video_or_voip")
        if (s.callMinutes == null) add("call_minutes")
        if (s.hour == null) add("hour")
        if (s.activeScreenControl == null && s.screenShare == null) add("active_screen_control")
        if (s.activeScreenControlConfidence == null) add("active_screen_control_confidence")
        if (s.highRiskAppInstalled == null) add("high_risk_app_installed")
        if (s.accessibilityServiceEnabled == null) add("accessibility_service_enabled")
        if (s.apkInstalledHours == null) add("apk_installed_hours")
        if (s.overlayService == null) add("overlay_service")
        if (s.payeeNew == null) add("payee_new")
        if (s.payeeVpaAgeDays == null) add("payee_vpa_age_days")
        if (s.payeeRisk == null) add("payee_risk")
        if (s.payeeSebiValid == null) add("payee_sebi_valid")
        if (s.baselineP95 == null) add("baseline_p95")
        if (s.creditClaimRecent == null) add("credit_claim_recent")
        if (s.realCreditFound == null) add("real_credit_found")
        if (s.qrRecent == null) add("qr_recent")
        if (s.qrFromGallery == null) add("qr_from_gallery")
        if (s.qrReceiveIntent == null) add("qr_receive_intent")
        if (s.recentFraudReportDays == null) add("recent_fraud_report_days")
        if (s.newPayees7d == null) add("new_payees_7d")
        if (s.risingAmounts == null) add("rising_amounts")
        if (s.smallInboundUnknown72h == null) add("small_inbound_unknown_72h")
        if (s.unofficialHelpline == null) add("unofficial_helpline")
        if (s.newPayeeSum72h == null) add("new_payee_sum_72h")
        if (s.heldRecentlyHours == null) add("held_recently_hours")
    }

    fun predicate(name: String, s: PaymentSession): Boolean = when (name) {
        "unknown_caller" -> s.callActive == true && s.callerInContacts == false
        "video_or_voip_call" -> s.callActive == true && s.callVideoOrVoip == true
        "long_call" -> s.callActive == true && s.callMinutes != null && s.callMinutes >= 30
        "odd_hour" -> s.hour != null && (s.hour < 7 || s.hour >= 22)
        "screen_share" -> screenControl(s) == true
        "fresh_apk" -> s.apkInstalledHours != null && s.apkInstalledHours < 72
        "overlay_service" -> s.overlayService == true
        "new_payee" -> s.payeeNew == true && !s.payeeSaved
        "amount_high" -> s.baselineP95 != null && s.baselineP95 > 0 && eff(s) > 3 * s.baselineP95
        "amount_very_high" -> s.baselineP95 != null && s.baselineP95 > 0 && eff(s) > 10 * s.baselineP95
        "fake_credit" -> s.creditClaimRecent == true && s.realCreditFound == false
        "qr_recent" -> s.qrRecent == true
        "qr_from_gallery" -> s.qrFromGallery == true
        "qr_receive_intent" -> s.qrReceiveIntent == true
        "recent_fraud_report" -> s.recentFraudReportDays != null && s.recentFraudReportDays <= 90
        "fee_remark" -> has(s.remark, fee)
        "invest_remark" -> has(s.remark, invest)
        "payee_not_sebi_valid" -> has(s.remark, invest) && s.payeeSebiValid == false
        "many_new_payees_7d" -> s.newPayees7d != null && s.newPayees7d >= 3
        "rising_amounts" -> s.risingAmounts == true
        "small_inbound_credit_unknown" -> s.smallInboundUnknown72h == true
        "unofficial_helpline" -> s.unofficialHelpline == true
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
        val baseline = s.baselineP95
        val amountAnomaly = if (baseline == null || baseline <= 0) 0.0 else {
            val ratio = max(eff(s), 1.0) / max(baseline, 1.0)
            min(1.0, max(0.0, log10(ratio) / 1.3))
        }
        val anomaly = amountAnomaly + if (predicate("odd_hour", s)) 0.15 else 0.0
        val payee = if (s.payeeSaved) 0.0 else
            max(s.payeeRisk ?: 0.0, (if (s.payeeNew == true) 0.35 else 0.0) +
                (if (s.payeeVpaAgeDays != null && s.payeeVpaAgeDays < 30) 0.3 else 0.0))
        val device = maxOf(if (screenControl(s) == true) 1.0 else 0.0,
                           if (predicate("fresh_apk", s)) 0.8 else 0.0,
                           if (s.overlayService == true) 0.7 else 0.0,
                           if (s.accessibilityServiceEnabled == true) 0.45 else 0.0,
                           if (s.highRiskAppInstalled == true) 0.35 else 0.0)
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
        var hardFloor = best?.let { it.complete && it.card.tierFloor != null } ?: false
        best?.card?.tierFloor?.let { f ->
            if (best.complete && idx(f) > idx(tier)) { tier = f; floors += "${best.card.id} complete -> floor $f" }
        }
        if (predicate("fake_credit", s) && idx(tier) < 3) { tier = "T3"; floors += "fake credit, no real credit -> T3" }
        if (predicate("fake_credit", s)) hardFloor = true
        val takeover = screenControl(s) == true || (predicate("fresh_apk", s) && s.overlayService == true)
        if (takeover && idx(tier) < 4) {
            tier = "T4"; floors += "active screen control or fresh APK plus overlay -> T4"
        }
        if (takeover) hardFloor = true
        val retry = s.heldRecentlyHours != null && s.heldRecentlyHours < 24 && s.payeeNew == true && !s.payeeSaved
        if (retry && idx(tier) < 3) { tier = "T3"; floors += "retry to a new payee within 24 h of a hold -> T3" }
        if (retry) hardFloor = true
        if (s.payeeSaved && !hardFloor && !takeover && idx(tier) > 1) {
            tier = "T1"; floors += "saved / recurring payee -> never above T1"
        }
        var budgetNote: String? = null
        if (tier == "T2" && budget != null) {
            if (budget.remaining(nowMs) <= 0) { tier = "T1"; budgetNote = "attention budget used up -> badge instead of question" }
            else budget.spend(nowMs)
        }
        val card = if (best != null && (best.complete || (tier != "T0" && best.anchorHit))) best.card else null
        val reason = when (tier) {
            "T0" -> "No high-risk pattern is confirmed from the available signals."
            "T1" -> "Caution: check the recipient name independently before paying."
            else -> card?.reason ?: if (retry) "You were warned about a similar payment today. Scammers often call back and ask again." else "Unusual payment pattern."
        }
        return Decision(Math.round(p * 1e4) / 1e4, tier, ACTIONS.getValue(tier), card?.id, card?.name, reason,
            if (card != null && tier == "T2") card.question else null,
            best?.hits ?: emptyList(), floors, budgetNote, c, unknownSignals(s))
    }

    companion object {
        val TIERS = listOf("T0", "T1", "T2", "T3", "T4")
        val THRESHOLDS = listOf(0.85 to "T4", 0.60 to "T3", 0.30 to "T2", 0.10 to "T1")
        val ACTIONS = mapOf(
            "T0" to "silent",
            "T1" to "caution badge; recipient is not verified",
            "T2" to "one neutral question",
            "T3" to "scam story + 60 s hold, reconfirm after the call ends",
            "T4" to "payment hold; offer trusted-contact and 1930 options",
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
