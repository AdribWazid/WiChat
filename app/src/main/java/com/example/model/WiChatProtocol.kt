package com.example.model

import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WiChatProtocol {
    const val PROTOCOL_NAME = "WICHAT"
    const val CURRENT_VERSION = 1
    const val DEFAULT_PORT = 8888
    const val DISCOVERY_PORT = 52525
    const val HEARTBEAT_TIMEOUT_MS = 10000L // 10s without beacon/ping = offline
    const val HEARTBEAT_INTERVAL_MS = 3000L // Broadcast beacon every 3s

    const val TYPE_HANDSHAKE = "HANDSHAKE"
    const val TYPE_HANDSHAKE_ACK = "HANDSHAKE_ACK"
    const val TYPE_CHAT_MESSAGE = "CHAT_MESSAGE"
    const val TYPE_MESSAGE_ACK = "MESSAGE_ACK"
    const val TYPE_PING = "PING"
    const val TYPE_PONG = "PONG"
    const val TYPE_BEACON = "BEACON"

    fun createBeaconJson(userId: String, displayName: String, port: Int): String {
        return JSONObject().apply {
            put("protocol", PROTOCOL_NAME)
            put("type", TYPE_BEACON)
            put("version", CURRENT_VERSION)
            put("userId", userId)
            put("displayName", displayName)
            put("port", port)
            put("timestamp", System.currentTimeMillis())
        }.toString()
    }

    fun parseBeacon(jsonString: String): BeaconData? {
        return try {
            val json = JSONObject(jsonString)
            if (json.optString("protocol") != PROTOCOL_NAME) return null
            if (json.optString("type") != TYPE_BEACON) return null
            val userId = json.getString("userId")
            val displayName = json.getString("displayName")
            val port = json.optInt("port", DEFAULT_PORT)
            val version = json.optInt("version", 1)
            BeaconData(userId, displayName, port, version)
        } catch (_: Exception) {
            null
        }
    }

    fun createHandshakeJson(userId: String, displayName: String, port: Int): String {
        return JSONObject().apply {
            put("protocol", PROTOCOL_NAME)
            put("type", TYPE_HANDSHAKE)
            put("version", CURRENT_VERSION)
            put("userId", userId)
            put("displayName", displayName)
            put("port", port)
            put("timestamp", System.currentTimeMillis())
        }.toString()
    }

    fun createHandshakeAckJson(userId: String, displayName: String, port: Int, success: Boolean): String {
        return JSONObject().apply {
            put("protocol", PROTOCOL_NAME)
            put("type", TYPE_HANDSHAKE_ACK)
            put("version", CURRENT_VERSION)
            put("userId", userId)
            put("displayName", displayName)
            put("port", port)
            put("success", success)
            put("timestamp", System.currentTimeMillis())
        }.toString()
    }

    fun createChatMessageJson(
        messageId: String,
        senderId: String,
        receiverId: String,
        content: String,
        timestamp: Long
    ): String {
        return JSONObject().apply {
            put("protocol", PROTOCOL_NAME)
            put("type", TYPE_CHAT_MESSAGE)
            put("messageId", messageId)
            put("senderId", senderId)
            put("receiverId", receiverId)
            put("content", content)
            put("timestamp", timestamp)
        }.toString()
    }

    fun createMessageAckJson(messageId: String, receiverId: String): String {
        return JSONObject().apply {
            put("protocol", PROTOCOL_NAME)
            put("type", TYPE_MESSAGE_ACK)
            put("messageId", messageId)
            put("receiverId", receiverId)
            put("timestamp", System.currentTimeMillis())
        }.toString()
    }

    fun createPingJson(userId: String): String {
        return JSONObject().apply {
            put("protocol", PROTOCOL_NAME)
            put("type", TYPE_PING)
            put("userId", userId)
            put("timestamp", System.currentTimeMillis())
        }.toString()
    }

    fun createPongJson(userId: String): String {
        return JSONObject().apply {
            put("protocol", PROTOCOL_NAME)
            put("type", TYPE_PONG)
            put("userId", userId)
            put("timestamp", System.currentTimeMillis())
        }.toString()
    }

    fun generateQrPayload(userId: String, displayName: String, ipAddress: String, port: Int): String {
        val encodedName = URLEncoder.encode(displayName, StandardCharsets.UTF_8.name())
        return "wichat://connect?uid=$userId&name=$encodedName&ip=$ipAddress&port=$port&v=$CURRENT_VERSION"
    }

    fun parseQrPayload(payload: String): QrPeerInfo? {
        return try {
            val uri = URI(payload.trim())
            if (uri.scheme != "wichat" || uri.host != "connect") return null
            val query = uri.rawQuery ?: return null
            val params = query.split("&").associate { param ->
                val parts = param.split("=")
                if (parts.size == 2) parts[0] to URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
                else parts[0] to ""
            }
            val userId = params["uid"] ?: return null
            val name = params["name"] ?: "WiChat User"
            val ip = params["ip"] ?: return null
            val port = params["port"]?.toIntOrNull() ?: DEFAULT_PORT
            val version = params["v"]?.toIntOrNull() ?: 1
            QrPeerInfo(userId, name, ip, port, version)
        } catch (_: Exception) {
            null
        }
    }
}

data class BeaconData(
    val userId: String,
    val displayName: String,
    val port: Int,
    val version: Int
)

data class QrPeerInfo(
    val userId: String,
    val displayName: String,
    val ipAddress: String,
    val port: Int,
    val version: Int
)
