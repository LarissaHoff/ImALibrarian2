package app.imalibrarian.domain.usecase

import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ScanResult
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.domain.repository.MetadataRepository
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val metadataRepository: MetadataRepository
) {
    fun searchLocalBooks(query: String) = bookRepository.searchBooks(query)

    suspend fun searchOnline(query: String): List<ScanResult> {
        return metadataRepository.searchByTitle(query)
    }

    fun searchByReadStatus(status: app.imalibrarian.domain.model.ReadStatus) =
        bookRepository.getBooksByReadStatus(status)

    fun searchByGenre(genre: String) = bookRepository.getBooksByGenre(genre)

    fun getFavourites() = bookRepository.getFavouriteBooks()
}