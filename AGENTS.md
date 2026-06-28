# AGENTS.md — Project Guide for AI Assistants

## Project: I'm a *Librarian*

Personal home-library cataloguing Android app with an Atomic Age / Mid-Century Modern visual style.

## Quick Reference

- **Base Folder**: `C:\Users\lari_\Documents\VibeCool\ImALibrarian2` (use this for all relative paths)
- **Package**: `app.imalibrarian`
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 35
- **Kotlin**: 2.0.21
- **Gradle**: 8.11.1
- **Build**: `./gradlew assembleDebug` (requires JAVA_HOME pointing to JDK 17+)
- **JDK Home**: `C:\Program Files\Android\Android Studio\jbr` (JetBrains Runtime bundled with Android Studio)
- **PowerShell build**: `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; ./gradlew assembleDebug`
- **Path quirk**: Project directory contains non-ASCII characters; `android.overridePathCheck=true` is set in `gradle.properties`

## Architecture

```
data/
  local/db/     → Room entities, DAOs, AppDatabase, Converters
  remote/api/   → Retrofit interfaces (GoogleBooksApi, OpenLibraryApi)
  remote/model/  → Network response models (kotlinx.serialization)
  repository/   → Repository implementations
  mapper/       → MetadataMerger (merges Google Books + Open Library results)
domain/
  model/        → Book, WishlistItem, ReadStatus, Priority, ScanResult, Statistics
  repository/   → Repository interfaces
  usecase/      → AddBook, SearchBooks, ScanBarcode, GetStatistics, ExportBooks, ImportBooks
scanner/        → BarcodeScannerManager, CoverScannerManager (CameraX + ML Kit)
ui/
  theme/        → Color, Type, Theme (Atomic Age palette)
  components/   → AtomicCard, StarburstRating, OrbitalProgress, RetroBarChart, badges
  screen/       → 12 Compose screens
  navigation/   → Screen routes + AppNavigation NavHost
viewmodel/      → 8 HiltViewModels (package: app.imalibrarian.viewmodel)
di/             → DatabaseModule, NetworkModule, RepositoryModule
```

## Key Domain Models

- `ReadStatus`: UNREAD, CURRENTLY_READING, FINISHED, **DID_NOT_FINISH**
- `Priority`: HIGH, MEDIUM, LOW (wishlist only)
- `Book.authorNames`: Comma-separated string (denormalized for search)
- `Book.seriesName` / `Book.seriesNumber`: Series support
- Duplicate detection: `getBooksByIsbn()` returns existing books with same ISBN

## API Integration

Two sources, merged by `MetadataMerger`:
1. Google Books API (`volumes?q=isbn:`) — priority
2. Open Library API (`books?bibkeys=ISBN:` + `search.json`) — fallback

Both are optional; app works offline-first with Room.

## Build & Run Commands

All commands require `JAVA_HOME` set to the bundled JetBrains Runtime (or any JDK 17+). The project directory contains non-ASCII characters, so always set `JAVA_HOME` from the same shell session before invoking Gradle.

```powershell
# PowerShell (recommended on Windows)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Build a debug APK (output: app/build/outputs/apk/debug/app-debug.apk)
./gradlew assembleDebug

# Run unit tests (note: pre-existing compile error in StatisticsUseCaseTest.kt
# unrelated to recent branches — see Known Warnings)
./gradlew testDebugUnitTest

# Install the debug APK on the currently connected device/emulator
./gradlew installDebug

# Launch the app on the connected device (after installDebug)
adb shell monkey -p app.imalibrarian -c android.intent.category.LAUNCHER 1
# or with an explicit component:
adb shell am start -n app.imalibrarian/.MainActivity

# Build + install + launch in one go
./gradlew installDebug && adb shell monkey -p app.imalibrarian -c android.intent.category.LAUNCHER 1
```

### ADB

`adb` is **not on the system PATH**. The full path is:

```
C:\Users\lari_\AppData\Local\Android\Sdk\platform-tools\adb.exe
```

Useful commands:

```powershell
# List connected devices
& "C:\Users\lari_\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices -l

# Restart the adb server (fixes stuck "offline" devices)
& "C:\Users\lari_\AppData\Local\Android\Sdk\platform-tools\adb.exe" kill-server
& "C:\Users\lari_\AppData\Local\Android\Sdk\platform-tools\adb.exe" start-server

# Reconnect to a wireless device
& "C:\Users\lari_\AppData\Local\Android\Sdk\platform-tools\adb.exe" connect <ip>:5555

# Tail logcat for this app
& "C:\Users\lari_\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat --pid=$(adb shell pidof -s app.imalibrarian)
```

If Gradle wrapper is missing: `./gradlew wrapper` (requires JAVA_HOME)

## Safety Rules

- **Before any destructive operation** (e.g., `pm clear`, `uninstall`, database wipe, reset), always ask for permission first and suggest the user export their data via the app's built-in Import/Export feature beforehand.

## Known Warnings (non-blocking)

- Deprecated `Icons.Filled.ArrowBack` → should migrate to `Icons.AutoMirrored.Filled.ArrowBack`
- Deprecated `SearchBar` overload → should migrate to new `inputField`-based API
- Deprecated `window.statusBarColor` → should use `enableEdgeToEdge()`
- Room `exportSchema = false` — schema not exported for migrations yet
- `@OptIn(ExperimentalCoroutinesApi::class)` needed for `suspendCancellableCoroutine` in scanner code

## Style Conventions

- Atomic Age visual theme: turquoise/teal primary, coral/mustard accent, cream background, starburst/rating components
- ReadStatus badge colors: UNREAD=outline, CURRENTLY_READING=ReadingBlue, FINISHED=SuccessGreen, DID_NOT_FINISH=DnfOrange
- All Compose screens use `hiltViewModel()`, `collectAsState()`, Material 3 with custom `ImALibrarianTheme`
- No XML layouts — all screens are Compose functions
- Room entities use String for ReadStatus/Priority enums (not TypeConverters for enums)
- Domain models `Book` and `ReadStatus` annotated with `@Serializable` for JSON import/export (serializer generated by kotlinx.serialization compiler plugin)

## What's Scaffolded vs Fully Implemented

| Feature | Status |
|---|---|
| Room database schema | ✅ Full |
| Book CRUD | ✅ Full |
| Wishlist CRUD | ✅ Full |
| Search/filter/sort | ✅ Full |
| Statistics dashboard | ✅ Full (retro charts) |
| Import/Export (CSV/JSON) | ✅ Full (file picker via SAF CreateDocument) |
| Barcode scanner UI | ✅ Scaffold (CameraX intent, needs live preview) |
| Cover scanner UI | ✅ Scaffold (ML Kit intent, needs live preview) |
| Metadata lookup | ✅ Full (Google Books + Open Library) |
| Duplicate detection | ✅ Full |
| Series support | ✅ Full |
| Cloud backup (Phase 2) | ❌ Not started |
| Goodreads/LibraryThing import | ❌ Not started |