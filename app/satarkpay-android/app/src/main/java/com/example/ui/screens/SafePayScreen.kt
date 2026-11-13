package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import com.example.thehro.Decision
import com.example.ui.PendingSafePayment
import com.example.ui.theme.*

private val VPA_PATTERN = Regex("^[A-Za-z0-9][A-Za-z0-9._-]{1,99}@[A-Za-z0-9][A-Za-z0-9.-]{1,31}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafePayScreen(
    upiId: String,
    onUpiChange: (String) -> Unit,
    payeeName: String,
    onNameChange: (String) -> Unit,
    amount: Long,
    onAmountChange: (Long) -> Unit,
    sourceChannel: PaymentSourceChannel,
    onChannelChange: (PaymentSourceChannel) -> Unit,
    isActiveCall: Boolean?,
    onSetActiveCall: (Boolean?) -> Unit,
    activeScreenControl: Boolean?,
    onSetActiveScreenControl: (Boolean?) -> Unit,
    decision: Decision?,
    demoMode: Boolean,
    pendingPayment: PendingSafePayment?,
    paymentAppReturned: Boolean,
    baselineP95: Double?,
    holdSeconds: Int,
    holdActive: Boolean,
    isFirstTimePayee: Boolean,
    txnCount: Int,
    isTrusted: Boolean,
    onMarkTrusted: () -> Unit,
    highRiskAppsCount: Int,
    isScanningApps: Boolean,
    onExecutePayment: (android.content.Context, Boolean) -> Unit,
    onTriggerEmergency: () -> Unit,
    onOpenTrustedContact: () -> Unit,
    onStartCooling: () -> Unit,
    onLoadPreset: (String, String, Long, PaymentSourceChannel, Boolean, Boolean?) -> Unit,
    onClearDemo: () -> Unit,
    onConfirmPaymentReportedSuccess: () -> Unit,
    onCancelPendingPayment: () -> Unit,
    onSpeakText: (String) -> Unit,
    onScanQr: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val win = rememberWindowSizeInfo()
    val paymentDetailsValid = VPA_PATTERN.matches(upiId.trim()) && amount > 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(horizontal = win.contentPadding, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Nav Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
                }
                Column {
                    Text(
                        text = "SafePay Pre-Payment Broker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SatarkInk,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "On-device metadata checks · unknown shown",
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkDim,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SatarkOkAlpha
            ) {
                Text(
                    text = "ON-DEVICE SANDBOX",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SatarkOk,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Demo Presets
        Text(
            text = "SIMULATION ONLY — UPI HANDOFF DISABLED",
            style = MaterialTheme.typography.labelSmall,
            color = SatarkDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip(
                label = "🚨 Digital Arrest (CBI)",
                isSelected = upiId.contains("cbi", ignoreCase = true),
                onClick = {
                    onLoadPreset(
                        "cybercbi91@okhdfcbank",
                        "CBI Security Cell",
                        25000L,
                        PaymentSourceChannel.WHATSAPP_UNSAVED,
                        true,
                        true
                    )
                }
            )
            PresetChip(
                label = "📲 Telegram Task Scam",
                isSelected = upiId.contains("task", ignoreCase = true) || upiId.contains("vip", ignoreCase = true),
                onClick = {
                    onLoadPreset(
                        "vipmerchant.task@paytm",
                        "VIP Task Merchant",
                        5000L,
                        PaymentSourceChannel.TELEGRAM,
                        false,
                        null
                    )
                }
            )
            PresetChip(
                label = "🛒 Grocery sample",
                isSelected = upiId.contains("kirana", ignoreCase = true),
                onClick = {
                    onLoadPreset(
                        "sharma.kirana@icici",
                        "Sharma General Store",
                        450L,
                        PaymentSourceChannel.DIRECT_SHOP_QR,
                        false,
                        false
                    )
                }
            )
        }

        if (demoMode) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SatarkWarnAlpha),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Simulation data only. It cannot open a real payment app.",
                        color = SatarkInk,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearDemo) { Text("Clear demo") }
                }
            }
        }

        // Payment Beneficiary Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Beneficiary Details (VPA & Amount)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SatarkInk
                )

                OutlinedTextField(
                    value = upiId,
                    onValueChange = onUpiChange,
                    label = { Text("Receiver UPI ID (VPA)") },
                    placeholder = { Text("e.g. name@okhdfcbank") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SatarkAccent) },
                    trailingIcon = {
                        IconButton(onClick = onScanQr, modifier = Modifier.testTag("safepay_scan_qr_button")) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = SatarkAccent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("safepay_upi_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = payeeName,
                        onValueChange = onNameChange,
                        label = { Text("Account Holder Name") },
                        placeholder = { Text("Optional name") },
                        modifier = Modifier.weight(1.3f).testTag("safepay_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = if (amount > 0) amount.toString() else "",
                        onValueChange = { str ->
                            val parsed = str.filter { it.isDigit() }.toLongOrNull() ?: 0L
                            onAmountChange(parsed)
                        },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("safepay_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                if (!paymentDetailsValid && !demoMode) {
                    Text("Enter a valid UPI ID and an amount greater than ₹0 to continue.", color = SatarkWarn, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Payee Ledger Card (First-time Payee vs Known Payee)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isTrusted) SatarkAccentAlpha else if (isFirstTimePayee) SatarkWarnAlpha else SatarkOkAlpha
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (isTrusted) SatarkAccent else if (isFirstTimePayee) SatarkWarn else SatarkOk)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isTrusted) SatarkAccent else if (isFirstTimePayee) SatarkWarn else SatarkOk,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isTrusted) Icons.Default.VerifiedUser else if (isFirstTimePayee) Icons.Default.PersonAdd else Icons.Default.History,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = when {
                                isTrusted -> "YOU MARKED THIS PAYEE TRUSTED"
                                isFirstTimePayee -> "NO LOCAL PAYMENT HISTORY"
                                else -> "LOCAL REPORTED HISTORY"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isTrusted) SatarkAccent else if (isFirstTimePayee) SatarkWarn else SatarkOk
                        )
                        Text(
                            text = when {
                                isTrusted -> "Your preference does not override confirmed scam or takeover signals."
                                isFirstTimePayee -> "No user-reported payments to this VPA in the last 90 days; new does not mean scam."
                                else -> "$txnCount user-reported completions in the last 90 days; not bank-verified."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkInk
                        )
                    }
                }

                if (!isTrusted && !demoMode) {
                    OutlinedButton(
                        onClick = onMarkTrusted,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Mark trusted", color = SatarkAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Communication & Device Context
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Payment Context (some fields are not scored)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SatarkInk
                )

                Text("Call status (unknown unless explicitly observed):", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = isActiveCall == null, onClick = { onSetActiveCall(null) }, label = { Text("Unknown") })
                    FilterChip(selected = isActiveCall == false, onClick = { onSetActiveCall(false) }, label = { Text("No") })
                    FilterChip(selected = isActiveCall == true, onClick = { onSetActiveCall(true) }, label = { Text("Yes") })
                }
                Text(
                    text = when (isActiveCall) {
                        null -> "This device cannot verify call state; it is not treated as 'no call'."
                        false -> "Call marked inactive for this check."
                        true -> "Call marked active; this alone does not prove a scam."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )

                Text("Active screen control (your observation; not auto-detected):", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = activeScreenControl == null, onClick = { onSetActiveScreenControl(null) }, label = { Text("Unknown") })
                    FilterChip(selected = activeScreenControl == false, onClick = { onSetActiveScreenControl(false) }, label = { Text("No") })
                    FilterChip(selected = activeScreenControl == true, onClick = { onSetActiveScreenControl(true) }, label = { Text("Yes") })
                }
                Text(
                    text = when (activeScreenControl) {
                        null -> "This is unknown unless you can directly observe remote screen sharing/control."
                        false -> "You report no active remote screen control."
                        true -> "You report active remote screen control; payment is held."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (activeScreenControl == true) SatarkDanger else SatarkDim
                )

                // Channel Selector
                Text("Origin noted by you (not scored in this version):", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChannelChip(
                        label = "WhatsApp",
                        isSelected = sourceChannel == PaymentSourceChannel.WHATSAPP_UNSAVED,
                        onClick = { onChannelChange(PaymentSourceChannel.WHATSAPP_UNSAVED) },
                        modifier = Modifier.weight(1f)
                    )
                    ChannelChip(
                        label = "Telegram",
                        isSelected = sourceChannel == PaymentSourceChannel.TELEGRAM,
                        onClick = { onChannelChange(PaymentSourceChannel.TELEGRAM) },
                        modifier = Modifier.weight(1f)
                    )
                    ChannelChip(
                        label = "SMS Alert",
                        isSelected = sourceChannel == PaymentSourceChannel.UNKNOWN_SMS,
                        onClick = { onChannelChange(PaymentSourceChannel.UNKNOWN_SMS) },
                        modifier = Modifier.weight(1f)
                    )
                    ChannelChip(
                        label = "Shop POS",
                        isSelected = sourceChannel == PaymentSourceChannel.DIRECT_SHOP_QR,
                        onClick = { onChannelChange(PaymentSourceChannel.DIRECT_SHOP_QR) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = if (isScanningApps) "Checking installed apps…" else
                        "Installed high-risk apps: $highRiskAppsCount. Installation alone is not active screen control.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
                Text(
                    text = baselineP95?.let { "Personal 90-day P95 baseline: ₹${it.toLong()}" }
                        ?: "Personal amount baseline unavailable (needs 20 user-reported payment completions).",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
            }
        }

        ThehroDecisionCard(
            decision = decision,
            demoMode = demoMode,
            pendingPayment = pendingPayment,
            paymentAppReturned = paymentAppReturned,
            holdSeconds = holdSeconds,
            holdActive = holdActive,
            amount = amount,
            paymentDetailsValid = paymentDetailsValid,
            onExecutePayment = { confirmed -> onExecutePayment(context, confirmed) },
            onStartHold = onStartCooling,
            onOpenReport = onTriggerEmergency,
            onOpenTrustedContact = onOpenTrustedContact,
            onConfirmReportedSuccess = onConfirmPaymentReportedSuccess,
            onCancelPending = onCancelPendingPayment,
            onSpeak = onSpeakText
        )

    }
}

@Composable
private fun ThehroDecisionCard(
    decision: Decision?,
    demoMode: Boolean,
    pendingPayment: PendingSafePayment?,
    paymentAppReturned: Boolean,
    holdSeconds: Int,
    holdActive: Boolean,
    amount: Long,
    paymentDetailsValid: Boolean,
    onExecutePayment: (Boolean) -> Unit,
    onStartHold: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenTrustedContact: () -> Unit,
    onConfirmReportedSuccess: () -> Unit,
    onCancelPending: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val tier = decision?.tier ?: "WAIT"
    val color = when (tier) {
        "T3", "T4" -> SatarkDanger
        "T1", "T2" -> SatarkWarn
        "WAIT" -> SatarkDim
        else -> SatarkAccent
    }
    val background = when (tier) {
        "T3", "T4" -> SatarkDangerAlpha
        "T1", "T2" -> SatarkWarnAlpha
        else -> SatarkPanelCard
    }
    val unknownSignals = decision?.unknownSignals.orEmpty()
    val title = when (tier) {
        "T0" -> "No high-risk pattern confirmed"
        "T1" -> "Caution — verify recipient"
        "T2" -> "One safety check required"
        "T3" -> "Payment held — pause and recheck"
        "T4" -> "Payment held — stronger safety signal"
        else -> "Updating current recipient details…"
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("thehro_decision_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("THEHRO · $tier", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
                    Text(title, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.ExtraBold)
                }
                decision?.let {
                    Surface(shape = RoundedCornerShape(8.dp), color = color) {
                        Text(
                            "Score ${"%.2f".format(it.p)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text("Score is a heuristic, not a probability or a bank verification.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
            Text(decision?.reason ?: "The current recipient's local history is being checked. Payment is disabled until THEHRO finishes.", style = MaterialTheme.typography.bodyMedium, color = SatarkInk)

            if (tier == "T2") {
                Text(
                    "Check: ${decision?.question ?: "Have you independently verified this recipient using a contact method you already trust?"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SatarkInk,
                    fontWeight = FontWeight.Bold
                )
            }
            if (decision != null && decision.evidence.isNotEmpty()) {
                Text("Signals matched: ${decision.evidence.joinToString { it.replace('_', ' ') }}", style = MaterialTheme.typography.bodySmall, color = SatarkInk)
            }
            if (decision != null && decision.floors.isNotEmpty()) {
                Text("Safety rules: ${decision.floors.joinToString()}", style = MaterialTheme.typography.bodySmall, color = SatarkDanger)
            }
            if (unknownSignals.isNotEmpty()) {
                Text(
                    "Not verified (${unknownSignals.size}): ${unknownSignals.take(5).joinToString { it.replace('_', ' ') }}${if (unknownSignals.size > 5) " + ${unknownSignals.size - 5} more" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SatarkPanel,
                modifier = Modifier.fillMaxWidth().clickable { onSpeak(decision?.reason ?: "No decision available.") }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(20.dp))
                    Text("Listen to this explanation", style = MaterialTheme.typography.bodySmall, color = SatarkAccent, fontWeight = FontWeight.Bold)
                }
            }

            if (demoMode) {
                Text("Simulation only — no payment action is available.", color = SatarkWarn, fontWeight = FontWeight.Bold)
            } else if (pendingPayment != null) {
                Text(
                    if (paymentAppReturned) "You returned from the UPI handoff for ${pendingPayment.upiId}. SatarkPay still cannot see the bank result."
                    else "UPI handoff opened for ${pendingPayment.upiId}. Return to SatarkPay after closing the UPI app to report what happened.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkInk
                )
                if (paymentAppReturned) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onConfirmReportedSuccess, modifier = Modifier.weight(1.2f)) {
                            Text("I completed it (self-report)", fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = onCancelPending, modifier = Modifier.weight(1f)) {
                            Text("Not completed", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                when (tier) {
                    "T0", "T1" -> Button(
                        onClick = { onExecutePayment(false) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("safepay_proceed_btn"),
                        enabled = paymentDetailsValid && decision != null,
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Text(if (tier == "T1") "I checked the recipient — continue" else "Continue to UPI app · ₹$amount", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    "T2" -> Button(
                        onClick = { onExecutePayment(true) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("safepay_question_confirm_btn"),
                        enabled = paymentDetailsValid && decision != null,
                        colors = ButtonDefaults.buttonColors(containerColor = SatarkWarn)
                    ) {
                        Text("I checked independently — continue", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    "T3" -> {
                        Text("The 60-second pause never auto-approves payment. Recheck the call and recipient; payment stays held while T3 persists.", style = MaterialTheme.typography.bodySmall, color = SatarkInk)
                        Button(onClick = onStartHold, enabled = !holdActive, modifier = Modifier.fillMaxWidth()) {
                            Text(if (holdActive) "Pause active · ${holdSeconds}s" else "Start 60-second pause")
                        }
                        OutlinedButton(onClick = onOpenReport, modifier = Modifier.fillMaxWidth()) { Text("Open dialer for 1930 · no call placed") }
                    }
                    "T4" -> {
                        Text("Nothing is sent or called automatically. You choose whether to contact someone you trust or open 1930 in the dialer.", style = MaterialTheme.typography.bodySmall, color = SatarkInk)
                        Button(onClick = onOpenTrustedContact, modifier = Modifier.fillMaxWidth()) {
                            Text("Open phone dialer to choose a trusted contact")
                        }
                        OutlinedButton(onClick = onOpenReport, modifier = Modifier.fillMaxWidth()) { Text("Open dialer for 1930 · no call placed") }
                    }
                    else -> Text("Current decision is updating. Wait before continuing.", color = SatarkDim)
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) SatarkAccentAlpha else SatarkPanelCard,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) SatarkAccent else SatarkBorder)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) SatarkAccent else SatarkInk,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun ChannelChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) SatarkAccentAlpha else SatarkBg,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) SatarkAccent else SatarkBorder)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) SatarkAccent else SatarkInk
            )
        }
    }
}
