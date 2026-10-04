package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
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
import com.example.ui.theme.*

@Composable
fun TopBarHeader(
    guardOn: Boolean,
    onToggleGuard: () -> Unit,
    voiceHindiOn: Boolean,
    onToggleVoice: () -> Unit,
    seniorModeOn: Boolean,
    onToggleSenior: () -> Unit,
    maskPiiOn: Boolean,
    onToggleMask: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SatarkPanel,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SatarkAccent,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "SatarkPay",
                            style = MaterialTheme.typography.titleMedium,
                            color = SatarkInk,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (guardOn) SatarkOkAlpha else SatarkDangerAlpha
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { onToggleGuard() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (guardOn) SatarkOk else SatarkDanger)
                                )
                                Text(
                                    text = if (guardOn) "Protected" else "Alert",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (guardOn) SatarkOk else SatarkDanger
                                )
                            }
                        }
                    }
                    Text(
                        text = "Real-Time UPI Scam Protection",
                        style = MaterialTheme.typography.bodySmall,
                        color = SatarkDim,
                        fontSize = 10.5.sp
                    )
                }
            }

            // Quick Actions: Voice Readout & Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("voice_toggle_chip")
                ) {
                    Icon(
                        imageVector = if (voiceHindiOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Voice Readout",
                        tint = if (voiceHindiOn) SatarkAccent else SatarkDim,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SatarkDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
