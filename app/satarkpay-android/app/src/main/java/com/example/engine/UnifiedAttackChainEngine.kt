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
    val englishVoiceSummary: String,
    val signals: List<AttackChainSignal>,
    val recommendedAction: String,
    val correlationSignature: String,
    val isFirstTimePayee: Boolean,
    val blockPayment: Boolean
)

enum class PaymentSourceChannel(val label: String) {
    DIRECT_SHOP_QR("Physical POS / Shop QR"),
    SAVED_CONTACT("Saved Contact"),
    WHATSAPP_UNSAVED("WhatsApp (Unsaved Number)"),
    TELEGRAM("Telegram Group / Channel"),
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
                    title = "First-Time Payee (New VPA)",
                    description = "This UPI ID ($payeeUpi) was not found in your trusted transaction history. Zero past transfers.",
                    severity = AttackChainRiskTier.CAUTION,
                    provenance = "PAYEE LEDGER"
                )
            )
        } else {
            baseScore -= 15
            signals.add(
                AttackChainSignal(
                    title = "Known & Verified Payee",
                    description = "$knownPayeeTxnCount previous successful payments recorded. Trust history verified on-device.",
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
                    title = "Ongoing Voice / Video Call",
                    description = "You are currently on a call while initiating this transfer. 87% of Digital Arrest frauds involve live call pressure.",
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
                    description = "Screen-sharing, remote access tool, or high-risk accessibility permissions detected on device.",
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
                        description = "Payment request originated from Telegram. SEBI & MHA advise that prepaid task and crypto channels carry extreme fraud risk.",
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
                        description = "Chat originated from an unsaved number. Government and banks never send payment links over personal WhatsApp.",
                        severity = AttackChainRiskTier.CAUTION,
                        provenance = "COMMUNICATION CONTEXT"
                    )
                )
            }
            PaymentSourceChannel.UNKNOWN_SMS -> {
                baseScore += 25
                signals.add(
                    AttackChainSignal(
                        title = "Unverified SMS Alert",
                        description = "Direct payment request via SMS without standard bank verification headers.",
                        severity = AttackChainRiskTier.CAUTION,
                        provenance = "COMMUNICATION CONTEXT"
                    )
                )
            }
            PaymentSourceChannel.DIRECT_SHOP_QR, PaymentSourceChannel.SAVED_CONTACT -> {
                baseScore -= 10
                signals.add(
                    AttackChainSignal(
                        title = "Physical POS / Saved Contact",
                        description = "Direct physical merchant QR or saved contact verification.",
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
                    title = "Coercion / Urgency Keywords Detected",
                    description = "Detected trigger words: ${matchedUrgencyWords.joinToString(", ")}. Fake authority pressure detected.",
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
                    description = "₹$amount transfer to an unverified first-time beneficiary exceeds safety threshold.",
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
                    headline = "🚨 Digital Arrest Trap: Active Call + Unknown Payee",
                    explanation = "An unknown caller is coercing you to transfer money to an unfamiliar UPI account ($payeeUpi) during a live call. Disconnect the call immediately and do not transfer funds!",
                    hindiVoiceSummary = "चेतावनी! लाइव कॉल पर किसी अनजान खाते में पैसे ना भेजें। तुरंत कॉल काटें।",
                    englishVoiceSummary = "Warning! Do not transfer funds to an unknown account during an active call. Hang up immediately.",
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
                    headline = "⚠️ Unverified Online Source + Unknown Beneficiary",
                    explanation = "This payment matches prepaid task or utility disconnection scam patterns. Take a 15-minute cooling period before proceeding.",
                    hindiVoiceSummary = "सावधान! ऑनलाइन चैट से मिले इस खाते पर फ्रॉड का खतरा है। 15 मिनट रुकें।",
                    englishVoiceSummary = "Caution! High fraud risk detected from unverified chat link. A 15-minute cooling delay is recommended.",
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
                    explanation = "A remote access tool or high-risk accessibility app is active while attempting an unverified transfer. A fraudster may be viewing your screen or OTP.",
                    hindiVoiceSummary = "खतरा! आपके फोन में स्क्रीन शेयर या रिमोट ऐप चालू है। पेमेंट तुरंत रोकें।",
                    englishVoiceSummary = "Danger! Remote screen sharing app detected. Stop payment immediately to prevent fund theft.",
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
                    headline = "🟡 First-Time Beneficiary Verification Required",
                    explanation = "You have never paid this UPI ID ($payeeUpi) before. Please double-check the recipient name and account details.",
                    hindiVoiceSummary = "आप इस खाते में पहली बार पैसे भेज रहे हैं। नाम और नंबर की जांच अवश्य करें।",
                    englishVoiceSummary = "First-time payee. Please verify the account holder name before proceeding.",
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
                    explanation = "Payee ($payeeUpi) exists in your trusted local ledger ($knownPayeeTxnCount past payments). Device and communication channels are secure.",
                    hindiVoiceSummary = "यह एक पुराना और सुरक्षित खाता है। आप सुरक्षित भुगतान कर सकते हैं।",
                    englishVoiceSummary = "Trusted beneficiary confirmed. It is safe to proceed with this payment.",
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
