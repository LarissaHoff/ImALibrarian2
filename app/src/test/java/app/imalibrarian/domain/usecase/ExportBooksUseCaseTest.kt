package app.imalibrarian.domain.usecase

import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.repository.BookRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class ExportBooksUseCaseTest {

    private val bookRepository: BookRepository = mock()
    private val useCase = ExportBooksUseCase(bookRepository)

    @Test
    fun `exportToJson returns valid JSON`() = runTest {
        val books = listOf(
            Book(
                id = 1,
                title = "Test Book",
                isbn13 = "9781234567890",
                readStatus = ReadStatus.FINISHED,
                rating = 4
            )
        )
        whenever(bookRepository.getAllBooks()).thenReturn(flowOf(books))

        val result = useCase.exportToJson()
        assertTrue(result.contains("Test Book"))
        assertTrue(result.contains("9781234567890"))
    }

    @Test
    fun `exportToCsv returns valid CSV with headers`() = runTest {
        val books = listOf(
            Book(
                id = 1,
                title = "Test Book",
                isbn13 = "9781234567890"
            )
        )
        whenever(bookRepository.getAllBooks()).thenReturn(flowOf(books))

        val result = useCase.exportToCsv()
        assertTrue(result.startsWith("id,title"))
        assertTrue(result.contains("Test Book"))
        assertTrue(result.contains("9781234567890"))
    }

    @Test
    fun `exportToCsv escapes commas in titles`() = runTest {
        val books = listOf(
            Book(id = 1, title = "Book, With, Commas")
        )
        whenever(bookRepository.getAllBooks()).thenReturn(flowOf(books))

        val result = useCase.exportToCsv()
        assertTrue(result.contains("\"Book, With, Commas\""))
    }
}