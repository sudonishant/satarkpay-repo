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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SigRow
import com.example.ui.components.SigTagType
import com.example.ui.theme.*

@Composable
fun ChatBeforePayScreen(
    isContactSaved: Boolean,
    onToggleContactSaved: (Boolean) -> Unit,
    chatDwellMinutes: Int,
    onSetDwellMinutes: (Int) -> Unit,
    frictionTier: String,
    coolingSeconds: Int,
    isCoolingActive: Boolean,
    onStartCooling: () -> Unit,
    onCancelPayment: () -> Unit,
    onProceedPayment: () -> Unit,
    onSpeakPrompt: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var q1SelfPay by remember { mutableStateOf<Boolean?>(null) }
    var q2OfficerOffer by remember { mutableStateOf<Boolean?>(null) }
    var q3LiveCall by remember { mutableStateOf<Boolean?>(null) }

    val isEscalatedToT3 = (!isContactSaved && chatDwellMinutes >= 8) || (q2OfficerOffer == true) || (q3LiveCall == true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("m1_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "M1 · Chat-Before-Pay Gate",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    onSpeakPrompt("रुकिए — जिस नंबर से बात हुई है वो सेव नहीं है। आठ मिनट में चैट दोबारा पढ़िए और तीन सवाल का जवाब दीजिए।")
                },
                modifier = Modifier.testTag("m1_voice_button")
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speak Hindi Warning", tint = SatarkAccent)
            }
        }

        // Active State Simulator Controls
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SIMULATOR: CHAT SESSION CONTEXT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SatarkAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Saved vs Unknown Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isContactSaved) "Contact: 👤 SAVED (Ravi Sharma)" else "Contact: ❓ UNKNOWN (+91 98765 43210)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SatarkInk,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Address book presence signal",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim
                        )
                    }
                    Switch(
                        checked = isContactSaved,
                        onCheckedChange = onToggleContactSaved,
                        modifier = Modifier.testTag("contact_saved_switch")
                    )
                }

                Divider(color = SatarkBorder)

                // Dwell Time Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Chat Session Dwell Time: $chatDwellMinutes minute",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SatarkInk
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2 to "Chhoti (<8m)", 12 to "Lambi (12m)", 25 to "Heavy (25m)").forEach { (mins, label) ->
                            FilterChip(
                                selected = chatDwellMinutes == mins,
                                onClick = { onSetDwellMinutes(mins) },
                                label = { Text("$mins min ($label)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SatarkAccentAlpha,
                                    selectedLabelColor = SatarkAccent
                                )
                            )
                        }
                    }
                }
            }
        }

        // Friction Tier Result Banner
        val tierColor = when {
            isEscalatedToT3 -> SatarkDanger
            frictionTier.contains("T2") -> SatarkWarn
            else -> SatarkOk
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = tierColor.copy(alpha = 0.12f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(tierColor)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isEscalatedToT3) Icons.Default.Warning else Icons.Default.Shield,
                        contentDescription = null,
                        tint = tierColor
                    )
                    Text(
                        text = if (isEscalatedToT3) "TIER T3: HOLD + HUMAN ANALYST ESCALATION" else frictionTier.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = tierColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isEscalatedToT3) {
                        "Aapke saath anjaan number se lambi baat hui hai ya officer/offer ka dabav hai. Payment hold par hai — analyst review window chalu hai."
                    } else if (frictionTier.contains("T2")) {
                        "Unknown number se chat ke turant baad payment app khula hai. 8 minute cooling period me chat dobara check karein."
                    } else {
                        "Saved contact ke sath safe fast path active hai. Kripya naye amount verify karein."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SatarkInk
                )
            }
        }

        // 3 Critical Questions Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3 CRITICAL SAFETY QUESTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SatarkAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Q1
                QuestionItem(
                    question = "Q1. Kya ye payment aap apni marzi se bhej rahe ho?",
                    answer = q1SelfPay,
                    onAnswer = { q1SelfPay = it }
                )

                // Q2
                QuestionItem(
                    question = "Q2. Kya kisi CBI/Police officer, job task ya lottery ne kaha?",
                    answer = q2OfficerOffer,
                    onAnswer = { q2OfficerOffer = it }
                )

                // Q3
                QuestionItem(
                    question = "Q3. Kya abhi dusre phone par live video/voice call chal rahi hai?",
                    answer = q3LiveCall,
                    onAnswer = { q3LiveCall = it }
                )
            }
        }

        // Active Cooling Clock if activated
        if (isCoolingActive) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SatarkWarnAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⏱ Cooling Window Running",
                            style = MaterialTheme.typography.titleSmall,
                            color = SatarkWarn,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Shant dimag se chat dobara padhein",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkInk
                        )
                    }
                    Text(
                        text = "${coolingSeconds}s",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SatarkWarn,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Action Bar (Cancel Payment is ALWAYS accessible!)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cancel Button (Red - Core principle: payment rukna aasan hona chahiye)
            Button(
                onClick = onCancelPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("cancel_payment_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SatarkDanger),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⛔ Cancel Payment (Paisa Mat Bhejo)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Cooling Button
            OutlinedButton(
                onClick = onStartCooling,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_cooling_button"),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkWarn))
            ) {
                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = SatarkWarn)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ruko 8 Min • Cooling Period Chalu Karo",
                    color = SatarkWarn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Proceed Button (Only if user has 100% verified)
            OutlinedButton(
                onClick = onProceedPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_pay_button"),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkOk))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SatarkOk)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Maine 100% Verify Kar Liya — Phir Pay Karo",
                    color = SatarkOk,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun QuestionItem(
    question: String,
    answer: Boolean?,
    onAnswer: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium,
            color = SatarkInk
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = answer == true,
                onClick = { onAnswer(true) },
                label = { Text("Haan (YES)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SatarkDangerAlpha,
                    selectedLabelColor = SatarkDanger
                )
            )
            FilterChip(
                selected = answer == false,
                onClick = { onAnswer(false) },
                label = { Text("Nahi (NO)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SatarkOkAlpha,
                    selectedLabelColor = SatarkOk
                )
            )
        }
    }
}
