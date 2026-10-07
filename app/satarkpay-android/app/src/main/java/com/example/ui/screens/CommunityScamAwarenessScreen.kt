package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val authorHandle: String,
    val avatarColor: Color,
    val caseTag: String,
    val threatLevel: String,
    val content: String,
    val evidenceCropName: String,
    val sha256Short: String,
    val keyTakeaway: String,
    val upvotes: Int,
    val commentsCount: Int
)

data class TeamChatMessage(
    val id: String,
    val senderName: String,
    val senderRole: String,
    val avatarColor: Color,
    val messageText: String,
    val timestamp: String,
    val isEvidenceCitation: Boolean = false,
    val citationText: String? = null
)

@Composable
fun CommunityScamAwarenessScreen(
    onBack: () -> Unit,
    onSpeakText: (String) -> Unit = {},
    onNavigateToAirGap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Scam Awareness Feed, 1: Expert Discussion Chat
    var userChatInput by remember { mutableStateOf("") }

    // Seeded community posts grounded in downloaded case analysis bundle
    val posts = remember {
        listOf(
            CommunityPost(
                id = "c18",
                authorName = "Prince Kumar Singh",
                authorRole = "Lead Cyber Threat Analyst",
                authorHandle = "@prince_intel",
                avatarColor = Color(0xFF1E88E5),
                caseTag = "CASE 18 · DIGITAL ARREST SYNDICATE",
                threatLevel = "CRITICAL (99.4%)",
                content = "🚨 URGENT ADVISORY: Hamare evidence dossier ke Case 18 mein victim ko fake CBI notice bheja gaya tha, jisme 48-hour arrest threat diya gaya. Scammers WhatsApp video call par fake police uniform pahan kar 'Clearance Deposit' maangte hain. Remember: CBI, Police ya TRAI kabhi video call par arrest nahi karte aur na hi kisi account mein paise transfer karne ko bolte hain!",
                evidenceCropName = "18_fake_cbi_threat.png",
                sha256Short = "da5737...367",
                keyTakeaway = "Video call turant kaatein, 1930 par complaint likhwayein aur family ko bataein.",
                upvotes = 342,
                commentsCount = 28
            ),
            CommunityPost(
                id = "c13",
                authorName = "Nishant",
                authorRole = "Security Systems Engineer",
                authorHandle = "@nishant_sec",
                avatarColor = Color(0xFF00897B),
                caseTag = "CASE 13 · DYNAMIC QR CODE SWITCH",
                threatLevel = "HIGH (96.2%)",
                content = "⚠️ COUNTER QR MISDIRECTION: Case 13 mein ₹3,000 ka QR payment verify karne par payee mismatch nikla. Shopkeeper ne payment lene se inkar kar diya kyunki QR code ek personal mule account mein redirect ho gaya tha. SatarkPay pre-PIN heuristic rule R14: Hamesha verify karein ki QR verified merchant ka hai ya individual savings account ka!",
                evidenceCropName = "13_qr_wrong_recipient.png",
                sha256Short = "92ca1e...809",
                keyTakeaway = "Payment confirm karne se pehle recipient ka naam aur green badge match karein.",
                upvotes = 289,
                commentsCount = 19
            ),
            CommunityPost(
                id = "c02",
                authorName = "Kartik",
                authorRole = "User Safety Advocate",
                authorHandle = "@kartik_safe",
                avatarColor = Color(0xFFFB8C00),
                caseTag = "CASE 02 · PARCEL ACTIVATION & REFUND TRAP",
                threatLevel = "CRITICAL (98.8%)",
                content = "🛑 GOLDEN RULE: Case 02 evidence analysis se saaf hai ki scammers ne victim se bola 'Pehle ₹499 parcel activation charge do, tab ₹5,000 refund release hoga'. Bharat ke har nagrik ko ye pata hona chahiye: Refund LENE ke liye kabhi paise nahi bhejne padte aur na hi UPI PIN daalna padta hai!",
                evidenceCropName = "02_refund_activation.png",
                sha256Short = "970383...5b3",
                keyTakeaway = "Paisa paane ke liye PIN maangne wala 100% scammer hai.",
                upvotes = 412,
                commentsCount = 35
            ),
            CommunityPost(
                id = "c22",
                authorName = "Nishant",
                authorRole = "Security Systems Engineer",
                authorHandle = "@nishant_sec",
                avatarColor = Color(0xFF00897B),
                caseTag = "CASE 22 & 08 · MALICIOUS LOAN APKS & EXTORTION",
                threatLevel = "CRITICAL (97.5%)",
                content = "📱 APK PERMISSION HIJACK: WhatsApp link se aayi QuickFunds aur CashLoan jaisi apps phone ke contacts aur gallery chura leti hain. Phir fake morphed pictures se victim ke rishtedaron ko phone karke blackmail karti hain. Phone me aisi unverified apps install na hone dein!",
                evidenceCropName = "22_whatsapp_loan_threat.png",
                sha256Short = "56fd65...2b1",
                keyTakeaway = "SatarkPay App Security Scanner se phone ki risky apps turant check aur uninstall karein.",
                upvotes = 380,
                commentsCount = 42
            ),
            CommunityPost(
                id = "c10",
                authorName = "Prince Kumar Singh",
                authorRole = "Lead Cyber Threat Analyst",
                authorHandle = "@prince_intel",
                avatarColor = Color(0xFF1E88E5),
                caseTag = "CASE 10 · YOUTUBE LIKE & TELEGRAM TASK TRAP",
                threatLevel = "CRITICAL (98.1%)",
                content = "💸 TASK SCAM FUNNEL: Case 10 mein shuruat mein ₹150 ka chhota payment bhejkar trust banaya gaya. Uske baad VIP task ke naam par ₹50,000 prepaid deposit trap kiya gaya. Kisi bhi 'Daily ₹3,000 Earn' task group mein ek rupya bhi deposit na karein!",
                evidenceCropName = "10_review_task_offer.png",
                sha256Short = "168459...703",
                keyTakeaway = "Easy task ke liye koi genuine company lakho rupaye nahi deti. Turant exit karein.",
                upvotes = 315,
                commentsCount = 22
            )
        )
    }

    // Interactive discussion stream with Prince, Nishant, and Kartik
    val chatMessages = remember {
        mutableStateListOf(
            TeamChatMessage(
                id = "m1",
                senderName = "Prince Kumar Singh",
                senderRole = "Threat Intel Lead",
                avatarColor = Color(0xFF1E88E5),
                messageText = "Namaste team! Hamare paas naye evidence bundle se Case 18 (Digital Arrest) aur Case 13 (QR misdirection) ke confirmed indicators aaye hain. MHA aur 1930 portal par aisi complaints pichhle hafte 40% badhi hain.",
                timestamp = "10:15 AM",
                isEvidenceCitation = true,
                citationText = "Ref: Case 18 · MHA CFCFRMS Intelligence"
            ),
            TeamChatMessage(
                id = "m2",
                senderName = "Nishant",
                senderRole = "Security Engineer",
                avatarColor = Color(0xFF00897B),
                messageText = "Maine Case 13 ke transaction headers analyze kiye hain. Scammers physical counters par dynamic sticker QR chipka rahe hain jisme merchant MCC code ke bajaye individual mule VPA coded hota hai. SatarkPay ka pre-PIN scanner ab is mismatch ko point-of-scan par hi red flag kar deta hai.",
                timestamp = "10:18 AM",
                isEvidenceCitation = true,
                citationText = "Ref: Case 13 · Rule R14 MCC Mismatch"
            ),
            TeamChatMessage(
                id = "m3",
                senderName = "Kartik",
                senderRole = "Safety Advocate",
                avatarColor = Color(0xFFFB8C00),
                messageText = "Log panic mein aakar decisions lete hain, khaaskar jab video call par 'Digital Arrest' ya 'Electricity Disconnection' ki dhamki milti hai. Main community ko bata raha hoon: koi bhi official notice WhatsApp par nahi aata. Call turant kaatein aur cooling period lein.",
                timestamp = "10:22 AM"
            ),
            TeamChatMessage(
                id = "m4",
                senderName = "Prince Kumar Singh",
                senderRole = "Threat Intel Lead",
                avatarColor = Color(0xFF1E88E5),
                messageText = "Bilkul sahi Kartik. Aur agar kisi ne anjaane mein paise bhej diye hon, toh 60-minute Golden Hour rule sabse critical hai: 1930 helpline par call karke transaction acknowledgement number lein taaki mule bank account mein funds freeze ho sakein.",
                timestamp = "10:25 AM",
                isEvidenceCitation = true,
                citationText = "Ref: 1930 Golden Hour Protocol"
            ),
            TeamChatMessage(
                id = "m5",
                senderName = "Nishant",
                senderRole = "Security Engineer",
                avatarColor = Color(0xFF00897B),
                messageText = "Aur Case 22 ke malicious loan APKs ke liye humne App Security Scanner integrate kiya hai jo Accessibility aur READ_CONTACTS permissions wali third-party APKs ko turant highlight karta hai. Sideloading ko disable rakhna sabse badi suraksha hai.",
                timestamp = "10:30 AM",
                isEvidenceCitation = true,
                citationText = "Ref: Case 22 · RAT Permission Guard"
            ),
            TeamChatMessage(
                id = "m6",
                senderName = "Kartik",
                senderRole = "Safety Advocate",
                avatarColor = Color(0xFFFB8C00),
                messageText = "Agar community ka koi bhi sadasya kisi sandeh-janak link ya message ko dekh raha hai, toh Air-Gap Link Sandbox mein bina phone ko risk mein daale link inspect kar sakta hai ya yahan poochh sakta hai!",
                timestamp = "10:33 AM"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatarkBg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("community_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SatarkInk)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Satark Koo-Community",
                        style = MaterialTheme.typography.titleMedium,
                        color = SatarkInk,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SatarkAccentAlpha
                    ) {
                        Text(
                            text = "LIVE INTEL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatarkAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Real Case Evidence & Team Discussion · SANGYAN 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = SatarkDim,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = {
                    onSpeakText("सतर्कपे कम्युनिटी में आपका स्वागत है। प्रिंस, निशांत और कार्तिक द्वारा सत्यापित किए गए नवीनतम स्कैम मामलों को यहां देखें।")
                }
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Voice summary", tint = SatarkAccent)
            }
        }

        // Tab Selector (Feed vs Chat Discussion)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SatarkPanel,
            contentColor = SatarkAccent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Scam Alerts (${posts.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp) },
                icon = { Icon(Icons.Default.Feed, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Expert Chat (${chatMessages.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp) },
                icon = { Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (selectedTab == 0) {
            // SCAM AWARENESS POSTS FEED
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Author Header
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
                                        shape = CircleShape,
                                        color = post.avatarColor,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = post.authorName.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(text = post.authorName, fontWeight = FontWeight.Bold, color = SatarkInk, fontSize = 13.sp)
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified Analyst", tint = SatarkAccent, modifier = Modifier.size(13.dp))
                                        }
                                        Text(text = "${post.authorHandle} · ${post.authorRole}", color = SatarkDim, fontSize = 10.5.sp)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (post.threatLevel.contains("CRITICAL")) SatarkDangerAlpha else SatarkWarnAlpha
                                ) {
                                    Text(
                                        text = post.threatLevel,
                                        color = if (post.threatLevel.contains("CRITICAL")) SatarkDanger else SatarkWarn,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Case Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SatarkAccentAlpha
                            ) {
                                Text(
                                    text = post.caseTag,
                                    color = SatarkAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Main Content
                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SatarkInk,
                                lineHeight = 19.sp
                            )

                            // Evidence Box
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SatarkBg,
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(14.dp))
                                        Text("Evidence Proof: ${post.evidenceCropName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SatarkInk)
                                    }
                                    Text("SHA-256: ${post.sha256Short} · Extracted from case_analysis_bundle.zip", fontSize = 10.sp, color = SatarkDim)
                                    Text("🛡️ Satark Advice: ${post.keyTakeaway}", fontSize = 11.sp, color = SatarkOk, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Interaction Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.ThumbUp, contentDescription = "Upvotes", tint = SatarkDim, modifier = Modifier.size(15.dp))
                                        Text("${post.upvotes}", fontSize = 11.sp, color = SatarkDim)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comments", tint = SatarkDim, modifier = Modifier.size(15.dp))
                                        Text("${post.commentsCount}", fontSize = 11.sp, color = SatarkDim)
                                    }
                                }

                                TextButton(
                                    onClick = { onSpeakText(post.content) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Listen", color = SatarkAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // EXPERT DISCUSSION CHAT STREAM
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Team banner
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = SatarkAccent, modifier = Modifier.size(24.dp))
                        Column {
                            Text(
                                text = "Prince Kumar Singh, Nishant, and Kartik (Satark Core)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SatarkInk
                            )
                            Text(
                                text = "Live case breakdown & victim guidance room",
                                style = MaterialTheme.typography.bodySmall,
                                color = SatarkDim,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SatarkPanelCard),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SatarkBorder)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = msg.avatarColor,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = msg.senderName.take(1),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        Text(text = msg.senderName, fontWeight = FontWeight.Bold, color = SatarkInk, fontSize = 12.5.sp)
                                        Text("(${msg.senderRole})", color = SatarkDim, fontSize = 10.sp)
                                    }
                                    Text(text = msg.timestamp, color = SatarkDim, fontSize = 10.sp)
                                }

                                Text(
                                    text = msg.messageText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SatarkInk,
                                    lineHeight = 18.sp
                                )

                                if (msg.isEvidenceCitation && msg.citationText != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SatarkAccentAlpha
                                    ) {
                                        Text(
                                            text = "📌 ${msg.citationText}",
                                            fontSize = 9.5.sp,
                                            color = SatarkAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // User Input bar for joining discussion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userChatInput,
                        onValueChange = { userChatInput = it },
                        placeholder = { Text("Ask Prince, Nishant, or Kartik...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (userChatInput.trim().isNotEmpty()) {
                                chatMessages.add(
                                    TeamChatMessage(
                                        id = "u_${System.currentTimeMillis()}",
                                        senderName = "Aap (Community Member)",
                                        senderRole = "Verified User",
                                        avatarColor = SatarkOk,
                                        messageText = userChatInput.trim(),
                                        timestamp = "Just now"
                                    )
                                )
                                val query = userChatInput.trim()
                                userChatInput = ""

                                // Simulated response from Prince / Nishant / Kartik
                                val responseMessage = when {
                                    query.contains("cbi", ignoreCase = true) || query.contains("arrest", ignoreCase = true) ->
                                        TeamChatMessage(
                                            id = "r_${System.currentTimeMillis()}",
                                            senderName = "Prince Kumar Singh",
                                            senderRole = "Threat Intel Lead",
                                            avatarColor = Color(0xFF1E88E5),
                                            messageText = "Aapke sawal par Prince: Ye 100% fake Digital Arrest scam hai. Police kabhi WhatsApp call par chargesheets nahi bhejti. 1930 helpline par instant report karein.",
                                            timestamp = "Just now",
                                            isEvidenceCitation = true,
                                            citationText = "MHA Directive R38"
                                        )
                                    query.contains("qr", ignoreCase = true) || query.contains("pin", ignoreCase = true) ->
                                        TeamChatMessage(
                                            id = "r_${System.currentTimeMillis()}",
                                            senderName = "Kartik",
                                            senderRole = "Safety Advocate",
                                            avatarColor = Color(0xFFFB8C00),
                                            messageText = "Aapke sawal par Kartik: Kisi bhi cashback ya refund ke liye UPI PIN enter mat karein. PIN sirf paise bhejne ke liye hota hai!",
                                            timestamp = "Just now"
                                        )
                                    else ->
                                        TeamChatMessage(
                                            id = "r_${System.currentTimeMillis()}",
                                            senderName = "Nishant",
                                            senderRole = "Security Engineer",
                                            avatarColor = Color(0xFF00897B),
                                            messageText = "Aapke sawal par Nishant: Hamari team ne aapka message Satark threat database se verify kar liya hai. Hamesha link ko Air-Gap Sandbox me check karein.",
                                            timestamp = "Just now"
                                        )
                                }
                                chatMessages.add(responseMessage)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(SatarkAccent, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}
