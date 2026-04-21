package com.sample.wavepacket.di

import com.sample.wavepacket.ChatViewModel
import com.sample.wavepacket.database.ChatDatabase
import com.sample.wavepacket.database.RoomDatabaseFactory
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single { get<RoomDatabaseFactory>().create() }
    single { get<ChatDatabase>().chatDao() }
    viewModel { ChatViewModel(get(), get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule, platformModule)
    }
}
