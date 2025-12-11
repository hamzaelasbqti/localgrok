package com.localgrok.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localgrok.data.repository.ChatRepository
import com.localgrok.data.repository.SettingsRepository
import com.localgrok.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for managing settings screen state
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val _appTheme = MutableStateFlow(AppTheme.SPACE)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()
    
    // Track original values to detect changes
    private var originalIp: String = ""
    private var originalPort: String = ""
    private var originalSearxngPort: String = ""
    
    init {
        loadSettings()
        loadTheme()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val ip = settingsRepository.serverIp.first() ?: ""
            val port = settingsRepository.serverPort.first()
            val searxngPort = settingsRepository.searxngPort.first()
            
            originalIp = ip
            originalPort = port.toString()
            originalSearxngPort = searxngPort.toString()
            
            _uiState.value = _uiState.value.copy(
                serverIp = ip,
                serverPort = port.toString(),
                searxngPort = searxngPort.toString(),
                isDirty = false
            )
        }
    }
    
    private fun loadTheme() {
        viewModelScope.launch {
            settingsRepository.appTheme.collect { theme ->
                _appTheme.value = theme
            }
        }
    }
    
    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setAppTheme(theme)
            _appTheme.value = theme
        }
    }
    
    fun updateServerIp(ip: String) {
        _uiState.value = _uiState.value.copy(
            serverIp = ip,
            isDirty = checkIfDirty(ip, _uiState.value.serverPort, _uiState.value.searxngPort)
        )
    }
    
    fun updateServerPort(port: String) {
        _uiState.value = _uiState.value.copy(
            serverPort = port,
            isDirty = checkIfDirty(_uiState.value.serverIp, port, _uiState.value.searxngPort)
        )
    }
    
    fun updateSearxngPort(port: String) {
        _uiState.value = _uiState.value.copy(
            searxngPort = port,
            isDirty = checkIfDirty(_uiState.value.serverIp, _uiState.value.serverPort, port)
        )
    }
    
    private fun checkIfDirty(ip: String, port: String, searxngPort: String): Boolean {
        return ip != originalIp || port != originalPort || searxngPort != originalSearxngPort
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            val port = state.serverPort.toIntOrNull() ?: SettingsRepository.DEFAULT_PORT
            val searxngPort = state.searxngPort.toIntOrNull() ?: SettingsRepository.DEFAULT_SEARXNG_PORT
            
            settingsRepository.setServerIp(state.serverIp)
            settingsRepository.setServerPort(port)
            settingsRepository.setSearxngPort(searxngPort)
            
            // Update original values after save
            originalIp = state.serverIp
            originalPort = state.serverPort
            originalSearxngPort = state.searxngPort
            
            _uiState.value = _uiState.value.copy(
                isSaved = true,
                isDirty = false,
                message = "Settings saved"
            )
            
            // Configure and test connection after saving
            chatRepository.configureServer(state.serverIp, port)
            chatRepository.configureSearxng(state.serverIp, searxngPort)
        }
    }
    
    fun hasUnsavedChanges(): Boolean {
        return _uiState.value.isDirty
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            isSaved = false
        )
    }
}

/**
 * UI State for settings screen
 */
data class SettingsUiState(
    val serverIp: String = "",
    val serverPort: String = "11434",
    val searxngPort: String = "8888",
    val isDirty: Boolean = false,
    val message: String? = null,
    val isSaved: Boolean = false
)
