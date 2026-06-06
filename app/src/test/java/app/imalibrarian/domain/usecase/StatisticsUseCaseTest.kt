package app.imalibrarian.domain.usecase

import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.model.Statistics
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.domain.repository.WishlistRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class StatisticsUseCaseTest {

    private val bookRepository: BookRepository = mock()
    private val wishlistRepository: WishlistRepository = mock()
    private val useCase = GetStatisticsUseCase(bookRepository, wishlistRepository)

    @Test
    fun `getStatistics returns correct counts`() = runTest {
        whenever(bookRepository.getBookCount()).thenReturn(10)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.FINISHED)).thenReturn(5)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.UNREAD)).thenReturn(3)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.CURRENTLY_READING)).thenReturn(1)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.DID_NOT_FINISH)).thenReturn(1)
        whenever(bookRepository.getBookCountByGenre()).thenReturn(mapOf("Fiction" to 7, "Non-Fiction" to 3))
        whenever(bookRepository.getAllAuthors()).thenReturn(listOf("Author One", "Author Two", "Author One"))
        whenever(wishlistRepository.getWishlistCount()).thenReturn(4)

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
        whenever(bookRepository.getBookCount()).thenReturn(10)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.FINISHED)).thenReturn(5)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.UNREAD)).thenReturn(3)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.CURRENTLY_READING)).thenReturn(1)
        whenever(bookRepository.getBookCountByStatus(ReadStatus.DID_NOT_FINISH)).thenReturn(1)
        whenever(bookRepository.getBookCountByGenre()).thenReturn(emptyMap())
        whenever(bookRepository.getAllAuthors()).thenReturn(emptyList())
        whenever(wishlistRepository.getWishlistCount()).thenReturn(0)

        val stats = useCase.getStatistics()

        assertEquals(0.6f, stats.readingProgress, 0.01f)
    }
}