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
    val win = rememberWindowSizeInfo()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SatarkPanel,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // On tablets add more horizontal padding so content is centred
                .padding(
                    horizontal = win.contentPadding,
                    vertical = if (win.isShortHeight) 8.dp else 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Shield icon — slightly larger on expanded screens
                val iconBoxSize = if (win.isExpanded) 42.dp else 36.dp
                val iconSize    = if (win.isExpanded) 24.dp else 20.dp
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SatarkAccent,
                    modifier = Modifier.size(iconBoxSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield",
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val titleFontSize = when {
                            win.isExpanded -> 20.sp
                            win.isMedium   -> 18.sp
                            else           -> 17.sp
                        }
                        Text(
                            text = "SatarkPay",
                            style = MaterialTheme.typography.titleMedium,
                            color = SatarkInk,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = titleFontSize
                        )
                        // Status pill
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
                    // Hide subtitle on very short screens to save vertical space
                    if (!win.isShortHeight) {
                        Text(
                            text = "Real-Time UPI Scam Protection",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            // Actions row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val btnSize = if (win.isExpanded) 44.dp else 36.dp
                val icnSize = if (win.isExpanded) 24.dp else 20.dp

                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier.size(btnSize).testTag("voice_toggle_chip")
                ) {
                    Icon(
                        imageVector = if (voiceHindiOn)
                            Icons.AutoMirrored.Filled.VolumeUp
                        else
                            Icons.Default.VolumeMute,
                        contentDescription = "Voice Readout",
                        tint = if (voiceHindiOn) SatarkAccent else SatarkDim,
                        modifier = Modifier.size(icnSize)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(btnSize).testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SatarkDim,
                        modifier = Modifier.size(icnSize)
                    )
                }
            }
        }
    }
}
