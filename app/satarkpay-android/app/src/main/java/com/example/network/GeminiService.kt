package com.example.network

import com.example.engine.RuleEngine
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "model" or "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isThinking: Boolean = false,
    val groundedSources: List<String> = emptyList()
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

    fun hasValidApiKey(): Boolean {
        val key = apiKey
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Multi-turn chat generation with Sanchalak
     * Models:
     * - gemini-3.5-flash (default general)
     * - gemini-3.1-flash-lite-preview (fast)
     * - gemini-3.1-pro-preview (complex / high thinking)
     */
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        modelName: String = "gemini-3.5-flash",
        enableHighThinking: Boolean = false,
        useGoogleSearch: Boolean = false,
        useGoogleMaps: Boolean = false
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please add your key in the Secrets panel."))
        }

        try {
            val root = JSONObject()

            // System Instruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            val sysText = JSONObject()
            sysText.put("text", """
                You are AI Sanchalak, India's premier cybersecurity, UPI, and anti-fraud investigator in SatarkPay app.
                You give decisive, honest, structured verdicts in 4 buckets:
                1. SCAM LIKELY (🔴)
                2. CAUTION (🟠)
                3. PAUSE - NAHI BATA SAKTA (🔵)
                4. SEEMS OK (🟢)
                
                Always structure your answers with:
                - Verdict badge & Confidence level
                - 3 Bullet Reasons explaining the scam mechanism
                - MAT KARO (What NOT to do in bold red clarity)
                - KARO (Immediate practical safe actions in Hinglish/English)
                - Regulatory references: National Cyber Crime Helpline 1930, cybercrime.gov.in, NPCI, RBI, SEBI SCORES.
                Keep responses concise, assertive, empathetic, and jargon-free.
            """.trimIndent())
            sysParts.put(sysText)
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            // Conversation Contents
            val contents = JSONArray()
            for (msg in history.takeLast(12)) {
                if (msg.sender == "system") continue
                val c = JSONObject()
                c.put("role", if (msg.sender == "user") "user" else "model")
                val parts = JSONArray()
                val p = JSONObject()
                p.put("text", msg.text)
                parts.put(p)
                c.put("parts", parts)
                contents.put(c)
            }

            // Append current user message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            val currentPart = JSONObject()
            currentPart.put("text", RuleEngine.redactPII(userMessage).redactedText)
            currentParts.put(currentPart)
            currentTurn.put("parts", currentParts)
            contents.put(currentTurn)

            root.put("contents", contents)

            // Tools (Search & Maps Grounding)
            val tools = JSONArray()
            if (useGoogleSearch) {
                val searchTool = JSONObject()
                searchTool.put("googleSearch", JSONObject())
                tools.put(searchTool)
            }
            if (useGoogleMaps) {
                val mapsTool = JSONObject()
                mapsTool.put("googleMaps", JSONObject())
                tools.put(mapsTool)
            }
            if (tools.length() > 0) {
                root.put("tools", tools)
            }

            // Generation config
            val genConfig = JSONObject()
            if (enableHighThinking && modelName.contains("pro")) {
                val thinkingConfig = JSONObject()
                thinkingConfig.put("thinkingLevel", "HIGH")
                genConfig.put("thinkingConfig", thinkingConfig)
            }
            if (genConfig.length() > 0) {
                root.put("generationConfig", genConfig)
            }

            val requestBody = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val url = "$BASE_URL$modelName:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error: ${response.code} $respBody")
                return@withContext Result.failure(Exception("Gemini API error: ${response.code}"))
            }

            val jsonResponse = JSONObject(respBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No candidate returned by Gemini"))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        textBuilder.append(p.getString("text"))
                    }
                }
            }

            // Extract grounding metadata citations if present
            val groundedSources = mutableListOf<String>()
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null && web.has("title")) {
                            groundedSources.add(web.getString("title"))
                        }
                    }
                }
            }

            val replyText = textBuilder.toString().ifEmpty { "Verified by AI Sanchalak." }
            Result.success(
                ChatMessage(
                    sender = "model",
                    text = replyText,
                    groundedSources = groundedSources
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Chat request failed", e)
            Result.failure(e)
        }
    }

    /**
     * Transcribe Audio using model gemini-3.5-transcribe
     */
    suspend fun transcribeAudio(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4"
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext Result.failure(Exception("Gemini API key is not configured."))
        }

        try {
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val root = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()

            val textPart = JSONObject()
            textPart.put("text", "Transcribe this audio recording accurately into text. Capture any Hindi, English, or Hinglish words verbatim.")
            parts.put(textPart)

            val audioPart = JSONObject()
            val inlineData = JSONObject()
            inlineData.put("mimeType", mimeType)
            inlineData.put("data", base64Audio)
            audioPart.put("inlineData", inlineData)
            parts.put(audioPart)

            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            val requestBody = root.toString().toRequestBody(JSON_MEDIA_TYPE)
            val url = "${BASE_URL}gemini-3.5-transcribe:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Transcription failed: ${response.code} $respBody"))
            }

            val jsonResponse = JSONObject(respBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val responseParts = content?.optJSONArray("parts")
            val transcribedText = responseParts?.optJSONObject(0)?.optString("text") ?: ""

            Result.success(transcribedText)
        } catch (e: Exception) {
            Log.e(TAG, "Transcribe failed", e)
            Result.failure(e)
        }
    }
}
