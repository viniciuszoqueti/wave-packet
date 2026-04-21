package com.sample.wavepacket.di

import com.sample.wavepacket.AfskHelper
import com.sample.wavepacket.IosAfskHelper
import com.sample.wavepacket.database.RoomDatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { RoomDatabaseFactory() }
    single<AfskHelper> { IosAfskHelper() }
}
