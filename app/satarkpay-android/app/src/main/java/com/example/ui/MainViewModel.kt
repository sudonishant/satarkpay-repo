package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SatarkRepository
import com.example.engine.*
import com.example.network.ChatMessage
import com.example.thehro.AttentionBudget
import com.example.thehro.CallStateProbe
import com.example.thehro.Decision
import com.example.thehro.PaymentSession
import com.example.thehro.PersonalBaseline
import com.example.thehro.ThehroEngine
import com.example.thehro.permitsPayment
import com.example.network.GeminiService
import com.example.util.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SatarkScreen {
    SPLASH,
    HOME,
    SAFEPAY,
    QR_SCANNER,
    CHAT_PAY,
    SCREENSHOT_RADAR,
    DOMAIN_TRUST,
    SANCHALAK_CHAT,
    WALLET_AUDIT,
    INTEL_FEED,
    REPORT_EVIDENCE,
    EMERGENCY_CONFIRM,
    GRIEVANCE_LADDER,
    APP_SECURITY,
    SETTINGS,
    ANALYST_CONSOLE,
    AIR_GAP_LINK,
    COMMUNITY
}

data class PendingSafePayment(val upiId: String, val payeeName: String, val amount: Long)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SatarkRepository(AppDatabase.getInstance(application))
    private val ttsManager = TtsManager(application)

    // --- App Security & Permission Scanner State ---
    val scannedApps = MutableStateFlow<List<InstalledAppPermissionInfo>>(emptyList())
    val permissionSummary = MutableStateFlow(
        PermissionAuditSummary(
            totalAppsScanned = 0,
            appsWithSmsCount = 0,
            appsWithLocationCount = 0,
            appsWithAccessibilityCount = 0,
            appsWithOverlayCount = 0,
            highRiskAppsCount = 0
        )
    )
    val isScanningApps = MutableStateFlow(false)

    // Navigation & Screen Stack
    private val _currentScreen = MutableStateFlow(SatarkScreen.HOME)
    val currentScreen: StateFlow<SatarkScreen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<SatarkScreen>()

    // Global Brand & Access Toggles
    val guardOn = MutableStateFlow(true)
    val voiceHindiOn = MutableStateFlow(true)
    val seniorModeOn = MutableStateFlow(false)
    val maskPiiOn = MutableStateFlow(true)

    // Data from Database
    val reports: StateFlow<List<ScamReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scans: StateFlow<List<ScreenshotScanEntity>> = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mandates: StateFlow<List<MandateAuditEntity>> = repository.allMandates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedIntel: StateFlow<List<IntelPatternEntity>> = repository.approvedIntel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviewQueueIntel: StateFlow<List<IntelPatternEntity>> = repository.reviewQueueIntel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkHistory: StateFlow<List<CheckHistoryEntity>> = repository.checkHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayees: StateFlow<List<PayeeLedgerEntity>> = repository.allPayees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SafePay Pre-Payment Broker State ---
    val safePayUpi = MutableStateFlow("")
    val safePayName = MutableStateFlow("")
    val safePayAmount = MutableStateFlow(0L)
    val safePayChannel = MutableStateFlow(PaymentSourceChannel.UNKNOWN_SMS)
    val safePayActiveCall = MutableStateFlow(CallStateProbe.read(application).inCall)
    val safePayActiveScreenControl = MutableStateFlow<Boolean?>(null)
    val safePayDemoMode = MutableStateFlow(false)
    val safePayPendingPayment = MutableStateFlow<PendingSafePayment?>(null)
    val safePayReturnedFromPaymentApp = MutableStateFlow(false)
    val safePayBaselineP95 = MutableStateFlow<Double?>(null)
    val safePayHoldSeconds = MutableStateFlow(0)
    val safePayHoldActive = MutableStateFlow(false)
    private var safePayHoldJob: Job? = null
    private var safePayLedgerJob: Job? = null

    // --- THEHRO v3 (RAKSHAM PS 02): workflow engine + attention budget ---
    private val thehro = ThehroEngine()
    private val attentionBudget = AttentionBudget(per30Days = 3)
    val thehroDecision = MutableStateFlow<Decision?>(null)
    val safePayIsFirstTime = MutableStateFlow(true)
    val safePayTxnCount = MutableStateFlow(0)
    val safePayTrusted = MutableStateFlow(false)

    // --- M1: Chat-Before-Pay State ---
    val isContactSaved = MutableStateFlow(false)
    val chatDwellMinutes = MutableStateFlow(12)
    val q1SelfPaying = MutableStateFlow<Boolean?>(null)
    val q2OfficerOrOffer = MutableStateFlow<Boolean?>(null)
    val q3LiveCallActive = MutableStateFlow<Boolean?>(null)

    val coolingTimerSeconds = MutableStateFlow(480) // 8 min cooling = 480 sec
    val isCoolingActive = MutableStateFlow(false)
    private var coolingJob: Job? = null

    // --- M2: Screenshot Radar State ---
    val burstScreenshotsCount = MutableStateFlow(3)
    val repeatPayeeCount = MutableStateFlow(3)
    val recentPayeeUpi = MutableStateFlow("7349123456@ptaxis")

    // --- M3: Domain Trust State ---
    val domainInput = MutableStateFlow("qr-pay.top")
    val domainAnalysisResult = MutableStateFlow<DomainAnalysisResult?>(null)

    // --- M4: Sanchalak Chat State ---
    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "model",
                text = "SatarkPay AI Sanchalak chalu hai. Koi bhi suspicious SMS, chat, UPI handle ya screenshot ka text yahan paste karein ya mic se bol kar poochhein."
            )
        )
    )
    val isChatLoading = MutableStateFlow(false)
    val isAudioRecording = MutableStateFlow(false)
    val enableHighThinking = MutableStateFlow(false)
    val enableSearchGrounding = MutableStateFlow(true)
    val enableMapsGrounding = MutableStateFlow(false)
    val selectedModel = MutableStateFlow("gemini-3.5-flash")
    val lastVerdict = MutableStateFlow<VerdictResult?>(null)
    val clientRedactionCount = MutableStateFlow(0)

    // --- M7 & R38: Emergency Golden Hour State ---
    val goldenHourSeconds = MutableStateFlow(3600L)
    val isEmergencyClockRunning = MutableStateFlow(true)
    private var emergencyClockJob: Job? = null

    val stepEmailSent = MutableStateFlow(false)
    val stepCallHelplineDone = MutableStateFlow(false)
    val stepStopPaymentDone = MutableStateFlow(false)
    val stepFamilyAlertSent = MutableStateFlow(false)

    val activeReportAmount = MutableStateFlow(0L)
    val activeReportUpi = MutableStateFlow("")
    val activeReportUtr = MutableStateFlow("")
    val activeReportTxn = MutableStateFlow("")
    val activeSuspectContact = MutableStateFlow("")
    val activeComplaintNumber = MutableStateFlow("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.seedInitialDataIfEmpty()
                refreshSafePayBaseline()
                analyzeCurrentDomain("qr-pay.top")
                withContext(Dispatchers.Main) {
                    startEmergencyClock()
                }
                scanDeviceApps()
                checkPayeeLedgerAndEvaluate()
            } catch (e: Exception) {
                Log.e("SatarkPay", "Startup initialization failed safely", e)
            }
        }
    }

    fun scanDeviceApps() {
        viewModelScope.launch {
            isScanningApps.value = true
            try {
                val apps = withContext(Dispatchers.IO) {
                    AppSecurityScanner.scanInstalledApplications(getApplication())
                }
                val summary = AppSecurityScanner.computeAuditSummary(apps)
                scannedApps.value = apps
                permissionSummary.value = summary

                if (summary.highRiskAppsCount > 0) {
                    speakText("Alert: High-risk remote control application detected on device. Please inspect.")
                }
            } catch (e: Exception) {
                Log.e("SatarkPay", "App security scan failed gracefully", e)
            } finally {
                isScanningApps.value = false
                evaluateSafePay()
            }
        }
    }

    fun uninstallApp(packageName: String) {
        AppSecurityScanner.requestAppUninstall(getApplication(), packageName)
        // Optimistically remove from list if user triggers uninstall
        val updated = scannedApps.value.filter { it.packageName != packageName }
        scannedApps.value = updated
        permissionSummary.value = AppSecurityScanner.computeAuditSummary(updated)
        speakText("ऐप हटाने का अनुरोध भेजा गया।")
    }

    fun openAppSettings(packageName: String) {
        AppSecurityScanner.openAppDetailsSettings(getApplication(), packageName)
    }

    // Navigation
    fun navigateTo(screen: SatarkScreen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenHistory.isNotEmpty()) {
            val previous = _screenHistory.removeAt(_screenHistory.size - 1)
            _currentScreen.value = previous
            return true
        }
        if (_currentScreen.value != SatarkScreen.HOME) {
            _currentScreen.value = SatarkScreen.HOME
            return true
        }
        return false
    }

    // Toggles
    fun toggleGuard() {
        guardOn.value = !guardOn.value
    }

    fun toggleVoice() {
        voiceHindiOn.value = !voiceHindiOn.value
        if (!voiceHindiOn.value) {
            ttsManager.stop()
        }
    }

    fun toggleSenior() {
        seniorModeOn.value = !seniorModeOn.value
    }

    fun toggleMask() {
        maskPiiOn.value = !maskPiiOn.value
    }

    fun speakText(text: String) {
        if (voiceHindiOn.value) {
            ttsManager.speak(text, isHindiPreferred = true)
        }
    }

    // --- M1: Chat-Before-Pay Actions ---
    fun setContactSaved(saved: Boolean) {
        isContactSaved.value = saved
    }

    fun setChatDwell(minutes: Int) {
        chatDwellMinutes.value = minutes
    }

    fun getFrictionTier(): String {
        val saved = isContactSaved.value
        val dwell = chatDwellMinutes.value
        return when {
            saved && dwell < 8 -> "T1: Fast Path (1 min check / call confirm)"
            saved && dwell >= 8 -> "T1: Notification (3 min + 3 Sawal)"
            !saved && dwell < 8 -> "T2: Cooling (8 min guided friction)"
            else -> "T3: Hold + Human Analyst Callback (<= 5 min)"
        }
    }

    fun startCoolingTimer() {
        isCoolingActive.value = true
        coolingTimerSeconds.value = 480
        coolingJob?.cancel()
        coolingJob = viewModelScope.launch {
            while (coolingTimerSeconds.value > 0 && isCoolingActive.value) {
                delay(1000)
                coolingTimerSeconds.value -= 1
            }
            isCoolingActive.value = false
        }
    }

    fun cancelPayment() {
        coolingJob?.cancel()
        isCoolingActive.value = false
        speakText("भुगतान रद्द कर दिया गया है। आप सुरक्षित हैं।")
        navigateTo(SatarkScreen.HOME)
    }

    // --- M2: Screenshot Radar Actions ---
    fun simulateAddScreenshot(payeeUpi: String = "7349123456@ptaxis", domain: String = "qr-pay.top", amount: Long = 3000) {
        viewModelScope.launch {
            val hash = SatarkRepository.computeSha256("img_${System.currentTimeMillis()}_$amount")
            val scan = ScreenshotScanEntity(
                filename = "screenshot_${System.currentTimeMillis()}.png",
                sourceDomain = domain,
                payeeUpi = payeeUpi,
                amount = amount,
                riskLevel = "L4_FAKE",
                isBurst = true,
                sha256Hash = hash
            )
            repository.insertScan(scan)
            burstScreenshotsCount.value += 1
            repeatPayeeCount.value += 1
            recentPayeeUpi.value = payeeUpi
        }
    }

    // --- M3: Domain Trust Actions ---
    fun analyzeCurrentDomain(url: String) {
        domainInput.value = url
        val result = RuleEngine.analyzeDomain(url)
        domainAnalysisResult.value = result
    }

    // --- M4: Sanchalak Chat & Scam Analysis Actions ---
    fun sendUserChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        // 1. Client-side PII Redaction (R28)
        val redaction = RuleEngine.redactPII(trimmed)
        clientRedactionCount.value += redaction.itemsRedactedCount

        // 2. Deterministic Local Rule Check + Bharat-BERT
        val deterministicResult = RuleEngine.analyzeText(trimmed)
        if (deterministicResult.isConversationalGreeting) {
            lastVerdict.value = null // Clear any prior scam popup card on greetings!
        } else {
            lastVerdict.value = deterministicResult
            speakText(deterministicResult.hindiVoiceSummary)
        }

        // Record in check history
        viewModelScope.launch {
            repository.insertHistory(
                CheckHistoryEntity(
                    checkType = "SANCHALAK",
                    summary = trimmed.take(50),
                    verdictBucket = deterministicResult.bucket.name,
                    details = deterministicResult.matchedRuleTitle
                )
            )
        }

        // Add user message to conversation list
        val displayUserText = if (maskPiiOn.value) redaction.redactedText else trimmed
        val currentList = chatMessages.value.toMutableList()
        currentList.add(ChatMessage(sender = "user", text = displayUserText))
        chatMessages.value = currentList

        // 3. Elevate with Gemini for conversational AI explanation
        viewModelScope.launch {
            isChatLoading.value = true

            val modelToUse = when {
                enableHighThinking.value -> "gemini-3.1-pro-preview"
                selectedModel.value == "gemini-3.1-flash-lite-preview" -> "gemini-3.1-flash-lite-preview"
                else -> "gemini-3.5-flash"
            }

            val result = GeminiService.sendChatMessage(
                history = chatMessages.value,
                userMessage = displayUserText,
                modelName = modelToUse,
                enableHighThinking = enableHighThinking.value,
                useGoogleSearch = enableSearchGrounding.value,
                useGoogleMaps = enableMapsGrounding.value
            )

            isChatLoading.value = false
            result.onSuccess { modelMsg ->
                val updated = chatMessages.value.toMutableList()
                updated.add(modelMsg)
                chatMessages.value = updated
            }.onFailure { err ->
                // Offline fallback structured reply
                val fallbackMsg = if (deterministicResult.isConversationalGreeting) {
                    ChatMessage(
                        sender = "model",
                        text = "Namaste! Main SatarkPay AI Sanchalak hoon — aapka personal cyber-defense & anti-fraud advisor.\n\nAap mujhse kisi bhi sandeh-janak message, suspicious WhatsApp offer, UPI link, fake customer care call, ya loan app dhamki ke baare mein poochh sakte hain. Main turant Bharat-BERT aur 35+ threat patterns se verify karke batata hoon.\n\nAapke saath kya sandeh hua hai?"
                    )
                } else {
                    ChatMessage(
                        sender = "model",
                        text = """
                            【Bharat-BERT & Threat Engine Verdict: ${deterministicResult.bucket.name}】
                            Rule: ${deterministicResult.matchedRuleTitle}
                            ${if (deterministicResult.bertScamProbability > 0.05f) "🧠 Bharat-BERT Confidence: ${(deterministicResult.bertScamProbability * 100).toInt()}%" else ""}
                            
                            • ${deterministicResult.reasons.joinToString("\n• ")}
                            
                            ⛔ MAT KARO: ${deterministicResult.matKaro}
                            
                            ✅ KARO:
                            • ${deterministicResult.karo.joinToString("\n• ")}
                            
                            [Citation: ${deterministicResult.citation}]
                            ${if (!GeminiService.hasValidApiKey()) "(Offline Bharat-BERT engine active — Add Gemini API key in Secrets panel for dynamic AI insights)" else "(Network offline, using on-device threat library)"}
                        """.trimIndent()
                    )
                }
                val updated = chatMessages.value.toMutableList()
                updated.add(fallbackMsg)
                chatMessages.value = updated
            }
        }
    }

    fun dismissVerdict() {
        lastVerdict.value = null
    }

    // v3 privacy rule (RAKSHAM PS 02): SatarkPay never records or uploads audio.
    // The old mic -> cloud transcription path was removed; the mic button is a no-op.
    fun toggleAudioRecording() {
        isAudioRecording.value = false
    }

    // --- M5: Wallet Mandate Audit Actions ---
    fun revokeMandate(id: Long) {
        viewModelScope.launch {
            repository.revokeMandate(id)
            speakText("ऑटो-पे मैंडेट रद्द कर दिया गया है।")
        }
    }

    // --- M6: Intel Feed Actions ---
    fun approveIntelPattern(id: Long) {
        viewModelScope.launch {
            repository.approveIntel(id)
        }
    }

    fun rejectIntelPattern(id: Long) {
        viewModelScope.launch {
            repository.rejectIntel(id)
        }
    }

    // --- M7 & R38: Emergency Fraud Actions ---
    private fun startEmergencyClock() {
        emergencyClockJob?.cancel()
        emergencyClockJob = viewModelScope.launch {
            while (isEmergencyClockRunning.value) {
                delay(1000)
                goldenHourSeconds.value += 1
            }
        }
    }

    fun markEmailSent() {
        stepEmailSent.value = true
        speakText("साइबर सेल को ईमेल भेज दिया गया है।")
    }

    fun markCallHelplineDone() {
        stepCallHelplineDone.value = true
        speakText("1930 हेल्पलाइन पर कॉल दर्ज कर दी गई है।")
    }

    fun markStopPaymentDone() {
        stepStopPaymentDone.value = true
        speakText("बैंक में भुगतान रोकने का अनुरोध भेज दिया गया है।")
    }

    fun markFamilyAlertSent() {
        stepFamilyAlertSent.value = true
        speakText("परिवार को अलर्ट संदेश भेज दिया गया है।")
    }


    // --- SafePay Pre-Payment Broker Actions ---
    private val vpaPattern = Regex("^[A-Za-z0-9][A-Za-z0-9._-]{1,99}@[A-Za-z0-9][A-Za-z0-9.-]{1,31}$")
    private fun isValidVpa(value: String) = vpaPattern.matches(value.trim())

    fun setSafePayUpi(upi: String) {
        val clean = upi.trim()
        if (!clean.equals(safePayUpi.value, ignoreCase = true)) {
            safePayName.value = ""
            safePayIsFirstTime.value = true
            safePayTxnCount.value = 0
            safePayTrusted.value = false
        }
        safePayUpi.value = clean
        checkPayeeLedgerAndEvaluate()
    }

    fun setSafePayName(name: String) {
        safePayName.value = name
        evaluateSafePay()
    }

    fun setSafePayAmount(amount: Long) {
        safePayAmount.value = amount
        evaluateSafePay()
    }

    fun setSafePayChannel(channel: PaymentSourceChannel) {
        safePayChannel.value = channel
        evaluateSafePay()
    }

    fun setSafePayActiveCall(active: Boolean?) {
        safePayActiveCall.value = active
        evaluateSafePay()
    }

    fun setSafePayActiveScreenControl(active: Boolean?) {
        safePayActiveScreenControl.value = active
        evaluateSafePay()
    }

    fun checkPayeeLedgerAndEvaluate() {
        safePayLedgerJob?.cancel()
        thehroDecision.value = null
        val clean = safePayUpi.value.lowercase().trim()
        safePayLedgerJob = viewModelScope.launch {
            delay(180)
            if (safePayDemoMode.value || !isValidVpa(clean)) {
                safePayIsFirstTime.value = true
                safePayTxnCount.value = 0
                safePayTrusted.value = false
                if (clean == safePayUpi.value.lowercase().trim()) evaluateSafePay()
                return@launch
            }
            val payee = repository.getPayee(clean)
            val reportedCount = repository.getRecentUserReportedAmountsForPayee(clean).size
            if (clean != safePayUpi.value.lowercase().trim() || safePayDemoMode.value) return@launch
            safePayIsFirstTime.value = reportedCount == 0
            safePayTxnCount.value = reportedCount
            safePayTrusted.value = payee?.isTrustVerified == true
            if (safePayName.value.isBlank() && payee?.payeeName?.isNotBlank() == true) {
                safePayName.value = payee.payeeName
            }
            evaluateSafePay()
        }
    }

    fun evaluateSafePay() {
        val probe = CallStateProbe.read(getApplication())
        val highRiskInstalled = scannedApps.value.takeIf { it.isNotEmpty() }
            ?.any { it.riskLevel == SensitiveRiskLevel.HIGH_RISK }
        val effectiveCallState = safePayActiveCall.value
        val session = PaymentSession(
            callActive = effectiveCallState,
            callerInContacts = null,
            callVideoOrVoip = if (probe.inCall == true) probe.voipOrVideo else null,
            callMinutes = null,
            hour = java.time.LocalTime.now().hour,
            activeScreenControl = safePayActiveScreenControl.value,
            activeScreenControlConfidence = null,
            highRiskAppInstalled = highRiskInstalled,
            accessibilityServiceEnabled = null,
            apkInstalledHours = null,
            overlayService = null,
            payeeNew = if (isValidVpa(safePayUpi.value)) safePayIsFirstTime.value else null,
            payeeSaved = safePayTrusted.value,
            payeeVpaAgeDays = null,
            payeeRisk = null,
            payeeSebiValid = null,
            amount = safePayAmount.value.toDouble(),
            baselineP95 = safePayBaselineP95.value,
        )
        val raw = thehro.decide(session, budget = null)
        val now = System.currentTimeMillis()
        thehroDecision.value = if (raw.tier == "T2" && attentionBudget.remaining(now) <= 0) {
            raw.copy(
                tier = "T1",
                action = ThehroEngine.ACTIONS.getValue("T1"),
                reason = "Caution: check the recipient name independently before paying.",
                question = null,
                budgetNote = "attention budget used up -> badge instead of question"
            )
        } else raw
    }

    private suspend fun refreshSafePayBaseline() {
        safePayBaselineP95.value = PersonalBaseline.p95(repository.getRecentUserReportedAmounts())
    }

    fun startSafePayHold() {
        safePayHoldJob?.cancel()
        safePayHoldSeconds.value = 60
        safePayHoldActive.value = true
        safePayHoldJob = viewModelScope.launch {
            repeat(60) {
                delay(1000)
                safePayHoldSeconds.value = (safePayHoldSeconds.value - 1).coerceAtLeast(0)
            }
            safePayHoldActive.value = false
            evaluateSafePay() // the countdown itself never releases a payment hold
        }
    }

    fun clearSafePayDemo() {
        if (safePayPendingPayment.value != null) return
        safePayDemoMode.value = false
        safePayPendingPayment.value = null
        safePayReturnedFromPaymentApp.value = false
        safePayUpi.value = ""
        safePayName.value = ""
        safePayAmount.value = 0L
        safePayChannel.value = PaymentSourceChannel.UNKNOWN_SMS
        safePayActiveCall.value = CallStateProbe.read(getApplication()).inCall
        safePayActiveScreenControl.value = null
        checkPayeeLedgerAndEvaluate()
    }

    fun loadSafePayPreset(
        upi: String,
        name: String,
        amount: Long,
        channel: PaymentSourceChannel,
        activeCall: Boolean,
        activeScreenControl: Boolean?
    ) {
        if (safePayPendingPayment.value != null) return
        safePayDemoMode.value = true
        safePayReturnedFromPaymentApp.value = false
        safePayUpi.value = upi
        safePayName.value = name
        safePayAmount.value = amount
        safePayChannel.value = channel
        safePayActiveCall.value = activeCall
        safePayActiveScreenControl.value = activeScreenControl
        checkPayeeLedgerAndEvaluate()
    }

    fun onQrScannedResult(parsed: com.example.util.ParsedUpiData) {
        if (safePayPendingPayment.value != null) {
            speakText("पहले पिछले UPI ऐप से लौटकर भुगतान का परिणाम दर्ज करें।")
            navigateTo(SatarkScreen.SAFEPAY)
            return
        }
        if (!isValidVpa(parsed.upiId)) {
            speakText("इस QR में मान्य UPI ID नहीं मिली। भुगतान विवरण नहीं बदले गए।")
            navigateTo(SatarkScreen.SAFEPAY)
            return
        }
        safePayDemoMode.value = false
        if (!parsed.upiId.equals(safePayUpi.value, ignoreCase = true)) safePayName.value = ""
        safePayUpi.value = parsed.upiId.trim()
        safePayName.value = parsed.payeeName.trim()
        safePayAmount.value = parsed.amount?.takeIf { it > 0L } ?: 0L
        safePayChannel.value = PaymentSourceChannel.DIRECT_SHOP_QR
        checkPayeeLedgerAndEvaluate()
        navigateTo(SatarkScreen.SAFEPAY)
    }

    fun markCurrentPayeeTrusted() {
        val clean = safePayUpi.value.lowercase().trim()
        if (safePayDemoMode.value || !isValidVpa(clean)) {
            speakText("पहले वास्तविक UPI ID दर्ज करें; सिमुलेशन को भरोसेमंद सूची में नहीं जोड़ा जा सकता।")
            return
        }
        viewModelScope.launch {
            repository.markPayeeTrusted(clean, safePayName.value)
            safePayTrusted.value = true
            evaluateSafePay()
            speakText("यह UPI ID आपकी स्थानीय भरोसेमंद सूची में जोड़ी गई है; इससे जोखिम संकेत बंद नहीं होंगे।")
        }
    }

    fun executeSafePayment(context: android.content.Context, questionConfirmed: Boolean = false) {
        val decision = thehroDecision.value
        if (safePayDemoMode.value) {
            speakText("यह केवल सिमुलेशन है। भुगतान करने के लिए डेमो हटाकर वास्तविक विवरण दर्ज करें।")
            return
        }
        if (!isValidVpa(safePayUpi.value) || safePayAmount.value <= 0L) {
            speakText("वास्तविक UPI ID और शून्य से अधिक राशि दर्ज करें।")
            return
        }
        if (decision == null || !decision.permitsPayment(questionConfirmed)) {
            val message = if (decision?.tier == "T2") {
                "जारी रखने से पहले स्क्रीन पर दिया गया सुरक्षा सवाल पढ़कर पुष्टि करें।"
            } else {
                "सुरक्षा जांच के कारण अभी भुगतान ऐप नहीं खोला जाएगा।"
            }
            speakText(message)
            return
        }
        if (decision.tier == "T2") {
            val now = System.currentTimeMillis()
            if (attentionBudget.remaining(now) <= 0) {
                evaluateSafePay()
                speakText("आज की सुरक्षा पुष्टि सीमा पूरी हो गई है; भुगतान जारी नहीं किया गया।")
                return
            }
            attentionBudget.spend(now)
        }
        if (safePayPendingPayment.value != null) {
            speakText("पहले पिछले भुगतान का परिणाम दर्ज करें।")
            return
        }
        val pending = PendingSafePayment(safePayUpi.value.trim(), safePayName.value.trim(), safePayAmount.value)
        safePayReturnedFromPaymentApp.value = false
        safePayPendingPayment.value = pending
        try {
            val upiUri = android.net.Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", pending.upiId)
                .appendQueryParameter("pn", pending.payeeName.take(80))
                .appendQueryParameter("am", pending.amount.toString())
                .appendQueryParameter("cu", "INR")
                .build()
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, upiUri).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = android.content.Intent.createChooser(intent, "Pay via UPI App").apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
            speakText("पेमेंट ऐप खोला जा रहा है। सावधानी बरतें।")
        } catch (e: Exception) {
            safePayPendingPayment.value = null
            safePayReturnedFromPaymentApp.value = false
            speakText("फोन में कोई UPI पेमेंट ऐप नहीं मिला। विवरण जांचें।")
        }
    }

    fun markSafePayReturnedFromPaymentApp() {
        if (safePayPendingPayment.value != null) safePayReturnedFromPaymentApp.value = true
    }

    fun confirmSafePayUserReportedSuccess() {
        val pending = safePayPendingPayment.value ?: return
        if (safePayDemoMode.value) return
        if (!safePayReturnedFromPaymentApp.value) {
            speakText("पेमेंट ऐप से लौटने के बाद ही परिणाम दर्ज करें।")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.recordUserReportedPayment(pending.upiId, pending.payeeName, pending.amount)
                refreshSafePayBaseline()
                safePayPendingPayment.value = null
                safePayReturnedFromPaymentApp.value = false
                checkPayeeLedgerAndEvaluate()
                speakText("आपकी बताई हुई सफल पेमेंट स्थानीय इतिहास में दर्ज हुई; बैंक से पुष्टि नहीं की गई है।")
            } catch (e: Exception) {
                Log.e("SatarkPay", "Could not record user-reported payment", e)
                speakText("पेमेंट का स्थानीय रिकॉर्ड सेव नहीं हुआ।")
            }
        }
    }

    fun cancelSafePayPendingPayment() {
        safePayPendingPayment.value = null
        safePayReturnedFromPaymentApp.value = false
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        coolingJob?.cancel()
        emergencyClockJob?.cancel()
        safePayHoldJob?.cancel()
    }
}
