package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SplashScreen(
    onEnableGuard: () -> Unit,
    onManualOnly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg),
        color = SatarkBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Shield Icon & Brand
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SatarkAccentAlpha,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SatarkAccent,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Text(
                text = "SatarkPay",
                style = MaterialTheme.typography.headlineLarge,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "“Paisa bhejne se pehle 60 second — fraud ke baad 60 minute.”",
                style = MaterialTheme.typography.bodyLarge,
                color = SatarkDim,
                textAlign = TextAlign.Center
            )

            // Permissions Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "SatarkPay ko chalane ke liye:",
                        style = MaterialTheme.typography.titleMedium,
                        color = SatarkInk,
                        fontWeight = FontWeight.Bold
                    )

                    // Granted signals
                    PermissionRow(
                        isAllowed = true,
                        title = "Chat session ka time",
                        subtitle = "Chat dwell timer (demo: user-set value · production: opt-in)"
                    )
                    PermissionRow(
                        isAllowed = true,
                        title = "Payment screenshot detect",
                        subtitle = "Photos (naam/thumbnail OCR entity parse, no upload)"
                    )
                    PermissionRow(
                        isAllowed = true,
                        title = "Notification filter",
                        subtitle = "AutoPay mandate aur bank OTP warning alag karna"
                    )

                    Divider(color = SatarkBorder)

                    // Strictly denied list
                    PermissionRow(
                        isAllowed = false,
                        title = "SMS, Contacts, Gallery, Accessibility",
                        subtitle = "Hum KABHI NAHI maangte — full zero-access privacy"
                    )
                }
            }

            // Privacy Guarantee Notice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SatarkOkAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SatarkOk,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Privacy Guarantee: Analysis device par hota hai (R28). Raw chat ya screenshot server par kabhi upload nahi hote.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkInk
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEnableGuard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("enable_guard_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SatarkAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🛡 Guard ON Karo",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onManualOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("manual_only_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkDim))
                ) {
                    Text(
                        text = "Sirf Manual Mode (Paste-and-Check)",
                        color = SatarkDim,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    isAllowed: Boolean,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isAllowed) SatarkOk else SatarkDanger,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isAllowed) SatarkInk else SatarkDanger,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim,
                fontSize = 11.5.sp
            )
        }
    }
}
