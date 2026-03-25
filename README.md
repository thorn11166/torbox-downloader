# TorBox Downloader

A production-ready Android app that intercepts torrent/magnet links from the default browser, sends them to the TorBox API for downloading, monitors status in real-time, and auto-downloads completed files to device storage.

## Features

### MVP Features
- **Intercept magnet/torrent links** from browser via intent filter
- **Send links to TorBox API** (user provides API key in settings)
- **Monitor TorBox download status** in real-time
- **Auto-download completed files** to device storage
- **View active downloads/queue** with real-time progress
- **View download history** with file details
- **Settings screen** for API key, storage location, and notification preferences

### Additional Features
- **Quick-access floating action button** to manually add magnet/torrent links
- **Secure API key storage** using encrypted SharedPreferences
- **Background status monitoring** using WorkManager
- **Offline resilience** with local SQLite database
- **Material Design 3** UI with light/dark theme support
- **Resume-capable downloads** with OkHttp
- **Comprehensive error handling** and user feedback

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Database**: Room (SQLite ORM)
- **Network**: Retrofit + OkHttp
- **Background Work**: WorkManager
- **Coroutines**: kotlinx.coroutines
- **Security**: androidx.security.crypto
- **Target API**: 34 (Android 15), Minimum API: 26 (Android 8.0)

## Project Structure

```
app/
├── src/main/
│   ├── kotlin/com/torbox/downloader/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   ├── RetrofitClient.kt
│   │   │   │   └── TorBoxService.kt
│   │   │   ├── db/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── DownloadDao.kt
│   │   │   │   └── RoomEntities.kt
│   │   │   ├── models/
│   │   │   │   └── TorBoxModels.kt
│   │   │   ├── repository/
│   │   │   │   └── TorBoxRepository.kt
│   │   │   └── security/
│   │   │       └── SecurePreferencesManager.kt
│   │   ├── ui/
│   │   │   ├── MainActivity.kt
│   │   │   ├── screens/
│   │   │   │   ├── ActiveDownloadsScreen.kt
│   │   │   │   ├── DownloadHistoryScreen.kt
│   │   │   │   └── SettingsScreen.kt
│   │   │   ├── state/
│   │   │   │   └── UIStates.kt
│   │   │   ├── theme/
│   │   │   │   └── Theme.kt
│   │   │   └── viewmodel/
│   │   │       └── DownloadViewModel.kt
│   │   ├── broadcast/
│   │   │   └── MagnetLinkReceiver.kt
│   │   └── work/
│   │       └── DownloadStatusWorker.kt
│   └── res/
│       ├── values/
│       │   ├── colors.xml
│       │   ├── dimens.xml
│       │   ├── strings.xml
│       │   └── themes.xml
│       └── xml/
│           ├── backup_descriptor.xml
│           └── data_extraction_rules.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK 34

### Building

1. Clone the repository:
```bash
git clone <repository-url>
cd torbox-downloader
```

2. Create/update `gradle.properties` (if needed):
```properties
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
```

3. Build the project:
```bash
./gradlew build
```

4. Run on device/emulator:
```bash
./gradlew installDebug
```

### Configuration

1. **Get TorBox API Key**:
   - Create account at [TorBox](https://torbox.app)
   - Generate API key in account settings

2. **Configure App**:
   - Open TorBox Downloader
   - Go to Settings tab
   - Enter your TorBox API key
   - Select download folder
   - Toggle auto-delete and notifications as desired
   - Tap Save Settings

### Permissions Required

- `INTERNET` - API calls to TorBox
- `READ_EXTERNAL_STORAGE` - Reading torrent files
- `WRITE_EXTERNAL_STORAGE` - Saving downloaded files
- `QUERY_ALL_PACKAGES` - Browser detection
- `POST_NOTIFICATIONS` - Download notifications (Android 13+)

## Usage

### Adding Downloads

1. **From Browser**:
   - Click on magnet link in browser
   - Select TorBox Downloader
   - Link automatically added to app

2. **Manual Entry**:
   - Tap the floating action button (+)
   - Paste magnet link or torrent URL
   - Tap Add Download

### Monitoring Progress

- **Active Downloads tab**: View real-time progress with speed and ETA
- **Status indicators**: Queued, Downloading, Completed, Failed
- **Progress bars**: Visual representation of download progress

### Downloading Files

- Once download completes on TorBox, tap "Download Complete"
- File saved to configured download folder
- Optionally auto-delete after download (Settings toggle)

### Download History

- View all downloaded files with metadata
- Timestamps for when added and downloaded
- File paths for downloaded items
- Clear all history with one tap

## Architecture

### MVVM Pattern
- **ViewModel**: Manages UI state and business logic (DownloadViewModel)
- **Repository**: Data access layer (TorBoxRepository)
- **Database**: Room-based persistent storage
- **API**: Retrofit service for TorBox API calls
- **UI**: Compose-based reactive screens

### Data Flow
```
UI (Compose Screens)
  ↓
ViewModel (State Management)
  ↓
Repository (Business Logic)
  ↓
Database (Room) + API (Retrofit)
```

### Key Design Decisions

1. **Encrypted SharedPreferences**: API key stored securely using Android Keystore
2. **WorkManager**: Background sync for download status (even when app is closed)
3. **Flow-based Updates**: Reactive data updates from database to UI
4. **Coroutines**: Async/await pattern for network and database operations
5. **Compose**: Modern declarative UI framework

## Error Handling

- Network errors handled with retry logic
- Invalid API keys detected at save time
- Storage permission errors with user feedback
- Graceful degradation for offline mode
- Comprehensive logging for debugging

## Security

- **API Key Encryption**: Stored using Android Keystore
- **Network Security**: HTTPS enforcement via network security config
- **Permission Checks**: Runtime permission handling for storage access
- **Data Validation**: Input sanitization for URLs and links

## Performance Optimizations

- **LazyColumn**: Efficient list rendering in Compose
- **WorkManager**: Low-power background tasks
- **Room Queries**: Indexed database access
- **OkHttp**: Connection pooling and caching
- **Coroutines**: Non-blocking I/O operations

## Testing

To test the app:

1. **Debug APK**: Build and install debug version on device
2. **Logcat**: Monitor logs with `adb logcat | grep "TorBox\|Retrofit\|Room"`
3. **Test Data**: Use test torrent links (e.g., Ubuntu ISO via magnet)
4. **Settings Screen**: Validate API key input and storage permissions

## Release Build

Generate release APK:

```bash
./gradlew assembleRelease
```

Signed APK (with your signing config):

```bash
./gradlew bundleRelease
```

## Common Issues

### "Invalid API key" Error
- Verify API key is correct in TorBox account settings
- Ensure internet connection is working
- Check that API key hasn't expired

### Files Not Saving
- Verify storage permissions granted
- Check that download folder is accessible
- Ensure device has sufficient storage space

### Background Sync Not Working
- WorkManager requires battery optimization to be disabled for the app
- Check Settings > Battery > Battery Optimization > Remove TorBox Downloader

## Future Enhancements

- Batch torrent management
- Download speed limiting
- Search and discovery of popular torrents
- Scheduled downloads
- Advanced filtering and sorting
- Push notifications for all statuses
- Torrent details/preview before downloading
- Multiple account support
- Download pause/resume
- Magnet/torrent file creation from local files

## License

MIT License - See LICENSE file for details

## Support

For issues, feature requests, or bug reports, please open an issue on GitHub.

## Credits

Built with Kotlin, Jetpack Compose, Room, and Retrofit for the TorBox platform.
