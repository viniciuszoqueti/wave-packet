package com.sample.wavepacket

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatMessage(
    val id: Int,
    val sender: String,
    val time: String,
    val text: String,
    val isFromMe: Boolean,
    val isSystem: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6750A4),
            onPrimary = Color.White,
            surface = Color(0xFFFBF8FD),
            onSurface = Color.Black,
            secondaryContainer = Color(0xFFEADDFF)
        )
    ) {
        val messages = remember {
            mutableStateListOf(
                ChatMessage(
                    1,
                    "OP_OMEGA_4",
                    "14:02",
                    "COORDINATES RECEIVED. PROCEED TO EXTRACTION POINT BRAVO. SIGNAL STRENGTH NOMINAL.",
                    false
                ),
                ChatMessage(
                    2,
                    "YOU",
                    "14:03",
                    "ROGER. ETA 0400 HOURS. ENCRYPTING REMAINING PACKETS FOR BURST TRANSMISSION.",
                    true
                ),
                ChatMessage(3, "", "", "AFSK Handshake Established", false, isSystem = true),
                ChatMessage(
                    4,
                    "OP_OMEGA_4",
                    "14:05",
                    "COPY THAT. AFSK TONE BURST RECEIVED AT -84DBM. STANDING BY FOR FINAL DECODE.",
                    false
                )
            )
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "AFSK Messaging",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            bottomBar = {
                ChatInputBar()
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFFBF8FD)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    if (message.isSystem) {
                        SystemMessageItem(message.text)
                    } else {
                        ChatMessageItem(message)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        // Header: Nome do remetente e Horário
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
                    text = "YOU",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (message.isFromMe) {
                // Pequeno ícone de waveform à esquerda das minhas mensagens
                WaveformIcon(modifier = Modifier.padding(end = 8.dp))
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
                    .background(if (message.isFromMe) Color(0xFF6750A4) else Color(0xFFE9E7EC))
                    .padding(16.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isFromMe) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                )
            }

            if (!message.isFromMe) {
                // Ícone de Play à direita das mensagens recebidas
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(24.dp).padding(start = 4.dp)
                )
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
        val barColors = listOf(0xFFD0BCFF, 0xFF6750A4, 0xFFD0BCFF)
        val heights = listOf(8.dp, 16.dp, 10.dp)
        heights.forEachIndexed { index, height ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(height)
                    .background(Color(barColors[index % barColors.size]), CircleShape)
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
            color = Color(0xFFF2F0F4),
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
fun ChatInputBar() {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Gray)
            }

            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Message", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F0F4),
                    unfocusedContainerColor = Color(0xFFF2F0F4),
                    disabledContainerColor = Color(0xFFF2F0F4),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = CircleShape,
                singleLine = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
