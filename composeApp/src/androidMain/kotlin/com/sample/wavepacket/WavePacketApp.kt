package com.sample.wavepacket

import android.app.Application
import com.sample.wavepacket.di.initKoin
import org.koin.android.ext.koin.androidContext

class WavePacketApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@WavePacketApp)
        }
    }
}
