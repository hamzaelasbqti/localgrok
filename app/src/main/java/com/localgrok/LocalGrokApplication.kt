package com.localgrok

import android.app.Application
import com.localgrok.data.local.LocalGrokDatabase
import com.localgrok.data.repository.ChatRepository
import com.localgrok.data.repository.SettingsRepository

/**
 * Application class for LocalGrok
 * Provides singleton instances of database and repositories
 */
class LocalGrokApplication : Application() {

    // Database
    val database: LocalGrokDatabase by lazy {
        LocalGrokDatabase.getDatabase(this)
    }

    // Repositories
    val chatRepository: ChatRepository by lazy {
        ChatRepository(
            chatDao = database.chatDao(),
            messageDao = database.messageDao()
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }

}

