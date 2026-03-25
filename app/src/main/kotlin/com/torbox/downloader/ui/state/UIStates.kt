package com.torbox.downloader.ui.state

import com.torbox.downloader.data.db.DownloadEntity
import com.torbox.downloader.data.db.DownloadHistoryEntity

data class DownloadUIState(
    val downloads: List<DownloadEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

data class HistoryUIState(
    val history: List<DownloadHistoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

data class SettingsUIState(
    val apiKey: String = "",
    val downloadFolder: String = "",
    val autoDelete: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)
