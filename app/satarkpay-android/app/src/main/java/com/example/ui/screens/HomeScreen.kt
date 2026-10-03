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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onNavigate: (SatarkScreen) -> Unit,
    onConfirmFraudQuick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Cooling Alert
        if (isCoolingActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(SatarkScreen.CHAT_PAY) }
                    .testTag("active_cooling_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SatarkWarnAlpha),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkWarn))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = SatarkWarn, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "Cooling Active: ${coolingSeconds}s",
                                style = MaterialTheme.typography.titleMedium,
                                color = SatarkWarn,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Chat review zaroori hai",
                                style = MaterialTheme.typography.bodySmall,
                                color = SatarkInk
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SatarkWarn)
                }
            }
        }

        // FEATURE HERO: FAKE APPS & PERMISSION AUDITOR CARD
        val hasFakeApps = permissionSummary.highRiskAppsCount > 0
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onNavigate(SatarkScreen.APP_SECURITY) }
                .testTag("hero_app_security_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasFakeApps) SatarkDangerAlpha else SatarkPanelCard
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (hasFakeApps) SatarkDanger else SatarkAccent)
            )
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasFakeApps) SatarkDanger else SatarkAccent,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (hasFakeApps) Icons.Default.Warning else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Fake App & Permission Scan",
                                style = MaterialTheme.typography.titleSmall,
                                color = SatarkInk,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasFakeApps) "🚨 ${permissionSummary.highRiskAppsCount} High-Risk Apps Mili!" else "Screen, SMS & Location Safe",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasFakeApps) SatarkDanger else SatarkOk,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (hasFakeApps) SatarkDanger else SatarkAccent
                    ) {
                        Text(
                            text = if (hasFakeApps) "Clean Now" else "Audit",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Mini Permission Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickBadge("High Risk: ${permissionSummary.highRiskAppsCount}", if (hasFakeApps) SatarkDanger else SatarkOk)
                    QuickBadge("SMS/OTP: ${permissionSummary.appsWithSmsCount}", SatarkPurple)
                    QuickBadge("Location: ${permissionSummary.appsWithLocationCount}", SatarkAccent)
                    QuickBadge("Accessibility: ${permissionSummary.appsWithAccessibilityCount}", SatarkWarn)
                }
            }
        }

        // 4 Quick Threat Metric Tiles (Short & Punchy!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeStatTile(
                modifier = Modifier.weight(1f),
                title = "Threats",
                value = "$intelCount",
                badge = "Live",
                badgeColor = SatarkDanger,
                onClick = { onNavigate(SatarkScreen.INTEL_FEED) }
            )
            HomeStatTile(
                modifier = Modifier.weight(1f),
                title = "Radar",
                value = "$burstScreenshots",
                badge = if (burstScreenshots >= 3) "Burst" else "Clean",
                badgeColor = if (burstScreenshots >= 3) SatarkWarn else SatarkOk,
                onClick = { onNavigate(SatarkScreen.SCREENSHOT_RADAR) }
            )
            HomeStatTile(
                modifier = Modifier.weight(1f),
                title = "Links",
                value = "L4",
                badge = "Spoof",
                badgeColor = SatarkAccent,
                onClick = { onNavigate(SatarkScreen.DOMAIN_TRUST) }
            )
            HomeStatTile(
                modifier = Modifier.weight(1f),
                title = "Mandates",
                value = "4",
                badge = "Audit",
                badgeColor = SatarkWarn,
                onClick = { onNavigate(SatarkScreen.WALLET_AUDIT) }
            )
        }

        // Two Primary Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onNavigate(SatarkScreen.SANCHALAK_CHAT) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("sanchalak_quick_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Sanchalak", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = onConfirmFraudQuick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("emergency_quick_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SatarkDanger)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("🚨 Paisa Gaya?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Text(
            text = "DEFENSE MODULES",
            style = MaterialTheme.typography.labelSmall,
            color = SatarkDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Sleek Clean Module Cards (Compact, short text)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactNavCard(
                icon = Icons.Default.SecurityUpdateWarning,
                iconTint = SatarkDanger,
                title = "Fake App & Permission Scanner",
                subtitle = "Remote control tools, Screen & SMS permissions",
                tag = "${permissionSummary.highRiskAppsCount} Threats",
                tagColor = if (hasFakeApps) SatarkDanger else SatarkOk,
                onClick = { onNavigate(SatarkScreen.APP_SECURITY) }
            )

            CompactNavCard(
                icon = Icons.Default.ChatBubbleOutline,
                iconTint = SatarkAccent,
                title = "M1 · Chat-Before-Pay Gate",
                subtitle = "Payment dwell timer & 3 safety questions",
                tag = "Active",
                tagColor = SatarkOk,
                onClick = { onNavigate(SatarkScreen.CHAT_PAY) }
            )

            CompactNavCard(
                icon = Icons.Default.PhotoCamera,
                iconTint = SatarkWarn,
                title = "M2 · Screenshot Radar",
                subtitle = "Burst payment & repeat payee staircase",
                tag = "$burstScreenshots Scans",
                tagColor = SatarkWarn,
                onClick = { onNavigate(SatarkScreen.SCREENSHOT_RADAR) }
            )

            CompactNavCard(
                icon = Icons.Default.Link,
                iconTint = SatarkAccent,
                title = "M3 · Domain & Link Trust",
                subtitle = "L1-L4 ladder & Razorpay gateway spoof test",
                tag = "L4 Fake",
                tagColor = SatarkDanger,
                onClick = { onNavigate(SatarkScreen.DOMAIN_TRUST) }
            )

            CompactNavCard(
                icon = Icons.Default.AccountBalanceWallet,
                iconTint = SatarkPurple,
                title = "M5 · AutoPay Mandates",
                subtitle = "Recurring daily traps & instant revocation",
                tag = "4 Active",
                tagColor = SatarkPurple,
                onClick = { onNavigate(SatarkScreen.WALLET_AUDIT) }
            )

            CompactNavCard(
                icon = Icons.Default.FolderZip,
                iconTint = SatarkOk,
                title = "M7 · Report & Evidence Pack",
                subtitle = "NCRP complaint, email draft & SHA256 hashes",
                tag = "Evidence",
                tagColor = SatarkOk,
                onClick = { onNavigate(SatarkScreen.REPORT_EVIDENCE) }
            )

            CompactNavCard(
                icon = Icons.Default.Emergency,
                iconTint = SatarkDanger,
                title = "🚨 Emergency (R38 Golden Hour)",
                subtitle = "Cyber Cell Email + 1930 Call + CFCFRMS Freeze",
                tag = "Action",
                tagColor = SatarkDanger,
                onClick = { onNavigate(SatarkScreen.EMERGENCY_CONFIRM) }
            )

            CompactNavCard(
                icon = Icons.Default.AdminPanelSettings,
                iconTint = SatarkDim,
                title = "Analyst Console (C1–C4)",
                subtitle = "SLA queues, OTA threat registry & reviews",
                tag = "Admin",
                tagColor = SatarkDim,
                onClick = { onNavigate(SatarkScreen.ANALYST_CONSOLE) }
            )
        }
    }
}

@Composable
private fun QuickBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.4f)))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun HomeStatTile(
    title: String,
    value: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = SatarkDim, fontSize = 11.sp)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = SatarkInk, fontWeight = FontWeight.Bold)
            Surface(shape = RoundedCornerShape(4.dp), color = badgeColor.copy(alpha = 0.15f)) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactNavCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    tag: String,
    tagColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = SatarkInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }

            Surface(shape = RoundedCornerShape(6.dp), color = tagColor.copy(alpha = 0.15f)) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = tagColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SatarkDim, modifier = Modifier.size(18.dp))
        }
    }
}
