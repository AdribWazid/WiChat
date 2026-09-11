package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {

    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            // First look specifically for wlan / eth interfaces
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                val name = intf.name.lowercase()
                if (name.contains("wlan") || name.contains("eth") || name.contains("ap")) {
                    for (addr in Collections.list(intf.inetAddresses)) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            return addr.hostAddress ?: "127.0.0.1"
                        }
                    }
                }
            }
            // Fallback to any non-loopback IPv4
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                for (addr in Collections.list(intf.inetAddresses)) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "127.0.0.1"
    }

    fun getBroadcastAddresses(): List<InetAddress> {
        val broadcastList = mutableListOf<InetAddress>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                for (interfaceAddress in intf.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null && broadcast is Inet4Address) {
                        broadcastList.add(broadcast)
                    }
                }
            }
        } catch (_: Exception) {
        }
        if (broadcastList.isEmpty()) {
            try {
                broadcastList.add(InetAddress.getByName("255.255.255.255"))
            } catch (_: Exception) {
            }
        }
        return broadcastList
    }

    fun getWifiName(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val connectionInfo = wifiManager?.connectionInfo
            val ssid = connectionInfo?.ssid
            if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
                return ssid.removeSurrounding("\"")
            }
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connMgr?.activeNetwork
            val caps = connMgr?.getNetworkCapabilities(activeNetwork)
            if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                return "Local Wi-Fi Network"
            }
            if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
                return "Local Ethernet"
            }
        } catch (_: Exception) {
        }
        return "Local Network (LAN)"
    }

    fun isWifiOrLanConnected(context: Context): Boolean {
        try {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connMgr?.activeNetwork ?: return false
            val caps = connMgr.getNetworkCapabilities(activeNetwork) ?: return false
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
        } catch (_: Exception) {
            return false
        }
    }
}
