package com.torbox.downloader.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.torbox.downloader.data.api.RetrofitClient
import com.torbox.downloader.data.db.AppDatabase
import com.torbox.downloader.data.db.DownloadEntity
import com.torbox.downloader.data.db.DownloadHistoryEntity
import com.torbox.downloader.data.models.DownloadLocalStatus
import com.torbox.downloader.data.repository.TorBoxRepository
import com.torbox.downloader.data.security.SecurePreferencesManager
import com.torbox.downloader.ui.state.DownloadUIState
import com.torbox.downloader.ui.state.HistoryUIState
import com.torbox.downloader.ui.state.SettingsUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "DownloadViewModel"
    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    private val securePrefs = SecurePreferencesManager(context)
    private val torBoxService = RetrofitClient.getTorBoxService()
    private val repository = TorBoxRepository(
        torBoxService,
        database.downloadDao(),
        database.downloadHistoryDao(),
        securePrefs,
        context
    )

    private val _downloadUIState = MutableStateFlow(DownloadUIState())
    val downloadUIState: StateFlow<DownloadUIState> = _downloadUIState.asStateFlow()

    private val _historyUIState = MutableStateFlow(HistoryUIState())
    val historyUIState: StateFlow<HistoryUIState> = _historyUIState.asStateFlow()

    private val _settingsUIState = MutableStateFlow(
        SettingsUIState(
            apiKey = securePrefs.getApiKey() ?: "",
            downloadFolder = repository.getDownloadFolder(),
            autoDelete = repository.shouldAutoDeleteCompleted(),
            notificationsEnabled = repository.areNotificationsEnabled()
        )
    )
    val settingsUIState: StateFlow<SettingsUIState> = _settingsUIState.asStateFlow()

    init {
        loadActiveDownloads()
        loadDownloadHistory()
    }

    private fun loadActiveDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getActiveDownloads().collect { downloads ->
                _downloadUIState.update { state ->
                    state.copy(
                        downloads = downloads,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadDownloadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDownloadHistory().collect { history ->
                _historyUIState.update { state ->
                    state.copy(
                        history = history,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addMagnetLink(magnetLink: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadUIState.update { it.copy(isLoading = true) }
            val result = repository.addMagnetLink(magnetLink)
            result.onSuccess {
                Log.d(tag, "Magnet link added successfully: $it")
                _downloadUIState.update { state ->
                    state.copy(
                        message = "Magnet link added successfully",
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                Log.e(tag, "Failed to add magnet link", e)
                _downloadUIState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to add magnet link",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addTorrentUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadUIState.update { it.copy(isLoading = true) }
            val result = repository.addTorrentUrl(url)
            result.onSuccess {
                Log.d(tag, "Torrent URL added successfully: $it")
                _downloadUIState.update { state ->
                    state.copy(
                        message = "Torrent added successfully",
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                Log.e(tag, "Failed to add torrent URL", e)
                _downloadUIState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to add torrent",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refreshDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadUIState.update { it.copy(isLoading = true) }
            val downloads = _downloadUIState.value.downloads
            for (download in downloads) {
                repository.updateDownloadStatus(download.torrentId)
            }
            _downloadUIState.update { it.copy(isLoading = false) }
        }
    }

    fun deleteDownload(torrentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDownload(torrentId).onSuccess {
                Log.d(tag, "Download deleted: $torrentId")
            }.onFailure { e ->
                Log.e(tag, "Failed to delete download", e)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearDownloadHistory().onSuccess {
                _historyUIState.update { state ->
                    state.copy(message = "History cleared")
                }
            }.onFailure { e ->
                _historyUIState.update { state ->
                    state.copy(error = e.message ?: "Failed to clear history")
                }
            }
        }
    }

    fun saveSettings(apiKey: String, downloadFolder: String, autoDelete: Boolean, notificationsEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _settingsUIState.update { it.copy(isLoading = true) }
            
            try {
                if (apiKey.isNotEmpty() && apiKey != securePrefs.getApiKey()) {
                    repository.validateApiKey(apiKey).onSuccess {
                        repository.setDownloadFolder(downloadFolder)
                        repository.setAutoDelete(autoDelete)
                        repository.setNotificationsEnabled(notificationsEnabled)
                        _settingsUIState.update { state ->
                            state.copy(
                                apiKey = apiKey,
                                downloadFolder = downloadFolder,
                                autoDelete = autoDelete,
                                notificationsEnabled = notificationsEnabled,
                                message = "Settings saved successfully",
                                isLoading = false
                            )
                        }
                    }.onFailure { e ->
                        _settingsUIState.update { state ->
                            state.copy(
                                error = "Invalid API key: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                } else {
                    repository.setDownloadFolder(downloadFolder)
                    repository.setAutoDelete(autoDelete)
                    repository.setNotificationsEnabled(notificationsEnabled)
                    _settingsUIState.update { state ->
                        state.copy(
                            downloadFolder = downloadFolder,
                            autoDelete = autoDelete,
                            notificationsEnabled = notificationsEnabled,
                            message = "Settings saved successfully",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error saving settings", e)
                _settingsUIState.update { state ->
                    state.copy(
                        error = e.message ?: "Failed to save settings",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun downloadFile(torrentId: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val download = _downloadUIState.value.downloads.find { it.torrentId == torrentId }
            if (download == null || download.downloadUrl.isNullOrEmpty()) {
                _downloadUIState.update { state ->
                    state.copy(error = "Download URL not available")
                }
                return@launch
            }

            try {
                val downloadFolder = repository.getDownloadFolder()
                val file = File(downloadFolder, fileName)
                file.parentFile?.mkdirs()

                // Simple file download with resume support
                val response = torBoxService.downloadFile(download.downloadUrl)
                val inputStream = response.byteStream()
                val outputStream = file.outputStream()

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                repository.saveDownloadedFile(torrentId, file.absolutePath)
                _downloadUIState.update { state ->
                    state.copy(message = "Download saved to ${file.absolutePath}")
                }

                // Auto-delete if enabled
                if (repository.shouldAutoDeleteCompleted()) {
                    deleteDownload(torrentId)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error downloading file", e)
                _downloadUIState.update { state ->
                    state.copy(error = "Failed to download: ${e.message}")
                }
            }
        }
    }

    fun clearMessages() {
        _downloadUIState.update { it.copy(message = null, error = null) }
        _historyUIState.update { it.copy(message = null, error = null) }
        _settingsUIState.update { it.copy(message = null, error = null) }
    }

    fun updateDownloadFolder(uri: Uri) {
        val path = getPathFromUri(uri)
        if (path != null) {
            _settingsUIState.update { state ->
                state.copy(downloadFolder = path)
            }
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                uri.path?.let { path ->
                    if (path.contains("/document/")) {
                        path.substringAfterLast("/document/").replace(":", "/")
                    } else {
                        path
                    }
                }
            }
            else -> {
                when (uri.scheme) {
                    "file" -> uri.path
                    "content" -> try {
                        val projection = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                cursor.getString(0)
                            } else {
                                uri.path
                            }
                        }
                    } catch (e: Exception) {
                        uri.path
                    }
                    else -> null
                }
            }
        }
    }
}
