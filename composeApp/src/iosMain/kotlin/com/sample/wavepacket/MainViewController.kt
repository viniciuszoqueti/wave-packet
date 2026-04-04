package com.sample.wavepacket

import androidx.compose.ui.window.ComposeUIViewController
import com.sample.wavepacket.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}
