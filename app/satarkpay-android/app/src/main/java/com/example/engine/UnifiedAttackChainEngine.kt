package com.example.engine

enum class AttackChainRiskTier(val label: String) {
    LOW_SAFE("SAFE TO PAY"),
    CAUTION("VERIFY PAYEE"),
    HIGH_RISK("HIGH RISK DETECTED"),
    CRITICAL_BLOCKED("ATTACK CHAIN BLOCKED")
}

data class AttackChainSignal(
    val title: String,
    val description: String,
    val severity: AttackChainRiskTier,
    val provenance: String // "LIVE ON DEVICE", "PAYEE LEDGER", "COMMUNICATION CONTEXT", "POLICY ENGINE"
)

data class AttackChainEvaluation(
    val riskTier: AttackChainRiskTier,
    val score: Int, // 0 - 100
    val headline: String,
    val explanation: String,
    val hindiVoiceSummary: String,
    val signals: List<AttackChainSignal>,
    val recommendedAction: String,
    val correlationSignature: String,
    val isFirstTimePayee: Boolean,
    val blockPayment: Boolean
)

enum class PaymentSourceChannel(val label: String) {
    DIRECT_SHOP_QR("Direct Shop QR / POS"),
    SAVED_CONTACT("Saved Phonebook Contact"),
    WHATSAPP_UNSAVED("WhatsApp (Unsaved Number)"),
    TELEGRAM("Telegram Group / Bot"),
    UNKNOWN_SMS("Unknown SMS Alert")
}

object UnifiedAttackChainEngine {

    private val URGENCY_KEYWORDS = listOf(
        "cbi", "police", "arrest", "customs", "narcotics", "court", "warrant",
        "power cut", "electricity", "bill unpaid", "disconnect", "light kat",
        "task", "youtube", "telegram", "vip group", "prepaid", "crypto", "trading",
        "lottery", "prize", "refund", "kyc expire", "sim block", "trai"
    )

    fun evaluate(
        payeeUpi: String,
        payeeName: String,
        amount: Long,
        isFirstTimePayee: Boolean,
        knownPayeeTxnCount: Int,
        isTrustVerified: Boolean,
        sourceChannel: PaymentSourceChannel,
        isActiveCall: Boolean,
        chatTextSnippet: String = "",
        hasRatOrAccessibilityTool: Boolean = false,
        highRiskAppsCount: Int = 0
    ): AttackChainEvaluation {
        val signals = mutableListOf<AttackChainSignal>()
        var baseScore = 5
        val lowerSnippet = chatTextSnippet.lowercase()
        val matchedUrgencyWords = URGENCY_KEYWORDS.filter { lowerSnippet.contains(it) }

        // 1. Payee Ledger Signal
        if (isFirstTimePayee) {
            baseScore += 35
            signals.add(
                AttackChainSignal(
                    title = "First-Time Payee VPA",
                    description = "Ye UPI ID ($payeeUpi) aapke local ledger me pehli baar dekhi gayi hai. Past zero transactions recorded.",
                    severity = AttackChainRiskTier.CAUTION,
                    provenance = "PAYEE LEDGER"
                )
            )
        } else {
            baseScore -= 15
            signals.add(
                AttackChainSignal(
                    title = "Known Historical Payee",
                    description = "$knownPayeeTxnCount previous successful payments recorded. Trust history established.",
                    severity = AttackChainRiskTier.LOW_SAFE,
                    provenance = "PAYEE LEDGER"
                )
            )
        }

        // 2. Active Call Context (Crucial Digital Arrest Signal)
        if (isActiveCall) {
            baseScore += 45
            signals.add(
                AttackChainSignal(
                    title = "Active Phone / Video Call In Progress",
                    description = "Aap call par hain aur simultaneously payment ho rahi hai. 87% Digital Arrest fraud live call par psychological pressure banakar karwaye jaate hain.",
                    severity = AttackChainRiskTier.CRITICAL_BLOCKED,
                    provenance = "LIVE ON DEVICE"
                )
            )
        }

        // 3. Device Risk / RAT Signal
        if (hasRatOrAccessibilityTool || highRiskAppsCount > 0) {
            baseScore += 35
            signals.add(
                AttackChainSignal(
                    title = "Device Security Risk / Remote Tool Present",
                    description = "Phone me high-risk permissions ya screen-sharing / accessibility tools detected hain.",
                    severity = AttackChainRiskTier.HIGH_RISK,
                    provenance = "LIVE ON DEVICE"
                )
            )
        }

        // 4. Communication Channel Signal
        when (sourceChannel) {
            PaymentSourceChannel.TELEGRAM -> {
                baseScore += 30
                signals.add(
                    AttackChainSignal(
                        title = "Telegram Origin Payment",
                        description = "Payment link ya QR Telegram group/bot se mila hai. SEBI & MHA advisory: Telegram based investment & task groups have 94% fraud incidence.",
                        severity = AttackChainRiskTier.HIGH_RISK,
                        provenance = "COMMUNICATION CONTEXT"
                    )
                )
            }
            PaymentSourceChannel.WHATSAPP_UNSAVED -> {
                baseScore += 20
                signals.add(
                    AttackChainSignal(
                        title = "Unsaved WhatsApp Sender",
                        description = "Chat unverified aur unsaved contact se aayi hai. Official entities personal WhatsApp par payment link nahi bhejte.",
                        severity = AttackChainRiskTier.CAUTION,
                        provenance = "COMMUNICATION CONTEXT"
                    )
                )
            }
            PaymentSourceChannel.UNKNOWN_SMS -> {
                baseScore += 25
                signals.add(
                    AttackChainSignal(
                        title = "Unknown SMS Alert",
                        description = "SMS me shortener ya unverified bank warning ke bagair direct payment ki maang ki gayi hai.",
                        severity = AttackChainRiskTier.CAUTION,
                        provenance = "COMMUNICATION CONTEXT"
                    )
                )
            }
            PaymentSourceChannel.DIRECT_SHOP_QR, PaymentSourceChannel.SAVED_CONTACT -> {
                baseScore -= 10
                signals.add(
                    AttackChainSignal(
                        title = "Physical POS / Trusted Source",
                        description = "Direct physical merchant ya saved contact verification.",
                        severity = AttackChainRiskTier.LOW_SAFE,
                        provenance = "COMMUNICATION CONTEXT"
                    )
                )
            }
        }

        // 5. Urgency / Threat Keywords
        if (matchedUrgencyWords.isNotEmpty()) {
            baseScore += 25
            signals.add(
                AttackChainSignal(
                    title = "Scam Threat Keywords Detected",
                    description = "Keywords found: ${matchedUrgencyWords.joinToString(", ")}. Fake authority coercion detected.",
                    severity = AttackChainRiskTier.HIGH_RISK,
                    provenance = "POLICY ENGINE"
                )
            )
        }

        // 6. High Amount to First-Time Payee
        if (isFirstTimePayee && amount >= 5000L) {
            baseScore += 15
            signals.add(
                AttackChainSignal(
                    title = "High Value Transfer to Unknown VPA",
                    description = "₹$amount transfer to an unverified first-time beneficiary exceeds standard low-risk threshold.",
                    severity = AttackChainRiskTier.CAUTION,
                    provenance = "POLICY ENGINE"
                )
            )
        }

        val clampedScore = baseScore.coerceIn(0, 100)

        // Correlate into explainable verdicts
        return when {
            // Lethal Combination: Active Call + First Time Payee
            isActiveCall && isFirstTimePayee -> {
                AttackChainEvaluation(
                    riskTier = AttackChainRiskTier.CRITICAL_BLOCKED,
                    score = clampedScore.coerceAtLeast(92),
                    headline = "🚨 Active Call + First-Time Payee (Digital Arrest Trap)",
                    explanation = "Scammer aapko live call par daara kar ya jaldbazi me anjaan UPI ID ($payeeUpi) par paise transfer karwa raha hai. Phone turant kaatein!",
                    hindiVoiceSummary = "चेतावनी! लाइव कॉल पर किसी अनजान खाते में पैसे ना भेजें। यह डिजिटल अरेस्ट या पुलिस फ्रॉड हो सकता है। तुरंत कॉल काटें।",
                    signals = signals,
                    recommendedAction = "IMMEDIATE_CANCEL_1930",
                    correlationSignature = "Active Call + First-Time Payee + High Coercion",
                    isFirstTimePayee = true,
                    blockPayment = true
                )
            }

            // High Risk: Telegram Task or Urgent SMS with first-time payee
            (sourceChannel == PaymentSourceChannel.TELEGRAM || matchedUrgencyWords.isNotEmpty()) && isFirstTimePayee -> {
                AttackChainEvaluation(
                    riskTier = AttackChainRiskTier.HIGH_RISK,
                    score = clampedScore.coerceAtLeast(78),
                    headline = "⚠️ Unverified Online Source + Unknown Payee",
                    explanation = "Telegram task, impersonation ya urgency coercion match hua hai. Is payee ($payeeUpi) ko payment karne se pehle 15 minute ka cooling zaroori hai.",
                    hindiVoiceSummary = "सावधान! टेलीग्राम या अनजान चैट से मिले इस यूपीआई पर फ्रॉड का भारी खतरा है। 15 मिनट रुकें।",
                    signals = signals,
                    recommendedAction = "COOLING_15MIN",
                    correlationSignature = "Unsaved Source + Urgency Pressure + First-Time Payee",
                    isFirstTimePayee = true,
                    blockPayment = false
                )
            }

            // Critical Device RAT Risk
            hasRatOrAccessibilityTool && isFirstTimePayee -> {
                AttackChainEvaluation(
                    riskTier = AttackChainRiskTier.CRITICAL_BLOCKED,
                    score = clampedScore.coerceAtLeast(90),
                    headline = "🚨 Screen-Sharing / Remote Access Threat",
                    explanation = "Aapke device par remote control ya accessibility app active hai aur unknown payee ko payment ki ja rahi hai. Scammer OTP ya screen dekh sakta hai.",
                    hindiVoiceSummary = "खतरा! आपके फोन में स्क्रीन शेयर या रिमोट ऐप चालू है। पेमेंट तुरंत रोकें।",
                    signals = signals,
                    recommendedAction = "AUDIT_DEVICE_APPS",
                    correlationSignature = "Remote RAT App + First-Time Payee",
                    isFirstTimePayee = true,
                    blockPayment = true
                )
            }

            // Moderate Caution: First time payee but no call or threat
            isFirstTimePayee -> {
                AttackChainEvaluation(
                    riskTier = AttackChainRiskTier.CAUTION,
                    score = clampedScore.coerceIn(35, 60),
                    headline = "🟡 First-Time Payee Verification Required",
                    explanation = "Aap is UPI ID ($payeeUpi) par pehli baar paise bhej rahe hain. Kripya receiver ka naam aur details dhyan se check karein.",
                    hindiVoiceSummary = "आप इस खाते में पहली बार पैसे भेज रहे हैं। नाम और नंबर की जांच अवश्य करें।",
                    signals = signals,
                    recommendedAction = "VERIFY_2MIN",
                    correlationSignature = "First-Time Payee Ledger Warning",
                    isFirstTimePayee = true,
                    blockPayment = false
                )
            }

            // Low Risk: Known payee, normal conditions
            else -> {
                AttackChainEvaluation(
                    riskTier = AttackChainRiskTier.LOW_SAFE,
                    score = clampedScore.coerceAtMost(25),
                    headline = "✅ Safe & Verified Beneficiary",
                    explanation = "Payee ($payeeUpi) aapke local ledger me maujood hai ($knownPayeeTxnCount past payments). Device aur context clean hain.",
                    hindiVoiceSummary = "यह एक पुराना और सुरक्षित खाता है। आप सुरक्षित भुगतान कर सकते हैं।",
                    signals = signals,
                    recommendedAction = "PAY_SAFELY",
                    correlationSignature = "Trusted Ledger Record + Clean Device",
                    isFirstTimePayee = false,
                    blockPayment = false
                )
            }
        }
    }
}
