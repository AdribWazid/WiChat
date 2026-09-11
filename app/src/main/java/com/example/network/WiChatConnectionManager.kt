package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.UserPreferences
import com.example.model.MessageDirection
import com.example.model.MessageEntity
import com.example.model.MessageStatus
import com.example.model.PeerEntity
import com.example.model.WiChatProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

class WiChatConnectionManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "WiChat_Conn"
    private val database = AppDatabase.getDatabase(context)
    private val messageDao = database.messageDao()
    private val peerDao = database.peerDao()
    private val userPrefs = UserPreferences(context)

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var watchdogJob: Job? = null
    private var udpEngine: UdpDiscoveryEngine? = null

    var currentPort: Int = WiChatProtocol.DEFAULT_PORT
        private set

    fun start() {
        stop()
        val profile = userPrefs.getUserProfile()
        currentPort = profile.preferredPort

        startServer()
        startWatchdog()

        udpEngine = UdpDiscoveryEngine(context, scope) { peerId, name, ip, port ->
            handleDiscoveredPeer(peerId, name, ip, port)
        }
        udpEngine?.start(profile.userId, profile.displayName, currentPort)
    }

    fun stop() {
        udpEngine?.stop()
        udpEngine = null

        watchdogJob?.cancel()
        watchdogJob = null

        serverJob?.cancel()
        serverJob = null

        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
    }

    private fun startServer() {
        serverJob = scope.launch(Dispatchers.IO) {
            try {
                // Try configured port, fallback to port+1 or available if busy
                var port = currentPort
                var bound = false
                var attempts = 0
                while (!bound && attempts < 5) {
                    try {
                        serverSocket = ServerSocket(port)
                        currentPort = port
                        bound = true
                    } catch (e: Exception) {
                        port++
                        attempts++
                    }
                }

                if (!bound) {
                    serverSocket = ServerSocket(0)
                    currentPort = serverSocket?.localPort ?: WiChatProtocol.DEFAULT_PORT
                }

                while (isActive && serverSocket?.isClosed == false) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        handleIncomingConnection(clientSocket)
                    } catch (e: Exception) {
                        if (!isActive || serverSocket?.isClosed == true) break
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Server error: ${e.message}")
            }
        }
    }

    private fun handleIncomingConnection(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            val remoteIp = socket.inetAddress.hostAddress ?: ""
            try {
                socket.soTimeout = 15000
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
                val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)

                val line = reader.readLine()
                if (line != null) {
                    processIncomingJson(line, remoteIp, writer)
                }
            } catch (e: Exception) {
                Log.d(tag, "Client connection handler ended: ${e.message}")
            } finally {
                try {
                    socket.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    private suspend fun processIncomingJson(jsonStr: String, remoteIp: String, writer: PrintWriter) {
        try {
            val json = JSONObject(jsonStr)
            val type = json.optString("type")
            val myProfile = userPrefs.getUserProfile()

            when (type) {
                WiChatProtocol.TYPE_HANDSHAKE -> {
                    val peerId = json.getString("userId")
                    val peerName = json.getString("displayName")
                    val peerPort = json.optInt("port", WiChatProtocol.DEFAULT_PORT)

                    val existingPeer = peerDao.getPeerById(peerId)
                    val updatedPeer = PeerEntity(
                        userId = peerId,
                        displayName = peerName,
                        ipAddress = remoteIp,
                        port = peerPort,
                        isOnline = true,
                        lastSeen = System.currentTimeMillis(),
                        isDirectOrQr = existingPeer?.isDirectOrQr ?: false
                    )
                    peerDao.insertOrUpdate(updatedPeer)

                    // Reply with Handshake ACK
                    val ack = WiChatProtocol.createHandshakeAckJson(
                        myProfile.userId,
                        myProfile.displayName,
                        currentPort,
                        true
                    )
                    writer.println(ack)
                }

                WiChatProtocol.TYPE_CHAT_MESSAGE -> {
                    val messageId = json.getString("messageId")
                    val senderId = json.getString("senderId")
                    val receiverId = json.getString("receiverId")
                    val content = json.getString("content")
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())

                    val incomingMessage = MessageEntity(
                        id = messageId,
                        senderId = senderId,
                        receiverId = receiverId,
                        conversationId = senderId,
                        content = content,
                        timestamp = timestamp,
                        status = MessageStatus.DELIVERED,
                        direction = MessageDirection.INCOMING
                    )
                    messageDao.insertMessage(incomingMessage)

                    // Immediately reply with MESSAGE_ACK so sender knows it was DELIVERED
                    val ackJson = WiChatProtocol.createMessageAckJson(messageId, myProfile.userId)
                    writer.println(ackJson)

                    // Update peer last seen & online
                    peerDao.updateStatus(senderId, true, System.currentTimeMillis())
                }

                WiChatProtocol.TYPE_MESSAGE_ACK -> {
                    val messageId = json.getString("messageId")
                    messageDao.updateStatus(messageId, MessageStatus.DELIVERED)
                }

                WiChatProtocol.TYPE_PING -> {
                    val peerId = json.getString("userId")
                    peerDao.updateStatus(peerId, true, System.currentTimeMillis())
                    val pong = WiChatProtocol.createPongJson(myProfile.userId)
                    writer.println(pong)
                }

                WiChatProtocol.TYPE_PONG -> {
                    val peerId = json.getString("userId")
                    peerDao.updateStatus(peerId, true, System.currentTimeMillis())
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse incoming payload: ${e.message}")
        }
    }

    private fun handleDiscoveredPeer(userId: String, displayName: String, ipAddress: String, port: Int) {
        scope.launch(Dispatchers.IO) {
            val existing = peerDao.getPeerById(userId)
            val peer = PeerEntity(
                userId = userId,
                displayName = displayName,
                ipAddress = ipAddress,
                port = port,
                isOnline = true,
                lastSeen = System.currentTimeMillis(),
                isDirectOrQr = existing?.isDirectOrQr ?: false
            )
            peerDao.insertOrUpdate(peer)
        }
    }

    private fun startWatchdog() {
        watchdogJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(4000)
                val now = System.currentTimeMillis()
                val peers = peerDao.getPeerById("") // trigger DAO or query
                // Query online peers and mark those exceeding timeout as offline
                try {
                    val allPeers = database.openHelper.readableDatabase
                    // Check all online peers directly
                    val cursor = allPeers.query("SELECT userId, lastSeen FROM peers WHERE isOnline = 1")
                    while (cursor.moveToNext()) {
                        val uid = cursor.getString(0)
                        val lastSeen = cursor.getLong(1)
                        if (now - lastSeen > WiChatProtocol.HEARTBEAT_TIMEOUT_MS) {
                            peerDao.updateStatus(uid, false, lastSeen)
                        }
                    }
                    cursor.close()
                } catch (e: Exception) {
                    Log.d(tag, "Watchdog query error: ${e.message}")
                }
            }
        }
    }

    suspend fun sendMessage(peer: PeerEntity, content: String): MessageEntity = withContext(Dispatchers.IO) {
        val myProfile = userPrefs.getUserProfile()
        val messageId = java.util.UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val message = MessageEntity(
            id = messageId,
            senderId = myProfile.userId,
            receiverId = peer.userId,
            conversationId = peer.userId,
            content = content,
            timestamp = timestamp,
            status = MessageStatus.SENDING,
            direction = MessageDirection.OUTGOING
        )
        // Store in local database immediately
        messageDao.insertMessage(message)

        // Attempt direct TCP socket transmission
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(peer.ipAddress, peer.port), 3500)
                socket.soTimeout = 4000
                val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))

                val packet = WiChatProtocol.createChatMessageJson(
                    messageId,
                    myProfile.userId,
                    peer.userId,
                    content,
                    timestamp
                )
                writer.println(packet)

                // Update to SENT
                messageDao.updateStatus(messageId, MessageStatus.SENT)

                // Await ACK
                val responseLine = reader.readLine()
                if (responseLine != null) {
                    val resJson = JSONObject(responseLine)
                    if (resJson.optString("type") == WiChatProtocol.TYPE_MESSAGE_ACK) {
                        messageDao.updateStatus(messageId, MessageStatus.DELIVERED)
                        peerDao.updateStatus(peer.userId, true, System.currentTimeMillis())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to send message: ${e.message}")
            messageDao.updateStatus(messageId, MessageStatus.FAILED)
            // If connection failed, update peer status to offline
            peerDao.updateStatus(peer.userId, false, peer.lastSeen)
        }

        messageDao.getMessageById(messageId) ?: message
    }

    suspend fun directIpConnect(
        ipAddress: String,
        port: Int,
        onProgress: (String) -> Unit
    ): Result<PeerEntity> = withContext(Dispatchers.IO) {
        val trimmedIp = ipAddress.trim()
        val parts = trimmedIp.split(".")
        val isIpv4 = parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }
        if (!isIpv4) {
            return@withContext Result.failure(Exception("Invalid IP address: '$trimmedIp'. Please enter a valid IPv4 address (e.g. 192.168.1.50)."))
        }
        if (port !in 1024..65535) {
            return@withContext Result.failure(Exception("Invalid port: $port. Port must be between 1024 and 65535."))
        }

        try {
            onProgress("Connecting to $trimmedIp:$port...")
            val myProfile = userPrefs.getUserProfile()

            val socket = Socket()
            socket.connect(InetSocketAddress(trimmedIp, port), 4000)
            socket.soTimeout = 5000

            onProgress("Verifying WiChat protocol handshake...")
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))

            val handshakePacket = WiChatProtocol.createHandshakeJson(
                myProfile.userId,
                myProfile.displayName,
                currentPort
            )
            writer.println(handshakePacket)

            val ackLine = reader.readLine() ?: throw Exception("Remote device closed connection before handshake.")
            val ackJson = JSONObject(ackLine)

            if (ackJson.optString("protocol") != WiChatProtocol.PROTOCOL_NAME ||
                ackJson.optString("type") != WiChatProtocol.TYPE_HANDSHAKE_ACK) {
                throw Exception("Target device is not running a compatible WiChat protocol.")
            }

            val peerId = ackJson.getString("userId")
            val peerName = ackJson.getString("displayName")
            val peerPort = ackJson.optInt("port", port)

            if (peerId == myProfile.userId) {
                throw Exception("Cannot connect to your own device!")
            }

            val verifiedPeer = PeerEntity(
                userId = peerId,
                displayName = peerName,
                ipAddress = trimmedIp,
                port = peerPort,
                isOnline = true,
                lastSeen = System.currentTimeMillis(),
                isDirectOrQr = true
            )
            peerDao.insertOrUpdate(verifiedPeer)

            onProgress("Connected to $peerName!")
            socket.close()
            Result.success(verifiedPeer)
        } catch (e: Exception) {
            val errorDesc = when {
                e.message?.contains("ECONNREFUSED", ignoreCase = true) == true ||
                e.message?.contains("Connection refused", ignoreCase = true) == true ->
                    "Connection refused by $trimmedIp:$port. Ensure WiChat is running on the other device."

                e.message?.contains("ETIMEDOUT", ignoreCase = true) == true ||
                e.message?.contains("timed out", ignoreCase = true) == true ->
                    "Connection timed out. Target device ($trimmedIp:$port) did not respond. Check Wi-Fi connection."

                e.message?.contains("EHOSTUNREACH", ignoreCase = true) == true ||
                e.message?.contains("No route to host", ignoreCase = true) == true ->
                    "Device at $trimmedIp is unreachable. Ensure both devices are on the same local Wi-Fi router."

                e.message?.contains("Cannot connect to your own device", ignoreCase = true) == true ->
                    "Cannot connect to your own device!"

                e.message?.contains("not running a compatible", ignoreCase = true) == true ->
                    e.message ?: "Target device is not running a compatible WiChat protocol."

                else ->
                    e.message ?: "Connection failed to $trimmedIp:$port. Ensure both devices are on the same Wi-Fi."
            }
            Result.failure(Exception(errorDesc, e))
        }
    }

    suspend fun retryMessage(message: MessageEntity, peer: PeerEntity) = withContext(Dispatchers.IO) {
        messageDao.updateStatus(message.id, MessageStatus.SENDING)
        val myProfile = userPrefs.getUserProfile()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(peer.ipAddress, peer.port), 3500)
                socket.soTimeout = 4000
                val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))

                val packet = WiChatProtocol.createChatMessageJson(
                    message.id,
                    myProfile.userId,
                    peer.userId,
                    message.content,
                    message.timestamp
                )
                writer.println(packet)

                messageDao.updateStatus(message.id, MessageStatus.SENT)

                val responseLine = reader.readLine()
                if (responseLine != null) {
                    val resJson = JSONObject(responseLine)
                    if (resJson.optString("type") == WiChatProtocol.TYPE_MESSAGE_ACK) {
                        messageDao.updateStatus(message.id, MessageStatus.DELIVERED)
                        peerDao.updateStatus(peer.userId, true, System.currentTimeMillis())
                    }
                }
            }
        } catch (e: Exception) {
            messageDao.updateStatus(message.id, MessageStatus.FAILED)
        }
    }
}
