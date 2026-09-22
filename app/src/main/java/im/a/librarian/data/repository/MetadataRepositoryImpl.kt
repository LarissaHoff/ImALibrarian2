package im.a.librarian.data.repository

import im.a.librarian.data.mapper.MetadataMerger
import im.a.librarian.data.remote.api.GoogleBooksApi
import im.a.librarian.data.remote.api.OpenLibraryApi
import im.a.librarian.data.remote.model.GoogleBooksResponse
import im.a.librarian.data.remote.model.OpenLibrarySearchResponse
import im.a.librarian.di.GoogleBooksApiKey
import im.a.librarian.domain.model.ScanResult
import im.a.librarian.domain.repository.MetadataRepository
import im.a.librarian.domain.util.IsbnNormalizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataRepositoryImpl @Inject constructor(
    private val googleBooksApi: GoogleBooksApi,
    private val openLibraryApi: OpenLibraryApi,
    @GoogleBooksApiKey private val googleBooksApiKey: String
) : MetadataRepository {

    override suspend fun lookupByIsbn(isbn: String): ScanResult {
        val sanitized = IsbnNormalizer.sanitize(isbn)
        val isbn13 = IsbnNormalizer.toIsbn13(sanitized).ifBlank { sanitized }
        val isbn10 = IsbnNormalizer.toIsbn10(isbn13)

        var failure: String? = null

        val googleResult: GoogleBooksResponse? = try {
            val response = withTimeoutOrNull(PER_SOURCE_TIMEOUT_MILLIS) {
                googleBooksApi.searchByIsbn("isbn:$isbn13", googleBooksApiKey.ifBlank { null })
            }
            if (response == null) failure = failure ?: "Google Books lookup timed out"
            response
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failure = failure ?: describeFailure("Google Books", e)
            null
        }

        val openLibraryResult: OpenLibrarySearchResponse? = try {
            val response = withTimeoutOrNull(PER_SOURCE_TIMEOUT_MILLIS) {
                openLibraryApi.getBookByIsbn("isbn:$isbn13")
            }
            if (response == null) failure = failure ?: "Open Library lookup timed out"
            response
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failure = failure ?: describeFailure("Open Library", e)
            null
        }

        val merged = MetadataMerger.mergeResults(googleResult, openLibraryResult, isbn10, isbn13)
        return when {
            merged is ScanResult.Found -> merged
            failure != null -> ScanResult.Error(failure)
            else -> ScanResult.NotFound
        }
    }

    override suspend fun searchByTitle(query: String): List<ScanResult> {
        val olResults = try {
            withTimeoutOrNull(PER_SOURCE_TIMEOUT_MILLIS) { openLibraryApi.searchByTitle(query) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        return MetadataMerger.mergeSearchResults(null, olResults)
    }

    private fun describeFailure(source: String, error: Exception): String = when {
        error is HttpException && error.code() == 429 ->
            "$source is busy right now (rate limit reached). Please try again later."
        error is HttpException && error.code() == 404 ->
            "$source has no record for this ISBN."
        error is HttpException ->
            "$source returned an error (HTTP ${error.code()})."
        error is IOException ->
            "Couldn't reach $source — check your internet connection."
        else ->
            "$source lookup failed: ${error.message ?: error.javaClass.simpleName}"
    }

    companion object {
        private const val PER_SOURCE_TIMEOUT_MILLIS = 8_000L
    }
}
