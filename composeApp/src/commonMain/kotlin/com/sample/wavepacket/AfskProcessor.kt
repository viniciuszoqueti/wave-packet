package com.sample.wavepacket

import kotlin.math.PI
import kotlin.math.sin

object AfskEncoder {
    fun encodeText(text: String): FloatArray {
        val samplesPerBit = AfskConfig.SAMPLES_PER_BIT
        val totalBytes = AfskConfig.PREAMBLE_COUNT + AfskConfig.START_FRAME_SIZE + text.length + AfskConfig.END_FRAME_SIZE
        val result = FloatArray(totalBytes * AfskConfig.BITS_PER_BYTE * samplesPerBit)
        val markStep = 2.0 * PI * AfskConfig.MARK_FREQ / AfskConfig.SAMPLE_RATE
        val spaceStep = 2.0 * PI * AfskConfig.SPACE_FREQ / AfskConfig.SAMPLE_RATE
        
        var phase = 0.0
        var index = 0

        fun writeByte(byte: Byte) {
            val b = byte.toInt()
            for (i in (AfskConfig.BITS_PER_BYTE - 1) downTo 0) {
                val step = if (((b shr i) and 1) == 1) markStep else spaceStep
                repeat(samplesPerBit) {
                    result[index++] = (sin(phase) * AfskConfig.AMPLITUDE).toFloat()
                    phase += step
                }
            }
        }

        repeat(AfskConfig.PREAMBLE_COUNT) { writeByte(AfskConfig.PREAMBLE_BYTE) }
        writeByte(AfskConfig.START_FRAME)
        text.forEach { writeByte(it.code.toByte()) }
        writeByte(AfskConfig.END_FRAME)
        
        return result
    }
}

class AfskDecoder(
    private val sampleRate: Double = AfskConfig.SAMPLE_RATE.toDouble(),
    private val onDecoded: (String) -> Unit
) {
    private val samplesPerBit = AfskConfig.SAMPLES_PER_BIT
    private var bitTimer = 0
    private var freqScore = 0
    private var crossTimer = 0
    private var bitCount = 0
    private var byteBuffer = 0
    private var isDecoding = false
    private val output = StringBuilder()
    private var idleTimer = 0
    private var lastState = 0

    fun reset() {
        bitTimer = 0
        freqScore = 0
        crossTimer = 0
        bitCount = 0
        byteBuffer = 0
        isDecoding = false
        output.clear()
        idleTimer = 0
        lastState = 0
    }

    fun processSample(sample: Float) {
        bitTimer++
        idleTimer++

        if (sample > AfskConfig.THRESHOLD && lastState <= 0) {
            recordCrossing()
            lastState = 1
        } else if (sample < -AfskConfig.THRESHOLD && lastState >= 0) {
            recordCrossing()
            lastState = -1
        }
        
        crossTimer++

        if (isDecoding && idleTimer > sampleRate) {
            flush()
        }

        if (bitTimer >= samplesPerBit) {
            val bit = if (freqScore >= 0) 1 else 0
            handleBit(bit)
            bitTimer = 0
            freqScore = 0
            idleTimer = 0
        }
    }

    private fun recordCrossing() {
        val freq = sampleRate / (crossTimer * 2)
        if (freq in AfskConfig.SPACE_RANGE_LOW..AfskConfig.SPACE_RANGE_HIGH) {
            freqScore--
        } else if (freq in AfskConfig.MARK_RANGE_LOW..AfskConfig.MARK_RANGE_HIGH) {
            freqScore++
        }
        crossTimer = 0
    }

    private fun handleBit(bit: Int) {
        if (!isDecoding) {
            byteBuffer = ((byteBuffer shl 1) or bit) and AfskConfig.BYTE_MASK
            if (byteBuffer.toByte() == AfskConfig.START_FRAME) {
                isDecoding = true
                bitCount = 0
                byteBuffer = 0
                output.clear()
                bitTimer = 0 
            }
            return
        }

        byteBuffer = (byteBuffer shl 1) or bit
        bitCount++

        if (bitCount == AfskConfig.BITS_PER_BYTE) {
            val byte = byteBuffer.toByte()
            if (byte == AfskConfig.END_FRAME) {
                flush()
            } else if (byteBuffer in 32..126 || byteBuffer == 10 || byteBuffer == 13) {
                output.append(byte.toInt().toChar())
            }
            byteBuffer = 0
            bitCount = 0
        }
    }

    private fun flush() {
        if (output.isNotEmpty()) {
            onDecoded(output.toString())
        }
        reset()
    }
}
