# TorBox Downloader - Architecture Documentation

## Overview

TorBox Downloader follows the **MVVM (Model-View-ViewModel)** pattern combined with a **clean architecture** approach. The app is designed for maintainability, testability, and scalability.

## Architectural Layers

### 1. Presentation Layer (UI)
Located in `ui/` directory:

- **Activities**: `MainActivity.kt` - Main entry point, handles intent interception
- **Screens**: Composable screens using Jetpack Compose
  - `ActiveDownloadsScreen`: Real-time download management
  - `DownloadHistoryScreen`: Historical records viewing
  - `SettingsScreen`: Configuration interface
- **ViewModel**: `DownloadViewModel.kt` - Manages UI state and business logic
- **State**: `UIStates.kt` - Immutable state classes for each screen
- **Theme**: Material Design 3 theme configuration

### 2. Domain Layer
Contains business logic:

- **Repository**: `TorBoxRepository.kt` - Abstracts data sources (API + Database)
- **Models**: Data classes representing domain concepts

### 3. Data Layer
Located in `data/` directory:

- **API**: `TorBoxService.kt` + `RetrofitClient.kt` - REST API integration
- **Database**: `AppDatabase.kt`, `DownloadDao.kt`, `RoomEntities.kt` - Local persistence
- **Security**: `SecurePreferencesManager.kt` - Encrypted storage
- **Models**: API response/request objects

### 4. Background/Async Layer
Located in `work/` and `broadcast/`:

- **WorkManager**: `DownloadStatusWorker.kt` - Periodic status sync
- **BroadcastReceiver**: `MagnetLinkReceiver.kt` - Intent interception

## Data Flow

### Adding a Torrent (User Action Flow)

```
User taps Magnet Link
        ↓
MagnetLinkReceiver or Manual Input
        ↓
MainActivity.handleIncomingIntent()
        ↓
DownloadViewModel.addMagnetLink()
        ↓
TorBoxRepository.addMagnetLink()
        ↓
TorBoxService.addTorrent() [HTTP POST]
        ↓
Response returned
        ↓
DownloadEntity saved to Room Database
        ↓
UI Flow collects new data
        ↓
ActiveDownloadsScreen re-renders
```

### Status Update Flow

```
WorkManager triggers DownloadStatusWorker
        ↓
Periodic interval (30 minutes)
        ↓
TorBoxRepository.updateDownloadStatus()
        ↓
For each active download: TorBoxService.getTorrentDetails()
        ↓
Update local DownloadEntity with new status
        ↓
Database changes flow to ViewModel
        ↓
UI updates automatically via Flow
```

### File Download Flow

```
User taps "Download Complete"
        ↓
DownloadViewModel.downloadFile()
        ↓
TorBoxRepository.saveDownloadedFile()
        ↓
TorBoxService.downloadFile() [HTTP GET with streaming]
        ↓
Save to file system (download folder)
        ↓
Create DownloadHistoryEntity
        ↓
Auto-delete if enabled
        ↓
Notification sent (if enabled)
```

## State Management

### ViewModel as State Holder

```kotlin
class DownloadViewModel : AndroidViewModel {
    private val _downloadUIState = MutableStateFlow(DownloadUIState())
    val downloadUIState: StateFlow<DownloadUIState> = _downloadUIState.asStateFlow()
    
    // Updates state immutably
    _downloadUIState.update { state ->
        state.copy(isLoading = false, downloads = newDownloads)
    }
}
```

### UI Collection Pattern

```kotlin
@Composable
fun ActiveDownloadsScreen(viewModel: DownloadViewModel) {
    val state by viewModel.downloadUIState.collectAsStateWithLifecycle()
    // Recompose when state changes
}
```

## Database Schema

### Downloads Table
```sql
CREATE TABLE downloads (
    torrentId TEXT PRIMARY KEY,
    torrentName TEXT NOT NULL,
    magnetLink TEXT,
    torrentUrl TEXT,
    status TEXT,
    fileSize LONG,
    downloadedBytes LONG,
    downloadSpeed LONG,
    downloadUrl TEXT,
    downloadPath TEXT,
    addedTimestamp LONG,
    completedTimestamp LONG,
    downloadedTimestamp LONG,
    lastSyncTimestamp LONG,
    errorMessage TEXT,
    autoDelete BOOLEAN,
    isDownloaded BOOLEAN
)
```

### Download History Table
```sql
CREATE TABLE download_history (
    id LONG PRIMARY KEY AUTO_INCREMENT,
    torrentId TEXT,
    torrentName TEXT NOT NULL,
    fileSize LONG,
    downloadPath TEXT,
    status TEXT,
    addedDate LONG,
    completedDate LONG,
    downloadedDate LONG
)
```

## API Integration

### TorBox API Endpoints Used

1. **Add Torrent**
   ```
   POST /api/v1/torrent/add
   Body: { "magnet": "..." } or { "torrent_url": "..." }
   ```

2. **Get User Torrents**
   ```
   GET /api/v1/user/torrents
   Header: Authorization: Bearer <API_KEY>
   ```

3. **Get Torrent Details**
   ```
   GET /api/v1/torrent/{id}
   Header: Authorization: Bearer <API_KEY>
   ```

### Retrofit Configuration

- **Base URL**: https://api.torbox.app/
- **Converter**: Kotlinx Serialization (JSON)
- **Interceptors**: Logging interceptor (debug only)
- **Timeout**: 30 seconds
- **Connection Pooling**: Enabled (OkHttp default)

## Concurrency Model

### Coroutines Usage

- **Main**: UI updates
- **IO**: Database and network operations
- **Dispatchers.IO**: Used for all I/O operations
- **Flow**: Cold streams for reactive updates

### WorkManager

- **Frequency**: Every 30 minutes
- **Constraints**: None (runs on any network)
- **Retry Policy**: Up to 3 attempts with backoff

## Security Considerations

### API Key Storage
- Encrypted using Android Keystore (API 23+)
- Accessed via `SecurePreferencesManager`
- Never logged or exposed
- Used as Bearer token in HTTP requests

### Network Security
- HTTPS enforced via `network_security_config.xml`
- Certificate pinning possible (future enhancement)
- OkHttp logging only in debug builds

### Storage Permissions
- Runtime permission checks for file operations
- Graceful fallback to app-private storage
- SAF (Storage Access Framework) ready

## Testing Strategy

### Unit Tests (Future)
```kotlin
@Test
fun testAddMagnetLink() {
    // Mock API responses
    // Verify repository updates database
    // Assert UI state changes
}
```

### Integration Tests (Future)
```kotlin
@Test
fun testEndToEndDownload() {
    // Create test download
    // Verify database persistence
    // Mock API responses
    // Verify file save
}
```

### Manual Testing
1. Add test magnet link (Ubuntu ISO)
2. Verify appears in Active Downloads
3. Mock completion in API response
4. Verify transition to Download button
5. Test file download and save

## Performance Optimization

### Memory
- **Lazy composition**: Only visible items rendered
- **Object pooling**: Room handles statement caching
- **Coroutine cancellation**: Automatic on screen exit

### Network
- **Connection pooling**: OkHttp reuses TCP connections
- **Request compression**: Handled by OkHttp
- **Caching**: Can be added to interceptor chain

### Database
- **Query indexing**: Indexed on torrentId and status
- **Paging**: Can be added with Room Paging 3
- **Transactions**: Used in batch operations

### Battery
- **WorkManager**: Respects device battery optimization
- **Frequency**: 30-minute sync window is reasonable
- **Coroutines**: Suspend functions avoid thread overhead

## Extensibility Points

### Adding New Features

1. **New Torrent Source**
   - Add to TorBoxService interface
   - Update Repository
   - Create new ViewModel state
   - Build new Screen

2. **New Storage Backend**
   - Extend TorBoxRepository
   - Implement new data source
   - No UI changes needed

3. **Advanced Notifications**
   - Create NotificationManager
   - Trigger from WorkManager or ViewModel
   - Use NotificationCompat

4. **Download Scheduling**
   - New Screen for schedule config
   - Store in database
   - Use AlarmManager or Notification Action

## Error Handling Strategy

### Network Errors
```
HTTP Failure → Result.failure(Exception)
→ ViewModel catches → Displays error message
→ User can retry manually or auto-retry via WorkManager
```

### Database Errors
```
Query failure → Log and suppress (graceful degradation)
Write failure → Retry with exponential backoff
Data corruption → Fallback to destructive migration
```

### Permission Errors
```
Storage denied → Show permission request dialog
Denied permanently → Explain and link to settings
Denied → Continue with default folder
```

## Code Organization

### Naming Conventions
- Classes: PascalCase
- Functions: camelCase
- Constants: UPPER_SNAKE_CASE
- Private members: _leadingUnderscore

### Package Structure
- Mirrors feature/layer organization
- Each layer is independent
- Clear dependency direction (UI → ViewModel → Repository → Data)

### Documentation
- Public API documented with KDoc
- Complex logic commented
- README for setup
- ARCHITECTURE.md for this document

## Future Improvements

1. **Testing**: Add unit and integration tests
2. **Pagination**: Implement Room Paging 3 for large lists
3. **Caching**: Add Retrofit cache interceptor
4. **Analytics**: Track user actions and errors
5. **Crash Reporting**: Integrate Firebase Crashlytics
6. **Feature Flags**: Add remote config for features
7. **Proguard**: Enhanced obfuscation rules
8. **Analytics Events**: User journey tracking

## Dependency Diagram

```
Presentation Layer
    ↓
UI ← ViewModel (state holder)
    ↓
Repository (data coordinator)
    ↓
    ├→ API (TorBoxService)
    ├→ Database (Room DAOs)
    ├→ Security (EncryptedPreferences)
    └→ WorkManager (background)
```

## Conclusion

The TorBox Downloader architecture balances simplicity with extensibility, providing a solid foundation for growth while maintaining clean separation of concerns. Each layer can be tested independently, and adding new features requires minimal changes to existing code.
