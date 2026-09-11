package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserProfile
import java.util.UUID

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wichat_profile", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_PORT = "preferred_port"
        private const val KEY_REGISTERED = "is_registered"
        private const val KEY_COLOR_INDEX = "color_index"
    }

    fun getUserProfile(): UserProfile {
        var userId = prefs.getString(KEY_USER_ID, null)
        if (userId == null) {
            userId = "wc_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
        val name = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
        val port = prefs.getInt(KEY_PORT, 8888)
        val isRegistered = prefs.getBoolean(KEY_REGISTERED, false)
        val colorIndex = prefs.getInt(KEY_COLOR_INDEX, (userId.hashCode().coerceAtLeast(0)) % 6)
        return UserProfile(
            userId = userId,
            displayName = name,
            preferredPort = port,
            avatarColorIndex = colorIndex,
            isRegistered = isRegistered
        )
    }

    fun saveProfile(displayName: String, isRegistered: Boolean = true) {
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, displayName.trim())
            .putBoolean(KEY_REGISTERED, isRegistered)
            .apply()
    }

    fun savePort(port: Int) {
        prefs.edit().putInt(KEY_PORT, port).apply()
    }
}
