package app.imalibrarian.domain.usecase

import app.imalibrarian.domain.model.ReadStatus
import app.imalibrarian.domain.model.Statistics
import app.imalibrarian.domain.model.AuthorCount
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.domain.repository.WishlistRepository
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val wishlistRepository: WishlistRepository
) {
    suspend fun getStatistics(): Statistics {
        val totalBooks = bookRepository.getBookCount()
        val booksRead = bookRepository.getBookCountByStatus(ReadStatus.FINISHED)
        val booksUnread = bookRepository.getBookCountByStatus(ReadStatus.UNREAD)
        val booksCurrentlyReading = bookRepository.getBookCountByStatus(ReadStatus.CURRENTLY_READING)
        val booksDidNotFinish = bookRepository.getBookCountByStatus(ReadStatus.DID_NOT_FINISH)
        val booksByGenre = bookRepository.getBookCountByGenre()
        val totalWishlistItems = wishlistRepository.getWishlistCount()
        val allAuthors = bookRepository.getAllAuthors()

        val mostCommonAuthors = allAuthors
            .flatMap { it.split(", ") }
            .groupingBy { it }
            .eachCount()
            .map { AuthorCount(it.key, it.value) }
            .sortedByDescending { it.count }
            .take(10)

        val readingProgress = if (totalBooks > 0) {
            (booksRead.toFloat() + booksCurrentlyReading.toFloat()) / totalBooks
        } else 0f

        return Statistics(
            totalBooks = totalBooks,
            totalWishlistItems = totalWishlistItems,
            booksRead = booksRead,
            booksUnread = booksUnread,
            booksCurrentlyReading = booksCurrentlyReading,
            booksDidNotFinish = booksDidNotFinish,
            booksByGenre = booksByGenre,
            mostCommonAuthors = mostCommonAuthors,
            readingProgress = readingProgress
        )
    }
}