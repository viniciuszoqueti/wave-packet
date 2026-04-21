package com.sample.wavepacket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.wavepacket.database.ChatDao
import com.sample.wavepacket.database.ChatMessageEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    val isListening: Boolean = false,
    val showAfskDialog: Boolean = false,
    val micPermissionGranted: Boolean = false,
    val pendingAfskText: String? = null
)

sealed interface ChatIntent {
    data class OnInputChanged(val text: String) : ChatIntent
    object OnSendMessage : ChatIntent
    object OnToggleListening : ChatIntent
    object OnDismissAfskDialog : ChatIntent
    object OnPlayAfskSignal : ChatIntent
    data class OnShowAfskDialog(val text: String) : ChatIntent
    object RequestMicPermission : ChatIntent
}

class ChatViewModel(
    private val chatDao: ChatDao,
    private val afskHelper: AfskHelper
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var listeningJob: Job? = null

    init {
        observeMessages()
        checkMicPermission()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatDao.getAllMessages().collect { entities ->
                _state.update { it.copy(messages = entities.map { e -> e.toDomain() }) }
            }
        }
    }

    private fun checkMicPermission() {
        viewModelScope.launch {
            val granted = afskHelper.hasMicPermission()
            _state.update { it.copy(micPermissionGranted = granted) }
        }
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.OnInputChanged -> {
                _state.update { it.copy(inputText = intent.text) }
            }

            ChatIntent.OnSendMessage -> {
                sendMessage()
            }

            ChatIntent.OnToggleListening -> {
                val newState = !_state.value.isListening
                _state.update { it.copy(isListening = newState) }
                if (newState) {
                    startAfskListening()
                } else {
                    stopAfskListening()
                }
            }

            ChatIntent.OnDismissAfskDialog -> {
                _state.update { it.copy(showAfskDialog = false, pendingAfskText = null) }
            }

            ChatIntent.OnPlayAfskSignal -> {
                playAfskSignal()
            }

            is ChatIntent.OnShowAfskDialog -> {
                _state.update { it.copy(pendingAfskText = intent.text, showAfskDialog = true) }
            }

            ChatIntent.RequestMicPermission -> {
                viewModelScope.launch {
                    val granted = afskHelper.requestMicPermission()
                    _state.update { it.copy(micPermissionGranted = granted) }
                }
            }
        }
    }

    private fun playAfskSignal() {
        viewModelScope.launch {
            _state.value.pendingAfskText?.let {
                afskHelper.playTextAsAfsk(it)
            }
            _state.update { it.copy(showAfskDialog = false, pendingAfskText = null) }
        }
    }

    private fun sendMessage() {
        val currentText = _state.value.inputText
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
            _state.update { it.copy(inputText = "", pendingAfskText = currentText, showAfskDialog = true) }
        }
    }

    private fun startAfskListening() {
        listeningJob?.cancel()
        listeningJob = viewModelScope.launch {
            afskHelper.startListening().collectLatest { decodedText ->
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val timeStr = "${now.hour}:${now.minute.toString().padStart(2, '0')}"

                chatDao.insertMessage(
                    ChatMessageEntity(
                        sender = "RECV_AFSK",
                        time = timeStr,
                        text = decodedText,
                        isFromMe = false
                    )
                )
            }
        }
    }

    private fun stopAfskListening() {
        afskHelper.stopListening()
        listeningJob?.cancel()
        listeningJob = null
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
