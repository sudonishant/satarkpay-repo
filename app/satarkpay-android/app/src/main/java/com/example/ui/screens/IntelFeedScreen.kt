package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IntelPatternEntity
import com.example.ui.theme.*

@Composable
fun IntelFeedScreen(
    approvedIntel: List<IntelPatternEntity>,
    reviewQueueIntel: List<IntelPatternEntity>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlatformFilter by remember { mutableStateOf("All") }

    val filteredList = if (selectedPlatformFilter == "All") {
        approvedIntel
    } else {
        approvedIntel.filter { it.platform.equals(selectedPlatformFilter, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("m6_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "M6 · Live Threat Intel Feed",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
        }

        // Crawler & Review Stats Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "INDIA-WIDE CRAWLER ENGINE (R21)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SatarkAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Aaj: 784 raw reports → 172 relevant → ${reviewQueueIntel.size} novel (Analyst REVIEW queue)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SatarkInk,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rule R21: Threat library me auto-publish NAHI hota. Human analyst review ke baad hi OTA registry push hoti hai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim,
                    fontSize = 11.5.sp
                )
            }
        }

        // Platform Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "WhatsApp", "Telegram", "UPI", "Call", "SMS").forEach { platform ->
                FilterChip(
                    selected = selectedPlatformFilter == platform,
                    onClick = { selectedPlatformFilter = platform },
                    label = { Text(platform, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SatarkAccentAlpha,
                        selectedLabelColor = SatarkAccent
                    )
                )
            }
        }

        // Review Queue Alert Card if items are pending
        if (reviewQueueIntel.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SatarkWarnAlpha),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkWarn)),
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
                            text = "⚡ NOVEL PATTERN PENDING REVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = SatarkWarn,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(shape = RoundedCornerShape(4.dp), color = SatarkWarn) {
                            Text("15-Sec SLA", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    val pending = reviewQueueIntel.first()
                    Text(
                        text = "${pending.familyCode} · ${pending.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = SatarkInk,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pending.sampleSnippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkInk
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onApprove(pending.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = SatarkOk),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve → OTA Push", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onReject(pending.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkDanger))
                        ) {
                            Text("Reject", color = SatarkDanger, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Text(
            text = "VERIFIED ACTIVE THREAT PATTERNS",
            style = MaterialTheme.typography.labelSmall,
            color = SatarkDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Pattern List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SatarkDangerAlpha
                            ) {
                                Text(
                                    text = "${item.familyCode} · ${item.platform}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SatarkDanger,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "🔥 ${item.reportedCount} cases reported",
                                style = MaterialTheme.typography.bodySmall,
                                color = SatarkDim,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = SatarkInk,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SatarkBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“${item.sampleSnippet}”",
                                style = MaterialTheme.typography.bodySmall,
                                color = SatarkInk,
                                modifier = Modifier.padding(8.dp),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
