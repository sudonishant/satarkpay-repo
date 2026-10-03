package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MandateAuditEntity
import com.example.ui.theme.*

@Composable
fun WalletAuditScreen(
    mandates: List<MandateAuditEntity>,
    onRevokeMandate: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            IconButton(onClick = onBack, modifier = Modifier.testTag("m5_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "M5 · Wallet & AutoPay Audit",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
        }

        // Linked Apps Overview Card
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
                    text = "LINKED UPI APPS & BANK ACCOUNTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SatarkAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Active Apps: PhonePe, Google Pay, Paytm, BHIM (4 Apps • 2 Linked Accounts)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SatarkInk
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SatarkWarnAlpha,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SatarkWarn, modifier = Modifier.size(16.dp))
                        Text(
                            text = "⚠️ 2 UPI Circle delegates authorized with secondary spending limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkWarn
                        )
                    }
                }
            }
        }

        Text(
            text = "ACTIVE RECURRING AUTOPAY MANDATES",
            style = MaterialTheme.typography.labelSmall,
            color = SatarkDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Mandates List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mandates, key = { it.id }) { item ->
                val isSuspicious = item.isSuspicious && !item.isRevoked
                val statusColor = when {
                    item.isRevoked -> SatarkDim
                    item.isSuspicious -> SatarkDanger
                    else -> SatarkOk
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mandate_card_${item.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(statusColor.copy(alpha = 0.5f)))
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (item.isRevoked) "REVOKED" else if (item.isSuspicious) "🔴 SUSPICIOUS" else "🟢 VERIFIED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = item.appName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SatarkDim
                                )
                            }

                            Text(
                                text = "₹${item.amount} / ${item.frequency}",
                                style = MaterialTheme.typography.titleMedium,
                                color = SatarkInk,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = item.payeeName,
                            style = MaterialTheme.typography.titleSmall,
                            color = SatarkInk,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = item.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSuspicious) SatarkWarn else SatarkDim
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next Debit: ${item.nextDebitDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SatarkDim,
                                fontSize = 11.5.sp
                            )

                            if (!item.isRevoked) {
                                Button(
                                    onClick = { onRevokeMandate(item.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (item.isSuspicious) SatarkDanger else SatarkBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Revoke Mandate",
                                        color = if (item.isSuspicious) Color.White else SatarkInk,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Mandate Deactivated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SatarkOk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
