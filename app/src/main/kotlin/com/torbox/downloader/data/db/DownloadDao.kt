package com.torbox.downloader.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE torrentId = :torrentId")
    suspend fun getDownload(torrentId: String): DownloadEntity?

    @Query("SELECT * FROM downloads ORDER BY addedTimestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN (:statuses) ORDER BY addedTimestamp DESC")
    fun getDownloadsByStatus(statuses: List<String>): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY addedTimestamp DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE torrentId = :torrentId")
    suspend fun deleteDownloadById(torrentId: String)

    @Query("DELETE FROM downloads WHERE status = :status")
    suspend fun deleteDownloadsByStatus(status: String)

    @Query("UPDATE downloads SET downloadUrl = :downloadUrl, completedTimestamp = :timestamp WHERE torrentId = :torrentId")
    suspend fun updateDownloadUrl(torrentId: String, downloadUrl: String, timestamp: Long)

    @Query("UPDATE downloads SET downloadPath = :path, downloadedTimestamp = :timestamp, isDownloaded = 1 WHERE torrentId = :torrentId")
    suspend fun markAsDownloaded(torrentId: String, path: String, timestamp: Long)
}

@Dao
interface DownloadHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: DownloadHistoryEntity)

    @Delete
    suspend fun deleteHistory(history: DownloadHistoryEntity)

    @Query("SELECT * FROM download_history ORDER BY addedDate DESC")
    fun getAllHistory(): Flow<List<DownloadHistoryEntity>>

    @Query("DELETE FROM download_history")
    suspend fun clearHistory()

    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("SELECT * FROM download_history WHERE torrentId = :torrentId")
    suspend fun getHistoryByTorrentId(torrentId: String): DownloadHistoryEntity?
}
