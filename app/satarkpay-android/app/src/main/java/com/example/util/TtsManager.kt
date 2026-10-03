package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Try Hindi first, fallback to Indian English or default
            val result = tts?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("en", "IN"))
            }
        } else {
            Log.e("TtsManager", "TTS initialization failed")
        }
    }

    fun speak(text: String, isHindiPreferred: Boolean = true) {
        if (!isInitialized) return
        val speechSnippet = text.take(240) // As specified: speak first 240 chars for instant 2-second clarity
        if (isHindiPreferred) {
            tts?.setLanguage(Locale("hi", "IN"))
        } else {
            tts?.setLanguage(Locale("en", "IN"))
        }
        tts?.speak(speechSnippet, TextToSpeech.QUEUE_FLUSH, null, "satark_alert")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
