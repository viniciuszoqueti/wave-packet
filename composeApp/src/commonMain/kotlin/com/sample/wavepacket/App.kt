package com.sample.wavepacket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sample.wavepacket.ui.BackgroundLight
import com.sample.wavepacket.ui.IconBackgroundLight
import com.sample.wavepacket.ui.Purple40
import com.sample.wavepacket.ui.Purple80
import com.sample.wavepacket.ui.SurfaceLight
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import wavepacket.composeapp.generated.resources.Res
import wavepacket.composeapp.generated.resources.afsk_listen
import wavepacket.composeapp.generated.resources.app_name
import wavepacket.composeapp.generated.resources.close
import wavepacket.composeapp.generated.resources.edit
import wavepacket.composeapp.generated.resources.listening_afsk
import wavepacket.composeapp.generated.resources.menu
import wavepacket.composeapp.generated.resources.message
import wavepacket.composeapp.generated.resources.play
import wavepacket.composeapp.generated.resources.play_afsk_desc
import wavepacket.composeapp.generated.resources.play_afsk_title
import wavepacket.composeapp.generated.resources.send
import wavepacket.composeapp.generated.resources.sender_you

@Composable
fun App(viewModel: ChatViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    AppContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    state: ChatState,
    onIntent: (ChatIntent) -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Purple40,
            onPrimary = Color.White,
            surface = BackgroundLight,
            onSurface = Color.Black,
            secondaryContainer = Purple80
        )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(Res.string.menu)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(Res.string.edit)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            bottomBar = {
                ChatInputBar(
                    text = state.inputText,
                    isListening = state.isListening,
                    micPermissionGranted = state.micPermissionGranted,
                    onTextChange = { onIntent(ChatIntent.OnInputChanged(it)) },
                    onSend = { onIntent(ChatIntent.OnSendMessage) },
                    onToggleListening = { onIntent(ChatIntent.OnToggleListening) },
                    onRequestPermission = { onIntent(ChatIntent.RequestMicPermission) }
                )
            }
        ) { padding ->
            val listState = rememberLazyListState()

            LaunchedEffect(state.messages.size) {
                if (state.messages.isNotEmpty()) {
                    listState.animateScrollToItem(state.messages.lastIndex)
                }
            }

            if (state.showAfskDialog) {
                AfskPlaybackDialog(
                    onDismiss = { onIntent(ChatIntent.OnDismissAfskDialog) },
                    onPlay = { onIntent(ChatIntent.OnPlayAfskSignal) }
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(BackgroundLight),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    if (message.isSystem) {
                        SystemMessageItem(message.text)
                    } else {
                        ChatMessageItem(
                            message = message,
                            onPlayClick = { onIntent(ChatIntent.OnShowAfskDialog(message.text)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onPlayClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (!message.isFromMe) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            } else {
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.sender_you),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Purple40
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (message.isFromMe) {
                WaveformIcon(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { onPlayClick() }
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (message.isFromMe) 20.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 20.dp
                        )
                    )
                    .background(if (message.isFromMe) Purple40 else SurfaceLight)
                    .padding(16.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isFromMe) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                )
            }

            if (!message.isFromMe) {
                IconButton(onClick = onPlayClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Purple40,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WaveformIcon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val barColors = listOf(Purple80, Purple40, Purple80)
        val heights = listOf(8.dp, 16.dp, 10.dp)
        heights.forEachIndexed { index, height ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(height)
                    .background(barColors[index % barColors.size], CircleShape)
            )
        }
    }
}

@Composable
fun SystemMessageItem(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = BackgroundLight,
            shape = CircleShape
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    isListening: Boolean,
    micPermissionGranted: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleListening: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (micPermissionGranted) {
                    onToggleListening()
                } else {
                    onRequestPermission()
                }
            }) {
                Icon(
                    imageVector = if (!micPermissionGranted) Icons.Default.MicOff
                    else if (isListening) Icons.Default.MicOff
                    else Icons.Default.Mic,
                    contentDescription = stringResource(Res.string.afsk_listen),
                    tint = if (!micPermissionGranted) Color.LightGray
                    else if (isListening) Color.Red
                    else Color.Gray
                )
            }

            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        if (isListening) stringResource(Res.string.listening_afsk) else stringResource(
                            Res.string.message
                        ),
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
                enabled = !isListening,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BackgroundLight,
                    unfocusedContainerColor = BackgroundLight,
                    disabledContainerColor = BackgroundLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledPlaceholderColor = Color.Gray
                ),
                shape = CircleShape,
                singleLine = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            FloatingActionButton(
                onClick = onSend,
                containerColor = Purple40,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(Res.string.send),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AfskPlaybackDialog(
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IconBackgroundLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.play_afsk_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.play_afsk_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(Res.string.play),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.close),
                        color = Purple40,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AfskPlaybackDialogPreview() {
    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.5f))) {
            AfskPlaybackDialog(onDismiss = {}, onPlay = {})
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    AppContent(
        state = ChatState(
            messages = listOf(
                ChatMessage(
                    id = 1,
                    sender = "OP_OMEGA_4",
                    time = "10:00",
                    text = "Incoming message via AFSK...",
                    isFromMe = false
                ),
                ChatMessage(
                    id = 2,
                    sender = "YOU",
                    time = "10:01",
                    text = "Copy that. Standing by.",
                    isFromMe = true
                ),
                ChatMessage(
                    id = 3,
                    sender = "SYSTEM",
                    time = "10:02",
                    text = "Signal strength: High",
                    isFromMe = false,
                    isSystem = true
                )
            ),
            inputText = "Hello World",
            isListening = false,
            showAfskDialog = false
        ),
        onIntent = {}
    )
}

@Preview
@Composable
fun ChatMessageMePreview() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ChatMessageItem(
                message = ChatMessage(
                    id = 1,
                    sender = "YOU",
                    time = "10:00",
                    text = "This is a message from me.",
                    isFromMe = true
                ),
                onPlayClick = {}
            )
        }
    }
}

@Preview
@Composable
fun ChatMessageOtherPreview() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ChatMessageItem(
                message = ChatMessage(
                    id = 1,
                    sender = "OP_OMEGA_4",
                    time = "10:01",
                    text = "This is a message from someone else.",
                    isFromMe = false
                ),
                onPlayClick = {}
            )
        }
    }
}

@Preview
@Composable
fun SystemMessagePreview() {
    MaterialTheme {
        SystemMessageItem("System Notification: Connection established.")
    }
}

@Preview
@Composable
fun ChatInputBarPreview() {
    MaterialTheme {
        ChatInputBar(
            text = "Draft message",
            isListening = false,
            micPermissionGranted = true,
            onTextChange = {},
            onSend = {},
            onToggleListening = {},
            onRequestPermission = {}
        )
    }
}

@Preview
@Composable
fun ChatInputBarListeningPreview() {
    MaterialTheme {
        ChatInputBar(
            text = "",
            isListening = true,
            micPermissionGranted = true,
            onTextChange = {},
            onSend = {},
            onToggleListening = {},
            onRequestPermission = {}
        )
    }
}
