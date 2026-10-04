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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
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
                            Text("सुरक्षा विराम: ${coolingSeconds} सेकंड", fontWeight = FontWeight.Bold, color = SatarkWarn, fontSize = 14.sp)
                            Text("जल्दबाजी में भुगतान न करें, चैट की जांच करें", color = SatarkInk, fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SatarkWarn, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 1. FRIENDLY DEVICE STATUS BANNER
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
                                "⚠️ फोन में ${permissionSummary.highRiskAppsCount} संदिग्ध ऐप मिलीं"
                            else
                                "🟢 आपका फोन सुरक्षित है",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SatarkInk
                        )
                        Text(
                            text = "स्क्रीन शेयर व जासूसी ऐप से सुरक्षा चालू है",
                            fontSize = 11.sp,
                            color = SatarkDim
                        )
                    }
                }

                TextButton(onClick = { onNavigate(SatarkScreen.APP_SECURITY) }) {
                    Text(
                        text = if (permissionSummary.highRiskAppsCount > 0) "जांचें" else "स्कैन करें",
                        color = if (permissionSummary.highRiskAppsCount > 0) SatarkDanger else SatarkAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. HERO: "पैसे भेजने से पहले जांचें" (PRE-PAYMENT CHECK BROKER)
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
                            Text("पैसे भेजने से पहले जांचें", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SatarkInk)
                            Text("SafePay • फ्रॉड अलर्ट और खाता सुरक्षा", fontSize = 11.sp, color = SatarkDim)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SatarkOkAlpha
                    ) {
                        Text(
                            text = "AI शील्ड",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatarkOk,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "किसी भी अनजान UPI ID या QR कोड पर पैसे भेजने से पहले यहाँ जांचें। यदि कोई कॉल पर दबाव बना रहा है या अनजान खाते में पैसे मांग रहा है, तो सतर्कपे तुरंत रोकेगा।",
                    fontSize = 12.sp,
                    color = SatarkInk,
                    lineHeight = 17.sp
                )

                // Quick Demo Test Chips (Easy for any user to test)
                Text("डेमो फ्रॉड चेक करके देखें:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SatarkDim)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoChip(
                        label = "🚨 डिजिटल अरेस्ट",
                        color = SatarkDanger,
                        onClick = { onNavigate(SatarkScreen.SAFEPAY) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoChip(
                        label = "📲 टेलीग्राम टास्क",
                        color = SatarkWarn,
                        onClick = { onNavigate(SatarkScreen.SAFEPAY) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoChip(
                        label = "🛒 राशन दुकान",
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
                    Text("UPI ID या QR कोड चेक करें", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 3. CORE 4-SERVICES (CLEAN 2x2 CONSUMER TILES)
        Text(
            text = "प्रमुख सुरक्षा सेवाएं",
            style = MaterialTheme.typography.labelSmall,
            color = SatarkDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ConsumerCard(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.Chat,
                iconColor = SatarkAccent,
                title = "AI संचालक",
                subtitle = "संदिग्ध मैसेज या कॉल पूछें",
                badge = "सहायक",
                badgeColor = SatarkAccent,
                onClick = { onNavigate(SatarkScreen.SANCHALAK_CHAT) }
            )

            val hasAppsRisk = permissionSummary.highRiskAppsCount > 0
            ConsumerCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.SecurityUpdateWarning,
                iconColor = if (hasAppsRisk) SatarkDanger else SatarkOk,
                title = "ऐप सुरक्षा",
                subtitle = if (hasAppsRisk) "${permissionSummary.highRiskAppsCount} खतरनाक ऐप मिलीं" else "सभी ऐप सुरक्षित हैं",
                badge = if (hasAppsRisk) "खतरा" else "सुरक्षित",
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
                title = "सुरक्षित खाते",
                subtitle = "आपके पहचाने हुए UPI खाते",
                badge = "खाता डायरी",
                badgeColor = SatarkOk,
                onClick = { onNavigate(SatarkScreen.SAFEPAY) }
            )

            ConsumerCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Warning,
                iconColor = SatarkDanger,
                title = "🚨 पैसे कट गए?",
                subtitle = "1930 साइबर हेल्पलाइन",
                badge = "आपातकालीन",
                badgeColor = SatarkDanger,
                onClick = onConfirmFraudQuick
            )
        }

        // 4. VERIFIED BENEFICIARY DIARY SNAPSHOT
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
                        Text("पहचाने हुए खाते (सुरक्षित)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SatarkInk)
                    }
                    Text("फ़ोन में सुरक्षित", fontSize = 10.sp, color = SatarkDim)
                }

                // 3 familiar records
                BeneficiaryRow("माँ (सुनीता देवी)", "mother.family@oksbi", "24 बार भुगतान हुआ")
                BeneficiaryRow("शर्मा किराना स्टोर", "sharma.kirana@icici", "11 बार भुगतान हुआ")
                BeneficiaryRow("बिजली बिल भुगतान", "sbpdcl.billpay@sbi", "4 बार भुगतान हुआ")

                Text(
                    text = "💡 जब भी कोई बिल्कुल नया या अनजान खाता आएगा, सतर्कपे आपको तुरंत सावधान करेगा।",
                    fontSize = 11.sp,
                    color = SatarkDim,
                    lineHeight = 15.sp
                )
            }
        }

        // 5. COLLAPSIBLE ADVANCED TOOLS (FOR HACKATHON JURY & EVALUATORS)
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
                        text = if (showExtraTools) "विशेषज्ञ टूल्स छुपाएं" else "अन्य टूल्स (स्क्रीनशॉट रडार, वेबसाइट जांच, मैंडेट)",
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
                    title = "स्क्रीनशॉट रडार",
                    subtitle = "तेजी से बार-बार पैसे कटने की जांच",
                    onClick = { onNavigate(SatarkScreen.SCREENSHOT_RADAR) }
                )
                ExtraToolRow(
                    icon = Icons.Default.Link,
                    title = "वेबसाइट व लिंक जांच",
                    subtitle = "फर्जी सरकारी व बैंक वेबसाइट पहचानें",
                    onClick = { onNavigate(SatarkScreen.DOMAIN_TRUST) }
                )
                ExtraToolRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "ऑटोपे मैंडेट ऑडिट",
                    subtitle = "छुपे हुए मासिक/दैनिक कटने वाले चार्ज रोकें",
                    onClick = { onNavigate(SatarkScreen.WALLET_AUDIT) }
                )
                ExtraToolRow(
                    icon = Icons.Default.FolderZip,
                    title = "साइबर सेल रिपोर्ट पैक",
                    subtitle = "NCRP शिकायत ड्राफ्ट व सबूत तैयार करें",
                    onClick = { onNavigate(SatarkScreen.REPORT_EVIDENCE) }
                )
                ExtraToolRow(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "एनालिस्ट कंसोल",
                    subtitle = "थ्रेट रजिस्ट्री व रिव्यू कतार",
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
