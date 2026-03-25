package com.torbox.downloader.data.api

import com.torbox.downloader.data.models.AddTorrentRequest
import com.torbox.downloader.data.models.AddTorrentResponse
import com.torbox.downloader.data.models.TorrentDetailsResponse
import com.torbox.downloader.data.models.UserTorrentsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import okhttp3.ResponseBody

interface TorBoxService {
    @GET("api/v1/user/torrents")
    suspend fun getUserTorrents(
        @Header("Authorization") auth: String
    ): UserTorrentsResponse

    @GET("api/v1/torrent/{id}")
    suspend fun getTorrentDetails(
        @Path("id") torrentId: String,
        @Header("Authorization") auth: String
    ): TorrentDetailsResponse

    @POST("api/v1/torrent/add")
    suspend fun addTorrent(
        @Body request: AddTorrentRequest,
        @Header("Authorization") auth: String
    ): AddTorrentResponse

    @Streaming
    @GET
    suspend fun downloadFile(
        @Query("url") downloadUrl: String
    ): ResponseBody
}
