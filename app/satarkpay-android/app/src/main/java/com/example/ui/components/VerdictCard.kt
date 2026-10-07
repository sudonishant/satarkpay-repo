package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
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
import com.example.engine.VerdictBucket
import com.example.engine.VerdictResult
import com.example.ui.theme.*

@Composable
fun VerdictCard(
    verdict: VerdictResult,
    onSpeak: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (badgeText, badgeBg, badgeTextColor, badgeIcon) = when (verdict.bucket) {
        VerdictBucket.SCAM_LIKELY -> Quadruple(
            "🔴 HIGH SCAM PROBABILITY",
            SatarkDangerAlpha,
            SatarkDanger,
            Icons.Default.Block
        )
        VerdictBucket.CAUTION -> Quadruple(
            "🟠 CAUTION DETECTED",
            SatarkWarnAlpha,
            SatarkWarn,
            Icons.Default.Warning
        )
        VerdictBucket.PAUSE_NAHI_BATA -> Quadruple(
            "🔵 INCONCLUSIVE (VERIFY DETAILS)",
            SatarkAccentAlpha,
            SatarkAccent,
            Icons.Default.Info
        )
        VerdictBucket.SEEMS_OK -> Quadruple(
            "🟢 NO KNOWN THREAT SIGNALS",
            SatarkOkAlpha,
            SatarkOk,
            Icons.Default.CheckCircle
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("verdict_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(badgeTextColor.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Badge + Speak Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(badgeTextColor))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(badgeIcon, contentDescription = null, tint = badgeTextColor, modifier = Modifier.size(16.dp))
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelLarge,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (onSpeak != null) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.testTag("speak_verdict_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read verdict in Hindi voice",
                            tint = SatarkAccent
                        )
                    }
                }
            }

            // Rule Match Title
            Text(
                text = verdict.matchedRuleTitle,
                style = MaterialTheme.typography.titleMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Bold
            )

            // Reason Bullets
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                verdict.reasons.forEach { reason ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", color = SatarkAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SatarkDim
                        )
                    }
                }
            }

            Divider(color = SatarkBorder)

            // MAT KARO Line (Red)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SatarkDangerAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "⛔ MAT KARO:",
                        style = MaterialTheme.typography.labelLarge,
                        color = SatarkDanger,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = verdict.matKaro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SatarkInk
                    )
                }
            }

            // KARO Lines (Green)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SatarkOkAlpha,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "✅ KARO:",
                        style = MaterialTheme.typography.labelLarge,
                        color = SatarkOk,
                        fontWeight = FontWeight.Bold
                    )
                    verdict.karo.forEach { k ->
                        Text(
                            text = "• $k",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SatarkInk
                        )
                    }
                }
            }

            // Citation / Source line
            Text(
                text = verdict.citation,
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim,
                fontSize = 11.5.sp
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
