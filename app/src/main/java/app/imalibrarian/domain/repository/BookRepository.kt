package app.imalibrarian.domain.repository

import app.imalibrarian.domain.model.Book
import app.imalibrarian.domain.model.ReadStatus
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun getBooksByIsbn(isbn10: String, isbn13: String): List<Book>
    fun searchBooks(query: String): Flow<List<Book>>
    fun getBooksByReadStatus(status: ReadStatus): Flow<List<Book>>
    fun getBooksByGenre(genre: String): Flow<List<Book>>
    fun getFavouriteBooks(): Flow<List<Book>>
    fun getBooksBySeries(seriesName: String): Flow<List<Book>>
    suspend fun addBook(book: Book): Long
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(book: Book)
    suspend fun getBookCount(): Int
    suspend fun getBookCountByStatus(status: ReadStatus): Int
    suspend fun getBookCountByGenre(): Map<String, Int>
    suspend fun getAllGenres(): List<String>
    suspend fun getAllAuthors(): List<String>
}