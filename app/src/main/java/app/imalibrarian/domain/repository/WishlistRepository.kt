package app.imalibrarian.domain.repository

import app.imalibrarian.domain.model.WishlistItem
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    fun getAllWishlistItems(): Flow<List<WishlistItem>>
    suspend fun getWishlistItemById(id: Long): WishlistItem?
    suspend fun getWishlistItemByIsbn(isbn10: String, isbn13: String): WishlistItem?
    fun searchWishlistItems(query: String): Flow<List<WishlistItem>>
    suspend fun addWishlistItem(item: WishlistItem): Long
    suspend fun updateWishlistItem(item: WishlistItem)
    suspend fun deleteWishlistItem(item: WishlistItem)
    suspend fun getWishlistCount(): Int
}