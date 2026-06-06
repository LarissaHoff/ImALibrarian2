package app.imalibrarian.data.local.db.dao

import androidx.room.*
import app.imalibrarian.data.local.db.entity.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items ORDER BY CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 WHEN 'LOW' THEN 2 ELSE 3 END, dateAdded DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE id = :id")
    suspend fun getWishlistItemById(id: Long): WishlistItemEntity?

    @Query("SELECT * FROM wishlist_items WHERE isbn13 = :isbn13 OR isbn10 = :isbn10")
    suspend fun getWishlistItemByIsbn(isbn10: String, isbn13: String): WishlistItemEntity?

    @Query("SELECT * FROM wishlist_items WHERE title LIKE '%' || :query || '%'")
    fun searchWishlistItems(query: String): Flow<List<WishlistItemEntity>>

    @Query("SELECT COUNT(*) FROM wishlist_items")
    suspend fun getWishlistCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity): Long

    @Update
    suspend fun updateWishlistItem(item: WishlistItemEntity)

    @Delete
    suspend fun deleteWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishlistItemById(id: Long)
}