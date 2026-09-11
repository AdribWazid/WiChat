package com.example.data

import android.content.Context
import com.example.model.AppThemeMode
import com.example.model.LocalNetworkInfo
import com.example.model.MessageEntity
import com.example.model.PeerEntity
import com.example.model.UserProfile
import com.example.network.NetworkUtils
import com.example.network.WiChatConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val database = AppDatabase.getDatabase(context)
    private val messageDao = database.messageDao()
    private val peerDao = database.peerDao()
    private val userPrefs = UserPreferences(context)
    val connectionManager = WiChatConnectionManager(context, scope)

    private val _userProfile = MutableStateFlow(userPrefs.getUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _themeMode = MutableStateFlow(userPrefs.getThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _networkInfo = MutableStateFlow(LocalNetworkInfo())
    val networkInfo: StateFlow<LocalNetworkInfo> = _networkInfo.asStateFlow()

    val allPeers: Flow<List<PeerEntity>> = peerDao.getAllPeers()
    val onlinePeers: Flow<List<PeerEntity>> = peerDao.getOnlinePeers()

    init {
        refreshNetworkInfo()
        if (_userProfile.value.isRegistered) {
            connectionManager.start()
        }
    }

    fun refreshNetworkInfo() {
        val isWifi = NetworkUtils.isWifiOrLanConnected(context)
        val ip = NetworkUtils.getLocalIpAddress()
        val ssid = NetworkUtils.getWifiName(context)
        val broadcasts = NetworkUtils.getBroadcastAddresses()
        val bcastStr = if (broadcasts.isNotEmpty()) broadcasts[0].hostAddress ?: "255.255.255.255" else "255.255.255.255"

        _networkInfo.value = LocalNetworkInfo(
            isConnectedToWifi = isWifi,
            ipAddress = ip,
            ssid = ssid,
            subnetBroadcast = bcastStr,
            listeningPort = connectionManager.currentPort
        )
    }

    fun getMessages(peerId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(peerId)
    }

    fun getPeer(peerId: String): Flow<PeerEntity?> {
        return peerDao.getPeerFlowById(peerId)
    }

    fun saveProfile(displayName: String) {
        userPrefs.saveProfile(displayName, true)
        _userProfile.value = userPrefs.getUserProfile()
        connectionManager.start()
        refreshNetworkInfo()
    }

    fun saveProfileWithAvatar(displayName: String, avatarUri: String?) {
        userPrefs.saveProfileWithAvatar(displayName, avatarUri, true)
        _userProfile.value = userPrefs.getUserProfile()
        connectionManager.start()
        refreshNetworkInfo()
    }

    fun copyAvatarFromUri(sourceUri: android.net.Uri): String? {
        return userPrefs.copyUriToLocalAvatar(sourceUri)
    }

    fun removeAvatar() {
        userPrefs.removeAvatar()
        _userProfile.value = userPrefs.getUserProfile()
    }

    fun saveThemeMode(mode: AppThemeMode) {
        userPrefs.saveThemeMode(mode)
        _themeMode.value = mode
    }

    suspend fun sendMessage(peerId: String, content: String): Boolean {
        val peer = peerDao.getPeerById(peerId) ?: return false
        connectionManager.sendMessage(peer, content)
        return true
    }

    suspend fun retryMessage(message: MessageEntity) {
        val peer = peerDao.getPeerById(message.conversationId) ?: return
        connectionManager.retryMessage(message, peer)
    }

    suspend fun directIpConnect(ip: String, port: Int, onProgress: (String) -> Unit): Result<PeerEntity> {
        val result = connectionManager.directIpConnect(ip, port, onProgress)
        refreshNetworkInfo()
        return result
    }

    suspend fun addPeerFromQr(userId: String, name: String, ip: String, port: Int): PeerEntity {
        val existing = peerDao.getPeerById(userId)
        val peer = PeerEntity(
            userId = userId,
            displayName = name,
            ipAddress = ip,
            port = port,
            isOnline = true,
            lastSeen = System.currentTimeMillis(),
            isDirectOrQr = true
        )
        peerDao.insertOrUpdate(peer)
        return peer
    }

    fun onNetworkChanged() {
        scope.launch(Dispatchers.IO) {
            peerDao.markAllOffline()
            refreshNetworkInfo()
            connectionManager.start()
        }
    }
}
