package com.localgrok.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.localgrok.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "localgrok_settings")

/**
 * Repository for managing user settings via DataStore
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val SERVER_IP_KEY = stringPreferencesKey("server_ip")
        private val SERVER_PORT_KEY = intPreferencesKey("server_port")
        private val SEARXNG_PORT_KEY = intPreferencesKey("searxng_port")
        private val DEFAULT_MODEL_KEY = stringPreferencesKey("default_model")
        private val APP_THEME_KEY = stringPreferencesKey("app_theme")

        const val DEFAULT_PORT = 11434
        const val DEFAULT_SEARXNG_PORT = 8080
        const val DEFAULT_MODEL = "gemma3:1b-it-qat"
    }

    /**
     * Get the server IP address
     */
    val serverIp: Flow<String> = context.dataStore.data.map { preferences ->
        val ip = preferences[SERVER_IP_KEY]
        // Treat empty strings as null to prevent crashes, default to loopback
        if (ip.isNullOrBlank()) "127.0.0.1" else ip
    }

    /**
     * Get the server port
     */
    val serverPort: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SERVER_PORT_KEY] ?: DEFAULT_PORT
    }

    /**
     * Get the SearXNG port (runs on same server as Ollama)
     */
    val searxngPort: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SEARXNG_PORT_KEY] ?: DEFAULT_SEARXNG_PORT
    }

    /**
     * Get the default model name
     */
    val defaultModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_MODEL_KEY] ?: DEFAULT_MODEL
    }

    /**
     * Get the app theme
     */
    val appTheme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[APP_THEME_KEY] ?: AppTheme.DARK.name
        try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            AppTheme.DARK
        }
    }

    /**
     * Save server IP address
     */
    suspend fun setServerIp(ip: String) {
        context.dataStore.edit { preferences ->
            // Store empty string as null to prevent crashes
            if (ip.isBlank()) {
                preferences.remove(SERVER_IP_KEY)
            } else {
                preferences[SERVER_IP_KEY] = ip.trim()
            }
        }
    }

    /**
     * Save server port
     */
    suspend fun setServerPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_PORT_KEY] = port
        }
    }

    /**
     * Save SearXNG port
     */
    suspend fun setSearxngPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[SEARXNG_PORT_KEY] = port
        }
    }

    /**
     * Save default model
     */
    suspend fun setDefaultModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_MODEL_KEY] = model
        }
    }

    /**
     * Save app theme
     */
    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = theme.name
        }
    }

    /**
     * Clear all settings
     */
    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

