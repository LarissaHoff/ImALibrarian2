package im.a.librarian.domain.usecase

import im.a.librarian.domain.model.Book
import im.a.librarian.domain.model.ReadStatus
import im.a.librarian.domain.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ExportBooksUseCaseTest {

    private val bookRepository: BookRepository = mockk()
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
        every { bookRepository.getAllBooks() } returns flowOf(books)

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
        every { bookRepository.getAllBooks() } returns flowOf(books)

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
        every { bookRepository.getAllBooks() } returns flowOf(books)

        val result = useCase.exportToCsv()
        assertTrue(result.contains("\"Book, With, Commas\""))
    }
}