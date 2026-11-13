package com.example.thehro

import android.content.Context
import android.media.AudioManager

/**
 * Reads a limited audio-mode hint; it cannot prove that every external calling app is idle.
 * No permission, no audio access, no phone number: just the mode flag.
 *  - MODE_IN_CALL           -> normal GSM call
 *  - MODE_IN_COMMUNICATION  -> VoIP / video call (WhatsApp, Meet, Skype ...)
 * Replaces the old hard-coded `activeCall = true` default (repo review, Oct 2026).
 * Finale task: add TelephonyCallback (API 31+) for call duration and caller-in-contacts.
 */
data class CallState(val inCall: Boolean?, val voipOrVideo: Boolean?)

object CallStateProbe {
    fun read(context: Context): CallState = try {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (am.mode) {
            AudioManager.MODE_IN_CALL -> CallState(inCall = true, voipOrVideo = false)
            AudioManager.MODE_IN_COMMUNICATION -> CallState(inCall = true, voipOrVideo = true)
            // MODE_NORMAL is not proof that a VoIP app has not hidden/omitted its mode signal.
            else -> CallState(inCall = null, voipOrVideo = null)
        }
    } catch (e: Exception) {
        CallState(inCall = null, voipOrVideo = null)
    }
}
