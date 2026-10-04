package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.SatarkScreen
import com.example.ui.components.TopBarHeader
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val guardOn by viewModel.guardOn.collectAsStateWithLifecycle()
            val voiceHindiOn by viewModel.voiceHindiOn.collectAsStateWithLifecycle()
            val seniorModeOn by viewModel.seniorModeOn.collectAsStateWithLifecycle()
            val maskPiiOn by viewModel.maskPiiOn.collectAsStateWithLifecycle()

            // State from ViewModel
            val isCoolingActive by viewModel.isCoolingActive.collectAsStateWithLifecycle()
            val coolingSeconds by viewModel.coolingTimerSeconds.collectAsStateWithLifecycle()
            val burstScreenshots by viewModel.burstScreenshotsCount.collectAsStateWithLifecycle()
            val approvedIntel by viewModel.approvedIntel.collectAsStateWithLifecycle()
            val reviewQueueIntel by viewModel.reviewQueueIntel.collectAsStateWithLifecycle()
            val mandates by viewModel.mandates.collectAsStateWithLifecycle()
            val isContactSaved by viewModel.isContactSaved.collectAsStateWithLifecycle()
            val chatDwellMinutes by viewModel.chatDwellMinutes.collectAsStateWithLifecycle()
            val repeatPayeeCount by viewModel.repeatPayeeCount.collectAsStateWithLifecycle()
            val recentPayeeUpi by viewModel.recentPayeeUpi.collectAsStateWithLifecycle()
            val currentDomainUrl by viewModel.domainInput.collectAsStateWithLifecycle()
            val domainAnalysisResult by viewModel.domainAnalysisResult.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
            val isAudioRecording by viewModel.isAudioRecording.collectAsStateWithLifecycle()
            val enableThinking by viewModel.enableHighThinking.collectAsStateWithLifecycle()
            val enableSearchGrounding by viewModel.enableSearchGrounding.collectAsStateWithLifecycle()
            val enableMapsGrounding by viewModel.enableMapsGrounding.collectAsStateWithLifecycle()
            val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
            val lastVerdict by viewModel.lastVerdict.collectAsStateWithLifecycle()
            val redactionCount by viewModel.clientRedactionCount.collectAsStateWithLifecycle()

            val goldenHourSeconds by viewModel.goldenHourSeconds.collectAsStateWithLifecycle()
            val stepEmailSent by viewModel.stepEmailSent.collectAsStateWithLifecycle()
            val stepCallHelplineDone by viewModel.stepCallHelplineDone.collectAsStateWithLifecycle()
            val stepStopPaymentDone by viewModel.stepStopPaymentDone.collectAsStateWithLifecycle()
            val stepFamilyAlertSent by viewModel.stepFamilyAlertSent.collectAsStateWithLifecycle()
            val reportAmount by viewModel.activeReportAmount.collectAsStateWithLifecycle()
            val reportUpi by viewModel.activeReportUpi.collectAsStateWithLifecycle()
            val reportUtr by viewModel.activeReportUtr.collectAsStateWithLifecycle()
            val reportTxn by viewModel.activeReportTxn.collectAsStateWithLifecycle()
            val suspectContact by viewModel.activeSuspectContact.collectAsStateWithLifecycle()
            val complaintNumber by viewModel.activeComplaintNumber.collectAsStateWithLifecycle()

            // SafePay Pre-Payment Broker State
            val safePayUpi by viewModel.safePayUpi.collectAsStateWithLifecycle()
            val safePayName by viewModel.safePayName.collectAsStateWithLifecycle()
            val safePayAmount by viewModel.safePayAmount.collectAsStateWithLifecycle()
            val safePayChannel by viewModel.safePayChannel.collectAsStateWithLifecycle()
            val safePayActiveCall by viewModel.safePayActiveCall.collectAsStateWithLifecycle()
            val safePaySnippet by viewModel.safePaySnippet.collectAsStateWithLifecycle()
            val safePayEvaluation by viewModel.safePayEvaluation.collectAsStateWithLifecycle()
            val safePayIsFirstTime by viewModel.safePayIsFirstTime.collectAsStateWithLifecycle()
            val safePayTxnCount by viewModel.safePayTxnCount.collectAsStateWithLifecycle()
            val safePayTrusted by viewModel.safePayTrusted.collectAsStateWithLifecycle()
            val allPayees by viewModel.allPayees.collectAsStateWithLifecycle()

            // App Security & Permission State
            val scannedApps by viewModel.scannedApps.collectAsStateWithLifecycle()
            val permissionSummary by viewModel.permissionSummary.collectAsStateWithLifecycle()
            val isScanningApps by viewModel.isScanningApps.collectAsStateWithLifecycle()

            // Back handler for custom state navigation
            BackHandler(enabled = currentScreen != SatarkScreen.HOME) {
                viewModel.navigateBack()
            }

            MyApplicationTheme(
                darkTheme = false,
                seniorMode = seniorModeOn
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SatarkBg),
                    topBar = {
                        if (currentScreen != SatarkScreen.SPLASH) {
                            TopBarHeader(
                                guardOn = guardOn,
                                onToggleGuard = { viewModel.toggleGuard() },
                                voiceHindiOn = voiceHindiOn,
                                onToggleVoice = { viewModel.toggleVoice() },
                                seniorModeOn = seniorModeOn,
                                onToggleSenior = { viewModel.toggleSenior() },
                                maskPiiOn = maskPiiOn,
                                onToggleMask = { viewModel.toggleMask() },
                                onOpenSettings = { viewModel.navigateTo(SatarkScreen.SETTINGS) }
                            )
                        }
                    },
                    bottomBar = {
                        if (currentScreen != SatarkScreen.SPLASH) {
                            NavigationBar(
                                containerColor = SatarkPanel,
                                tonalElevation = 8.dp
                            ) {
                                val navItems = listOf(
                                    Triple(SatarkScreen.HOME, "होम", Icons.Default.Home),
                                    Triple(SatarkScreen.SAFEPAY, "सुरक्षित पे", Icons.Default.Shield),
                                    Triple(SatarkScreen.SANCHALAK_CHAT, "AI साथी", Icons.Default.Chat),
                                    Triple(SatarkScreen.APP_SECURITY, "ऐप्स", Icons.Default.SecurityUpdateWarning),
                                    Triple(SatarkScreen.REPORT_EVIDENCE, "शिकायत", Icons.Default.FolderZip)
                                )

                                navItems.forEach { (screen, label, icon) ->
                                    val isSelected = currentScreen == screen
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.navigateTo(screen) },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (isSelected) SatarkAccent else SatarkDim
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                color = if (isSelected) SatarkAccent else SatarkDim
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = SatarkAccentAlpha
                                        ),
                                        modifier = Modifier.testTag("nav_item_$label")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(SatarkBg)
                    ) {
                        when (currentScreen) {
                            SatarkScreen.SPLASH -> SplashScreen(
                                onEnableGuard = {
                                    viewModel.guardOn.value = true
                                    viewModel.navigateTo(SatarkScreen.HOME)
                                },
                                onManualOnly = {
                                    viewModel.guardOn.value = false
                                    viewModel.navigateTo(SatarkScreen.HOME)
                                }
                            )

                            SatarkScreen.HOME -> HomeScreen(
                                isCoolingActive = isCoolingActive,
                                coolingSeconds = coolingSeconds,
                                burstScreenshots = burstScreenshots,
                                intelCount = approvedIntel.size,
                                permissionSummary = permissionSummary,
                                payees = allPayees,
                                onNavigate = { viewModel.navigateTo(it) },
                                onConfirmFraudQuick = {
                                    viewModel.confirmFraudAndTriggerEmergency()
                                }
                            )

                            SatarkScreen.SAFEPAY -> SafePayScreen(
                                upiId = safePayUpi,
                                onUpiChange = { viewModel.setSafePayUpi(it) },
                                payeeName = safePayName,
                                onNameChange = { viewModel.setSafePayName(it) },
                                amount = safePayAmount,
                                onAmountChange = { viewModel.setSafePayAmount(it) },
                                sourceChannel = safePayChannel,
                                onChannelChange = { viewModel.setSafePayChannel(it) },
                                isActiveCall = safePayActiveCall,
                                onToggleActiveCall = { viewModel.toggleSafePayActiveCall() },
                                chatSnippet = safePaySnippet,
                                onSnippetChange = { viewModel.setSafePaySnippet(it) },
                                evaluation = safePayEvaluation,
                                isFirstTimePayee = safePayIsFirstTime,
                                txnCount = safePayTxnCount,
                                isTrusted = safePayTrusted,
                                onMarkTrusted = { viewModel.markCurrentPayeeTrusted() },
                                highRiskAppsCount = permissionSummary.highRiskAppsCount,
                                onExecutePayment = { ctx -> viewModel.executeSafePayment(ctx) },
                                onTriggerEmergency = {
                                    viewModel.confirmFraudAndTriggerEmergency(safePayAmount, safePayUpi)
                                },
                                onStartCooling = {
                                    viewModel.startCoolingTimer()
                                    viewModel.navigateTo(SatarkScreen.CHAT_PAY)
                                },
                                onLoadPreset = { upi, name, amt, ch, call, snip ->
                                    viewModel.loadSafePayPreset(upi, name, amt, ch, call, snip)
                                },
                                onSpeakText = { viewModel.speakText(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.APP_SECURITY -> AppSecurityScreen(
                                scannedApps = scannedApps,
                                summary = permissionSummary,
                                isScanning = isScanningApps,
                                onRescan = { viewModel.scanDeviceApps() },
                                onUninstallApp = { viewModel.uninstallApp(it) },
                                onOpenSettings = { viewModel.openAppSettings(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.CHAT_PAY -> ChatBeforePayScreen(
                                isContactSaved = isContactSaved,
                                onToggleContactSaved = { viewModel.setContactSaved(it) },
                                chatDwellMinutes = chatDwellMinutes,
                                onSetDwellMinutes = { viewModel.setChatDwell(it) },
                                frictionTier = viewModel.getFrictionTier(),
                                coolingSeconds = coolingSeconds,
                                isCoolingActive = isCoolingActive,
                                onStartCooling = { viewModel.startCoolingTimer() },
                                onCancelPayment = { viewModel.cancelPayment() },
                                onProceedPayment = {
                                    viewModel.speakText("पेमेंट सत्यापित हो गया है।")
                                    viewModel.navigateTo(SatarkScreen.HOME)
                                },
                                onSpeakPrompt = { viewModel.speakText(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.SCREENSHOT_RADAR -> ScreenshotRadarScreen(
                                burstCount = burstScreenshots,
                                repeatCount = repeatPayeeCount,
                                recentPayeeUpi = recentPayeeUpi,
                                maskPiiOn = maskPiiOn,
                                onSimulatePayment = {
                                    viewModel.simulateAddScreenshot()
                                },
                                onSpeakPrompt = { viewModel.speakText(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.DOMAIN_TRUST -> DomainTrustScreen(
                                currentUrl = currentDomainUrl,
                                analysisResult = domainAnalysisResult,
                                onAnalyzeUrl = { viewModel.analyzeCurrentDomain(it) },
                                onSpeakPrompt = { viewModel.speakText(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.SANCHALAK_CHAT -> SanchalakChatScreen(
                                messages = chatMessages,
                                isLoading = isChatLoading,
                                isRecordingAudio = isAudioRecording,
                                enableThinking = enableThinking,
                                onToggleThinking = { viewModel.enableHighThinking.value = it },
                                enableSearchGrounding = enableSearchGrounding,
                                onToggleSearchGrounding = { viewModel.enableSearchGrounding.value = it },
                                enableMapsGrounding = enableMapsGrounding,
                                onToggleMapsGrounding = { viewModel.enableMapsGrounding.value = it },
                                selectedModel = selectedModel,
                                onSelectModel = { viewModel.selectedModel.value = it },
                                lastVerdict = lastVerdict,
                                redactionCount = redactionCount,
                                onSendMessage = { viewModel.sendUserChatMessage(it) },
                                onToggleRecordAudio = { viewModel.toggleAudioRecording() },
                                onSpeakText = { viewModel.speakText(it) },
                                onEscalateToAnalyst = { viewModel.navigateTo(SatarkScreen.ANALYST_CONSOLE) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.WALLET_AUDIT -> WalletAuditScreen(
                                mandates = mandates,
                                onRevokeMandate = { viewModel.revokeMandate(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.INTEL_FEED -> IntelFeedScreen(
                                approvedIntel = approvedIntel,
                                reviewQueueIntel = reviewQueueIntel,
                                onApprove = { viewModel.approveIntelPattern(it) },
                                onReject = { viewModel.rejectIntelPattern(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.REPORT_EVIDENCE -> ReportEvidenceScreen(
                                amount = reportAmount,
                                upiId = reportUpi,
                                utr = reportUtr,
                                txnId = reportTxn,
                                maskPiiOn = maskPiiOn,
                                onToggleMask = { viewModel.toggleMask() },
                                onConfirmFraudTriggerEmergency = {
                                    viewModel.confirmFraudAndTriggerEmergency(reportAmount, reportUpi)
                                },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.EMERGENCY_CONFIRM -> EmergencyActionScreen(
                                goldenHourSeconds = goldenHourSeconds,
                                amount = reportAmount,
                                upiId = reportUpi,
                                utr = reportUtr,
                                txnId = reportTxn,
                                suspectContact = suspectContact,
                                complaintNumber = complaintNumber,
                                stepEmailSent = stepEmailSent,
                                onMarkEmailSent = { viewModel.markEmailSent() },
                                stepCallHelplineDone = stepCallHelplineDone,
                                onMarkCallHelplineDone = { viewModel.markCallHelplineDone() },
                                stepStopPaymentDone = stepStopPaymentDone,
                                onMarkStopPaymentDone = { viewModel.markStopPaymentDone() },
                                stepFamilyAlertSent = stepFamilyAlertSent,
                                onMarkFamilyAlertSent = { viewModel.markFamilyAlertSent() },
                                onSpeakPrompt = { viewModel.speakText(it) },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.GRIEVANCE_LADDER -> GrievanceLadderScreen(
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.SETTINGS -> SettingsScreen(
                                guardOn = guardOn,
                                onToggleGuard = { viewModel.toggleGuard() },
                                voiceHindiOn = voiceHindiOn,
                                onToggleVoice = { viewModel.toggleVoice() },
                                seniorModeOn = seniorModeOn,
                                onToggleSenior = { viewModel.toggleSenior() },
                                maskPiiOn = maskPiiOn,
                                onToggleMask = { viewModel.toggleMask() },
                                onBack = { viewModel.navigateBack() }
                            )

                            SatarkScreen.ANALYST_CONSOLE -> AnalystConsoleScreen(
                                onBack = { viewModel.navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
