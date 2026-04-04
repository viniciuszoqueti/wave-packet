package com.sample.wavepacket

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform