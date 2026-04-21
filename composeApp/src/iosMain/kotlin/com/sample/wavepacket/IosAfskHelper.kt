package com.sample.wavepacket

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.setActive
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IosAfskHelper : AfskHelper {
    private val _decodedFlow = MutableSharedFlow<String>()
    private val engine = AVAudioEngine()
    private val player = AVAudioPlayerNode()
    private var isListening = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private var decoder: AfskDecoder? = null

    init {
        setupAudioSession()
        engine.attachNode(player)
        val format = AVAudioFormat(
            standardFormatWithSampleRate = AfskConfig.SAMPLE_RATE.toDouble(),
            channels = 1u
        )
        engine.connect(player, engine.mainMixerNode, format)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setupAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        session.setActive(true, error = null)
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun playTextAsAfsk(text: String) {
        val pcmData = AfskEncoder.encodeText(text)
        val format = AVAudioFormat(
            standardFormatWithSampleRate = AfskConfig.SAMPLE_RATE.toDouble(),
            channels = 1u
        )
        val buffer = AVAudioPCMBuffer(pCMFormat = format, frameCapacity = pcmData.size.toUInt())

        buffer.setFrameLength(pcmData.size.toUInt())
        val channelData: CPointer<FloatVar>? = buffer.floatChannelData?.get(0)
        if (channelData != null) {
            for (i in pcmData.indices) {
                channelData[i] = pcmData[i]
            }
        }

        if (!engine.running) {
            engine.startAndReturnError(null)
        }

        player.scheduleBuffer(buffer, atTime = null, options = 0u, completionHandler = null)
        player.play()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun startListening(): Flow<String> {
        if (isListening) return _decodedFlow
        isListening = true

        val inputNode = engine.inputNode
        val bus = 0uL
        val format = inputNode.outputFormatForBus(bus)
        
        if (format.sampleRate <= 0.0) {
            isListening = false
            return _decodedFlow
        }

        decoder = AfskDecoder(sampleRate = format.sampleRate) { decodedText ->
            scope.launch {
                _decodedFlow.emit(decodedText)
            }
        }

        inputNode.installTapOnBus(
            bus,
            bufferSize = 1024u,
            format = format
        ) { buffer: AVAudioPCMBuffer?, _ ->
            if (buffer == null) return@installTapOnBus

            val channelData = buffer.floatChannelData?.get(0) ?: return@installTapOnBus
            val frameCount = buffer.frameLength.toInt()
            
            for (i in 0 until frameCount) {
                decoder?.processSample(channelData[i])
            }
        }


        if (!engine.running) {
            try {
                engine.startAndReturnError(null)
            } catch (e: Exception) {
                isListening = false
                inputNode.removeTapOnBus(bus)
            }
        }

        return _decodedFlow
    }

    override fun stopListening() {
        isListening = false
        engine.inputNode.removeTapOnBus(0uL)
        player.stop()
    }

    override suspend fun hasMicPermission(): Boolean {
        return AVAudioSession.sharedInstance()
            .recordPermission() == AVAudioSessionRecordPermissionGranted
    }

    override suspend fun requestMicPermission(): Boolean = suspendCoroutine { continuation ->
        AVAudioSession.sharedInstance().requestRecordPermission { granted ->
            continuation.resume(granted)
        }
    }
}
