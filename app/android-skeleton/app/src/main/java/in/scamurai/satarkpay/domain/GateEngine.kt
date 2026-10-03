package `in`.scamurai.satarkpay.domain

/**
 * M1 · Chat-Before-Pay matrix (R15/R16/R23).
 * Principle: BLOCK kuch nahi hota — sirf friction tier decide hota hai.
 * Demo parity: web/_part_app.js → m1Run() / m1.tier
 */
enum class Tier { T1_FAST, T1_NOTIFY, T2_COOLING, T3_HOLD, NUDGE_ONLY }

data class GateInput(
    val contactSaved: Boolean,
    val chatDwellMillis: Long,          // sirf foreground dwell (chat screen)
    val secondsSinceLeavingChat: Long,
    val officerOrOfferMentioned: Boolean = false,
    val fastPathUsedInLast24hForPayee: Boolean = false,
)

object GateEngine {
    const val LONG_CHAT_MS = 8 * 60_000L            // 8 min → cooling boundary
    const val LONG_CHAT_NOTIFY_MS = 10 * 60_000L    // 10 min → notification (deck requirement)
    const val GAP_LIMIT_S = 5 * 60L                 // chat chhod ke 5 min ke andar UPI khula

    fun tier(i: GateInput): Tier = when {
        i.officerOrOfferMentioned -> Tier.T3_HOLD                   // rule R1 hook
        i.secondsSinceLeavingChat > GAP_LIMIT_S -> Tier.NUDGE_ONLY  // purani baat ka credit nahi
        i.contactSaved && i.chatDwellMillis < LONG_CHAT_MS ->
            if (i.fastPathUsedInLast24hForPayee) Tier.T1_NOTIFY else Tier.T1_FAST
        i.contactSaved && i.chatDwellMillis >= LONG_CHAT_NOTIFY_MS -> Tier.T1_NOTIFY
        i.contactSaved -> Tier.T1_NOTIFY
        i.chatDwellMillis < LONG_CHAT_MS -> Tier.T2_COOLING         // unknown + chhoti chat
        else -> Tier.T3_HOLD                                        // unknown + lambi chat
    }

    /** Anti-cheat: 5 s me wapas UPI par aaya to us duration ka credit nahi. */
    fun creditDwell(rawDwellMs: Long, leftAndReturnedWithinMs: Long?): Long =
        if (leftAndReturnedWithinMs != null && leftAndReturnedWithinMs <= 5_000L) rawDwellMs - leftAndReturnedWithinMs
        else rawDwellMs
}
