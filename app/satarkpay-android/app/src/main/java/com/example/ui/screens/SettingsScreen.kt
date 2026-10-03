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
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    guardOn: Boolean,
    onToggleGuard: () -> Unit,
    voiceHindiOn: Boolean,
    onToggleVoice: () -> Unit,
    seniorModeOn: Boolean,
    onToggleSenior: () -> Unit,
    maskPiiOn: Boolean,
    onToggleMask: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedLanguage by remember { mutableStateOf("Hinglish") }
    var cyberCellEmail by remember { mutableStateOf("patnacyberpps-bih@gov.in") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Text(
                text = "S11 · Settings & Consent Centre",
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )
        }

        // Section 1: Guard & Protection
        SettingsSectionCard(title = "CORE PROTECTION") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SatarkPay Guard Status", style = MaterialTheme.typography.bodyMedium, color = SatarkInk, fontWeight = FontWeight.SemiBold)
                    Text("Auto-intercept chat dwell & burst payments", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                }
                Switch(checked = guardOn, onCheckedChange = { onToggleGuard() })
            }
        }

        // Section 2: Accessibility & Voice
        SettingsSectionCard(title = "ACCESSIBILITY & VOICE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("👁 Senior Mode (+25% Font Scale)", style = MaterialTheme.typography.bodyMedium, color = SatarkInk, fontWeight = FontWeight.SemiBold)
                    Text("Enhanced contrast & 56dp tap targets", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                }
                Switch(checked = seniorModeOn, onCheckedChange = { onToggleSenior() })
            }

            Divider(color = SatarkBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🔊 Hindi Voice Alerts (Web/Native TTS)", style = MaterialTheme.typography.bodyMedium, color = SatarkInk, fontWeight = FontWeight.SemiBold)
                    Text("First 240 chars of verdict read in Hindi", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                }
                Switch(checked = voiceHindiOn, onCheckedChange = { onToggleVoice() })
            }
        }

        // Section 3: Privacy & Redaction
        SettingsSectionCard(title = "PRIVACY & LOCAL COMPUTATION") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🔒 Client-Side PII Redaction (R28)", style = MaterialTheme.typography.bodyMedium, color = SatarkInk, fontWeight = FontWeight.SemiBold)
                    Text("Mask mobile numbers, cards & UPI handles", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
                }
                Switch(checked = maskPiiOn, onCheckedChange = { onToggleMask() })
            }

            Divider(color = SatarkBorder)

            Text(
                text = "Zero-Access Guarantee: SMS, Contacts, Gallery hum KABHI NAHI maangte. Screenshots ki image store nahi hoti, sirf cryptographic hash banta hai.",
                style = MaterialTheme.typography.bodySmall,
                color = SatarkOk
            )
        }

        // Section 4: Emergency Contacts
        SettingsSectionCard(title = "EMERGENCY POLICE & BANK CONTACTS") {
            OutlinedTextField(
                value = cyberCellEmail,
                onValueChange = { cyberCellEmail = it },
                label = { Text("Cyber Police Station Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SatarkInk,
                    unfocusedTextColor = SatarkInk,
                    focusedBorderColor = SatarkAccent,
                    unfocusedBorderColor = SatarkBorder
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Text(
                text = "Default: Patna / Bihar Cyber Police Station & State Nodal Officer",
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim,
                fontSize = 11.sp
            )
        }

        // Section 5: Honest Limits & Legal
        SettingsSectionCard(title = "HONEST LIMITS & PRINCIPLES") {
            Text(text = "1. App kabhi kisi payment ko zabardasti block nahi karti — Cancel hamesha user ke hath me hota hai.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
            Text(text = "2. Fake confidence nahi: 'PAUSE — PAKA NAHI BATA SAKTA' ek valid bucket hai.", style = MaterialTheme.typography.bodySmall, color = SatarkDim)
            Text(text = "3. Version: SatarkPay 1.0 (Built for India • Zero Adware)", style = MaterialTheme.typography.bodySmall, color = SatarkAccent)
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}
