package com.sample.wavepacket.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

actual class RoomDatabaseFactory {
    actual fun create(): ChatDatabase {
        val dbFilePath = NSHomeDirectory() + "/chat.db"
        return Room.databaseBuilder<ChatDatabase>(
            name = dbFilePath,
            factory = { ChatDatabaseConstructor.initialize() }
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
