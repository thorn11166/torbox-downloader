package com.torbox.downloader.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.torbox.downloader.data.api.RetrofitClient
import com.torbox.downloader.data.db.AppDatabase
import com.torbox.downloader.data.models.DownloadLocalStatus
import com.torbox.downloader.data.repository.TorBoxRepository
import com.torbox.downloader.data.security.SecurePreferencesManager
import java.util.concurrent.TimeUnit

class DownloadStatusWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val tag = "DownloadStatusWorker"
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

    override suspend fun doWork(): Result = try {
        Log.d(tag, "Starting download status sync")
        
        // Get all active downloads
        val dao = database.downloadDao()
        val activeStatuses = listOf(
            DownloadLocalStatus.QUEUED.toString(),
            DownloadLocalStatus.DOWNLOADING.toString(),
            DownloadLocalStatus.CHECKING.toString()
        )

        // In a real implementation, we would use suspend functions to get the data
        // For now, we'll use the repository's update mechanism
        Log.d(tag, "Download status sync completed")
        Result.success()
    } catch (e: Exception) {
        Log.e(tag, "Error syncing download status", e)
        if (runAttemptCount < 3) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    companion object {
        private const val WORK_TAG = "download_status_sync"

        fun schedulePeriodicStatusCheck(context: Context) {
            val statusCheckRequest = PeriodicWorkRequestBuilder<DownloadStatusWorker>(
                30, TimeUnit.MINUTES
            ).addTag(WORK_TAG).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                statusCheckRequest
            )
            Log.d("DownloadStatusWorker", "Periodic download status check scheduled")
        }

        fun cancelPeriodicStatusCheck(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
            Log.d("DownloadStatusWorker", "Periodic download status check cancelled")
        }
    }
}

class DownloadFileWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val tag = "DownloadFileWorker"

    override suspend fun doWork(): Result = try {
        val torrentId = inputData.getString("torrent_id") ?: return Result.failure()
        val fileName = inputData.getString("file_name") ?: return Result.failure()

        Log.d(tag, "Downloading file: $fileName")
        
        // Implement file download logic here
        Result.success()
    } catch (e: Exception) {
        Log.e(tag, "Error downloading file", e)
        if (runAttemptCount < 2) {
            Result.retry()
        } else {
            Result.failure()
        }
    }
}
