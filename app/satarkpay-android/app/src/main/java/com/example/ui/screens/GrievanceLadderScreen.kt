package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GrievanceLadderScreen(
    onBack: () -> Unit,
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("s10_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "S10 · Grievance Escalation Ladder",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
        }

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
                    text = "SEBI & REGULATED ENTITY ESCALATION (R30)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SatarkAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Agar fraud kisi registered broker, advisor ya exchange intermediary se juda hai — to ye statutory sequence follow karein:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
            }
        }

        // Ladder Steps
        LadderStepItem(
            stepNumber = 1,
            title = "Level 1: Intermediary / Broker Compliance Officer",
            timeline = "Resolution window: 7 to 15 Days",
            details = "Written complaint with ledger statement and transaction IDs directly to broker compliance officer."
        )

        LadderStepItem(
            stepNumber = 2,
            title = "Level 2: Stock Exchange (NSE / BSE Investor Cell)",
            timeline = "Resolution window: 15 to 30 Days",
            details = "Exchange Investor Grievance Cell (IGRC). Exchange can block disputed broker settlements."
        )

        LadderStepItem(
            stepNumber = 3,
            title = "Level 3: SEBI SCORES Portal (scores.gov.in)",
            timeline = "Statutory 30 Days SLA",
            details = "SEBI Complaints Redress System. Intermediary is legally mandated to address or face regulatory action."
        )

        LadderStepItem(
            stepNumber = 4,
            title = "Level 4: SMART ODR Portal (smartodr.in)",
            timeline = "Online Conciliation & Arbitration",
            details = "Independent Online Dispute Resolution with appointed mediator or arbitral tribunal."
        )

        LadderStepItem(
            stepNumber = 5,
            title = "Level 5: National Cyber Crime Portal (cybercrime.gov.in)",
            timeline = "CFCFRMS Police FIR",
            details = "Criminal syndicate investigation under IPC / BNS & IT Act."
        )
    }
}

@Composable
private fun LadderStepItem(
    stepNumber: Int,
    title: String,
    timeline: String,
    details: String
) {
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
                    color = SatarkAccentAlpha
                ) {
                    Text(
                        text = "STEP $stepNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = SatarkAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = timeline,
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkWarn,
                    fontSize = 11.5.sp
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim
            )
        }
    }
}
