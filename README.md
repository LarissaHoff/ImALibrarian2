# I'm a *Librarian*

A personal home-library cataloguing Android app with an Atomic Age / Space Age visual design.

## Features

- **My Library** — Catalogue books you own with full bibliographic data
- **Wishlist** — Track books you want to acquire, with priority levels
- **Barcode Scanning** — Scan ISBN barcodes with CameraX + ML Kit
- **Cover Scanning** — Photograph book covers for image recognition
- **Title Search** — Search Google Books & Open Library APIs
- **Import/Export** — CSV and JSON format support
- **Statistics** — Retro-inspired charts for your reading habits
- **Duplicate Detection** — Warns when the same ISBN is scanned twice
- **Series Support** — Organize books in series (Discworld, Foundation, etc.)
- **Multiple Copies** — Add more than one copy of the same book
- **Did Not Finish** — Track books you started but didn't complete

## Tech Stack

- Kotlin
- Jetpack Compose UI
- Material 3 (Atomic Age custom theme)
- Room database (offline-first)
- Hilt dependency injection
- MVVM architecture
- CameraX + ML Kit Barcode Scanning
- Retrofit + kotlinx.serialization
- Coil for image loading
- Navigation Compose

## Setup

1. Clone the repository
2. Open in Android Studio (Flamingo or later)
3. Sync Gradle
4. Run on a device/emulator with Android 10+ (API 29)

### Google Books API (Optional)

The app works without an API key but you can add one for higher rate limits:

1. Get a key from [Google Cloud Console](https://console.cloud.google.com/)
2. Add to `local.properties`:
   ```
   GOOGLE_BOOKS_API_KEY=your_key_here
   ```

### ISBNdb (Optional)

For additional metadata, supply an ISBNdb API key in `local.properties`:
```
ISBNDB_API_KEY=your_key_here
```

## Architecture

```
app/src/main/java/app/imalibrarian/
├── data/
│   ├── local/db/          # Room entities, DAOs, database
│   ├── remote/            # Retrofit API interfaces & models
│   ├── repository/        # Repository implementations
│   └── mapper/            # MetadataMerger
├── domain/
│   ├── model/             # Domain models (Book, WishlistItem, etc.)
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Use cases
├── scanner/               # CameraX + ML Kit scanner managers
├── ui/
│   ├── theme/             # Atomic Age theme (Color, Type, Theme)
│   ├── components/        # Reusable Compose components
│   ├── screen/            # All screens
│   ├── navigation/        # Navigation graph
│   └── viewmodel/         # ViewModels
├── di/                    # Hilt DI modules
├── ImALibrarianApplication.kt
└── MainActivity.kt
```

## Visual Design

The app uses an **Atomic Age / Mid-Century Modern** design system:

- **Primary**: Turquoise / Teal / Aqua
- **Accent**: Coral / Orange / Mustard Yellow
- **Background**: Cream / Off-white
- **Dark mode**: Deep Navy / Space Black
- **Typography**: Monospace-inspired with bold display headings
- **Components**: Starburst ratings, orbital progress indicators, retro bar charts

## License

Private project — All rights reserved.