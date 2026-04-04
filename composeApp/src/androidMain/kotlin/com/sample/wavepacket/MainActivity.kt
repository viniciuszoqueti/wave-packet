package com.sample.wavepacket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sample.wavepacket.di.sharedModule
import org.koin.compose.KoinApplication
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AppContent(
        state = ChatState(
            messages = listOf(
                ChatMessage(
                    id = 1,
                    sender = "SYSTEM",
                    time = "12:00",
                    text = "Android Preview Mode",
                    isFromMe = false,
                    isSystem = true
                )
            )
        ),
        onIntent = {}
    )
}
