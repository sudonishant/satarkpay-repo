package com.example.thehro

import android.content.Context
import android.media.AudioManager

/**
 * Reads whether a call is in progress from AudioManager mode.
 * No permission, no audio access, no phone number: just the mode flag.
 *  - MODE_IN_CALL           -> normal GSM call
 *  - MODE_IN_COMMUNICATION  -> VoIP / video call (WhatsApp, Meet, Skype ...)
 * Replaces the old hard-coded `activeCall = true` default (repo review, Oct 2026).
 * Finale task: add TelephonyCallback (API 31+) for call duration and caller-in-contacts.
 */
data class CallState(val inCall: Boolean, val voipOrVideo: Boolean)

object CallStateProbe {
    fun read(context: Context): CallState = try {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mode = am.mode
        CallState(
            inCall = mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION,
            voipOrVideo = mode == AudioManager.MODE_IN_COMMUNICATION,
        )
    } catch (e: Exception) {
        CallState(inCall = false, voipOrVideo = false)   // fail safe: unknown = no call signal
    }
}
