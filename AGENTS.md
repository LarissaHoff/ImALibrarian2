# AGENTS.md — Project Guide for AI Assistants

## Project: I'm a *Librarian*

Personal home-library cataloguing Android app with an Atomic Age / Mid-Century Modern visual style.

## Quick Reference

- **Package**: `app.imalibrarian`
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 35
- **Kotlin**: 2.0.21
- **Gradle**: 8.11.1
- **Build**: `./gradlew assembleDebug` (requires JAVA_HOME pointing to JDK 17+)
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
  viewmodel/    → 8 HiltViewModels
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

```bash
# Build
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest

# Install on device
./gradlew installDebug
```

If Gradle wrapper is missing: `./gradlew wrapper` (requires JAVA_HOME)

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

## What's Scaffolded vs Fully Implemented

| Feature | Status |
|---|---|
| Room database schema | ✅ Full |
| Book CRUD | ✅ Full |
| Wishlist CRUD | ✅ Full |
| Search/filter/sort | ✅ Full |
| Statistics dashboard | ✅ Full (retro charts) |
| Import/Export (CSV/JSON) | ✅ Full |
| Barcode scanner UI | ✅ Scaffold (CameraX intent, needs live preview) |
| Cover scanner UI | ✅ Scaffold (ML Kit intent, needs live preview) |
| Metadata lookup | ✅ Full (Google Books + Open Library) |
| Duplicate detection | ✅ Full |
| Series support | ✅ Full |
| Cloud backup (Phase 2) | ❌ Not started |
| Goodreads/LibraryThing import | ❌ Not started |