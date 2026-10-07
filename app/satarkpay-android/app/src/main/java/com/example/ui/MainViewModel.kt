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
import com.example.network.GeminiService
import com.example.util.AudioRecorderHelper
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
    ANALYST_CONSOLE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SatarkRepository(AppDatabase.getInstance(application))
    private val ttsManager = TtsManager(application)
    private val audioRecorder = AudioRecorderHelper(application)

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
    val safePayUpi = MutableStateFlow("cybercbi91@okhdfcbank")
    val safePayName = MutableStateFlow("CBI Digital Cell")
    val safePayAmount = MutableStateFlow(25000L)
    val safePayChannel = MutableStateFlow(PaymentSourceChannel.WHATSAPP_UNSAVED)
    val safePayActiveCall = MutableStateFlow(true)
    val safePaySnippet = MutableStateFlow("Urgent verification security deposit under CBI Mumbai. Do not disconnect call.")
    val safePayEvaluation = MutableStateFlow<AttackChainEvaluation?>(null)
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
    val goldenHourSeconds = MutableStateFlow(42L)
    val isEmergencyClockRunning = MutableStateFlow(true)
    private var emergencyClockJob: Job? = null

    val stepEmailSent = MutableStateFlow(false)
    val stepCallHelplineDone = MutableStateFlow(false)
    val stepStopPaymentDone = MutableStateFlow(false)
    val stepFamilyAlertSent = MutableStateFlow(false)

    val activeReportAmount = MutableStateFlow(5000L)
    val activeReportUpi = MutableStateFlow("7349123456@ptaxis")
    val activeReportUtr = MutableStateFlow("UTR492817290123")
    val activeReportTxn = MutableStateFlow("TXN8912301948")
    val activeSuspectContact = MutableStateFlow("9876543210")
    val activeComplaintNumber = MutableStateFlow("NCRP-2026-BH-89123")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.seedInitialDataIfEmpty()
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

        // 2. Deterministic Local Rule Check
        val deterministicResult = RuleEngine.analyzeText(trimmed)
        lastVerdict.value = deterministicResult

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

        // Read Hindi voice alert if voice is enabled
        speakText(deterministicResult.hindiVoiceSummary)

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
                val fallbackMsg = ChatMessage(
                    sender = "model",
                    text = """
                        【Deterministic Engine Verdict: ${deterministicResult.bucket.name}】
                        Rule: ${deterministicResult.matchedRuleTitle}
                        
                        • ${deterministicResult.reasons.joinToString("\n• ")}
                        
                        ⛔ MAT KARO: ${deterministicResult.matKaro}
                        
                        ✅ KARO:
                        • ${deterministicResult.karo.joinToString("\n• ")}
                        
                        [Citation: ${deterministicResult.citation}]
                        ${if (!GeminiService.hasValidApiKey()) "(Offline rule engine active — Add Gemini API key in Secrets panel for dynamic AI insights)" else "(Network offline, using on-device threat library)"}
                    """.trimIndent()
                )
                val updated = chatMessages.value.toMutableList()
                updated.add(fallbackMsg)
                chatMessages.value = updated
            }
        }
    }

    // Audio Transcription (gemini-3.5-transcribe)
    fun toggleAudioRecording() {
        if (!isAudioRecording.value) {
            val started = audioRecorder.startRecording()
            if (started) {
                isAudioRecording.value = true
            }
        } else {
            val audioBytes = audioRecorder.stopRecording()
            isAudioRecording.value = false
            if (audioBytes != null && audioBytes.isNotEmpty()) {
                viewModelScope.launch {
                    isChatLoading.value = true
                    val transcribeResult = GeminiService.transcribeAudio(audioBytes)
                    isChatLoading.value = false
                    transcribeResult.onSuccess { transcribed ->
                        if (transcribed.isNotEmpty()) {
                            sendUserChatMessage(transcribed)
                        }
                    }.onFailure { err ->
                        sendUserChatMessage("Suspicious payment demand audio recording received. Please inspect caller claims.")
                    }
                }
            }
        }
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

    fun confirmFraudAndTriggerEmergency(amount: Long = 5000, upi: String = "7349123456@ptaxis") {
        activeReportAmount.value = amount
        activeReportUpi.value = upi
        goldenHourSeconds.value = 0
        stepEmailSent.value = false
        stepCallHelplineDone.value = false
        stepStopPaymentDone.value = false

        viewModelScope.launch {
            val report = ScamReportEntity(
                title = "UPI Fraud ₹$amount via $upi",
                amount = amount,
                upiId = upi,
                utrNumber = activeReportUtr.value,
                txnId = activeReportTxn.value,
                suspectContact = activeSuspectContact.value,
                status = "CONFIRMED",
                complaintNumber = activeComplaintNumber.value,
                evidenceHashes = "SHA256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            )
            repository.insertReport(report)
        }

        speakText("तुरंत 1930 पर कॉल कीजिए। ट्रांज़ैक्शन आईडी और यूटीआर सामने रखिए।")
        navigateTo(SatarkScreen.EMERGENCY_CONFIRM)
    }

    // --- SafePay Pre-Payment Broker Actions ---
    fun setSafePayUpi(upi: String) {
        safePayUpi.value = upi.trim()
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

    fun toggleSafePayActiveCall() {
        safePayActiveCall.value = !safePayActiveCall.value
        evaluateSafePay()
    }

    fun setSafePaySnippet(snippet: String) {
        safePaySnippet.value = snippet
        evaluateSafePay()
    }

    fun checkPayeeLedgerAndEvaluate() {
        viewModelScope.launch {
            val clean = safePayUpi.value.lowercase().trim()
            val payee = repository.getPayee(clean)
            if (payee != null) {
                safePayIsFirstTime.value = payee.paymentCount <= 0
                safePayTxnCount.value = payee.paymentCount
                safePayTrusted.value = payee.isTrustVerified
                if (safePayName.value.isBlank() && payee.payeeName.isNotBlank()) {
                    safePayName.value = payee.payeeName
                }
            } else {
                safePayIsFirstTime.value = true
                safePayTxnCount.value = 0
                safePayTrusted.value = false
            }
            evaluateSafePay()
        }
    }

    fun evaluateSafePay() {
        val hasRat = scannedApps.value.any { it.riskLevel == SensitiveRiskLevel.HIGH_RISK }
        val eval = UnifiedAttackChainEngine.evaluate(
            payeeUpi = safePayUpi.value,
            payeeName = safePayName.value,
            amount = safePayAmount.value,
            isFirstTimePayee = safePayIsFirstTime.value,
            knownPayeeTxnCount = safePayTxnCount.value,
            isTrustVerified = safePayTrusted.value,
            sourceChannel = safePayChannel.value,
            isActiveCall = safePayActiveCall.value,
            chatTextSnippet = safePaySnippet.value,
            hasRatOrAccessibilityTool = hasRat,
            highRiskAppsCount = permissionSummary.value.highRiskAppsCount
        )
        safePayEvaluation.value = eval
    }

    fun loadSafePayPreset(
        upi: String,
        name: String,
        amount: Long,
        channel: PaymentSourceChannel,
        activeCall: Boolean,
        snippet: String
    ) {
        safePayUpi.value = upi
        safePayName.value = name
        safePayAmount.value = amount
        safePayChannel.value = channel
        safePayActiveCall.value = activeCall
        safePaySnippet.value = snippet
        checkPayeeLedgerAndEvaluate()
    }

    fun onQrScannedResult(parsed: com.example.util.ParsedUpiData) {
        if (parsed.upiId.isNotBlank()) {
            safePayUpi.value = parsed.upiId
        }
        if (parsed.payeeName.isNotBlank()) {
            safePayName.value = parsed.payeeName
        }
        if (parsed.amount != null && parsed.amount > 0) {
            safePayAmount.value = parsed.amount
        }
        safePayChannel.value = PaymentSourceChannel.DIRECT_SHOP_QR
        checkPayeeLedgerAndEvaluate()
        navigateTo(SatarkScreen.SAFEPAY)
    }

    fun markCurrentPayeeTrusted() {
        viewModelScope.launch {
            repository.setPayeeTrustStatus(safePayUpi.value.lowercase().trim(), true)
            safePayTrusted.value = true
            evaluateSafePay()
            speakText("इस खाते को सत्यापित सूची में जोड़ दिया गया है।")
        }
    }

    fun executeSafePayment(context: android.content.Context) {
        val eval = safePayEvaluation.value
        if (eval?.blockPayment == true) {
            speakText("चेतावनी! सुरक्षा खतरे के कारण यह भुगतान रोका गया है। कृपया पहले सुरक्षा निर्देशों का पालन करें।")
            return
        }

        viewModelScope.launch {
            repository.recordSuccessfulPayment(
                upiId = safePayUpi.value.lowercase().trim(),
                name = safePayName.value,
                amount = safePayAmount.value,
                riskVerdict = eval?.riskTier?.name ?: "SAFE"
            )
            checkPayeeLedgerAndEvaluate()
        }

        try {
            val encodedName = android.net.Uri.encode(safePayName.value)
            val upiUri = android.net.Uri.parse("upi://pay?pa=${safePayUpi.value}&pn=$encodedName&am=${safePayAmount.value}&cu=INR")
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, upiUri).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = android.content.Intent.createChooser(intent, "Pay via UPI App").apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
            speakText("पेमेंट ऐप खोला जा रहा है। सावधानी बरतें।")
        } catch (e: Exception) {
            speakText("फोन में कोई UPI पेमेंट ऐप नहीं मिला। विवरण जांचें।")
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        coolingJob?.cancel()
        emergencyClockJob?.cancel()
    }
}
