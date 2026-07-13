package im.a.librarian.domain.usecase

import im.a.librarian.domain.model.Book
import im.a.librarian.domain.model.ScanResult
import im.a.librarian.domain.repository.BookRepository
import im.a.librarian.domain.repository.MetadataRepository
import javax.inject.Inject

class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val metadataRepository: MetadataRepository
) {
    fun searchLocalBooks(query: String) = bookRepository.searchBooks(query)

    fun searchLocalBooksByTitleOrAuthor(query: String) =
        bookRepository.searchLocalBooksByTitleOrAuthor(query)

    suspend fun searchOnline(query: String): List<ScanResult> {
        return metadataRepository.searchByTitle(query)
    }

    fun searchByReadStatus(status: im.a.librarian.domain.model.ReadStatus) =
        bookRepository.getBooksByReadStatus(status)

    fun searchByGenre(genre: String) = bookRepository.getBooksByGenre(genre)

    fun getFavourites() = bookRepository.getFavouriteBooks()
}