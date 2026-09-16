package im.a.librarian.domain.usecase

import im.a.librarian.domain.model.ReadStatus
import im.a.librarian.domain.model.Statistics
import im.a.librarian.domain.repository.BookRepository
import im.a.librarian.domain.repository.WishlistRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class StatisticsUseCaseTest {

    private val bookRepository: BookRepository = mockk()
    private val wishlistRepository: WishlistRepository = mockk()
    private val useCase = GetStatisticsUseCase(bookRepository, wishlistRepository)

    @Test
    fun `getStatistics returns correct counts`() = runTest {
        coEvery { bookRepository.getBookCount() } returns 10
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.FINISHED) } returns 5
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.UNREAD) } returns 3
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.CURRENTLY_READING) } returns 1
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.DID_NOT_FINISH) } returns 1
        coEvery { bookRepository.getBookCountByGenre() } returns mapOf("Fiction" to 7, "Non-Fiction" to 3)
        coEvery { bookRepository.getAllAuthors() } returns listOf("Author One", "Author Two", "Author One")
        coEvery { wishlistRepository.getWishlistCount() } returns 4

        val stats = useCase.getStatistics()

        assertEquals(10, stats.totalBooks)
        assertEquals(5, stats.booksRead)
        assertEquals(3, stats.booksUnread)
        assertEquals(1, stats.booksCurrentlyReading)
        assertEquals(1, stats.booksDidNotFinish)
        assertEquals(4, stats.totalWishlistItems)
    }

    @Test
    fun `getStatistics calculates reading progress`() = runTest {
        coEvery { bookRepository.getBookCount() } returns 10
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.FINISHED) } returns 5
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.UNREAD) } returns 3
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.CURRENTLY_READING) } returns 1
        coEvery { bookRepository.getBookCountByStatus(ReadStatus.DID_NOT_FINISH) } returns 1
        coEvery { bookRepository.getBookCountByGenre() } returns emptyMap()
        coEvery { bookRepository.getAllAuthors() } returns emptyList()
        coEvery { wishlistRepository.getWishlistCount() } returns 0

        val stats = useCase.getStatistics()

        assertEquals(0.6f, stats.readingProgress, 0.01f)
    }
}