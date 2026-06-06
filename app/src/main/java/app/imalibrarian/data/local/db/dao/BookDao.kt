package app.imalibrarian.data.local.db.dao

import androidx.room.*
import app.imalibrarian.data.local.db.entity.BookAuthorCrossRef
import app.imalibrarian.data.local.db.entity.BookEntity
import app.imalibrarian.data.local.db.entity.BookPhotoEntity
import app.imalibrarian.data.local.db.entity.BookTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE isbn13 = :isbn13 OR isbn10 = :isbn10")
    suspend fun getBooksByIsbn(isbn10: String, isbn13: String): List<BookEntity>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR authorNames LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE readStatus = :status")
    fun getBooksByReadStatus(status: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE genre = :genre")
    fun getBooksByGenre(genre: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isFavourite = 1")
    fun getFavouriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE seriesName = :seriesName ORDER BY seriesNumber ASC")
    fun getBooksBySeries(seriesName: String): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Query("SELECT COUNT(*) FROM books WHERE readStatus = :status")
    suspend fun getBookCountByStatus(status: String): Int

    @Query("SELECT genre, COUNT(*) as count FROM books GROUP BY genre")
    suspend fun getBookCountByGenre(): List<GenreCount>

    @Query("SELECT DISTINCT genre FROM books ORDER BY genre")
    suspend fun getAllGenres(): List<String>

    @Query("SELECT DISTINCT authorNames FROM books ORDER BY authorNames")
    suspend fun getAllAuthors(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookAuthorCrossRef(ref: BookAuthorCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookTagCrossRef(ref: BookTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookPhoto(photo: BookPhotoEntity)

    @Query("DELETE FROM book_author_cross_ref WHERE bookId = :bookId")
    suspend fun deleteBookAuthorRefs(bookId: Long)

    @Query("DELETE FROM book_tag_cross_ref WHERE bookId = :bookId")
    suspend fun deleteBookTagRefs(bookId: Long)

    @Query("DELETE FROM book_photos WHERE bookId = :bookId")
    suspend fun deleteBookPhotos(bookId: Long)
}

data class GenreCount(val genre: String, val count: Int)