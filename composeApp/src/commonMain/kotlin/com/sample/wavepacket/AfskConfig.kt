package com.sample.wavepacket

object AfskConfig {
    const val SAMPLE_RATE = 44100
    const val MARK_FREQ = 2400.0
    const val SPACE_FREQ = 5600.0
    const val BAUD_RATE = 25
    const val SAMPLES_PER_BIT = SAMPLE_RATE / BAUD_RATE
    const val PREAMBLE_BYTE = 0xAA.toByte()
    const val PREAMBLE_COUNT = 4
    const val START_FRAME = 0x02.toByte()
    const val END_FRAME = 0x03.toByte()
    const val START_FRAME_SIZE = 1
    const val END_FRAME_SIZE = 1
    const val BITS_PER_BYTE = 8
    const val BYTE_MASK = 0xFF
    const val AMPLITUDE = 0.5f
    const val THRESHOLD = 0.02f
    const val MARK_RANGE_LOW = 1000.0
    const val MARK_RANGE_HIGH = 4000.0
    const val SPACE_RANGE_LOW = 4000.0
    const val SPACE_RANGE_HIGH = 8000.0
}
