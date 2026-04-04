package com.sample.wavepacket.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual class RoomDatabaseFactory(private val context: Context) {
    actual fun create(): ChatDatabase {
        val dbFile = context.getDatabasePath("chat.db")
        return Room.databaseBuilder<ChatDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath,
            factory = { ChatDatabaseConstructor.initialize() }
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
