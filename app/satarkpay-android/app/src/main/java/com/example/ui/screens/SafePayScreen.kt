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
    onScanQr: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val win = rememberWindowSizeInfo()

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
                        text = "Unified Attack Chain & Payee Ledger",
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
            text = "TEST ATTACK CHAIN SCENARIOS:",
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
                        "Urgent verification security deposit under CBI Mumbai arrest warrant. Do not disconnect call."
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
                        "Prepaid task recharge ₹5,000 for ₹7,500 daily guaranteed bonus."
                    )
                }
            )
            PresetChip(
                label = "🛒 Local Grocery (Safe)",
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
            }
        }

        // Payee Ledger Card (First-time Payee vs Known Payee)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
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
                        Text(
                            text = if (isFirstTimePayee) "⚠️ FIRST-TIME BENEFICIARY" else "🟢 VERIFIED BENFICIARY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isFirstTimePayee) SatarkWarn else SatarkOk
                        )
                        Text(
                            text = if (isFirstTimePayee)
                                "Zero past transactions recorded with this UPI ID."
                            else
                                "$txnCount previous successful payments recorded in local ledger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkInk
                        )
                    }
                }

                if (isFirstTimePayee) {
                    OutlinedButton(
                        onClick = onMarkTrusted,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Save as Known", color = SatarkAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                    text = "Payment Context & Attack Triggers",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SatarkInk
                )

                // Active Call Switch
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
                                    text = if (isActiveCall) "Ongoing Voice / Video Call Active" else "No Active Call",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActiveCall) SatarkDanger else SatarkInk
                                )
                                Text(
                                    text = if (isActiveCall) "Digital Arrest coercion indicator triggered" else "Normal background state",
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
                Text("Payment Request Origin / Channel:", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
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
            }
        }

        // EXPLAINABLE VERDICT CARD
        evaluation?.let { eval ->
            val isDanger = eval.riskTier == AttackChainRiskTier.CRITICAL_BLOCKED
            val isWarning = eval.riskTier == AttackChainRiskTier.HIGH_RISK || eval.riskTier == AttackChainRiskTier.CAUTION
            val tierColor = when {
                isDanger -> SatarkDanger
                isWarning -> SatarkWarn
                else -> SatarkOk
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("attack_chain_verdict_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isDanger -> SatarkDangerAlpha
                        isWarning -> SatarkWarnAlpha
                        else -> SatarkOkAlpha
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (eval.riskTier) {
                                    AttackChainRiskTier.CRITICAL_BLOCKED -> "🔴 CRITICAL THREAT DETECTED"
                                    AttackChainRiskTier.HIGH_RISK -> "🟠 HIGH SCAM RISK"
                                    AttackChainRiskTier.CAUTION -> "🟡 VERIFICATION REQUIRED"
                                    AttackChainRiskTier.LOW_SAFE -> "🟢 SAFE BENEFICIARY"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = tierColor
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = tierColor,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${eval.score}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = eval.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SatarkInk
                    )

                    // Audio Readout Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SatarkPanel,
                        modifier = Modifier.fillMaxWidth().clickable { onSpeakText(eval.englishVoiceSummary) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Listen: ${eval.englishVoiceSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SatarkAccent,
                                maxLines = 2
                            )
                        }
                    }

                    // Action Buttons
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
                                    "Disconnect Call & Report to 1930 Cyber Cell",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
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
                                    Text("15m Cooling Pause", color = Color.White, fontWeight = FontWeight.Bold)
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
                                    Text("Verified • Pay ₹$amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = onMarkTrusted,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Trust", color = SatarkAccent, fontWeight = FontWeight.Bold)
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
                                    "Proceed to UPI App (Pay ₹$amount)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
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
