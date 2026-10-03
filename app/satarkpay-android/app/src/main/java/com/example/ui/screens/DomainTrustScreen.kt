package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DomainAnalysisResult
import com.example.engine.DomainTrustLevel
import com.example.ui.components.SigRow
import com.example.ui.components.SigTagType
import com.example.ui.theme.*

@Composable
fun DomainTrustScreen(
    currentUrl: String,
    analysisResult: DomainAnalysisResult?,
    onAnalyzeUrl: (String) -> Unit,
    onSpeakPrompt: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var inputUrl by remember { mutableStateOf(currentUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("m3_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "M3 · Domain & Link Trust",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    onSpeakPrompt("यह वेबसाइट नकली लग रही है। कोई भुगतान या संवेदनशील जानकारी न दें।")
                },
                modifier = Modifier.testTag("m3_voice_button")
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speak Warning", tint = SatarkAccent)
            }
        }

        // Search Input Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CHECK ANY PAYMENT LINK / PORTAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = SatarkAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = {
                        inputUrl = it
                        onAnalyzeUrl(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("domain_input_field"),
                    placeholder = { Text("e.g. qr-pay.top or rbi.org.in", color = SatarkDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SatarkInk,
                        unfocusedTextColor = SatarkInk,
                        focusedBorderColor = SatarkAccent,
                        unfocusedBorderColor = SatarkBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        IconButton(onClick = { onAnalyzeUrl(inputUrl) }) {
                            Icon(Icons.Default.Search, contentDescription = "Analyze", tint = SatarkAccent)
                        }
                    }
                )

                // Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("qr-pay.top", "sbi-kyc-verify.xyz", "rbi.org.in", "amazon.in").forEach { chip ->
                        FilterChip(
                            selected = inputUrl == chip,
                            onClick = {
                                inputUrl = chip
                                onAnalyzeUrl(chip)
                            },
                            label = { Text(chip, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SatarkAccentAlpha,
                                selectedLabelColor = SatarkAccent
                            )
                        )
                    }
                }
            }
        }

        // Analysis Result Card
        analysisResult?.let { res ->
            val (badgeText, badgeColor, badgeBg) = when (res.trustLevel) {
                DomainTrustLevel.L1_VERIFIED_GOV_BANK -> Triple("L1 · VERIFIED REGULATOR / BANK", SatarkOk, SatarkOkAlpha)
                DomainTrustLevel.L2_VERIFIED_MERCHANT -> Triple("L2 · VERIFIED MERCHANT", SatarkOk, SatarkOkAlpha)
                DomainTrustLevel.L3_UNVERIFIED_CLEAN -> Triple("L3 · CLEAN UNVERIFIED PORTAL", SatarkWarn, SatarkWarnAlpha)
                DomainTrustLevel.L4_LOOKALIKE_FAKE -> Triple("L4 · LOOKALIKE / FAKE PHISHING", SatarkDanger, SatarkDangerAlpha)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(badgeColor)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(badgeColor))
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelLarge,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = "Domain: ${res.domain}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleMedium,
                        color = SatarkInk,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = res.warningMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SatarkInk
                    )

                    Divider(color = SatarkBorder)

                    // Signals
                    Text(
                        text = "IDENTIFIED TRUST SIGNALS:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SatarkDim,
                        fontWeight = FontWeight.Bold
                    )
                    res.signals.forEach { sig ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (res.trustLevel == DomainTrustLevel.L4_LOOKALIKE_FAKE) Icons.Default.Warning else Icons.Default.Check,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = sig,
                                style = MaterialTheme.typography.bodySmall,
                                color = SatarkDim
                            )
                        }
                    }

                    // Gateway vs Merchant Mismatch Warning Card
                    if (res.hasGatewayMismatch) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SatarkWarnAlpha,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "⚠️ GATEWAY VS MERCHANT MISMATCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SatarkWarn,
                                    fontWeight = FontWeight.Bold
                                )
                                SigRow(label = "Payment Gateway", value = res.gatewayName ?: "Razorpay", tagText = "Verified Gateway", tagType = SigTagType.OK)
                                SigRow(label = "Actual Merchant", value = res.merchantName ?: "quickearn-pro", tagText = "Phishing Merchant", tagType = SigTagType.DANGER)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "“Gateway achha ≠ Merchant achha” — 2026 me scammers legit payment gateways ke peeche fake illegal portals register karte hain.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SatarkInk
                                )
                            }
                        }
                    }

                    // Bottom Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onBack() },
                            colors = ButtonDefaults.buttonColors(containerColor = SatarkDanger),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Koi Payment Mat Karo", color = Color.White)
                        }
                    }
                }
            }
        }

        // Trust Ladder Reference Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "4-LEVEL TRUST LADDER (M3 SPEC)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SatarkDim,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(text = "• L1: Verified Regulator / Bank (rbi.org.in, npci.org.in, sbi.co.in)", style = MaterialTheme.typography.bodySmall, color = SatarkOk)
                Text(text = "• L2: Verified High-Reputation Merchants (amazon.in, flipkart.com)", style = MaterialTheme.typography.bodySmall, color = SatarkOk)
                Text(text = "• L3: Clean Unverified (Normal safety check advised)", style = MaterialTheme.typography.bodySmall, color = SatarkWarn)
                Text(text = "• L4: Lookalike / Fake (.top, .xyz, IP literals, sub-brand spoof)", style = MaterialTheme.typography.bodySmall, color = SatarkDanger)
            }
        }
    }
}
