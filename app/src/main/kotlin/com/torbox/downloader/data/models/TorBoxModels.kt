package com.torbox.downloader.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddTorrentRequest(
    @SerialName("magnet")
    val magnet: String? = null,
    @SerialName("torrent_url")
    val torrentUrl: String? = null
)

@Serializable
data class AddTorrentResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: TorrentData? = null,
    @SerialName("error")
    val error: String? = null
)

@Serializable
data class TorrentData(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("status")
    val status: String,
    @SerialName("progress")
    val progress: Int = 0,
    @SerialName("size")
    val size: Long = 0,
    @SerialName("speed")
    val speed: Long = 0,
    @SerialName("hash")
    val hash: String = "",
    @SerialName("added_at")
    val addedAt: Long = 0,
    @SerialName("completed_at")
    val completedAt: Long? = null
)

@Serializable
data class UserTorrentsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: List<TorrentData>? = null,
    @SerialName("error")
    val error: String? = null
)

@Serializable
data class TorrentDetailsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: TorrentDetails? = null,
    @SerialName("error")
    val error: String? = null
)

@Serializable
data class TorrentDetails(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("status")
    val status: String,
    @SerialName("progress")
    val progress: Int,
    @SerialName("size")
    val size: Long,
    @SerialName("speed")
    val speed: Long,
    @SerialName("hash")
    val hash: String,
    @SerialName("added_at")
    val addedAt: Long,
    @SerialName("completed_at")
    val completedAt: Long?,
    @SerialName("download_url")
    val downloadUrl: String? = null,
    @SerialName("files")
    val files: List<FileInfo>? = null
)

@Serializable
data class FileInfo(
    @SerialName("name")
    val name: String,
    @SerialName("size")
    val size: Long,
    @SerialName("status")
    val status: String
)

// Torrent status constants
object TorrentStatus {
    const val QUEUED = "queued"
    const val DOWNLOADING = "downloading"
    const val COMPLETED = "completed"
    const val FAILED = "failed"
    const val CHECKING = "checking"
}

// Download status for local tracking
enum class DownloadLocalStatus {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    ARCHIVED;

    override fun toString(): String = name.lowercase()
}
