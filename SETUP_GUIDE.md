# TorBox Downloader - Setup and Development Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Development Setup](#development-setup)
3. [Building](#building)
4. [Running](#running)
5. [Configuration](#configuration)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements
- **OS**: Windows, macOS, or Linux
- **RAM**: 8GB minimum (16GB recommended)
- **Disk Space**: 10GB for Android SDK and build artifacts
- **Internet**: Required for Gradle dependencies

### Software Requirements
- **Java**: JDK 11 or later
- **Android Studio**: Arctic Fox (2020.3.1) or later
- **Gradle**: Included in Android Studio
- **Git**: For version control

### Device Requirements
- **Android API Level**: 26 (Android 8.0) minimum
- **Target API Level**: 34 (Android 15)
- **Device**: Physical device or Android emulator
- **Screen Sizes**: Phones (5"-6.5"), Tablets (7"+)

## Development Setup

### Step 1: Install Java Development Kit (JDK)

**Windows:**
```bash
# Using Chocolatey
choco install openjdk11
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@11
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install openjdk-11-jdk
```

**Verify:**
```bash
java -version
```

### Step 2: Install Android Studio

1. Download from [developer.android.com](https://developer.android.com/studio)
2. Follow installation wizard for your OS
3. Complete initial setup:
   - Accept licenses: `sdkmanager --licenses`
   - Install SDK components
   - Create virtual device (emulator) if needed

### Step 3: Clone Repository

```bash
git clone <repository-url>
cd torbox-downloader
```

### Step 4: Configure Android SDK

Create/update `local.properties` in project root:
```properties
sdk.dir=/path/to/android/sdk

# macOS example
# sdk.dir=/Users/username/Library/Android/sdk

# Linux example
# sdk.dir=/home/username/Android/sdk

# Windows example
# sdk.dir=C:\\Users\\username\\AppData\\Local\\Android\\sdk
```

### Step 5: Configure Gradle

Edit `gradle.properties` if needed:
```properties
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
android.nonTransitiveRClass=true
```

### Step 6: Sync Gradle

```bash
# From project root
./gradlew --version  # Should output Gradle 8.x.x

# Sync dependencies
./gradlew assemble
```

## Building

### Debug Build

```bash
# Build debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Release Build

```bash
# Build release APK (unsigned)
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

### Signed Release Build

1. **Create Keystore** (first time only):
```bash
keytool -genkey -v -keystore torbox-release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias torbox-key
```

2. **Add to gradle.properties**:
```properties
RELEASE_STORE_FILE=../torbox-release.keystore
RELEASE_STORE_PASSWORD=your_password
RELEASE_KEY_ALIAS=torbox-key
RELEASE_KEY_PASSWORD=your_password
```

3. **Build Signed APK**:
```bash
./gradlew bundleRelease  # Creates AAB for Play Store
# or
./gradlew assembleRelease  # Creates signed APK
```

### Build Variants

```bash
# Debug with logging
./gradlew assembleDebug

# Release with ProGuard optimization
./gradlew assembleRelease
```

## Running

### Via Android Studio

1. **Open Project**:
   - File → Open
   - Select project root
   - Wait for Gradle sync

2. **Run App**:
   - Select device/emulator from toolbar
   - Click "Run" button (green play icon)
   - Or use keyboard shortcut: Shift + F10 (Windows/Linux) or Ctrl + R (macOS)

### Via Command Line

```bash
# Install on connected device
./gradlew installDebug

# Install and launch
./gradlew installDebug
adb shell am start -n com.torbox.downloader/.ui.MainActivity

# Direct run (with optional build)
./gradlew installDebug
```

### On Emulator

```bash
# List available emulators
emulator -list-avds

# Launch specific emulator
emulator -avd Pixel_4_API_30

# Wait for boot, then run
./gradlew installDebug
```

### Debug with Logcat

```bash
# Filter logs
adb logcat | grep "TorBox\|retrofit\|Room"

# Real-time monitoring
adb logcat -f /tmp/android.log

# Clear previous logs
adb logcat -c
```

## Configuration

### TorBox API Setup

1. **Create Account**:
   - Visit [torbox.app](https://torbox.app)
   - Sign up for account

2. **Generate API Key**:
   - Login to account
   - Navigate to API settings/account
   - Create new API token/key
   - Copy API key

3. **Configure in App**:
   - Open Settings tab in app
   - Paste API key
   - Tap Save Settings
   - You'll see "Settings saved successfully"

### Storage Configuration

1. **Default Location**:
   - `Download` folder in app's external storage directory
   - Path: `/storage/emulated/0/Android/data/com.torbox.downloader/files/Download/`

2. **Custom Location**:
   - Settings tab → "Select Folder"
   - Navigate to desired folder
   - System will request permission
   - Folder path updates in settings

3. **Storage Permissions**:
   - App requests on first use
   - Grant "Allow"
   - Persistence across app restarts

### Notifications

In Settings:
- **Enable notifications**: Toggle on/off
- **Auto-delete completed**: Toggle to auto-remove finished downloads

## Troubleshooting

### Gradle Sync Failures

**Problem**: "Failed to resolve dependency"

**Solution**:
```bash
# Clear Gradle cache
./gradlew clean

# Update dependencies
./gradlew --refresh-dependencies build

# Check internet connection
# Verify gradle.properties configuration
```

### Build Failures

**Problem**: "Could not find symbol class DownloadViewModel"

**Solution**:
```bash
# Invalidate caches
./gradlew cleanBuildCache

# Rebuild
./gradlew clean build
```

**Problem**: "Execution failed for task ':app:lintVitalRelease'"

**Solution**:
```gradle
// Add to build.gradle.kts app section
android {
    lint {
        disable.add("MissingTranslation")
    }
}
```

### Runtime Errors

**Problem**: "Unable to locate a Java Runtime"

**Solution**:
```bash
# Set JAVA_HOME
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.x

# macOS/Linux
export JAVA_HOME=/usr/libexec/java_home -v 11
```

**Problem**: "Permission denied: /dev/kvm"

**Solution**:
```bash
# Linux - Enable KVM for emulator
sudo usermod -a -G kvm $USER
# Logout and login for changes to take effect
```

**Problem**: "API key is invalid"

**Solution**:
- Verify API key in account settings
- Ensure no extra spaces or characters
- Check key hasn't expired
- Verify account has access to API

**Problem**: "No storage permission"

**Solution**:
- Grant permission when prompted
- In Android Settings → Apps → TorBox Downloader:
  - Permissions → Storage → Allow
  - Permissions → Files and media → Allow all files

### Emulator Issues

**Problem**: "Emulator very slow"

**Solution**:
- Use x86 or x86_64 ABI (not ARM)
- Allocate more RAM to emulator (4GB+)
- Enable graphics acceleration
- Close other applications

**Problem**: "Cannot connect to internet on emulator"

**Solution**:
- Check emulator DNS settings
- Verify development machine internet
- Configure emulator network settings
- Use proxy if required in corporate network

### Device Issues

**Problem**: "ADB not recognizing device"

**Solution**:
```bash
# Enable USB debugging on device
# Settings → Developer options → USB debugging → ON

# Authorize computer
# When prompted, tap "Allow"

# Verify connection
adb devices
```

**Problem**: "Device offline in Android Studio"

**Solution**:
```bash
# Reconnect device
adb disconnect
adb connect <device-ip-address>

# Or via USB
adb kill-server
adb start-server
```

## IDE Configuration

### Android Studio Settings

1. **Configure Gradle**:
   - File → Settings → Build, Execution, Deployment → Gradle
   - Gradle JDK: Set to JDK 11+
   - Android Gradle Plugin version: 8.2.0

2. **Code Style**:
   - File → Settings → Editor → Code Style → Kotlin
   - Import scheme: Kotlin style guide

3. **Plugins**:
   - File → Settings → Plugins
   - Ensure installed:
     - Android Studio built-in plugins
     - Kotlin plugin (usually included)

### Useful Shortcuts

| Action | Windows/Linux | macOS |
|--------|--------------|-------|
| Run | Shift + F10 | Ctrl + R |
| Debug | Shift + F9 | Ctrl + D |
| Build | Ctrl + F9 | Cmd + F9 |
| Stop | Ctrl + F2 | Cmd + F2 |
| Rerun | Ctrl + F5 | Cmd + F5 |

## Next Steps

1. **First Run**:
   - Run debug build
   - Grant permissions
   - Add test magnet link
   - Verify download appears

2. **Development**:
   - Modify code
   - Hot reload with Compose Preview
   - Use debugger for breakpoints
   - Check logcat for issues

3. **Testing**:
   - Test with real torrent links
   - Test offline behavior
   - Verify storage persistence
   - Check battery impact

4. **Deployment**:
   - Create signed APK
   - Test on multiple devices
   - Prepare Play Store listing
   - Submit for review

## Support Resources

- [Android Developer Docs](https://developer.android.com/docs)
- [Jetpack Compose](https://developer.android.com/jetpack/compose/documentation)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
