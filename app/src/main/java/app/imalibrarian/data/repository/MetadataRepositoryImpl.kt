package app.imalibrarian.data.repository

import app.imalibrarian.data.mapper.MetadataMerger
import app.imalibrarian.data.remote.api.GoogleBooksApi
import app.imalibrarian.data.remote.api.OpenLibraryApi
import app.imalibrarian.domain.model.ScanResult
import app.imalibrarian.domain.repository.MetadataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataRepositoryImpl @Inject constructor(
    private val googleBooksApi: GoogleBooksApi,
    private val openLibraryApi: OpenLibraryApi
) : MetadataRepository {

    override suspend fun lookupByIsbn(isbn: String): ScanResult {
        val isbn10: String
        val isbn13: String
        if (isbn.length == 10) {
            isbn10 = isbn
            isbn13 = convertToIsbn13(isbn)
        } else {
            isbn13 = isbn
            isbn10 = convertToIsbn10(isbn)
        }

        return try {
            val googleQuery = "isbn:$isbn13"
            val googleResult = try { googleBooksApi.searchByIsbn(googleQuery) } catch (_: Exception) { null }
            val olResult = try {
                openLibraryApi.getBookByIsbn("ISBN:$isbn13")
            } catch (_: Exception) { null }
            MetadataMerger.mergeResults(googleResult, olResult, isbn10, isbn13)
        } catch (_: Exception) {
            ScanResult.NotFound
        }
    }

    override suspend fun searchByTitle(query: String): List<ScanResult> {
        val googleResults = try { googleBooksApi.searchByTitle(query) } catch (_: Exception) { null }
        val olResults = try { openLibraryApi.searchByTitle(query) } catch (_: Exception) { null }
        return MetadataMerger.mergeSearchResults(googleResults, olResults)
    }

    private fun convertToIsbn13(isbn10: String): String {
        if (isbn10.length != 10) return ""
        val prefix = "978${isbn10.dropLast(1)}"
        var sum = 0
        for (i in prefix.indices) {
            val digit = prefix[i].digitToInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val checkDigit = (10 - (sum % 10)) % 10
        return "$prefix$checkDigit"
    }

    private fun convertToIsbn10(isbn13: String): String {
        if (isbn13.length != 13 || !isbn13.startsWith("978")) return ""
        val base = isbn13.substring(3, 12)
        var sum = 0
        for (i in base.indices) {
            sum += base[i].digitToInt() * (10 - i)
        }
        val checkDigit = (11 - (sum % 11)) % 11
        val checkChar = if (checkDigit == 10) "X" else checkDigit.toString()
        return "$base$checkChar"
    }
}