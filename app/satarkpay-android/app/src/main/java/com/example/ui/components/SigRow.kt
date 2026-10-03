package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

enum class SigTagType {
    OK, WARN, DANGER, INFO
}

@Composable
fun SigRow(
    label: String,
    value: String,
    tagText: String,
    tagType: SigTagType,
    modifier: Modifier = Modifier
) {
    val (tagBg, tagFg) = when (tagType) {
        SigTagType.OK -> SatarkOkAlpha to SatarkOk
        SigTagType.WARN -> SatarkWarnAlpha to SatarkWarn
        SigTagType.DANGER -> SatarkDangerAlpha to SatarkDanger
        SigTagType.INFO -> SatarkAccentAlpha to SatarkAccent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SatarkDim
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = SatarkInk,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = tagBg
        ) {
            Text(
                text = tagText,
                style = MaterialTheme.typography.labelSmall,
                color = tagFg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
