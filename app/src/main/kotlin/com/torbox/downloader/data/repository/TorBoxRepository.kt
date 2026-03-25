package com.torbox.downloader.data.repository

import android.content.Context
import android.util.Log
import com.torbox.downloader.data.api.TorBoxService
import com.torbox.downloader.data.db.DownloadDao
import com.torbox.downloader.data.db.DownloadEntity
import com.torbox.downloader.data.db.DownloadHistoryDao
import com.torbox.downloader.data.db.DownloadHistoryEntity
import com.torbox.downloader.data.models.AddTorrentRequest
import com.torbox.downloader.data.models.DownloadLocalStatus
import com.torbox.downloader.data.models.TorrentStatus
import com.torbox.downloader.data.security.SecurePreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File

class TorBoxRepository(
    private val torBoxService: TorBoxService,
    private val downloadDao: DownloadDao,
    private val downloadHistoryDao: DownloadHistoryDao,
    private val securePrefs: SecurePreferencesManager,
    private val context: Context
) {
    private val tag = "TorBoxRepository"

    suspend fun addMagnetLink(magnetLink: String): Result<String> = try {
        val apiKey = securePrefs.getApiKey() ?: return Result.failure(Exception("API key not configured"))
        val request = AddTorrentRequest(magnet = magnetLink)
        val response = torBoxService.addTorrent(request, "Bearer $apiKey")

        if (response.success && response.data != null) {
            val data = response.data
            val entity = DownloadEntity(
                torrentId = data.id,
                torrentName = data.name,
                magnetLink = magnetLink,
                status = DownloadLocalStatus.QUEUED.toString(),
                fileSize = data.size,
                addedTimestamp = System.currentTimeMillis()
            )
            downloadDao.insertDownload(entity)
            Result.success(data.id)
        } else {
            Result.failure(Exception(response.error ?: "Unknown error adding torrent"))
        }
    } catch (e: Exception) {
        Log.e(tag, "Error adding magnet link", e)
        Result.failure(e)
    }

    suspend fun addTorrentUrl(torrentUrl: String): Result<String> = try {
        val apiKey = securePrefs.getApiKey() ?: return Result.failure(Exception("API key not configured"))
        val request = AddTorrentRequest(torrentUrl = torrentUrl)
        val response = torBoxService.addTorrent(request, "Bearer $apiKey")

        if (response.success && response.data != null) {
            val data = response.data
            val entity = DownloadEntity(
                torrentId = data.id,
                torrentName = data.name,
                torrentUrl = torrentUrl,
                status = DownloadLocalStatus.QUEUED.toString(),
                fileSize = data.size,
                addedTimestamp = System.currentTimeMillis()
            )
            downloadDao.insertDownload(entity)
            Result.success(data.id)
        } else {
            Result.failure(Exception(response.error ?: "Unknown error adding torrent"))
        }
    } catch (e: Exception) {
        Log.e(tag, "Error adding torrent URL", e)
        Result.failure(e)
    }

    suspend fun updateDownloadStatus(torrentId: String): Result<Unit> = try {
        val apiKey = securePrefs.getApiKey() ?: return Result.failure(Exception("API key not configured"))
        val response = torBoxService.getTorrentDetails(torrentId, "Bearer $apiKey")

        if (response.success && response.data != null) {
            val data = response.data
            val existingDownload = downloadDao.getDownload(torrentId)

            if (existingDownload != null) {
                val newStatus = when (data.status) {
                    TorrentStatus.DOWNLOADING -> DownloadLocalStatus.DOWNLOADING.toString()
                    TorrentStatus.COMPLETED -> DownloadLocalStatus.COMPLETED.toString()
                    TorrentStatus.FAILED -> DownloadLocalStatus.FAILED.toString()
                    TorrentStatus.QUEUED -> DownloadLocalStatus.QUEUED.toString()
                    else -> existingDownload.status
                }

                val updated = existingDownload.copy(
                    status = newStatus,
                    fileSize = data.size,
                    downloadedBytes = ((data.progress / 100f) * data.size).toLong(),
                    downloadSpeed = data.speed,
                    downloadUrl = data.downloadUrl,
                    completedTimestamp = data.completedAt,
                    lastSyncTimestamp = System.currentTimeMillis()
                )
                downloadDao.updateDownload(updated)

                // If completed and we have a download URL, prepare for download
                if (data.status == TorrentStatus.COMPLETED && !data.downloadUrl.isNullOrEmpty()) {
                    downloadDao.updateDownloadUrl(torrentId, data.downloadUrl, System.currentTimeMillis())
                }
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.error ?: "Unknown error fetching torrent details"))
        }
    } catch (e: Exception) {
        Log.e(tag, "Error updating download status", e)
        Result.failure(e)
    }

    fun getActiveDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getDownloadsByStatus(
            listOf(
                DownloadLocalStatus.QUEUED.toString(),
                DownloadLocalStatus.DOWNLOADING.toString(),
                DownloadLocalStatus.CHECKING.toString()
            )
        ).catch { e ->
            Log.e(tag, "Error fetching active downloads", e)
        }
    }

    fun getAllDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getAllDownloads().catch { e ->
            Log.e(tag, "Error fetching all downloads", e)
        }
    }

    fun getDownloadHistory(): Flow<List<DownloadHistoryEntity>> {
        return downloadHistoryDao.getAllHistory().catch { e ->
            Log.e(tag, "Error fetching download history", e)
        }
    }

    suspend fun deleteDownload(torrentId: String): Result<Unit> = try {
        downloadDao.deleteDownloadById(torrentId)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(tag, "Error deleting download", e)
        Result.failure(e)
    }

    suspend fun clearDownloadHistory(): Result<Unit> = try {
        downloadHistoryDao.clearHistory()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(tag, "Error clearing history", e)
        Result.failure(e)
    }

    suspend fun saveDownloadedFile(torrentId: String, filePath: String): Result<Unit> = try {
        downloadDao.markAsDownloaded(torrentId, filePath, System.currentTimeMillis())
        
        val download = downloadDao.getDownload(torrentId)
        if (download != null) {
            val historyEntity = DownloadHistoryEntity(
                torrentId = torrentId,
                torrentName = download.torrentName,
                fileSize = download.fileSize,
                downloadPath = filePath,
                status = DownloadLocalStatus.COMPLETED.toString(),
                addedDate = download.addedTimestamp,
                downloadedDate = System.currentTimeMillis()
            )
            downloadHistoryDao.insertHistory(historyEntity)
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(tag, "Error saving downloaded file", e)
        Result.failure(e)
    }

    fun getDownloadFolder(): String {
        val savedPath = securePrefs.getDownloadFolder()
        return if (savedPath != null && File(savedPath).exists()) {
            savedPath
        } else {
            context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath
        }
    }

    suspend fun setDownloadFolder(path: String) {
        securePrefs.setDownloadFolder(path)
    }

    fun shouldAutoDeleteCompleted(): Boolean {
        return securePrefs.getAutoDelete()
    }

    suspend fun setAutoDelete(enabled: Boolean) {
        securePrefs.setAutoDelete(enabled)
    }

    fun areNotificationsEnabled(): Boolean {
        return securePrefs.getNotificationsEnabled()
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        securePrefs.setNotificationsEnabled(enabled)
    }

    suspend fun validateApiKey(apiKey: String): Result<Boolean> = try {
        val response = torBoxService.getUserTorrents("Bearer $apiKey")
        if (response.success) {
            securePrefs.setApiKey(apiKey)
            Result.success(true)
        } else {
            Result.failure(Exception("Invalid API key"))
        }
    } catch (e: Exception) {
        Log.e(tag, "Error validating API key", e)
        Result.failure(e)
    }
}
