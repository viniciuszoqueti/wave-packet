package com.sample.wavepacket

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AndroidAfskHelper(
    private val context: Context,
    private val permissionManager: AndroidPermissionManager
) : AfskHelper {
    private val _decodedFlow = MutableSharedFlow<String>()
    private var isListening = false
    private var listeningJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val decoder = AfskDecoder { decodedText ->
        scope.launch {
            _decodedFlow.emit(decodedText)
        }
    }

    override suspend fun playTextAsAfsk(text: String) {
        val pcmData = AfskEncoder.encodeText(text)
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(AfskConfig.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcmData.size * 4)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcmData, 0, pcmData.size, AudioTrack.WRITE_BLOCKING)
        audioTrack.play()

        val playTimeMs = (pcmData.size.toDouble() / AfskConfig.SAMPLE_RATE * 1000).toLong()
        kotlinx.coroutines.delay(playTimeMs + 100)
        audioTrack.release()
    }

    @SuppressLint("MissingPermission")
    override fun startListening(): Flow<String> {
        if (isListening) return _decodedFlow
        isListening = true

        listeningJob = scope.launch {
            val bufferSize = AudioRecord.getMinBufferSize(
                AfskConfig.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
            ) * 2

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                AfskConfig.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                isListening = false
                return@launch
            }

            decoder.reset()
            recorder.startRecording()

            val audioBuffer = FloatArray(bufferSize)

            while (isActive && isListening) {
                val read = recorder.read(audioBuffer, 0, audioBuffer.size, AudioRecord.READ_BLOCKING)
                for (i in 0 until read) {
                    decoder.processSample(audioBuffer[i])
                }
            }

            recorder.stop()
            recorder.release()
        }

        return _decodedFlow
    }

    override fun stopListening() {
        isListening = false
        listeningJob?.cancel()
    }

    override suspend fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestMicPermission(): Boolean {
        if (hasMicPermission()) return true
        return permissionManager.requestPermission(Manifest.permission.RECORD_AUDIO)
    }
}
