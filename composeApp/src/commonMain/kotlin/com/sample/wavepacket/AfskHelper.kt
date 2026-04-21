package com.sample.wavepacket

import kotlinx.coroutines.flow.Flow

interface AfskHelper {
    suspend fun playTextAsAfsk(text: String)
    fun startListening(): Flow<String>
    fun stopListening()
    suspend fun hasMicPermission(): Boolean
    suspend fun requestMicPermission(): Boolean
}
