package com.torbox.downloader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.torbox.downloader.data.models.DownloadLocalStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val torrentId: String,
    val torrentName: String,
    val magnetLink: String? = null,
    val torrentUrl: String? = null,
    val status: String = DownloadLocalStatus.QUEUED.toString(),
    val fileSize: Long = 0,
    val downloadedBytes: Long = 0,
    val downloadSpeed: Long = 0,
    val downloadUrl: String? = null,
    val downloadPath: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long? = null,
    val downloadedTimestamp: Long? = null,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val autoDelete: Boolean = false,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val torrentId: String,
    val torrentName: String,
    val fileSize: Long,
    val downloadPath: String? = null,
    val status: String,
    val addedDate: Long,
    val completedDate: Long? = null,
    val downloadedDate: Long? = null
)
