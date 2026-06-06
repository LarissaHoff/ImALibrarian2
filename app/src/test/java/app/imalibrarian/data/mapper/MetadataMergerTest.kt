package app.imalibrarian.data.mapper

import app.imalibrarian.data.remote.model.*
import app.imalibrarian.domain.model.ScanResult
import org.junit.Assert.*
import org.junit.Test

class MetadataMergerTest {

    @Test
    fun `mergeResults with Google data returns Found result`() {
        val googleResponse = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "1",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Test Book",
                        subtitle = "A Subtitle",
                        authors = listOf("Author One"),
                        publisher = "Test Publisher",
                        publishedDate = "2020",
                        pageCount = 300,
                        language = "en",
                        industryIdentifiers = listOf(
                            GoogleIndustryIdentifier(type = "ISBN_13", identifier = "9781234567890")
                        ),
                        categories = listOf("Fiction")
                    )
                )
            )
        )

        val result = MetadataMerger.mergeResults(googleResponse, null, "", "9781234567890")

        assertTrue(result is ScanResult.Found)
        val found = result as ScanResult.Found
        assertEquals("Test Book", found.title)
        assertEquals("A Subtitle", found.subtitle)
        assertEquals(listOf("Author One"), found.authors)
        assertEquals("Test Publisher", found.publisher)
        assertEquals(300, found.pageCount)
        assertEquals("9781234567890", found.isbn13)
        assertEquals(0.8f, found.confidence)
    }

    @Test
    fun `mergeResults with null data returns NotFound`() {
        val result = MetadataMerger.mergeResults(null, null, "", "")
        assertTrue(result is ScanResult.NotFound)
    }

    @Test
    fun `mergeResults merges Google and OpenLibrary data`() {
        val googleResponse = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "1",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Test Book",
                        authors = listOf("Author One"),
                        publisher = "Google Publisher",
                        pageCount = 300,
                        industryIdentifiers = listOf(
                            GoogleIndustryIdentifier(type = "ISBN_13", identifier = "9781234567890")
                        )
                    )
                )
            )
        )

        val olResponse = OpenLibraryBookResponse(
            map = mapOf(
                "ISBN:9781234567890" to OpenLibraryBookData(
                    title = "Test Book",
                    publishers = listOf(OpenLibraryPublisher(name = "OL Publisher")),
                    number_of_pages = 350,
                    isbn_13 = listOf("9781234567890")
                )
            )
        )

        val result = MetadataMerger.mergeResults(googleResponse, olResponse, "", "9781234567890")
        val found = result as ScanResult.Found

        assertEquals("Test Book", found.title)
        assertEquals(300, found.pageCount)
        assertEquals(1.0f, found.confidence)
    }

    @Test
    fun `parseYear extracts year from date string`() {
        val googleResponse = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "1",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Old Book",
                        publishedDate = "1956-01-01"
                    )
                )
            )
        )

        val result = MetadataMerger.mergeResults(googleResponse, null, "", "")
        val found = result as ScanResult.Found
        assertEquals(1956, found.originalPublicationYear)
    }

    @Test
    fun `mergeSearchResults deduplicates by ISBN`() {
        val googleResponse = GoogleBooksResponse(
            totalItems = 1,
            items = listOf(
                GoogleBookItem(
                    id = "1",
                    volumeInfo = GoogleVolumeInfo(
                        title = "Test Book",
                        authors = listOf("Author"),
                        industryIdentifiers = listOf(
                            GoogleIndustryIdentifier(type = "ISBN_13", identifier = "9781234567890")
                        )
                    )
                )
            )
        )

        val olResults = OpenLibrarySearchResponse(
            numFound = 1,
            docs = listOf(
                OpenLibrarySearchDoc(
                    key = "/works/1",
                    title = "Test Book",
                    author_name = listOf("Author"),
                    isbn = listOf("9781234567890")
                )
            )
        )

        val results = MetadataMerger.mergeSearchResults(googleResponse, olResults)
        assertEquals(1, results.size)
    }
}