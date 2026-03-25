package com.torbox.downloader.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferencesManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "torbox_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, null)
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_API_KEY).apply()
    }

    fun setDownloadFolder(path: String) {
        sharedPreferences.edit().putString(KEY_DOWNLOAD_FOLDER, path).apply()
    }

    fun getDownloadFolder(): String? {
        return sharedPreferences.getString(KEY_DOWNLOAD_FOLDER, null)
    }

    fun setAutoDelete(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_DELETE, enabled).apply()
    }

    fun getAutoDelete(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_DELETE, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_DOWNLOAD_FOLDER = "download_folder"
        private const val KEY_AUTO_DELETE = "auto_delete"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
