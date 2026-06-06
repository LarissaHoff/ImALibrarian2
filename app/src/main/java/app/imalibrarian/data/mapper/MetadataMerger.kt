package app.imalibrarian.data.mapper

import app.imalibrarian.data.remote.model.*
import app.imalibrarian.data.local.db.entity.BookEntity
import app.imalibrarian.domain.model.ScanResult

object MetadataMerger {

    fun mergeResults(
        googleResult: GoogleBooksResponse?,
        openLibraryResult: OpenLibraryBookResponse?,
        isbn10: String = "",
        isbn13: String = ""
    ): ScanResult {
        val googleBook = googleResult?.items?.firstOrNull()?.volumeInfo
        val olBook = openLibraryResult?.map?.entries?.firstOrNull()?.value

        if (googleBook == null && olBook == null) {
            return ScanResult.NotFound
        }

        val title = googleBook?.title?.takeIf { it.isNotBlank() }
            ?: olBook?.title?.takeIf { it.isNotBlank() }
            ?: ""

        val subtitle = googleBook?.subtitle?.takeIf { it.isNotBlank() }
            ?: olBook?.subtitle?.takeIf { it.isNotBlank() }
            ?: ""

        val authors = googleBook?.authors?.takeIf { it.isNotEmpty() }
            ?: olBook?.authors?.map { it.name }?.takeIf { it.isNotEmpty() }
            ?: emptyList()

        val publisher = googleBook?.publisher?.takeIf { it.isNotBlank() }
            ?: olBook?.publishers?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
            ?: ""

        val pageCount = googleBook?.pageCount?.takeIf { it > 0 }
            ?: olBook?.number_of_pages?.takeIf { it > 0 }
            ?: 0

        val language = googleBook?.language?.takeIf { it.isNotBlank() }
            ?: ""

        val genre = googleBook?.categories?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: olBook?.subjects?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
            ?: ""

        val publishedDate = googleBook?.publishedDate ?: olBook?.publish_date ?: ""
        val publicationYear = parseYear(publishedDate)

        val identifiedIsbn10 = isbn10.takeIf { it.isNotBlank() }
            ?: googleBook?.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier
            ?: olBook?.isbn_10?.firstOrNull()
            ?: ""

        val identifiedIsbn13 = isbn13.takeIf { it.isNotBlank() }
            ?: googleBook?.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
            ?: olBook?.isbn_13?.firstOrNull()
            ?: ""

        val coverUrl = googleBook?.imageLinks?.large?.takeIf { it.isNotBlank() }
            ?: googleBook?.imageLinks?.medium?.takeIf { it.isNotBlank() }
            ?: googleBook?.imageLinks?.thumbnail?.takeIf { it.isNotBlank() }
            ?: olBook?.cover?.large?.takeIf { it.isNotBlank() }
            ?: olBook?.cover?.medium?.takeIf { it.isNotBlank() }
            ?: ""

        val hasGoogleData = googleBook != null
        val hasOlData = olBook != null
        val confidence = when {
            hasGoogleData && hasOlData -> 1.0f
            hasGoogleData || hasOlData -> 0.8f
            else -> 0.5f
        }

        return ScanResult.Found(
            title = title,
            subtitle = subtitle,
            authors = authors,
            isbn10 = identifiedIsbn10,
            isbn13 = identifiedIsbn13,
            publisher = publisher,
            pageCount = pageCount,
            language = language,
            genre = genre,
            originalPublicationYear = publicationYear,
            coverUrl = coverUrl,
            confidence = confidence
        )
    }

    fun mergeSearchResults(
        googleResults: GoogleBooksResponse?,
        olResults: OpenLibrarySearchResponse?
    ): List<ScanResult> {
        val results = mutableListOf<ScanResult>()

        googleResults?.items?.forEach { item ->
            val info = item.volumeInfo
            results.add(
                ScanResult.Found(
                    title = info.title,
                    subtitle = info.subtitle,
                    authors = info.authors,
                    isbn10 = info.industryIdentifiers.find { it.type == "ISBN_10" }?.identifier ?: "",
                    isbn13 = info.industryIdentifiers.find { it.type == "ISBN_13" }?.identifier ?: "",
                    publisher = info.publisher,
                    pageCount = info.pageCount,
                    language = info.language,
                    genre = info.categories.firstOrNull() ?: "",
                    originalPublicationYear = parseYear(info.publishedDate),
                    coverUrl = info.imageLinks.thumbnail ?: "",
                    confidence = 0.8f
                )
            )
        }

        olResults?.docs?.forEach { doc ->
            val existingIsbn = results.filterIsInstance<ScanResult.Found>()
                .any { it.isbn10 in doc.isbn || it.isbn13 in doc.isbn }
            if (!existingIsbn) {
                results.add(
                    ScanResult.Found(
                        title = doc.title,
                        subtitle = doc.subtitle ?: "",
                        authors = doc.author_name,
                        isbn10 = doc.isbn.find { it.length == 10 } ?: "",
                        isbn13 = doc.isbn.find { it.length == 13 } ?: "",
                        publisher = doc.publisher.firstOrNull() ?: "",
                        pageCount = doc.number_of_pages_median ?: 0,
                        language = doc.language.firstOrNull() ?: "",
                        genre = doc.subject.firstOrNull() ?: "",
                        originalPublicationYear = doc.first_publish_year,
                        coverUrl = doc.cover_i?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" } ?: "",
                        confidence = 0.7f
                    )
                )
            }
        }

        return results.sortedByDescending { (it as? ScanResult.Found)?.confidence }
    }

    private fun parseYear(dateStr: String): Int? {
        if (dateStr.isBlank()) return null
        val year = dateStr.take(4).toIntOrNull()
        return year?.takeIf { it in 1000..2099 }
    }
}