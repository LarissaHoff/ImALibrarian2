package im.a.librarian.domain.usecase

import im.a.librarian.domain.model.Book
import im.a.librarian.domain.repository.BookRepository
import im.a.librarian.domain.repository.MetadataRepository
import im.a.librarian.domain.model.ScanResult
import javax.inject.Inject

class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val metadataRepository: MetadataRepository
) {
    suspend fun lookupIsbn(isbn: String): ScanResult {
        return metadataRepository.lookupByIsbn(isbn)
    }

    suspend fun addBook(book: Book): Long {
        return bookRepository.addBook(book)
    }

    suspend fun addBookFromScan(scanResult: ScanResult.Found): Long {
        val book = Book(
            title = scanResult.title,
            subtitle = scanResult.subtitle,
            authorNames = scanResult.authors.joinToString(", "),
            isbn10 = scanResult.isbn10,
            isbn13 = scanResult.isbn13,
            publisher = scanResult.publisher,
            pageCount = scanResult.pageCount,
            language = scanResult.language,
            genre = scanResult.genre,
            originalPublicationYear = scanResult.originalPublicationYear,
            coverImagePath = scanResult.coverUrl
        )
        return bookRepository.addBook(book)
    }

    suspend fun checkDuplicate(isbn10: String, isbn13: String): List<Book> {
        return bookRepository.getBooksByIsbn(isbn10, isbn13)
    }
}