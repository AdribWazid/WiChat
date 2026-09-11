package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}

enum class MessageDirection {
    OUTGOING,
    INCOMING
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val conversationId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENDING,
    val direction: MessageDirection = MessageDirection.OUTGOING
)

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val ipAddress: String,
    val port: Int = 8888,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isDirectOrQr: Boolean = false
)

data class UserProfile(
    val userId: String,
    val displayName: String,
    val avatarUri: String? = null,
    val preferredPort: Int = 8888,
    val avatarColorIndex: Int = 0,
    val isRegistered: Boolean = false
)

data class LocalNetworkInfo(
    val isConnectedToWifi: Boolean = false,
    val ipAddress: String = "127.0.0.1",
    val ssid: String = "Not connected",
    val subnetBroadcast: String = "255.255.255.255",
    val listeningPort: Int = 8888
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    VERIFYING,
    CONNECTED,
    FAILED
}

enum class AppThemeMode(val key: String, val title: String, val description: String) {
    AUTO(
        key = "auto",
        title = "Auto (Day – Light, Night – Dark)",
        description = "Automatically use Light mode during the day and Dark mode at night/system dark mode"
    ),
    LIGHT(
        key = "light",
        title = "Light",
        description = "Always use the Light theme"
    ),
    DARK(
        key = "dark",
        title = "Dark",
        description = "Always use the Dark theme"
    );

    companion object {
        fun fromKey(key: String?): AppThemeMode =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: AUTO
    }
}
