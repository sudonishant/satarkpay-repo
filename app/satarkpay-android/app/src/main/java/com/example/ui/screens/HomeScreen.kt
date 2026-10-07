package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayeeLedgerEntity
import com.example.engine.PermissionAuditSummary
import com.example.ui.SatarkScreen
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    isCoolingActive: Boolean,
    coolingSeconds: Int,
    burstScreenshots: Int,
    intelCount: Int,
    permissionSummary: PermissionAuditSummary,
    payees: List<PayeeLedgerEntity> = emptyList(),
    onNavigate: (SatarkScreen) -> Unit,
    onConfirmFraudQuick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showExtraTools by remember { mutableStateOf(false) }
    val win = rememberWindowSizeInfo()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(horizontal = win.contentPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Cooling Alert (if triggered)
        if (isCoolingActive) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigate(SatarkScreen.CHAT_PAY) }
                    .testTag("active_cooling_banner"),
                shape = RoundedCornerShape(14.dp),
                color = SatarkWarnAlpha,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SatarkWarn)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = SatarkWarn, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Column {
                            Text("Cooling Period Active: ${coolingSeconds}s", fontWeight = FontWeight.Bold, color = SatarkWarn, fontSize = 14.sp)
                            Text("High-risk chat pause active. Avoid hurried transfers.", color = SatarkInk, fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SatarkWarn, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 1. DEVICE PROTECTION STATUS BANNER
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SatarkPanelCard,
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (permissionSummary.highRiskAppsCount > 0) SatarkDangerAlpha else SatarkOkAlpha,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (permissionSummary.highRiskAppsCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (permissionSummary.highRiskAppsCount > 0) SatarkDanger else SatarkOk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (permissionSummary.highRiskAppsCount > 0)
                                "⚠️ ${permissionSummary.highRiskAppsCount} Sensitive Apps Found"
                            else
                                "🟢 Device Protected • Clean",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SatarkInk
                        )
                        Text(
                            text = "Screen-sharing & Remote RAT protection active",
                            fontSize = 11.sp,
                            color = SatarkDim
                        )
                    }
                }

                TextButton(onClick = { onNavigate(SatarkScreen.APP_SECURITY) }) {
                    Text(
                        text = if (permissionSummary.highRiskAppsCount > 0) "Review" else "Scan",
                        color = if (permissionSummary.highRiskAppsCount > 0) SatarkDanger else SatarkAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. HERO: "VERIFY BEFORE YOU PAY" PRE-PAYMENT BROKER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onNavigate(SatarkScreen.SAFEPAY) }
                .testTag("hero_safepay_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SatarkAccent,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column {
                            Text("Verify Before You Pay", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SatarkInk)
                            Text("SafePay • Pre-Payment Scam Prevention", fontSize = 11.sp, color = SatarkDim)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SatarkOkAlpha
                    ) {
                        Text(
                            text = "LIVE SHIELD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatarkOk,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Check any unfamiliar UPI ID or QR code before transferring funds. SatarkPay blocks active call coercion, first-time VPA traps, and unverified chat origins.",
                    fontSize = 12.sp,
                    color = SatarkInk,
                    lineHeight = 17.sp
                )

                // Quick Demo Scenario Chips
                Text("Test Demo Attack Chains:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SatarkDim)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoChip(
                        label = "🚨 Digital Arrest",
                        color = SatarkDanger,
                        onClick = { onNavigate(SatarkScreen.SAFEPAY) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoChip(
                        label = "📲 Telegram Task",
                        color = SatarkWarn,
                        onClick = { onNavigate(SatarkScreen.SAFEPAY) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoChip(
                        label = "🛒 Local Grocery",
                        color = SatarkOk,
                        onClick = { onNavigate(SatarkScreen.SAFEPAY) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Primary CTA button
                Button(
                    onClick = { onNavigate(SatarkScreen.SAFEPAY) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check UPI ID / Scan QR Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 3. CORE SECURITY CONTROLS (2x2 GRID)
        Text(
            text = "SECURITY CONTROLS",
            style = MaterialTheme.typography.labelSmall,
            color = SatarkDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        val hasAppsRisk = permissionSummary.highRiskAppsCount > 0
        if (win.isTabletOrLarger) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.Chat,
                    iconColor = SatarkAccent,
                    title = "AI Cyber Advisor",
                    subtitle = "Analyze suspicious SMS & chats",
                    badge = "Gemini AI",
                    badgeColor = SatarkAccent,
                    onClick = { onNavigate(SatarkScreen.SANCHALAK_CHAT) }
                )
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.SecurityUpdateWarning,
                    iconColor = if (hasAppsRisk) SatarkDanger else SatarkOk,
                    title = "App Security",
                    subtitle = if (hasAppsRisk) "${permissionSummary.highRiskAppsCount} Sensitive Apps" else "All Apps Clean",
                    badge = if (hasAppsRisk) "Review" else "Safe",
                    badgeColor = if (hasAppsRisk) SatarkDanger else SatarkOk,
                    onClick = { onNavigate(SatarkScreen.APP_SECURITY) }
                )
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = SatarkOk,
                    title = "Trusted Payees",
                    subtitle = "Verified account directory",
                    badge = "On-Device",
                    badgeColor = SatarkOk,
                    onClick = { onNavigate(SatarkScreen.SAFEPAY) }
                )
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning,
                    iconColor = SatarkDanger,
                    title = "🚨 Lost Money?",
                    subtitle = "Call 1930 & Freeze Bank",
                    badge = "Emergency",
                    badgeColor = SatarkDanger,
                    onClick = onConfirmFraudQuick
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.Chat,
                    iconColor = SatarkAccent,
                    title = "AI Cyber Advisor",
                    subtitle = "Analyze suspicious SMS & chats",
                    badge = "Gemini AI",
                    badgeColor = SatarkAccent,
                    onClick = { onNavigate(SatarkScreen.SANCHALAK_CHAT) }
                )
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.SecurityUpdateWarning,
                    iconColor = if (hasAppsRisk) SatarkDanger else SatarkOk,
                    title = "App Security",
                    subtitle = if (hasAppsRisk) "${permissionSummary.highRiskAppsCount} Sensitive Apps" else "All Apps Clean",
                    badge = if (hasAppsRisk) "Review" else "Safe",
                    badgeColor = if (hasAppsRisk) SatarkDanger else SatarkOk,
                    onClick = { onNavigate(SatarkScreen.APP_SECURITY) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = SatarkOk,
                    title = "Trusted Payees",
                    subtitle = "Verified account directory",
                    badge = "On-Device",
                    badgeColor = SatarkOk,
                    onClick = { onNavigate(SatarkScreen.SAFEPAY) }
                )
                ConsumerCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning,
                    iconColor = SatarkDanger,
                    title = "🚨 Lost Money?",
                    subtitle = "Call 1930 & Freeze Bank",
                    badge = "Emergency",
                    badgeColor = SatarkDanger,
                    onClick = onConfirmFraudQuick
                )
            }
        }

        // 4. VERIFIED BENEFICIARY DIRECTORY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = SatarkOk, modifier = Modifier.size(16.dp))
                        Text("Verified Payee Directory", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SatarkInk)
                    }
                    Text("Encrypted Locally", fontSize = 10.sp, color = SatarkDim)
                }

                BeneficiaryRow("Mom (Sunita Devi)", "mother.family@oksbi", "24 payments • Verified")
                BeneficiaryRow("Sharma General Store", "sharma.kirana@icici", "11 payments • Verified")
                BeneficiaryRow("Electricity Bill Utility", "sbpdcl.billpay@sbi", "4 payments • Verified")

                Text(
                    text = "💡 SatarkPay automatically detects when a transfer is attempted to a brand new, unverified VPA and initiates safety checks.",
                    fontSize = 11.sp,
                    color = SatarkDim,
                    lineHeight = 15.sp
                )
            }
        }

        // 5. COLLAPSIBLE SPECIALIZED DEFENSE TOOLS
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showExtraTools = !showExtraTools },
            shape = RoundedCornerShape(12.dp),
            color = SatarkPanel
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = SatarkDim, modifier = Modifier.size(16.dp))
                    Text(
                        text = if (showExtraTools) "Hide Specialized Tools" else "More Security Tools (Radar, Domains, Mandates)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SatarkInk
                    )
                }
                Icon(
                    imageVector = if (showExtraTools) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SatarkDim
                )
            }
        }

        AnimatedVisibility(visible = showExtraTools) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExtraToolRow(
                    icon = Icons.Default.PhotoCamera,
                    title = "Screenshot Velocity Radar",
                    subtitle = "Detect rapid burst payment attempts & mule staircase",
                    onClick = { onNavigate(SatarkScreen.SCREENSHOT_RADAR) }
                )
                ExtraToolRow(
                    icon = Icons.Default.Link,
                    title = "Domain & Payment Link Trust",
                    subtitle = "L1-L4 ladder & fake gateway spoof detection",
                    onClick = { onNavigate(SatarkScreen.DOMAIN_TRUST) }
                )
                ExtraToolRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "AutoPay Mandate Audit",
                    subtitle = "Inspect and revoke hidden recurring debit charges",
                    onClick = { onNavigate(SatarkScreen.WALLET_AUDIT) }
                )
                ExtraToolRow(
                    icon = Icons.Default.FolderZip,
                    title = "Cyber Cell Evidence Pack",
                    subtitle = "NCRP complaint draft & SHA256 forensic hashes",
                    onClick = { onNavigate(SatarkScreen.REPORT_EVIDENCE) }
                )
                ExtraToolRow(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Threat Analyst Console",
                    subtitle = "SLA triage queue & threat registry review",
                    onClick = { onNavigate(SatarkScreen.ANALYST_CONSOLE) }
                )
            }
        }
    }
}

@Composable
private fun DemoChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.5f))),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ConsumerCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SatarkInk
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BeneficiaryRow(name: String, vpa: String, tag: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SatarkPanel)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SatarkInk)
            Text(vpa, fontSize = 10.sp, color = SatarkDim)
        }
        Surface(shape = RoundedCornerShape(4.dp), color = SatarkOkAlpha) {
            Text(
                text = tag,
                color = SatarkOk,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun ExtraToolRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = SatarkPanelCard,
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SatarkAccentAlpha,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(16.dp))
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SatarkInk)
                Text(subtitle, fontSize = 11.sp, color = SatarkDim)
            }
        }
    }
}
