package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.model.AppThemeMode
import com.example.model.ConnectionState
import com.example.model.LocalNetworkInfo
import com.example.model.MessageEntity
import com.example.model.PeerEntity
import com.example.model.QrPeerInfo
import com.example.model.UserProfile
import com.example.model.WiChatProtocol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class DirectIpUiState {
    object Idle : DirectIpUiState()
    data class Progress(val stage: ConnectionState, val message: String) : DirectIpUiState()
    data class Success(val peer: PeerEntity) : DirectIpUiState()
    data class Error(val errorMessage: String) : DirectIpUiState()
}

class WiChatViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ChatRepository(application, viewModelScope)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val networkInfo: StateFlow<LocalNetworkInfo> = repository.networkInfo
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode

    val allPeers: StateFlow<List<PeerEntity>> = repository.allPeers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val onlinePeers: StateFlow<List<PeerEntity>> = repository.onlinePeers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeerId = MutableStateFlow<String?>(null)
    val selectedPeerId: StateFlow<String?> = _selectedPeerId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChatPeer: StateFlow<PeerEntity?> = _selectedPeerId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getPeer(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<MessageEntity>> = _selectedPeerId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _directIpState = MutableStateFlow<DirectIpUiState>(DirectIpUiState.Idle)
    val directIpState: StateFlow<DirectIpUiState> = _directIpState.asStateFlow()

    fun selectPeer(peerId: String) {
        _selectedPeerId.value = peerId
    }

    fun clearSelectedPeer() {
        _selectedPeerId.value = null
    }

    fun saveProfile(displayName: String) {
        repository.saveProfile(displayName)
    }

    fun saveProfileWithAvatar(displayName: String, avatarUri: String?) {
        repository.saveProfileWithAvatar(displayName, avatarUri)
    }

    fun copyAvatarFromUri(sourceUri: android.net.Uri): String? {
        return repository.copyAvatarFromUri(sourceUri)
    }

    fun removeAvatar() {
        repository.removeAvatar()
    }

    fun setThemeMode(mode: AppThemeMode) {
        repository.saveThemeMode(mode)
    }

    fun refreshNetwork() {
        repository.refreshNetworkInfo()
    }

    fun sendMessage(content: String) {
        val peerId = _selectedPeerId.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(peerId, content.trim())
        }
    }

    fun retryMessage(message: MessageEntity) {
        viewModelScope.launch {
            repository.retryMessage(message)
        }
    }

    fun resetDirectIpState() {
        _directIpState.value = DirectIpUiState.Idle
    }

    fun connectDirectIp(ip: String, port: Int, onSuccess: (PeerEntity) -> Unit) {
        val cleanIp = ip.trim()
        if (cleanIp.isEmpty()) {
            _directIpState.value = DirectIpUiState.Error("Please enter a valid IP address")
            return
        }
        if (port !in 1024..65535) {
            _directIpState.value = DirectIpUiState.Error("Port must be between 1024 and 65535")
            return
        }

        viewModelScope.launch {
            _directIpState.value = DirectIpUiState.Progress(ConnectionState.CONNECTING, "Connecting to $cleanIp:$port...")
            val result = repository.directIpConnect(cleanIp, port) { progressMsg ->
                val stage = if (progressMsg.contains("Verifying")) ConnectionState.VERIFYING else ConnectionState.CONNECTING
                _directIpState.value = DirectIpUiState.Progress(stage, progressMsg)
            }

            result.onSuccess { peer ->
                _directIpState.value = DirectIpUiState.Success(peer)
                _selectedPeerId.value = peer.userId
                onSuccess(peer)
            }.onFailure { err ->
                _directIpState.value = DirectIpUiState.Error(err.message ?: "Connection failed. Check if remote device is running WiChat.")
            }
        }
    }

    fun connectFromQr(payload: String, onSuccess: (PeerEntity) -> Unit, onError: (String) -> Unit) {
        val qrInfo = WiChatProtocol.parseQrPayload(payload)
        if (qrInfo == null) {
            onError("Invalid WiChat QR Code. Please scan a valid WiChat code.")
            return
        }
        connectFromQrInfo(qrInfo, onSuccess, onError)
    }

    fun connectFromQrInfo(qrInfo: QrPeerInfo, onSuccess: (PeerEntity) -> Unit, onError: (String) -> Unit) {
        if (qrInfo.userId == userProfile.value.userId) {
            onError("Cannot connect to your own QR code.")
            return
        }

        viewModelScope.launch {
            val result = repository.directIpConnect(qrInfo.ipAddress, qrInfo.port) { _ -> }
            if (result.isSuccess) {
                val peer = result.getOrThrow()
                _selectedPeerId.value = peer.userId
                onSuccess(peer)
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Target device (${qrInfo.ipAddress}:${qrInfo.port}) is unreachable. Ensure both devices are on the same local Wi-Fi."
                onError(errorMsg)
            }
        }
    }
}
