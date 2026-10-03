package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.engine.VerdictResult
import com.example.network.ChatMessage
import com.example.ui.components.VerdictCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SanchalakChatScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    isRecordingAudio: Boolean,
    enableThinking: Boolean,
    onToggleThinking: (Boolean) -> Unit,
    enableSearchGrounding: Boolean,
    onToggleSearchGrounding: (Boolean) -> Unit,
    enableMapsGrounding: Boolean,
    onToggleMapsGrounding: (Boolean) -> Unit,
    selectedModel: String,
    onSelectModel: (String) -> Unit,
    lastVerdict: VerdictResult?,
    redactionCount: Int,
    onSendMessage: (String) -> Unit,
    onToggleRecordAudio: () -> Unit,
    onSpeakText: (String) -> Unit,
    onEscalateToAnalyst: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val chipScrollState = rememberScrollState()

    // Auto scroll on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
    ) {
        // Header
        Surface(
            color = SatarkPanel,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sanchalak_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AI Sanchalak",
                                style = MaterialTheme.typography.titleMedium,
                                color = SatarkInk,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SatarkAccentAlpha
                            ) {
                                Text(
                                    text = if (enableThinking) "🧠 Thinking High" else selectedModel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SatarkAccent,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (redactionCount > 0) "🔒 client-side redaction: $redactionCount items masked (R28)" else "Zero data upload • On-device PII masking",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkOk,
                            fontSize = 11.sp
                        )
                    }

                    // Human Analyst Escalation
                    OutlinedButton(
                        onClick = onEscalateToAnalyst,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkWarn))
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = SatarkWarn, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Human Desk", color = SatarkWarn, fontSize = 11.sp)
                    }
                }

                // AI Capabilities Bar (High Thinking, Search Grounding, Maps Grounding, Fast Model)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = enableThinking,
                        onClick = { onToggleThinking(!enableThinking) },
                        label = { Text("🧠 High Thinking (gemini-3.1-pro)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SatarkAccentAlpha,
                            selectedLabelColor = SatarkAccent
                        ),
                        modifier = Modifier.testTag("thinking_mode_chip")
                    )

                    FilterChip(
                        selected = enableSearchGrounding,
                        onClick = { onToggleSearchGrounding(!enableSearchGrounding) },
                        label = { Text("🌐 Search Grounding", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SatarkOkAlpha,
                            selectedLabelColor = SatarkOk
                        ),
                        modifier = Modifier.testTag("search_grounding_chip")
                    )

                    FilterChip(
                        selected = enableMapsGrounding,
                        onClick = { onToggleMapsGrounding(!enableMapsGrounding) },
                        label = { Text("📍 Maps Grounding", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SatarkWarnAlpha,
                            selectedLabelColor = SatarkWarn
                        ),
                        modifier = Modifier.testTag("maps_grounding_chip")
                    )

                    FilterChip(
                        selected = selectedModel == "gemini-3.1-flash-lite-preview",
                        onClick = {
                            if (selectedModel == "gemini-3.1-flash-lite-preview") {
                                onSelectModel("gemini-3.5-flash")
                            } else {
                                onSelectModel("gemini-3.1-flash-lite-preview")
                            }
                        },
                        label = { Text("⚡ Flash-Lite Fast", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SatarkAccentAlpha,
                            selectedLabelColor = SatarkAccent
                        )
                    )
                }
            }
        }

        // Pre-populated Quick Test Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipScrollState)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf(
                "Digital Arrest" to "Your mobile number is involved in CBI Mumbai money laundering case. Do not disconnect video call or police will arrest you.",
                "Telegram Task" to "Congratulations! Earn ₹3,000 daily doing YouTube likes. Deposit ₹5,000 to VIP task account for ₹8,500 return.",
                "Fake SEBI Reg" to "SEBI Registered Analyst INZ99887711. Guaranteed 500% profit Upper Circuit tips. Pay ₹10,000 advance fees.",
                "KYC APK Trap" to "Dear customer your SIM card will be blocked in 24 hours. Click link to download quicksupport.apk to update KYC.",
                "Refund QR Scan" to "Scan this QR code and enter UPI PIN to receive ₹5,000 cashback reward.",
                "Bank OTP Warning" to "SBI Alert: Never share OTP with anyone. Bank never calls asking for OTP or UPI PIN. Beware of fraud."
            )

            chips.forEach { (label, text) ->
                AssistChip(
                    onClick = {
                        inputText = text
                        onSendMessage(text)
                    },
                    label = { Text(label, fontSize = 11.5.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SatarkPanelCard,
                        labelColor = SatarkInk
                    ),
                    border = AssistChipDefaults.assistChipBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)
                    )
                )
            }
        }

        // Active Deterministic Verdict Card (if available from last analyzed message)
        lastVerdict?.let { verdict ->
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                VerdictCard(
                    verdict = verdict,
                    onSpeak = { onSpeakText(verdict.hindiVoiceSummary) }
                )
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        color = if (isUser) SatarkAccent else SatarkPanelCard,
                        border = if (isUser) null else CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else SatarkInk
                            )

                            // Grounded sources citations
                            if (msg.groundedSources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "🌐 Grounded Sources: ${msg.groundedSources.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SatarkAccent,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = SatarkAccent,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (enableThinking) "🧠 Sanchalak high thinking investigation..." else "AI Sanchalak analyzing pattern...",
                            style = MaterialTheme.typography.bodySmall,
                            color = SatarkDim
                        )
                    }
                }
            }
        }

        // Input Bar (Text + Microphone Audio Transcribe + Send)
        Surface(
            color = SatarkPanel,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Audio Mic button with gemini-3.5-transcribe
                IconButton(
                    onClick = onToggleRecordAudio,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isRecordingAudio) SatarkDanger else SatarkPanelCard)
                        .testTag("audio_transcribe_button")
                ) {
                    Icon(
                        imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Microphone Transcribe (gemini-3.5-transcribe)",
                        tint = if (isRecordingAudio) Color.White else SatarkAccent
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isRecordingAudio) "Recording voice audio..." else "Type, paste chat, or speak...",
                            color = SatarkDim,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sanchalak_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SatarkInk,
                        unfocusedTextColor = SatarkInk,
                        focusedBorderColor = SatarkAccent,
                        unfocusedBorderColor = SatarkBorder
                    ),
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val text = inputText
                            inputText = ""
                            onSendMessage(text)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SatarkAccent)
                        .testTag("sanchalak_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
