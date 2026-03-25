# TorBox API Integration Guide

## Overview

TorBox Downloader integrates with the TorBox REST API to manage torrent downloads. This document details all API interactions, error handling, and best practices.

## Authentication

All requests require a Bearer token in the `Authorization` header:

```http
Authorization: Bearer YOUR_API_KEY
```

**API Key Generation**:
1. Create account at https://torbox.app
2. Navigate to Account Settings or API section
3. Generate new API token
4. Copy and save securely

**Important**: Never commit API keys to version control. Use environment variables or secure storage.

## API Base URL

- **Production**: `https://api.torbox.app/`
- **Version**: `v1`
- **Format**: `https://api.torbox.app/api/v1/`

## Endpoints

### 1. Add Torrent

**Endpoint**: `POST /api/v1/torrent/add`

**Purpose**: Add new torrent from magnet link or torrent file URL

**Request Body**:
```json
{
  "magnet": "magnet:?xt=urn:btih:...",
  "torrent_url": "https://example.com/file.torrent"
}
```

Note: Provide either `magnet` OR `torrent_url`, not both.

**Kotlin Usage**:
```kotlin
val request = AddTorrentRequest(magnet = "magnet:?xt=...")
val response = torBoxService.addTorrent(request, "Bearer $apiKey")
```

**Success Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "abc123",
    "name": "Ubuntu 22.04 LTS",
    "status": "queued",
    "progress": 0,
    "size": 3000000000,
    "speed": 0,
    "hash": "abc123def456",
    "added_at": 1703267890,
    "completed_at": null
  }
}
```

**Error Responses**:
- `400 Bad Request`: Invalid magnet/URL format
- `401 Unauthorized`: Invalid API key
- `429 Too Many Requests`: Rate limited

**Error Response**:
```json
{
  "success": false,
  "error": "Invalid magnet link format"
}
```

### 2. Get User Torrents

**Endpoint**: `GET /api/v1/user/torrents`

**Purpose**: Retrieve all torrents for authenticated user

**Query Parameters**:
- `offset` (optional): Pagination offset (default: 0)
- `limit` (optional): Items per page (default: 50, max: 200)

**Kotlin Usage**:
```kotlin
val response = torBoxService.getUserTorrents("Bearer $apiKey")
```

**Success Response (200)**:
```json
{
  "success": true,
  "data": [
    {
      "id": "abc123",
      "name": "Ubuntu 22.04 LTS",
      "status": "downloading",
      "progress": 45,
      "size": 3000000000,
      "speed": 1500000,
      "hash": "abc123def456",
      "added_at": 1703267890,
      "completed_at": null
    },
    {
      "id": "def456",
      "name": "Debian 11",
      "status": "completed",
      "progress": 100,
      "size": 4000000000,
      "speed": 0,
      "hash": "def456ghi789",
      "added_at": 1703260000,
      "completed_at": 1703265000
    }
  ]
}
```

### 3. Get Torrent Details

**Endpoint**: `GET /api/v1/torrent/{id}`

**Purpose**: Get detailed information about specific torrent

**Path Parameters**:
- `id` (required): Torrent ID

**Kotlin Usage**:
```kotlin
val response = torBoxService.getTorrentDetails("abc123", "Bearer $apiKey")
```

**Success Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "abc123",
    "name": "Ubuntu 22.04 LTS",
    "status": "completed",
    "progress": 100,
    "size": 3000000000,
    "speed": 0,
    "hash": "abc123def456",
    "added_at": 1703267890,
    "completed_at": 1703275890,
    "download_url": "https://cache.torbox.app/file/abc123",
    "files": [
      {
        "name": "ubuntu-22.04.iso",
        "size": 3000000000,
        "status": "completed"
      }
    ]
  }
}
```

### 4. Download File

**Endpoint**: `GET /api/v1/download`

**Purpose**: Download completed torrent file

**Query Parameters**:
- `url` (required): Download URL from torrent details

**Note**: This endpoint handles streaming and supports range requests for resume capability.

**Kotlin Usage**:
```kotlin
val response = torBoxService.downloadFile(downloadUrl)
val inputStream = response.byteStream()
val outputStream = File(path).outputStream()
inputStream.use { input ->
    outputStream.use { output ->
        input.copyTo(output)
    }
}
```

## Data Models

### TorrentData

Represents a torrent in TorBox system:

```kotlin
@Serializable
data class TorrentData(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("status")
    val status: String,  // queued, downloading, completed, failed
    @SerialName("progress")
    val progress: Int = 0,  // 0-100
    @SerialName("size")
    val size: Long = 0,  // bytes
    @SerialName("speed")
    val speed: Long = 0,  // bytes/sec
    @SerialName("hash")
    val hash: String = "",
    @SerialName("added_at")
    val addedAt: Long = 0,  // Unix timestamp
    @SerialName("completed_at")
    val completedAt: Long? = null  // Unix timestamp or null
)
```

### TorrentStatus

Status values returned by API:

```kotlin
object TorrentStatus {
    const val QUEUED = "queued"           // Waiting to download
    const val DOWNLOADING = "downloading" // Currently downloading
    const val COMPLETED = "completed"     // Fully downloaded
    const val FAILED = "failed"           // Download failed
    const val CHECKING = "checking"       // Verifying data
}
```

## Error Handling

### HTTP Status Codes

| Code | Meaning | Retry | Action |
|------|---------|-------|--------|
| 200 | Success | No | Process response |
| 400 | Bad Request | No | Fix request format |
| 401 | Unauthorized | No | Verify API key |
| 404 | Not Found | No | Check torrent ID |
| 429 | Rate Limited | Yes | Wait and retry |
| 500 | Server Error | Yes | Retry with backoff |
| 503 | Service Down | Yes | Wait and retry |

### Error Response Format

```json
{
  "success": false,
  "error": "Error message describing issue"
}
```

### Retry Strategy

Implemented in `TorBoxRepository`:

```kotlin
suspend fun updateDownloadStatus(torrentId: String): Result<Unit> {
    return try {
        // API call
        val response = torBoxService.getTorrentDetails(torrentId, "Bearer $apiKey")
        if (response.success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Retry Policy**:
- WorkManager: Up to 3 attempts with exponential backoff
- User actions: Manual retry via UI button
- Network errors: Automatic retry with backoff

## Rate Limiting

TorBox API enforces rate limits:

- **Limit**: 100 requests per minute per API key
- **Response Header**: `X-RateLimit-Remaining`
- **Action**: Wait if 429 received

**Implementation**:

```kotlin
// OkHttp includes built-in backoff
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

## Best Practices

### 1. API Key Security

```kotlin
// GOOD: Encrypted storage
val apiKey = securePrefs.getApiKey()  // Retrieved from encrypted SharedPreferences

// BAD: Hardcoded
const val API_KEY = "abc123"  // Never do this

// BAD: In BuildConfig
buildConfigField "String", "API_KEY", '"abc123"'  // Not secure
```

### 2. Request Frequency

- **Manual actions**: Immediate API call
- **Status checks**: Every 30 minutes via WorkManager
- **Batch operations**: Combine requests where possible

### 3. Error Messages

```kotlin
// User-friendly error messages
val errorMsg = when (exception) {
    is IOException -> "Network error. Check your connection."
    is HttpException -> when (exception.code()) {
        401 -> "Invalid API key. Please reconfigure."
        429 -> "Too many requests. Please wait."
        else -> "API error: ${exception.message()}"
    }
    else -> "Unexpected error: ${exception.message}"
}
```

### 4. Timeout Configuration

```kotlin
// Balanced timeout values
.connectTimeout(30, TimeUnit.SECONDS)  // Connection establishment
.readTimeout(30, TimeUnit.SECONDS)     // Data transfer
.writeTimeout(30, TimeUnit.SECONDS)    // Request sending
```

### 5. Request/Response Logging

Debug build only:

```kotlin
val logging = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}
```

## Testing Against TorBox API

### Manual Testing Steps

1. **Setup**:
   - Get valid API key
   - Configure in app settings
   - Verify internet connection

2. **Test Add Torrent**:
   - Use public torrent magnet link (e.g., Ubuntu ISO)
   - Verify success response with torrent ID
   - Check torrent appears in active downloads

3. **Test Status Sync**:
   - Add torrent
   - Wait 30 minutes (or trigger manual refresh)
   - Verify status updates from API

4. **Test Download**:
   - Wait for torrent to complete
   - Tap "Download Complete"
   - Verify file saves to download folder

### Mock Testing (Development)

Use Retrofit mock interceptor for testing:

```kotlin
// Pseudo-code for mock interceptor
val mockResponse = AddTorrentResponse(
    success = true,
    data = TorrentData(
        id = "test123",
        name = "Test Torrent",
        status = "queued",
        size = 1000000
    )
)
```

## Troubleshooting API Issues

### Problem: "Invalid API key"

**Causes**:
- Typo in API key
- Key expired
- Wrong key copied

**Solution**:
- Regenerate key in TorBox account
- Clear app cache and re-enter

### Problem: "Too many requests"

**Causes**:
- Manual refresh too frequent
- App re-syncing excessively

**Solution**:
- Wait 1 minute before next request
- Check WorkManager frequency setting

### Problem: "Torrent not found"

**Causes**:
- Torrent ID incorrect
- Torrent expired from TorBox
- User account limitation

**Solution**:
- Verify torrent ID is correct
- Check account status on torbox.app

### Problem: Download URL not returned

**Causes**:
- Torrent still downloading/checking
- Download URL expired
- Server issue

**Solution**:
- Wait for completion status
- Re-fetch details (will get new URL)
- Retry if server error

## API Versioning

Current implementation targets TorBox API v1. If API changes:

1. Update `RetrofitClient.BASE_URL`
2. Add migration logic in `TorBoxRepository`
3. Handle backward compatibility
4. Increment app version

## Future Enhancements

Possible future API integrations:

- Get torrent history
- Delete torrent from TorBox
- Set bandwidth limits
- Pause/resume downloads
- Schedule downloads
- Webhook notifications
- Advanced filtering/search

## References

- [TorBox Official API Docs](https://torbox.app/docs/api)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [OkHttp Interceptors](https://square.github.io/okhttp/interceptors/)
