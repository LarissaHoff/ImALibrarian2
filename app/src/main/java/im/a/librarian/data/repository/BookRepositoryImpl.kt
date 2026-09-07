package im.a.librarian.data.repository

import im.a.librarian.data.local.db.dao.*
import im.a.librarian.data.local.db.entity.*
import im.a.librarian.domain.model.*
import im.a.librarian.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val authorDao: AuthorDao,
    private val tagDao: TagDao,
    private val bookPhotoDao: BookPhotoDao
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBookById(id: Long): Book? {
        return bookDao.getBookById(id)?.toDomain()
    }

    override suspend fun getBooksByIsbn(isbn10: String, isbn13: String): List<Book> {
        return bookDao.getBooksByIsbn(isbn10, isbn13).map { it.toDomain() }
    }

    override fun searchBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchLocalBooksByTitleOrAuthor(query: String): Flow<List<Book>> {
        return bookDao.searchLocalBooksByTitleOrAuthor(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBooksByReadStatus(status: ReadStatus): Flow<List<Book>> {
        return bookDao.getBooksByReadStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBooksByGenre(genre: String): Flow<List<Book>> {
        return bookDao.getBooksByGenre(genre).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavouriteBooks(): Flow<List<Book>> {
        return bookDao.getFavouriteBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBooksBySeries(seriesName: String): Flow<List<Book>> {
        return bookDao.getBooksBySeries(seriesName).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addBook(book: Book): Long {
        return bookDao.insertBook(book.toEntity())
    }

    override suspend fun updateBook(book: Book) {
        bookDao.updateBook(book.toEntity())
    }

    override suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book.toEntity())
    }

    override suspend fun getBookCount(): Int = bookDao.getBookCount()
    override suspend fun getBookCountByStatus(status: ReadStatus): Int =
        bookDao.getBookCountByStatus(status.name)

    override suspend fun getBookCountByGenre(): Map<String, Int> {
        return bookDao.getBookCountByGenre().associate { it.genre to it.count }
    }

    override suspend fun getAllGenres(): List<String> = bookDao.getAllGenres()
    override suspend fun getAllAuthors(): List<String> = bookDao.getAllAuthors()
    override suspend fun getAllSeriesNames(): List<String> = bookDao.getAllSeriesNames()
    override suspend fun getAllPublishers(): List<String> = bookDao.getAllPublishers()

    private fun BookEntity.toDomain() = Book(
        id = id,
        title = title,
        subtitle = subtitle,
        authorNames = authorNames,
        isbn10 = isbn10,
        isbn13 = isbn13,
        publisher = publisher,
        placeOfPublication = placeOfPublication,
        pageCount = pageCount,
        language = language,
        originalPublicationYear = originalPublicationYear,
        editionPublicationYear = editionPublicationYear,
        editionNumber = editionNumber,
        printingNumber = printingNumber,
        genre = genre,
        subgenre = subgenre,
        dateAcquired = dateAcquired,
        purchasePrice = purchasePrice,
        sourceOfPurchase = sourceOfPurchase,
        shelfLocation = shelfLocation,
        readStatus = ReadStatus.valueOf(readStatus),
        rating = rating,
        personalNotes = personalNotes,
        translator = translator,
        isFavourite = isFavourite,
        seriesName = seriesName,
        seriesNumber = seriesNumber,
        coverImagePath = coverImagePath,
        dateAdded = dateAdded
    )

    private fun Book.toEntity() = BookEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        authorNames = authorNames,
        isbn10 = isbn10,
        isbn13 = isbn13,
        publisher = publisher,
        placeOfPublication = placeOfPublication,
        pageCount = pageCount,
        language = language,
        originalPublicationYear = originalPublicationYear,
        editionPublicationYear = editionPublicationYear,
        editionNumber = editionNumber,
        printingNumber = printingNumber,
        genre = genre,
        subgenre = subgenre,
        dateAcquired = dateAcquired,
        purchasePrice = purchasePrice,
        sourceOfPurchase = sourceOfPurchase,
        shelfLocation = shelfLocation,
        readStatus = readStatus.name,
        rating = rating,
        personalNotes = personalNotes,
        translator = translator,
        isFavourite = isFavourite,
        seriesName = seriesName,
        seriesNumber = seriesNumber,
        coverImagePath = coverImagePath,
        dateAdded = dateAdded
    )
}