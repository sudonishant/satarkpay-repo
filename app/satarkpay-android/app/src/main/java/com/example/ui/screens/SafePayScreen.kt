package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.ui.theme.*

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
    isActiveCall: Boolean,
    onToggleActiveCall: () -> Unit,
    chatSnippet: String,
    onSnippetChange: (String) -> Unit,
    evaluation: AttackChainEvaluation?,
    isFirstTimePayee: Boolean,
    txnCount: Int,
    isTrusted: Boolean,
    onMarkTrusted: () -> Unit,
    highRiskAppsCount: Int,
    onExecutePayment: (android.content.Context) -> Unit,
    onTriggerEmergency: () -> Unit,
    onStartCooling: () -> Unit,
    onLoadPreset: (String, String, Long, PaymentSourceChannel, Boolean, String) -> Unit,
    onSpeakText: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Nav & Provenance Header
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SatarkInk)
                }
                Column {
                    Text(
                        text = "SafePay Pre-Payment Broker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SatarkInk
                    )
                    Text(
                        text = "Unified Attack Chain & Payee Ledger",
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkDim
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SatarkOkAlpha,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkOk))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(SatarkOk, CircleShape))
                    Text(
                        text = "LIVE ON DEVICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SatarkOk
                    )
                }
            }
        }

        // Quick Demo Scenarios Row
        Text(
            text = "DEMO ATTACK CHAIN SCENARIOS",
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
                label = "🚨 Digital Arrest",
                isSelected = upiId.contains("cbi", ignoreCase = true),
                onClick = {
                    onLoadPreset(
                        "cybercbi91@okhdfcbank",
                        "CBI Digital Cell",
                        25000L,
                        PaymentSourceChannel.WHATSAPP_UNSAVED,
                        true,
                        "Urgent verification security deposit under CBI Mumbai. Do not disconnect call."
                    )
                }
            )
            PresetChip(
                label = "📲 Telegram Task",
                isSelected = upiId.contains("task", ignoreCase = true) || upiId.contains("vip", ignoreCase = true),
                onClick = {
                    onLoadPreset(
                        "vipmerchant.task@paytm",
                        "VIP Task Merchant",
                        5000L,
                        PaymentSourceChannel.TELEGRAM,
                        false,
                        "Prepaid task recharge ₹5,000 for ₹7,500 withdrawal bonus."
                    )
                }
            )
            PresetChip(
                label = "🛒 Local Kirana",
                isSelected = upiId.contains("kirana", ignoreCase = true),
                onClick = {
                    onLoadPreset(
                        "sharma.kirana@icici",
                        "Sharma General Store",
                        450L,
                        PaymentSourceChannel.DIRECT_SHOP_QR,
                        false,
                        "Milk and groceries payment"
                    )
                }
            )
        }

        // Payee & Payment Input Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Beneficiary Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SatarkInk
                    )
                    Text(
                        text = "[MANUAL / QR]",
                        style = MaterialTheme.typography.labelSmall,
                        color = SatarkDim,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = upiId,
                    onValueChange = onUpiChange,
                    label = { Text("Receiver UPI ID (VPA)") },
                    placeholder = { Text("e.g. name@okhdfcbank") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SatarkAccent) },
                    modifier = Modifier.fillMaxWidth().testTag("safepay_upi_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SatarkAccent,
                        unfocusedBorderColor = SatarkBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = payeeName,
                        onValueChange = onNameChange,
                        label = { Text("Payee Name") },
                        placeholder = { Text("Account Holder") },
                        modifier = Modifier.weight(1.3f).testTag("safepay_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SatarkAccent,
                            unfocusedBorderColor = SatarkBorder
                        )
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
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SatarkAccent,
                            unfocusedBorderColor = SatarkBorder
                        )
                    )
                }
            }
        }

        // First-Time Payee Ledger Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFirstTimePayee) SatarkWarnAlpha else SatarkOkAlpha
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (isFirstTimePayee) SatarkWarn else SatarkOk)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isFirstTimePayee) SatarkWarn else SatarkOk,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFirstTimePayee) Icons.Default.PersonAdd else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isFirstTimePayee) "FIRST-TIME PAYEE" else "KNOWN BENEFICIARY",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isFirstTimePayee) SatarkWarn else SatarkOk
                            )
                            Surface(shape = RoundedCornerShape(4.dp), color = SatarkBorder) {
                                Text(
                                    text = "LEDGER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SatarkDim,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isFirstTimePayee)
                                "Past transactions: 0 · Unfamiliar VPA"
                            else
                                "Past transactions: $txnCount · Recorded in local ledger",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkInk
                        )
                    }
                }

                if (isFirstTimePayee) {
                    TextButton(onClick = onMarkTrusted) {
                        Text("Trust", color = SatarkAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Context & Communication Correlation
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Correlated Threat Context",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SatarkInk
                    )
                    Surface(shape = RoundedCornerShape(4.dp), color = SatarkBorder) {
                        Text(
                            text = "DEVICE + CHANNEL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatarkDim,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Active Call Toggle (Digital Arrest trigger)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActiveCall) SatarkDangerAlpha else SatarkBg,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isActiveCall) SatarkDanger else SatarkBorder)
                    ),
                    modifier = Modifier.fillMaxWidth().clickable { onToggleActiveCall() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = if (isActiveCall) Icons.Default.PhoneInTalk else Icons.Default.PhoneDisabled,
                                contentDescription = null,
                                tint = if (isActiveCall) SatarkDanger else SatarkDim
                            )
                            Column {
                                Text(
                                    text = if (isActiveCall) "Active Phone / Video Call ON" else "No Active Call",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActiveCall) SatarkDanger else SatarkInk
                                )
                                Text(
                                    text = if (isActiveCall) "Digital Arrest trap indicator active" else "Normal background state",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SatarkDim
                                )
                            }
                        }
                        Switch(
                            checked = isActiveCall,
                            onCheckedChange = { onToggleActiveCall() },
                            colors = SwitchDefaults.colors(checkedThumbColor = SatarkDanger, checkedTrackColor = SatarkDangerAlpha)
                        )
                    }
                }

                // Channel Selector
                Text("Payment Origin / Chat Channel:", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
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
                        label = "SMS",
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

                // Device Scanner Signal info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Device Shield Scan: ${if (highRiskAppsCount > 0) "$highRiskAppsCount High-Risk Apps" else "Clean"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (highRiskAppsCount > 0) SatarkDanger else SatarkOk,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "PM Sandbox",
                        style = MaterialTheme.typography.labelSmall,
                        color = SatarkDim
                    )
                }
            }
        }

        // UNIFIED ATTACK CHAIN VERDICT CARD
        evaluation?.let { eval ->
            val tierColor = when (eval.riskTier) {
                AttackChainRiskTier.LOW_SAFE -> SatarkOk
                AttackChainRiskTier.CAUTION -> SatarkWarn
                AttackChainRiskTier.HIGH_RISK -> SatarkWarn
                AttackChainRiskTier.CRITICAL_BLOCKED -> SatarkDanger
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("attack_chain_verdict_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (eval.riskTier) {
                        AttackChainRiskTier.LOW_SAFE -> SatarkOkAlpha
                        AttackChainRiskTier.CAUTION -> SatarkWarnAlpha
                        AttackChainRiskTier.HIGH_RISK -> SatarkWarnAlpha
                        AttackChainRiskTier.CRITICAL_BLOCKED -> SatarkDangerAlpha
                    }
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(tierColor)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Headline & Score Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = eval.riskTier.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tierColor,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = eval.headline,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SatarkInk
                            )
                        }

                        // Score Dial / Badge
                        Surface(
                            shape = CircleShape,
                            color = tierColor,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${eval.score}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Explanation
                    Text(
                        text = eval.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SatarkInk
                    )

                    // Hindi Voice Prompt Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SatarkPanel)
                            .clickable { onSpeakText(eval.hindiVoiceSummary) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(18.dp))
                            Text(
                                text = "सुनिए (Hindi Voice Advisory)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SatarkAccent
                            )
                        }
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(18.dp))
                    }

                    HorizontalDivider(color = SatarkBorder)

                    // Attack Chain Correlated Signals List
                    Text(
                        text = "CORRELATED ATTACK SIGNALS (${eval.signals.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SatarkDim,
                        letterSpacing = 1.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        eval.signals.forEach { signal ->
                            SignalRow(signal)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ACTION BUTTONS BASED ON RISK TIER
                    when (eval.riskTier) {
                        AttackChainRiskTier.CRITICAL_BLOCKED -> {
                            Button(
                                onClick = onTriggerEmergency,
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("safepay_blocked_action_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SatarkDanger)
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "🚨 Call Kaatein & 1930 Cyber Cell Alert Karein",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        AttackChainRiskTier.HIGH_RISK -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onStartCooling,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SatarkWarn)
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("15m Cooling Active", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onTriggerEmergency,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Report Scam", color = SatarkDanger, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AttackChainRiskTier.CAUTION -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onExecutePayment(context) },
                                    modifier = Modifier.weight(1.2f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SatarkWarn)
                                ) {
                                    Text("Verified & Pay ₹$amount", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onMarkTrusted,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Trust Payee", color = SatarkAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AttackChainRiskTier.LOW_SAFE -> {
                            Button(
                                onClick = { onExecutePayment(context) },
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("safepay_proceed_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SatarkOk)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Proceed to UPI App (₹$amount)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
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
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) SatarkAccent else SatarkInk,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
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

@Composable
private fun SignalRow(signal: AttackChainSignal) {
    val badgeColor = when (signal.severity) {
        AttackChainRiskTier.LOW_SAFE -> SatarkOk
        AttackChainRiskTier.CAUTION -> SatarkWarn
        AttackChainRiskTier.HIGH_RISK -> SatarkWarn
        AttackChainRiskTier.CRITICAL_BLOCKED -> SatarkDanger
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SatarkPanel)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(badgeColor, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = signal.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = SatarkInk
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SatarkBorder
                ) {
                    Text(
                        text = signal.provenance,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SatarkDim,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = signal.description,
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim,
                fontSize = 11.sp
            )
        }
    }
}
