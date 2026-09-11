package com.example.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.WiChatProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class UdpDiscoveryEngine(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onPeerDiscovered: (userId: String, displayName: String, ipAddress: String, port: Int) -> Unit
) {
    private val tag = "WiChat_UDP"
    private var sendJob: Job? = null
    private var receiveJob: Job? = null
    private var receiverSocket: DatagramSocket? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    fun start(userId: String, displayName: String, localPort: Int) {
        stop()
        acquireMulticastLock()
        startReceiver()
        startSender(userId, displayName, localPort)
    }

    fun stop() {
        sendJob?.cancel()
        sendJob = null

        receiveJob?.cancel()
        receiveJob = null

        try {
            receiverSocket?.close()
        } catch (_: Exception) {
        }
        receiverSocket = null

        releaseMulticastLock()
    }

    private fun acquireMulticastLock() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifiManager?.createMulticastLock("WiChatMulticastLock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to acquire multicast lock: ${e.message}")
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (_: Exception) {
        }
        multicastLock = null
    }

    private fun startReceiver() {
        receiveJob = scope.launch(Dispatchers.IO) {
            try {
                val socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(WiChatProtocol.DISCOVERY_PORT))
                    broadcast = true
                }
                receiverSocket = socket

                val buffer = ByteArray(2048)
                while (isActive && !socket.isClosed) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        val message = String(packet.data, packet.offset, packet.length, StandardCharsets.UTF_8)
                        val beacon = WiChatProtocol.parseBeacon(message)
                        if (beacon != null) {
                            val peerIp = packet.address.hostAddress ?: ""
                            val myIp = NetworkUtils.getLocalIpAddress()
                            // Ignore packets from self
                            if (peerIp != myIp && peerIp.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    onPeerDiscovered(beacon.userId, beacon.displayName, peerIp, beacon.port)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (!isActive || socket.isClosed) break
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "UDP receiver error: ${e.message}")
            }
        }
    }

    private fun startSender(userId: String, displayName: String, localPort: Int) {
        sendJob = scope.launch(Dispatchers.IO) {
            var senderSocket: DatagramSocket? = null
            try {
                senderSocket = DatagramSocket().apply { broadcast = true }
                while (isActive) {
                    val beaconJson = WiChatProtocol.createBeaconJson(userId, displayName, localPort)
                    val data = beaconJson.toByteArray(StandardCharsets.UTF_8)
                    val broadcastTargets = NetworkUtils.getBroadcastAddresses()

                    for (target in broadcastTargets) {
                        try {
                            val packet = DatagramPacket(data, data.size, target, WiChatProtocol.DISCOVERY_PORT)
                            senderSocket.send(packet)
                        } catch (_: Exception) {
                        }
                    }
                    delay(WiChatProtocol.HEARTBEAT_INTERVAL_MS)
                }
            } catch (e: Exception) {
                Log.e(tag, "UDP sender error: ${e.message}")
            } finally {
                try {
                    senderSocket?.close()
                } catch (_: Exception) {
                }
            }
        }
    }
}
