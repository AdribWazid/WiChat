package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.model.AppThemeMode
import com.example.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class UserPreferences(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wichat_profile", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_AVATAR_URI = "avatar_uri"
        private const val KEY_PORT = "preferred_port"
        private const val KEY_REGISTERED = "is_registered"
        private const val KEY_COLOR_INDEX = "color_index"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val AVATAR_FILE_NAME = "user_avatar.jpg"
    }

    fun getThemeMode(): AppThemeMode {
        val key = prefs.getString(KEY_THEME_MODE, AppThemeMode.AUTO.key)
        return AppThemeMode.fromKey(key)
    }

    fun saveThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.key).apply()
    }

    fun getUserProfile(): UserProfile {
        var userId = prefs.getString(KEY_USER_ID, null)
        if (userId == null) {
            userId = "wc_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
        val name = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
        var avatarUri = prefs.getString(KEY_AVATAR_URI, null)
        // Verify local avatar file actually exists if configured
        if (avatarUri != null && avatarUri.startsWith("file://")) {
            val localPath = avatarUri.removePrefix("file://")
            if (!File(localPath).exists()) {
                avatarUri = null
            }
        }
        val port = prefs.getInt(KEY_PORT, 8888)
        val isRegistered = prefs.getBoolean(KEY_REGISTERED, false)
        val colorIndex = prefs.getInt(KEY_COLOR_INDEX, (userId.hashCode().coerceAtLeast(0)) % 6)
        return UserProfile(
            userId = userId,
            displayName = name,
            avatarUri = avatarUri,
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

    fun saveProfileWithAvatar(displayName: String, avatarUri: String?, isRegistered: Boolean = true) {
        val editor = prefs.edit()
            .putString(KEY_DISPLAY_NAME, displayName.trim())
            .putBoolean(KEY_REGISTERED, isRegistered)
        if (avatarUri != null) {
            editor.putString(KEY_AVATAR_URI, avatarUri)
        } else {
            editor.remove(KEY_AVATAR_URI)
            try {
                val avatarFile = File(context.filesDir, AVATAR_FILE_NAME)
                if (avatarFile.exists()) {
                    avatarFile.delete()
                }
            } catch (_: Exception) {}
        }
        editor.apply()
    }

    fun copyUriToLocalAvatar(sourceUri: Uri): String? {
        return try {
            val avatarFile = File(context.filesDir, AVATAR_FILE_NAME)
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(avatarFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            "file://${avatarFile.absolutePath}"
        } catch (_: Exception) {
            null
        }
    }

    fun removeAvatar() {
        try {
            val avatarFile = File(context.filesDir, AVATAR_FILE_NAME)
            if (avatarFile.exists()) {
                avatarFile.delete()
            }
        } catch (_: Exception) {}
        prefs.edit().remove(KEY_AVATAR_URI).apply()
    }

    fun savePort(port: Int) {
        prefs.edit().putInt(KEY_PORT, port).apply()
    }
}
