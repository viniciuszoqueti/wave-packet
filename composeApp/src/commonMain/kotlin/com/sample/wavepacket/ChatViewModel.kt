package com.sample.wavepacket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.wavepacket.database.ChatDao
import com.sample.wavepacket.database.ChatMessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ChatMessage(
    val id: Int,
    val sender: String,
    val time: String,
    val text: String,
    val isFromMe: Boolean,
    val isSystem: Boolean = false
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isListening: Boolean = false
)

sealed interface ChatIntent {
    data class OnInputChanged(val text: String) : ChatIntent
    object OnSendMessage : ChatIntent
    object OnToggleListening : ChatIntent
}

class ChatViewModel(private val chatDao: ChatDao) : ViewModel() {
    private val _inputText = MutableStateFlow("")
    private val _isListening = MutableStateFlow(false)

    val state: StateFlow<ChatState> = combine(
        chatDao.getAllMessages(),
        _inputText,
        _isListening
    ) { messages, inputText, isListening ->
        ChatState(
            messages = messages.map { it.toDomain() },
            inputText = inputText,
            isListening = isListening
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatState())

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.OnInputChanged -> {
                _inputText.value = intent.text
            }
            ChatIntent.OnSendMessage -> {
                sendMessage()
            }
            ChatIntent.OnToggleListening -> {
                _isListening.update { !it }
                if (_isListening.value) {
                    startAfskListening()
                } else {
                    stopAfskListening()
                }
            }
        }
    }

    private fun sendMessage() {
        val currentText = _inputText.value
        if (currentText.isBlank()) return

        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val timeStr = "${now.hour}:${now.minute.toString().padStart(2, '0')}"
            
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "YOU",
                    time = timeStr,
                    text = currentText,
                    isFromMe = true
                )
            )
            _inputText.value = ""
        }
    }

    private fun startAfskListening() {
        // Mocking AFSK reception after 2 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            if (_isListening.value) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val timeStr = "${now.hour}:${now.minute.toString().padStart(2, '0')}"
                
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sender = "OP_OMEGA_4",
                        time = timeStr,
                        text = "RECEIVED VIA AFSK: PACKET 0x42 DECODED.",
                        isFromMe = false
                    )
                )
                _isListening.value = false
            }
        }
    }

    private fun stopAfskListening() {
        // Logic to stop microphone/AFSK decoder
    }

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        sender = sender,
        time = time,
        text = text,
        isFromMe = isFromMe,
        isSystem = isSystem
    )
}
