package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GoldenHourClock(
    elapsedSeconds: Long,
    title: String = "GOLDEN HOUR (1930 RESPONSE WINDOW)",
    subtitle: String = "Pehle 60 minute me helpline call se paise freeze hone ke 80% chances hote hain",
    modifier: Modifier = Modifier
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val (clockColor, clockAlpha) = when {
        minutes < 10 -> SatarkOk to SatarkOkAlpha
        minutes < 30 -> SatarkWarn to SatarkWarnAlpha
        else -> SatarkDanger to SatarkDangerAlpha
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(clockColor))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = clockColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = clockAlpha,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = clockColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = timeFormatted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = clockColor
                    )
                }
            }
        }
    }
}
