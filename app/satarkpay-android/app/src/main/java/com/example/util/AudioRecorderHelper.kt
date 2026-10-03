package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileInputStream

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var tempAudioFile: File? = null
    var isRecording = false
        private set

    fun startRecording(): Boolean {
        return try {
            val outputDir = context.cacheDir
            tempAudioFile = File.createTempFile("voice_inquiry_", ".m4a", outputDir)

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(tempAudioFile?.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            true
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start audio recording", e)
            isRecording = false
            false
        }
    }

    fun stopRecording(): ByteArray? {
        if (!isRecording) return null
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false

            val file = tempAudioFile
            if (file != null && file.exists()) {
                val bytes = FileInputStream(file).use { it.readBytes() }
                file.delete()
                bytes
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to stop recording", e)
            recorder?.release()
            recorder = null
            isRecording = false
            null
        }
    }
}
