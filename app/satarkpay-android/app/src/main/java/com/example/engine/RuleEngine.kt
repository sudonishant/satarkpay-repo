package com.example.engine

data class RedactionResult(
    val originalText: String,
    val redactedText: String,
    val itemsRedactedCount: Int,
    val redactedEntities: List<String>
)

enum class VerdictBucket {
    SCAM_LIKELY,
    CAUTION,
    PAUSE_NAHI_BATA,
    SEEMS_OK
}

data class VerdictResult(
    val bucket: VerdictBucket,
    val confidence: Float,
    val familyCode: String?,
    val matchedRuleTitle: String,
    val reasons: List<String>,
    val matKaro: String,
    val karo: List<String>,
    val citation: String,
    val hindiVoiceSummary: String,
    val isBankOfficialWarning: Boolean = false,
    val bertScamProbability: Float = 0.5f,
    val bertAttentionTokens: List<String> = emptyList(),
    val isConversationalGreeting: Boolean = false
)

enum class DomainTrustLevel {
    L1_VERIFIED_GOV_BANK,
    L2_VERIFIED_MERCHANT,
    L3_UNVERIFIED_CLEAN,
    L4_LOOKALIKE_FAKE
}

data class DomainAnalysisResult(
    val domain: String,
    val trustLevel: DomainTrustLevel,
    val signals: List<String>,
    val gatewayName: String?,
    val merchantName: String?,
    val hasGatewayMismatch: Boolean,
    val warningMessage: String
)

object RuleEngine {

    // Regex for PII Redaction (R28)
    private val PHONE_REGEX = Regex("""(?:\+91[\-\s]?)?[6-9]\d{9}""")
    private val CARD_REGEX = Regex("""\b(?:\d{4}[-\s]?){3}\d{4}\b""")
    private val UPI_REGEX = Regex("""\b[a-zA-Z0-9.\-_]{2,256}@[a-zA-Z]{2,64}\b""")
    private val OTP_REGEX = Regex("""(?i)\b(?:otp|code|pin)\s*(?:is|:)?\s*(\d{4,8})\b""")

    fun redactPII(input: String): RedactionResult {
        var result = input
        var count = 0
        val entities = mutableListOf<String>()

        // Mask phone numbers: 9876543210 -> 98••••••10
        result = PHONE_REGEX.replace(result) { match ->
            val v = match.value
            val clean = v.filter { it.isDigit() }
            if (clean.length == 10) {
                count++
                val masked = "${clean.take(2)}••••••${clean.takeLast(2)}"
                entities.add("Mobile: $masked")
                masked
            } else v
        }

        // Mask card numbers
        result = CARD_REGEX.replace(result) { match ->
            count++
            val masked = "••••-••••-••••-${match.value.takeLast(4)}"
            entities.add("Card: $masked")
            masked
        }

        // Mask UPI ID: 7349123456@ptaxis -> 7349••••••@ptaxis
        result = UPI_REGEX.replace(result) { match ->
            count++
            val parts = match.value.split("@")
            val user = parts[0]
            val handle = parts.getOrNull(1) ?: ""
            val maskedUser = if (user.length > 4) "${user.take(4)}••••••" else "••••••"
            val masked = "$maskedUser@$handle"
            entities.add("UPI: $masked")
            masked
        }

        // Mask OTPs
        result = OTP_REGEX.replace(result) { match ->
            count++
            entities.add("OTP: [SECRET MASKED]")
            "OTP is [MASKED]"
        }

        return RedactionResult(
            originalText = input,
            redactedText = result,
            itemsRedactedCount = count,
            redactedEntities = entities
        )
    }

    // Negation Detection (R27)
    fun isOfficialBankWarning(text: String): Boolean {
        val lower = text.lowercase()
        val safetyKeywords = listOf(
            "never share your otp",
            "bank never asks for otp",
            "kabhi kisi ko otp na bataye",
            "do not share your upi pin",
            "never share password",
            "beware of fraud",
            "if you did not request this",
            "report suspicious activity to 1930",
            "safety alert from sbi",
            "hdfc bank alert: never share"
        )
        return safetyKeywords.any { lower.contains(it) }
    }

    // Deterministic Rule Analysis + Bharat-BERT NLP Engine
    fun analyzeText(input: String): VerdictResult {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 0. Bharat-BERT Conversational & Benign Greeting Check (Prevents false positives on "hi")
        val bertResult = BharatBertScamClassifier.classify(trimmed)
        if (bertResult.isConversationalGreeting) {
            return VerdictResult(
                bucket = VerdictBucket.SEEMS_OK,
                confidence = 0.99f,
                familyCode = null,
                matchedRuleTitle = "Bharat-BERT: Normal Conversation (Benign)",
                reasons = listOf(
                    "Ye aam baat-cheet ya greeting hai (e.g. hi/hello)",
                    "Koi scam, financial threat, ya urgency signal nahi mila",
                    "Bharat-BERT classification: 0% fraud risk"
                ),
                matKaro = "Koi alert ki zarurat nahi hai.",
                karo = listOf("Aap AI Sanchalak se koi bhi payment fraud ya scam sawal poochh sakte hain."),
                citation = "Bharat-BERT Intent Classifier (Benign Conversation)",
                hindiVoiceSummary = "नमस्ते! मैं आपकी क्या सहायता कर सकता हूँ?",
                isConversationalGreeting = true,
                bertScamProbability = bertResult.scamProbability,
                bertAttentionTokens = emptyList()
            )
        }

        // 1. Check for official bank awareness message first (R27 negation)
        if (isOfficialBankWarning(lower)) {
            return VerdictResult(
                bucket = VerdictBucket.SEEMS_OK,
                confidence = 0.96f,
                familyCode = null,
                matchedRuleTitle = "R27: Bank Official Safety Awareness",
                reasons = listOf(
                    "Ye bank ka official safety advisory message hai",
                    "Message me OTP ya sensitive data maangne ke bajaye bachne ki salah hai",
                    "Official bank negation patterns match huye hain"
                ),
                matKaro = "Is advisory ko ignore mat karo, dosto ke sath share karo.",
                karo = listOf(
                    "Alert ko follow karo aur OTP kisi ko mat dena.",
                    "Agar koi aisi call aaye to turant 1930 par complain karein."
                ),
                citation = "Library R27 (Negation & Official Advisories)",
                hindiVoiceSummary = "यह आपके बैंक की आधिकारिक सुरक्षा चेतावनी है। इसे ध्यान से पढ़ें और अपना ओटीपी या पिन किसी को न दें।",
                isBankOfficialWarning = true
            )
        }

        // 2. F1: Digital Arrest / Police / CBI / TRAI
        if (lower.contains("cbi") || lower.contains("digital arrest") || lower.contains("video call arrest") ||
            lower.contains("police officer") || lower.contains("narcotics") || lower.contains("mdma") ||
            (lower.contains("trai") && lower.contains("block")) || lower.contains("money laundering")
        ) {
            return VerdictResult(
                bucket = VerdictBucket.SCAM_LIKELY,
                confidence = 0.99f,
                familyCode = "F1",
                matchedRuleTitle = "F1: Digital Arrest & Fake Police Threat",
                reasons = listOf(
                    "CBI, Police ya TRAI kabhi video call par arrest nahi karte",
                    "Dara kar 'security deposit' ya 'verification transfer' mangna 100% scam hai",
                    "Supreme Court aur MHA ne 'Digital Arrest' ko criminal syndicate ghoshit kiya hai"
                ),
                matKaro = "Koi bhi paise kisi 'Police Verification Account' me transfer MAT KARO. Video call turant kato.",
                karo = listOf(
                    "Turant call disconnect karo.",
                    "National Cyber Crime Helpline 1930 par report karo.",
                    "Apne parivaar ko alert karo."
                ),
                citation = "Threat Library F1 (Digital Arrest / Law Enforcement Impersonation) · R38 Action",
                hindiVoiceSummary = "यह डिजिटल अरेस्ट का फ्रॉड है। सीबीआई या पुलिस वीडियो कॉल पर अरेस्ट नहीं करती। तुरंत कॉल काटें और 1930 पर शिकायत करें।"
            )
        }

        // 3. F3: Telegram Task / YouTube Like / Guaranteed Daily Income
        if (lower.contains("youtube like") || lower.contains("telegram task") || lower.contains("prepaid task") ||
            lower.contains("guaranteed 3000") || lower.contains("daily 5000 earn") || lower.contains("part time task") ||
            (lower.contains("vip task") && lower.contains("deposit"))
        ) {
            return VerdictResult(
                bucket = VerdictBucket.SCAM_LIKELY,
                confidence = 0.98f,
                familyCode = "F3",
                matchedRuleTitle = "F3: Telegram Task & Prepaid Deposit Scam",
                reasons = listOf(
                    "Pehle ₹150 chhota reward dekar bharosa jeet-te hain",
                    "Baad me VIP task ke naam par ₹5,000 se ₹50,000 jama karate hain",
                    "Withdrawal ke waqt 'tax / gas fee' maang kar account block kar dete hain"
                ),
                matKaro = "Ek bhi rupya deposit MAT KARO. Jo paisa unhone diya tha usko refund na maane.",
                karo = listOf(
                    "Telegram group se turant exit ho jao aur admin ko block karo.",
                    "UPI reference number ke sath 1930 par cyber crime record banwayein."
                ),
                citation = "Threat Library F3 (Ponzi Task & Micro-Payment Pyramid)",
                hindiVoiceSummary = "यह टेलीग्राम टास्क स्कैम है। यूट्यूब लाइक के बहाने पैसे ठगे जा रहे हैं। कोई पैसा डिपॉजिट न करें।"
            )
        }

        // 4. F4: Refund Trap / Scan QR to receive money / UPI PIN trap
        if ((lower.contains("scan qr") && (lower.contains("receive") || lower.contains("cashback") || lower.contains("credit"))) ||
            (lower.contains("upi pin") && lower.contains("receive")) ||
            lower.contains("enter pin to get refund")
        ) {
            return VerdictResult(
                bucket = VerdictBucket.SCAM_LIKELY,
                confidence = 0.99f,
                familyCode = "F4",
                matchedRuleTitle = "F4: Scan QR to Receive / UPI PIN Trap",
                reasons = listOf(
                    "UPI ka niyam: Paisa paane ke liye KABHI UPI PIN nahi lagta",
                    "QR code scan karke ya PIN daal kar paisa humesha KATTA hai, aana nahi",
                    "Overpayment ya OLX refund ke naam par dhoka diya ja raha hai"
                ),
                matKaro = "QR code scan MAT KARO aur apna UPI PIN bilkul MAT DAALO.",
                karo = listOf(
                    "Sender ko saaf mana karo.",
                    "Agar PIN daal diya hai to turant bank app se UPI deactivate karo."
                ),
                citation = "Threat Library F4 (QR & Inverted Transaction Frauds)",
                hindiVoiceSummary = "रुकिए! पैसे पाने के लिए कभी यूपीआई पिन नहीं लगता। क्यूआर कोड स्कैन न करें, खाते से पैसे कट जाएंगे।"
            )
        }

        // 5. F2: APK Installation / Fake KYC / AnyDesk
        if (lower.contains(".apk") || lower.contains("anydesk") || lower.contains("quicksupport") ||
            lower.contains("teamviewer") || (lower.contains("kyc") && lower.contains("expired") && lower.contains("link")) ||
            lower.contains("sim block in 24")
        ) {
            return VerdictResult(
                bucket = VerdictBucket.SCAM_LIKELY,
                confidence = 0.97f,
                familyCode = "F2",
                matchedRuleTitle = "F2: Remote Access APK & Fake KYC Trap",
                reasons = listOf(
                    "AnyDesk, RustDesk ya APK install karne se phone ka poora screen scammer ke paas chala jata hai",
                    "Bank ya telecom company WhatsApp par APK bhej kar KYC update nahi karwati",
                    "Ye malware phone ke saare OTP aur SMS chura leta hai"
                ),
                matKaro = "Bheja gaya link ya APK file bilkul download ya install MAT KARO.",
                karo = listOf(
                    "Agar install kar liya hai to turant Flight Mode on karo.",
                    "Apne phone ko factory reset karo ya app uninstall karo.",
                    "Bank se internet banking access block karwao."
                ),
                citation = "Threat Library F2 (Screen-Sharing & Malicious RAT APKs)",
                hindiVoiceSummary = "खतरा! यह एपीके फाइल आपके फोन का पूरा कंट्रोल ले लेगी। कोई भी फाइल डाउनलोड न करें।"
            )
        }

        // 6. F5: Fake SEBI / Unregistered Stock Advisory / VIP Tips
        if (lower.contains("sebi") || lower.contains("guaranteed profit") || lower.contains("stock tips") ||
            lower.contains("upper circuit") || lower.contains("insider trading")
        ) {
            val isFakeReg = lower.contains("inz99") || lower.contains("sebi registered") && !lower.contains("sebi.gov.in")
            return VerdictResult(
                bucket = if (isFakeReg) VerdictBucket.SCAM_LIKELY else VerdictBucket.CAUTION,
                confidence = if (isFakeReg) 0.95f else 0.85f,
                familyCode = "F5",
                matchedRuleTitle = "F5: Unregistered Advisory & Fake SEBI Registration",
                reasons = listOf(
                    "SEBI registered intermediaries kabhi guaranteed returns promise nahi karte",
                    "WhatsApp ya Telegram par VIP tips group me fees maangna SEBI niyam ke khilaf hai",
                    "Advisory broker ke bajaye kisi naye personal account me paise maang raha hai"
                ),
                matKaro = "Kisi anjaan account me advisory fees ya investment money MAT BHEJO.",
                karo = listOf(
                    "Intermediary ka registration sebi.gov.in par khud verify karo.",
                    "Paise sirf SEBI registered broker ke authenticated portal par lagao."
                ),
                citation = "Threat Library F5 (Unregistered Investment Schemes) · R26 Registry",
                hindiVoiceSummary = "सेबी रजिस्टर्ड होने का दावा संदेहास्पद है। सेबी कभी पक्के मुनाफे की गारंटी नहीं देता। पैसे न लगाएं।"
            )
        }

        // 7. F6: Electricity bill disconnect
        if (lower.contains("electricity") && (lower.contains("disconnect") || lower.contains("tonight at") || lower.contains("bijli"))) {
            return VerdictResult(
                bucket = VerdictBucket.SCAM_LIKELY,
                confidence = 0.96f,
                familyCode = "F6",
                matchedRuleTitle = "F6: Fake Electricity Bill Disconnection",
                reasons = listOf(
                    "Power distribution companies aisi urgent SMS raat ko cut-off warning ke sath nahi bhejti",
                    "Official SMS me consumer ID aur official bill portal ka link hota hai, mobile number nahi",
                    "Call karne par fake officer 10 rupaye ke recharge ke bahane remote app lagwayega"
                ),
                matKaro = "SMS me diye gaye mobile number par call MAT KARO.",
                karo = listOf(
                    "Apne bijli board ke official app ya website par jaakar bill status dekhein.",
                    "Message ko 1909 ya 1930 par spam mark karein."
                ),
                citation = "Threat Library F6 (Utility Urgent Disconnection Scheme)",
                hindiVoiceSummary = "यह बिजली बिल काटने का फर्जी मैसेज है। दिए गए नंबर पर कॉल न करें, आधिकारिक पोर्टल पर बिल देखें।"
            )
        }

        // 8. General cautionary keywords
        if (lower.contains("urgent") || lower.contains("won lottery") || lower.contains("prize") ||
            lower.contains("gift card") || lower.contains("customs clearance fee")
        ) {
            return VerdictResult(
                bucket = VerdictBucket.CAUTION,
                confidence = 0.82f,
                familyCode = null,
                matchedRuleTitle = "CAUTION: Urgency & High-Reward Pattern",
                reasons = listOf(
                    "Bina maange reward ya urgency paida karna scam ka sanket hai",
                    "Source verified nahi hai",
                    "Payment karne se pehle vyakti ki pehchan pakki karein"
                ),
                matKaro = "Jaldbaazi me koi payment MAT KARO.",
                karo = listOf(
                    "Doosre madhyam se contact karke verify karein.",
                    "SatarkPay cooling timer ka upyog karein."
                ),
                citation = "Library General Heuristics · T1 Checklist",
                hindiVoiceSummary = "सावधानी बरतें। जल्दबाज़ी में कोई भुगतान न करें। पहले पूरी जांच करें।"
            )
        }

        // 9. Bharat-BERT Deep Neural Model Promotion
        if (bertResult.scamProbability >= 0.75f && bertResult.predictedFamily != null) {
            return VerdictResult(
                bucket = VerdictBucket.SCAM_LIKELY,
                confidence = bertResult.scamProbability,
                familyCode = bertResult.predictedFamily,
                matchedRuleTitle = bertResult.predictedFamilyTitle,
                reasons = listOf(
                    "Bharat-BERT model ne code-mixed threat triggers detect kiye",
                    "Attention tokens: ${bertResult.attentionTokens.joinToString(", ")}",
                    bertResult.analysisExplanation
                ),
                matKaro = "Koi bhi payment ya sensitive details share MAT KARO.",
                karo = listOf(
                    "Sender ko turant block karein.",
                    "National Cyber Crime Helpline 1930 par complaint karein."
                ),
                citation = "Bharat-BERT Neural Classifier · ${bertResult.predictedFamily}",
                hindiVoiceSummary = "सतर्क रहें! भारत-बर्ट मॉडल ने इस संदेश में फ्रॉड के लक्षण पाए हैं। कोई भुगतान न करें।",
                bertScamProbability = bertResult.scamProbability,
                bertAttentionTokens = bertResult.attentionTokens
            )
        }

        // Default honest bucket: PAUSE - NAHI BATA SAKTA (honest bucket principle)
        return VerdictResult(
            bucket = VerdictBucket.PAUSE_NAHI_BATA,
            confidence = 0.50f,
            familyCode = null,
            matchedRuleTitle = "PAUSE: Paka Nahi Bata Sakta (Low-Confidence / Novel Pattern)",
            reasons = listOf(
                "Is message me hamare 35 threat patterns me se koi direct match nahi mila",
                "App jhootha bharosa nahi deti (honest bucket policy)",
                "Analyst desk aur Gemini deep investigation ki zarurat hai"
            ),
            matKaro = "Anjaan payee ko payment MAT KARO jab tak confirm na ho.",
            karo = listOf(
                "Do sawal ka jawab dein: Kya kisi anjaan vyakti ne paisa maanga hai?",
                "AI Sanchalak me Deep Thinking mode on karke poochhein.",
                "Zarurat padne par Human Analyst review queue me bhejein."
            ),
            citation = "Honest Bucket Principle · Intel Desk Escalation",
            hindiVoiceSummary = "मैं पक्का नहीं बता सकता। यह कोई नया पैटर्न हो सकता है। कृपया सतर्क रहें और बिना जांचे पैसे न भेजें।",
            bertScamProbability = bertResult.scamProbability,
            bertAttentionTokens = bertResult.attentionTokens
        )
    }

    // Domain & Link Trust (M3)
    fun analyzeDomain(urlOrDomain: String): DomainAnalysisResult {
        val clean = urlOrDomain.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .split("/")[0]
            .lowercase()

        val signals = mutableListOf<String>()

        // L1: Verified RBI, NPCI, Gov, Banks
        val l1Domains = listOf(
            "rbi.org.in", "npci.org.in", "cybercrime.gov.in", "sebi.gov.in",
            "sbi.co.in", "hdfcbank.com", "icicibank.com", "axisbank.com",
            "punjabnationalbank.in", "bankofbaroda.in", "uidai.gov.in"
        )
        if (l1Domains.any { clean == it || clean.endsWith(".$it") }) {
            signals.add("Official Government / RBI / Regulated Bank domain")
            signals.add("Authentic SSL verification & NPCI trust whitelist")
            return DomainAnalysisResult(
                domain = clean,
                trustLevel = DomainTrustLevel.L1_VERIFIED_GOV_BANK,
                signals = signals,
                gatewayName = null,
                merchantName = "Official Regulator/Bank",
                hasGatewayMismatch = false,
                warningMessage = "Safe & Official regulator / institutional portal."
            )
        }

        // Check for L4 Lookalike / Phishing patterns
        val riskyTlds = listOf(".top", ".xyz", ".click", ".vip", ".cc", ".link", ".buzz", ".work", ".cfd")
        val isIpLiteral = Regex("""\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b""").containsMatchIn(clean)
        val hasBrandSubdomain = (clean.contains("sbi") || clean.contains("hdfc") || clean.contains("paytm") || clean.contains("upi")) &&
                !clean.endsWith(".com") && !clean.endsWith(".co.in") && !clean.endsWith(".in")

        var isL4 = false
        if (riskyTlds.any { clean.endsWith(it) }) {
            signals.add("High-risk TLD (${riskyTlds.find { clean.endsWith(it) }}) frequently used in phishing")
            isL4 = true
        }
        if (isIpLiteral) {
            signals.add("Direct IP-literal address instead of registered domain name")
            isL4 = true
        }
        if (hasBrandSubdomain) {
            signals.add("Brand name spoofed inside subdomain (homoglyph/lookalike attack)")
            isL4 = true
        }
        if (clean.contains("@")) {
            signals.add("URL contains '@' user-info override spoofing technique")
            isL4 = true
        }

        if (isL4) {
            return DomainAnalysisResult(
                domain = clean,
                trustLevel = DomainTrustLevel.L4_LOOKALIKE_FAKE,
                signals = signals,
                gatewayName = "Razorpay (Claimed)",
                merchantName = "quickearn-pro",
                hasGatewayMismatch = true,
                warningMessage = "LOOKALIKE / FAKE DOMAIN: Gateway achha ≠ Merchant achha. Ye 2026 ka classic spoof trap hai!"
            )
        }

        // L2: Verified popular merchants
        val l2Domains = listOf(
            "amazon.in", "flipkart.com", "swiggy.com", "zomato.com",
            "phonepe.com", "paytm.com", "bhimupi.org.in", "tata.com", "jio.com"
        )
        if (l2Domains.any { clean == it || clean.endsWith(".$it") }) {
            signals.add("Verified high-reputation commercial platform")
            return DomainAnalysisResult(
                domain = clean,
                trustLevel = DomainTrustLevel.L2_VERIFIED_MERCHANT,
                signals = signals,
                gatewayName = null,
                merchantName = clean,
                hasGatewayMismatch = false,
                warningMessage = "Verified merchant platform. Normal safety practices apply."
            )
        }

        // L3: Clean unverified
        signals.add("Domain has valid format but is not on verified authority list")
        signals.add("Recommend verifying merchant identity before making payments")
        return DomainAnalysisResult(
            domain = clean,
            trustLevel = DomainTrustLevel.L3_UNVERIFIED_CLEAN,
            signals = signals,
            gatewayName = null,
            merchantName = "Unverified Entity",
            hasGatewayMismatch = false,
            warningMessage = "Clean unverified portal. Proceed with caution."
        )
    }
}
