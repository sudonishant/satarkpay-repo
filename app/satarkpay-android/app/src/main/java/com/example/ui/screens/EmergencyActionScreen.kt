package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GoldenHourClock
import com.example.ui.components.SigRow
import com.example.ui.components.SigTagType
import com.example.ui.theme.*

@Composable
fun EmergencyActionScreen(
    goldenHourSeconds: Long,
    amount: Long,
    upiId: String,
    utr: String,
    txnId: String,
    suspectContact: String,
    complaintNumber: String,
    stepEmailSent: Boolean,
    onMarkEmailSent: () -> Unit,
    stepCallHelplineDone: Boolean,
    onMarkCallHelplineDone: () -> Unit,
    stepStopPaymentDone: Boolean,
    onMarkStopPaymentDone: () -> Unit,
    stepFamilyAlertSent: Boolean,
    onMarkFamilyAlertSent: () -> Unit,
    onSpeakPrompt: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showDraftDialog by remember { mutableStateOf(false) }
    var showScriptDialog by remember { mutableStateOf(false) }

    val completedSteps = listOf(stepEmailSent, stepCallHelplineDone, stepStopPaymentDone).count { it }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("emergency_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🚨 FRAUD CONFIRM — ACTION CENTER",
                    style = MaterialTheme.typography.titleMedium,
                    color = SatarkDanger,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Steps: $completedSteps/3 Completed • Golden Hour Window Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
            }
            IconButton(
                onClick = {
                    onSpeakPrompt("तुरंत 1930 पर कॉल कीजिए। ट्रांज़ैक्शन आईडी, यूटीआर और रकम सामने रखिए। फ़्रीज़ रिक्वेस्ट इसी कॉल पर जाती है।")
                },
                modifier = Modifier.testTag("emergency_voice_button")
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Voice instructions", tint = SatarkAccent)
            }
        }

        // Golden Hour Clock Component
        GoldenHourClock(
            elapsedSeconds = goldenHourSeconds,
            title = "⏱ GOLDEN HOUR COUNTDOWN (R30/R38)",
            subtitle = "Pehle 60 minute me helpline call se paise freeze hone ke 80% chances hote hain"
        )

        // STEP 1: EMAIL TO CYBER CELL
        EmergencyStepCard(
            stepNumber = 1,
            title = "Cyber Cell & Bank Nodal Email",
            isDone = stepEmailSent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SigRow(label = "To (Cyber Cells)", value = "patnacyberpps-bih@gov.in, cciu-bih@nic.in", tagText = "State Cells", tagType = SigTagType.INFO)
                SigRow(label = "Cc", value = "nodalofficer@bank.com", tagText = "Bank Nodal", tagType = SigTagType.INFO)
                SigRow(label = "Subject", value = "EMERGENCY: UPI Fraud ₹$amount • UTR: $utr", tagText = "Pre-formatted", tagType = SigTagType.WARN)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:patnacyberpps-bih@gov.in,cciu-bih@nic.in")
                                putExtra(Intent.EXTRA_SUBJECT, "EMERGENCY: UPI Fraud ₹$amount • UTR: $utr")
                                putExtra(Intent.EXTRA_TEXT, """
                                    Respected Cyber Crime Officer & Bank Nodal Officer,
                                    
                                    I am reporting an urgent financial fraud committed against me:
                                    - Loss Amount: ₹$amount
                                    - Suspect UPI ID: $upiId
                                    - UTR Number: $utr
                                    - UPI Transaction ID: $txnId
                                    - Suspect Mobile: $suspectContact
                                    - Incident Time: Just now
                                    
                                    Kindly trigger immediate beneficiary bank freeze on CFCFRMS portal.
                                    Formal complaint hash: SHA256 e3b0c442...
                                """.trimIndent())
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Cyber Cell Complaint"))
                            } catch (e: Exception) {
                                // Fallback
                            }
                            onMarkEmailSent()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp).testTag("send_email_intent_button")
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (stepEmailSent) "Mail Bhej Diya ✓" else "Mail Bhejo", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showDraftDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(42.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkDim))
                    ) {
                        Text("Draft Dekho", color = SatarkDim, fontSize = 12.sp)
                    }
                }
            }
        }

        // STEP 2: CALL HELPLINE (1930) + SCRIPT
        EmergencyStepCard(
            stepNumber = 2,
            title = "1930 / Helpline Direct Call",
            isDone = stepCallHelplineDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1930 par call lagakar exact script padhein taaki operator turant CFCFRMS hold ticket generate kare:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )

                // On-screen call script
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SatarkBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🗣 OPERATOR KO KYA BOLNA HAI (SCRIPT):",
                            style = MaterialTheme.typography.labelSmall,
                            color = SatarkAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "“Sir, mere sath abhi ₹$amount ka UPI fraud hua hai. UTR number $utr aur Txn ID $txnId hai. Beneficiary UPI $upiId par CFCFRMS par turant freeze request daal dijiye.”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SatarkInk
                        )
                    }
                }

                // Call buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                            try {
                                context.startActivity(dialIntent)
                            } catch (e: Exception) {
                                // fallback
                            }
                            onMarkCallHelplineDone()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SatarkOk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp).testTag("call_1930_button")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dial 1930", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1909"))
                            try {
                                context.startActivity(dialIntent)
                            } catch (e: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(42.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkDim))
                    ) {
                        Text("1909 Spam", color = SatarkDim, fontSize = 12.sp)
                    }
                }
            }
        }

        // STEP 3: PAYMENT STOP & BENEFICIARY HOLD
        EmergencyStepCard(
            stepNumber = 3,
            title = "Payment Stop & Beneficiary Freeze",
            isDone = stepStopPaymentDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bank portal me dispute & CFCFRMS hold file karein:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )

                SigRow(label = "Action 1", value = "Raise UPI Chargeback / Dispute", tagText = "Bank App", tagType = SigTagType.OK)
                SigRow(label = "Action 2", value = "CFCFRMS Beneficiary Hold", tagText = "1930 Ticket", tagType = SigTagType.DANGER)
                SigRow(label = "Action 3", value = "Revoke All Unknown Mandates", tagText = "M5 Audit", tagType = SigTagType.WARN)

                Button(
                    onClick = onMarkStopPaymentDone,
                    colors = ButtonDefaults.buttonColors(containerColor = if (stepStopPaymentDone) SatarkBorder else SatarkDanger),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("stop_payment_confirm_button")
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (stepStopPaymentDone) "Stop Request Recorded ✓" else "Payment Stop Request Confirm Karo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // STEP 4: OPTIONAL FAMILY ALERT
        EmergencyStepCard(
            stepNumber = 4,
            title = "Parivaar Alert (Optional)",
            isDone = stepFamilyAlertSent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Parivaar walo ko alert karein taaki scammer unse 'hospital bill' ya 'police fine' ke naam par paise na maange.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Alert: Mere phone par ek fraud call/message aayi thi. Agar mere naam se koi emergency paisa maange to bilkul mat dena. Main theek hoon.")
                        }
                        try {
                            context.startActivity(Intent.createChooser(shareIntent, "Parivaar ko alert bhejo"))
                        } catch (e: Exception) {}
                        onMarkFamilyAlertSent()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkWarn))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = SatarkWarn, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (stepFamilyAlertSent) "Alert Shared ✓" else "WhatsApp / SMS Alert Bhejo", color = SatarkWarn, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Email Draft Dialog
    if (showDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            title = { Text("Pre-Formatted Complaint Draft", color = SatarkInk, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Subject: Urgent: UPI Cyber Fraud ₹$amount • UTR $utr", style = MaterialTheme.typography.bodyMedium, color = SatarkInk, fontWeight = FontWeight.Bold)
                    Divider(color = SatarkBorder)
                    Text(
                        text = "I am filing an urgent complaint regarding unauthorized fraudulent debit of ₹$amount via UPI handle $upiId on date ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())}. Transaction ID: $txnId, UTR: $utr. Kindly place immediate lien on beneficiary account via CFCFRMS under National Cyber Crime Reporting mechanism.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkDim
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDraftDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent)) {
                    Text("Band Karo", color = Color.White)
                }
            },
            containerColor = SatarkPanelCard
        )
    }
}

@Composable
private fun EmergencyStepCard(
    stepNumber: Int,
    title: String,
    isDone: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isDone) SatarkOk else SatarkBorder)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
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
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDone) SatarkOkAlpha else SatarkAccentAlpha,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$stepNumber",
                                color = if (isDone) SatarkOk else SatarkAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = SatarkInk,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isDone) {
                    Surface(shape = RoundedCornerShape(4.dp), color = SatarkOkAlpha) {
                        Text(
                            text = "DONE ✓",
                            color = SatarkOk,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            content()
        }
    }
}
