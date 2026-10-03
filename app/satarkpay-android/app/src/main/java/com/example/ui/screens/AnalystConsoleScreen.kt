package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SigRow
import com.example.ui.components.SigTagType
import com.example.ui.theme.*

@Composable
fun AnalystConsoleScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("C1 Queue", "C2 Case Detail", "C3 Review Desk", "C4 Rule Registry")

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
            IconButton(onClick = onBack, modifier = Modifier.testTag("analyst_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "SatarkPay Analyst Console (C1–C4)",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
        }

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SatarkPanel,
            contentColor = SatarkAccent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (selectedTab) {
            0 -> C1QueueTab()
            1 -> C2CaseDetailTab()
            2 -> C3ReviewDeskTab()
            3 -> C4RegistryTab()
        }
    }
}

@Composable
private fun C1QueueTab() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ACTIVE ESCALATED & LOW-CONFIDENCE CASES (SLA ≤ 5 MIN)", style = MaterialTheme.typography.labelSmall, color = SatarkAccent, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Case #SP-4091 · WhatsApp F1", style = MaterialTheme.typography.titleSmall, color = SatarkInk, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(4.dp), color = SatarkDangerAlpha) {
                        Text("SLA: 01:42 LEFT", color = SatarkDanger, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text("User in T3 Hold: 25 min chat with unknown number claiming CBI officer. ₹25,000 transfer demanded.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent), shape = RoundedCornerShape(6.dp)) {
                        Text("Open Case File", color = Color.White, fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp)) {
                        Text("Trigger Callback", color = SatarkOk, fontSize = 12.sp)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Case #SP-4092 · Novel Telegram F3", style = MaterialTheme.typography.titleSmall, color = SatarkInk, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(4.dp), color = SatarkWarnAlpha) {
                        Text("SLA: 04:10 LEFT", color = SatarkWarn, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text("Honest Bucket: PAUSE - NAHI BATA SAKTA. Novel crypto staking task disguised as e-commerce review.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
            }
        }
    }
}

@Composable
private fun C2CaseDetailTab() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("CASE INVESTIGATION · SP-4091", style = MaterialTheme.typography.labelSmall, color = SatarkAccent, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SigRow(label = "Suspect Number", value = "98••••••10 (Masked R28)", tagText = "Unknown", tagType = SigTagType.DANGER)
                SigRow(label = "Claimed Identity", value = "Inspector Vijay Kumar, CBI Cyber Cell", tagText = "Fake", tagType = SigTagType.DANGER)
                SigRow(label = "Requested UPI", value = "gov.cbi.verification@fakeaxis", tagText = "Lookalike Handle", tagType = SigTagType.DANGER)
                SigRow(label = "Chat Dwell Duration", value = "25 Minutes 12 Seconds", tagText = "T3 Hold Triggered", tagType = SigTagType.WARN)

                Divider(color = SatarkBorder)

                Text("Analyst Override & Direct Guidance:", style = MaterialTheme.typography.labelSmall, color = SatarkInk, fontWeight = FontWeight.Bold)
                Text("Confirm 100% scam. Dispatch automated Hindi voice callback explaining CBI protocols.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)

                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = SatarkDanger), modifier = Modifier.fillMaxWidth()) {
                    Text("Push SCAM LIKELY Verdict & Cancel Payment", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun C3ReviewDeskTab() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("CRAWLER REVIEW DESK (15-SECOND VERDICT SLA)", style = MaterialTheme.typography.labelSmall, color = SatarkAccent, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("FedEx / Narcotics Taiwan Courier Scheme", style = MaterialTheme.typography.titleSmall, color = SatarkInk, fontWeight = FontWeight.Bold)
                Text("Scammer claims 140g MDMA intercepted at Mumbai customs in user's name.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = SatarkOk), shape = RoundedCornerShape(6.dp)) {
                        Text("Approve Rule R39", color = Color.White)
                    }
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(6.dp)) {
                        Text("Reject", color = SatarkDanger)
                    }
                }
            }
        }
    }
}

@Composable
private fun C4RegistryTab() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("LIVE THREAT REGISTRY OTA DEPLOYMENT", style = MaterialTheme.typography.labelSmall, color = SatarkAccent, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Current Active Version: v2026.10.02-pack2", style = MaterialTheme.typography.bodyMedium, color = SatarkInk, fontWeight = FontWeight.Bold)
                Text("Included Rules: R1–R14 (Deck Core) • R15–R25 (Pack 1) • R26–R38 (Pack 2)", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                Text("Integrity Hash: SHA256 e3b0c442...991b (Signed by Satark Root)", style = MaterialTheme.typography.bodySmall, color = SatarkOk, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent), modifier = Modifier.fillMaxWidth()) {
                    Text("Push OTA Diff Update to Clients", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
