package com.sample.wavepacket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val permissionManager: AndroidPermissionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        permissionManager.register(this)

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
