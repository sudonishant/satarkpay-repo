package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.ScreenshotScanEntity
import com.example.ui.components.SigRow
import com.example.ui.components.SigTagType
import com.example.ui.theme.*

@Composable
fun ScreenshotRadarScreen(
    burstCount: Int,
    repeatCount: Int,
    recentPayeeUpi: String,
    maskPiiOn: Boolean,
    onSimulatePayment: () -> Unit,
    onSpeakPrompt: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScanForDetail by remember { mutableStateOf<ScreenshotScanEntity?>(null) }

    // Mock initial scan list if empty
    val sampleScans = remember {
        mutableStateListOf(
            ScreenshotScanEntity(
                id = 1,
                filename = "scan_0938_qr.png",
                sourceDomain = "shop-qr.top",
                payeeUpi = "7349123456@ptaxis",
                amount = 3000,
                riskLevel = "L4_FAKE",
                isBurst = true,
                sha256Hash = "8a3e7b1029df49a883726bfca00192e"
            ),
            ScreenshotScanEntity(
                id = 2,
                filename = "scan_0931_pay.png",
                sourceDomain = "qr-pay.top",
                payeeUpi = "7349123456@ptaxis",
                amount = 1500,
                riskLevel = "L4_FAKE",
                isBurst = true,
                sha256Hash = "b4918237fa90123cb8291048ca09182"
            ),
            ScreenshotScanEntity(
                id = 3,
                filename = "scan_0927_amazon.png",
                sourceDomain = "amazon.in",
                payeeUpi = "amazonpay@apl",
                amount = 450,
                riskLevel = "L2_VERIFIED_MERCHANT",
                isBurst = false,
                sha256Hash = "c78291038ba9810237fa0918274bb01"
            )
        )
    }

    val displayUpi = if (maskPiiOn && recentPayeeUpi.length > 6) {
        "${recentPayeeUpi.take(4)}••••••@${recentPayeeUpi.substringAfter('@')}"
    } else recentPayeeUpi

    val isStaircaseEscalated = repeatCount >= 3

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("m2_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "M2 · Screenshot Radar",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    onSpeakPrompt("तीस मिनट में तीन पेमेंट स्क्रीनशॉट मिले हैं। रुकिए, यह धोखे का पैटर्न है।")
                },
                modifier = Modifier.testTag("m2_voice_button")
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speak Warning", tint = SatarkAccent)
            }
        }

        // Burst Indicator Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = if (burstCount >= 3) SatarkWarnAlpha else SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (burstCount >= 3) SatarkWarn else SatarkBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (burstCount >= 3) SatarkWarn else SatarkOk
                    )
                    Column {
                        Text(
                            text = "📸 $burstCount Payment Screenshots (30 min)",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (burstCount >= 3) SatarkWarn else SatarkInk,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (burstCount >= 3) "⚠️ BURST DETECTED: Task/deposit fraud frequency" else "Normal screenshot frequency",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim
                        )
                    }
                }
            }
        }

        // Payee Staircase Card (R22 Repeat Guard)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (isStaircaseEscalated) SatarkDanger else SatarkBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAYEE STAIRCASE GUARD (R22)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isStaircaseEscalated) SatarkDanger else SatarkAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isStaircaseEscalated) SatarkDangerAlpha else SatarkOkAlpha
                    ) {
                        Text(
                            text = if (isStaircaseEscalated) "3rd+ Payment: COOLING HOLD" else "Safe Frequency",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isStaircaseEscalated) SatarkDanger else SatarkOk,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Payee: $displayUpi",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SatarkInk,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$repeatCount payments recorded in last 24h. Scam syndicates demand small ₹1,500, then ₹3,000, then ₹15,000 staircase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
            }
        }

        // Actions row: Simulate next payment burst + Privacy flush
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onSimulatePayment()
                    sampleScans.add(
                        0,
                        ScreenshotScanEntity(
                            id = System.currentTimeMillis(),
                            filename = "scan_recent_${System.currentTimeMillis()}.png",
                            sourceDomain = "fast-reward-qr.top",
                            payeeUpi = "7349123456@ptaxis",
                            amount = 5000,
                            riskLevel = "L4_FAKE",
                            isBurst = true,
                            sha256Hash = "d92182048aa0192384fab10293847aa"
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("simulate_payment_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Simulate Agla Pay", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { sampleScans.clear() },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("clear_screenshots_button"),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkDim))
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = SatarkDim, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Privacy Flush", color = SatarkDim, style = MaterialTheme.typography.labelMedium)
            }
        }

        Text(
            text = "RECENT INGESTED SCREENSHOTS",
            style = MaterialTheme.typography.labelSmall,
            color = SatarkDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Screenshot List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sampleScans) { scan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedScanForDetail = scan }
                        .testTag("screenshot_item_${scan.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SatarkBorder,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = SatarkDim, modifier = Modifier.size(24.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = scan.sourceDomain,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SatarkInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${scan.amount} • ${scan.filename}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SatarkDim
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (scan.riskLevel == "L4_FAKE") SatarkDangerAlpha else SatarkOkAlpha
                        ) {
                            Text(
                                text = if (scan.riskLevel == "L4_FAKE") "🔴 L4 Fake" else "🟢 L2 Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (scan.riskLevel == "L4_FAKE") SatarkDanger else SatarkOk,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog (S3b)
    selectedScanForDetail?.let { scan ->
        AlertDialog(
            onDismissRequest = { selectedScanForDetail = null },
            title = {
                Text(
                    text = "S3b · Screenshot Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    color = SatarkInk,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SigRow(label = "Source Domain", value = scan.sourceDomain, tagText = if (scan.riskLevel == "L4_FAKE") "Phishing TLD" else "Safe", tagType = if (scan.riskLevel == "L4_FAKE") SigTagType.DANGER else SigTagType.OK)
                    SigRow(label = "Payee UPI", value = if (maskPiiOn) "${scan.payeeUpi.take(4)}••••••" else scan.payeeUpi, tagText = "Masked R28", tagType = SigTagType.INFO)
                    SigRow(label = "Amount Demanded", value = "₹${scan.amount}", tagText = "Prepaid", tagType = SigTagType.WARN)
                    SigRow(label = "SHA-256 Provenance", value = scan.sha256Hash.take(16) + "...", tagText = "Evidence Hash", tagType = SigTagType.INFO)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Privacy note: Original image device se delete ho jaati hai, sirf hash aur extracted entity store hoti hai.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkDim,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedScanForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent)
                ) {
                    Text("Theek Hai", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        sampleScans.remove(scan)
                        selectedScanForDetail = null
                    }
                ) {
                    Text("Delete Scan", color = SatarkDanger)
                }
            },
            containerColor = SatarkPanelCard
        )
    }
}
