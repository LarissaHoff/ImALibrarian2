package im.a.librarian.data.repository

import im.a.librarian.data.mapper.MetadataMerger
import im.a.librarian.data.remote.api.GoogleBooksApi
import im.a.librarian.data.remote.api.OpenLibraryApi
import im.a.librarian.domain.model.ScanResult
import im.a.librarian.domain.repository.MetadataRepository
import im.a.librarian.domain.util.IsbnNormalizer
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataRepositoryImpl @Inject constructor(
    private val googleBooksApi: GoogleBooksApi,
    private val openLibraryApi: OpenLibraryApi
) : MetadataRepository {

    override suspend fun lookupByIsbn(isbn: String): ScanResult {
        val sanitized = IsbnNormalizer.sanitize(isbn)
        val isbn13 = IsbnNormalizer.toIsbn13(sanitized).ifBlank { sanitized }
        val isbn10 = IsbnNormalizer.toIsbn10(isbn13)

        return try {
            val googleQuery = "isbn:$isbn13"
            val googleResult = try {
                withTimeoutOrNull(PER_SOURCE_TIMEOUT_MILLIS) { googleBooksApi.searchByIsbn(googleQuery) }
            } catch (_: Exception) { null }

            val olKey = "ISBN:$isbn13"
            val olResult = try {
                withTimeoutOrNull(PER_SOURCE_TIMEOUT_MILLIS) { openLibraryApi.getBookByIsbn(olKey) }
            } catch (_: Exception) { null }

            MetadataMerger.mergeResults(googleResult, olResult, isbn10, isbn13)
        } catch (_: Exception) {
            ScanResult.NotFound
        }
    }

    override suspend fun searchByTitle(query: String): List<ScanResult> {
        val olResults = try {
            withTimeoutOrNull(PER_SOURCE_TIMEOUT_MILLIS) { openLibraryApi.searchByTitle(query) }
        } catch (_: Exception) {
            null
        }
        return MetadataMerger.mergeSearchResults(null, olResults)
    }

    companion object {
        private const val PER_SOURCE_TIMEOUT_MILLIS = 8_000L
    }
}