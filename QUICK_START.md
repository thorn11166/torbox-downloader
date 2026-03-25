# 🚀 TorBox Downloader - Quick Start (5 Minutes)

## What is This?

A **complete, production-ready Android app** that downloads torrents via TorBox API. Intercept magnet links, track downloads in real-time, and auto-save files.

## 30-Second Setup

```bash
# 1. Have Android Studio? Open this folder
# File → Open → Select this folder

# 2. Or use CLI:
./gradlew installDebug

# 3. Open app on device/emulator
# Settings tab → Enter TorBox API key → Done!
```

## 60-Second Overview

**What It Does**:
- ✅ Intercept magnet links from browser
- ✅ Send to TorBox for downloading
- ✅ Monitor progress in real-time
- ✅ Auto-download completed files
- ✅ Keep history of downloads

**How It Works**:
1. You click magnet link in browser
2. App intercepts it
3. TorBox downloads the torrent
4. App monitors progress
5. When done, file auto-saves to phone

**3 Screens**:
- 📥 **Active Downloads**: Current torrents with progress bars
- 📋 **History**: All your past downloads
- ⚙️ **Settings**: API key, storage folder, notifications

## Before You Start

✅ Android Studio installed  
✅ JDK 11+  
✅ Android SDK 26+  
✅ TorBox account (free at torbox.app)  
✅ TorBox API key (get in account settings)

## Build & Run

### Option 1: Android Studio (Easiest)

1. Open Android Studio
2. File → Open → Select this folder
3. Wait for Gradle sync (2-3 minutes first time)
4. Select device/emulator from toolbar
5. Click green Run button (or Shift+F10)

### Option 2: Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or one command:
./gradlew installDebug  # Builds and installs automatically
```

### Option 3: Both at Once

```bash
# Build, install, and run
./gradlew installDebug && adb shell am start -n com.torbox.downloader/.ui.MainActivity
```

## First Run

1. **Grant Permissions**
   - App asks for storage access → Tap "Allow"

2. **Configure API Key**
   - Open app → Settings tab
   - Paste your TorBox API key
   - Tap "Save Settings"
   - You'll see "Settings saved successfully"

3. **Start Downloading**
   - Go to Active Downloads tab
   - Tap + button (floating action button)
   - Paste magnet link or torrent URL
   - Tap "Add Download"

4. **Monitor Progress**
   - Watch real-time progress bar
   - See download speed
   - Wait for completion

5. **Download to Phone**
   - When complete, tap "Download Complete"
   - File saves to your phone's Download folder
   - View in Download History tab

## File Structure (TL;DR)

```
Key Files You Need:
├── app/build.gradle.kts         ← Dependencies
├── app/src/main/AndroidManifest ← Permissions & intents
├── app/src/main/kotlin/         ← All source code
│   └── com/torbox/downloader/
│       ├── data/                ← API, Database, Models
│       ├── ui/                  ← Screens & UI
│       └── work/                ← Background tasks
├── README.md                     ← Features & setup
├── ARCHITECTURE.md               ← How it works inside
├── SETUP_GUIDE.md                ← Dev environment
└── API_INTEGRATION.md            ← TorBox API details
```

## Common Tasks

### Add Magnet Link Manually
1. Tap + button in Active Downloads
2. Paste link
3. Tap "Add Download"

### Change Download Folder
1. Settings tab
2. Tap "Select Folder"
3. Pick new folder
4. Tap "Save Settings"

### Auto-Delete Completed
1. Settings tab
2. Toggle "Auto-delete after download"
3. Saves settings

### Enable Notifications
1. Settings tab
2. Toggle "Enable notifications"
3. You'll get notified when downloads complete

### View Logcat (Debug)
```bash
adb logcat | grep TorBox
```

### Clear App Data
```bash
adb shell pm clear com.torbox.downloader
```

## Troubleshooting

### "API key is invalid"
→ Copy-paste again from torbox.app account settings

### "No storage permission"
→ Tap "Allow" when app asks for permission

### "Network error"
→ Check internet connection, wait a moment, try again

### "Gradle sync failed"
```bash
./gradlew clean
./gradlew build
```

### App crashes on launch
```bash
./gradlew installDebug
adb logcat | grep FATAL
```

### "ADB: command not found"
```bash
# Add to PATH or use full path:
# Windows: C:\Users\YourName\AppData\Local\Android\Sdk\platform-tools
# macOS: ~/Library/Android/sdk/platform-tools
# Linux: ~/Android/sdk/platform-tools
```

## Architecture (Super Quick)

```
UI Layer (Compose Screens)
    ↓
ViewModel (State Management)
    ↓
Repository (Business Logic)
    ↓
┌─────────────────┐
│ Database (Room) │  Local storage
│ API (Retrofit)  │  Remote storage
└─────────────────┘
```

**Data Flow**:
1. User action → ViewModel
2. ViewModel → Repository
3. Repository → API or Database
4. Response → ViewModel
5. ViewModel → UI redraws

## Next Steps

**After First Run**:
1. ✅ Test with real magnet link
2. ✅ Wait for download to complete
3. ✅ Tap "Download Complete"
4. ✅ Find file on phone

**For Development**:
- Read `ARCHITECTURE.md` to understand code structure
- Read `SETUP_GUIDE.md` for deeper dev setup
- Read `API_INTEGRATION.md` for API details

**For Deployment**:
- Run `./gradlew assembleRelease` for production build
- Use signed APK for Play Store submission
- See `SETUP_GUIDE.md` for signing details

## Key Technologies

- **Kotlin**: Modern Android language
- **Jetpack Compose**: Modern UI framework
- **Room**: Database
- **Retrofit**: API calls
- **Material Design 3**: UI design system
- **WorkManager**: Background tasks
- **Coroutines**: Async operations

All are industry-standard and production-quality.

## Features Included

✅ Magnet link interception  
✅ Torrent URL support  
✅ Real-time progress monitoring  
✅ Background status sync  
✅ Auto-download to storage  
✅ Download history  
✅ Settings screen  
✅ Encrypted API key storage  
✅ Light/Dark theme  
✅ Material Design 3 UI  
✅ Error handling & retry logic  
✅ Offline capability  

## Documentation Files

| File | Purpose |
|------|---------|
| **README.md** | Features & overview |
| **QUICK_START.md** | This file - 5 min start |
| **SETUP_GUIDE.md** | Detailed dev setup |
| **ARCHITECTURE.md** | How code is organized |
| **API_INTEGRATION.md** | TorBox API reference |
| **PROJECT_SUMMARY.md** | Complete summary |

## Support

**Having Issues?**
1. Check relevant .md file (docs are comprehensive)
2. Check logcat: `adb logcat | grep TorBox`
3. Verify TorBox API key is correct
4. Try `./gradlew clean build`

## Ready?

```bash
cd torbox-downloader
./gradlew installDebug
# Open app, add Settings, start downloading! 🎉
```

**Time from now to first download**: ~5 minutes  
**Technical difficulty**: Beginner-friendly  
**Code quality**: Production-ready

Enjoy! 🚀

---

**Questions?** Everything is documented. Start with README.md, then drill down.
