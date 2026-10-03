package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
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
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Main Branding Row
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
                        color = SatarkAccentAlpha,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SatarkAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "SatarkPay",
                            style = MaterialTheme.typography.titleLarge,
                            color = SatarkInk,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "60s Pre-Pay • 60m Post-Fraud",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Guard status badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (guardOn) SatarkOkAlpha else SatarkDangerAlpha,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleGuard() }
                            .testTag("guard_toggle_chip")
                    ) {
                        Text(
                            text = if (guardOn) "🛡 Guard ON" else "⚠️ Guard OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (guardOn) SatarkOk else SatarkDanger,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                            tint = SatarkDim
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Quick Toggles Row (Voice, Senior, Mask)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Toggle
                FilterChip(
                    selected = voiceHindiOn,
                    onClick = onToggleVoice,
                    label = {
                        Text(
                            text = if (voiceHindiOn) "🔊 voice: हिंदी ON" else "🔇 voice: OFF",
                            fontSize = 11.5.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SatarkAccentAlpha,
                        selectedLabelColor = SatarkAccent,
                        containerColor = SatarkPanelCard,
                        labelColor = SatarkDim
                    ),
                    modifier = Modifier.testTag("voice_toggle_chip")
                )

                // Senior Mode Toggle
                FilterChip(
                    selected = seniorModeOn,
                    onClick = onToggleSenior,
                    label = {
                        Text(
                            text = if (seniorModeOn) "👁 senior: ON" else "👁 senior: OFF",
                            fontSize = 11.5.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SatarkWarnAlpha,
                        selectedLabelColor = SatarkWarn,
                        containerColor = SatarkPanelCard,
                        labelColor = SatarkDim
                    ),
                    modifier = Modifier.testTag("senior_toggle_chip")
                )

                // Mask Toggle
                FilterChip(
                    selected = maskPiiOn,
                    onClick = onToggleMask,
                    label = {
                        Text(
                            text = if (maskPiiOn) "🔒 mask: ON" else "🔓 mask: OFF",
                            fontSize = 11.5.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SatarkOkAlpha,
                        selectedLabelColor = SatarkOk,
                        containerColor = SatarkPanelCard,
                        labelColor = SatarkDim
                    ),
                    modifier = Modifier.testTag("mask_toggle_chip")
                )
            }
        }
    }
}
