# Privacy Policy

**Last updated: July 13, 2026**

## Overview

ImALibrarian ("the App") is a personal home-library cataloguing app. Your privacy is important to us. This policy describes what data the App collects, how it is used, and your rights.

## Data Collection and Use

### Data Stored Locally on Your Device

All library data you enter into the App — including book titles, authors, ISBNs, personal notes, ratings, tags, cover photos, and wishlist items — is stored exclusively **on your device** in a local database (Room/SQLite) and internal app storage.

This data never leaves your device unless you explicitly export it (via the built-in CSV/JSON export feature) or take a backup.

### Network Requests

When you search for a book by ISBN or title, the App sends that ISBN or search query to:

- **Google Books API** (`https://www.googleapis.com/books/v1/`)
- **Open Library API** (`https://openlibrary.org/`)

These requests are made solely to retrieve book metadata (title, author, cover image, publication info) to enrich your library entries. **No personal information, device identifiers, or your existing library contents are transmitted** — only the ISBN or search term you typed.

### Camera and Media

- **Barcode scanning** uses on-device ML Kit processing. Captured camera frames are processed in memory and immediately discarded — they are never saved or transmitted.
- **Cover scanning** (OCR) uses on-device ML Kit text recognition. Captured frames are processed in memory and immediately discarded.
- When you take a cover photo or pick an image from your gallery for a book entry, that image is saved to the App's internal storage on your device and associated with the book record. It is not transmitted anywhere.

### Third-Party Services

The App uses **no** analytics, crash reporting, telemetry, advertising, or push notification services. No Firebase, no Google Analytics, no Crashlytics, no ad networks are integrated.

## Permissions

| Permission | Purpose |
|-----------|---------|
| **Camera** | Barcode scanning, cover scanning, taking book cover photos |
| **Internet** | Looking up book metadata via Google Books and Open Library APIs |
| **Storage** | Picking cover images from your gallery (read); saving picked images (write, Android 9 and below only) |

## Data Sharing

The App **does not share** any personal data with third parties. It does not sell, rent, or trade your information.

## Data Security

Since all data remains on your device, its security depends on your device's own protections (screen lock, encryption, etc.). No data is stored on or transmitted to any server controlled by the developer.

## Children's Privacy

The App does not knowingly collect any personal information from children.

## Changes to This Policy

If this policy changes, the "Last updated" date at the top will be revised.

## Contact

If you have questions about this privacy policy, please open an issue at:
[https://github.com/LarissaHoff/ImALibrarian2](https://github.com/LarissaHoff/ImALibrarian2)
