package im.a.librarian.data.local.db.dao

import androidx.room.*
import im.a.librarian.data.local.db.entity.BookPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookPhotoDao {
    @Query("SELECT * FROM book_photos WHERE bookId = :bookId")
    fun getPhotosForBook(bookId: Long): Flow<List<BookPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: BookPhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: BookPhotoEntity)
}