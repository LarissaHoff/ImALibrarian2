package app.imalibrarian.data.local.db.dao

import androidx.room.*
import app.imalibrarian.data.local.db.entity.AuthorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {
    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun getAllAuthors(): Flow<List<AuthorEntity>>

    @Query("SELECT * FROM authors WHERE id = :id")
    suspend fun getAuthorById(id: Long): AuthorEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthor(author: AuthorEntity): Long

    @Query("SELECT * FROM authors WHERE name = :name")
    suspend fun getAuthorByName(name: String): AuthorEntity?

    @Delete
    suspend fun deleteAuthor(author: AuthorEntity)
}