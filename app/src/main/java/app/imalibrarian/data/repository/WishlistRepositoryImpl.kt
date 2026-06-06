package app.imalibrarian.data.repository

import app.imalibrarian.data.local.db.dao.WishlistDao
import app.imalibrarian.data.local.db.entity.WishlistItemEntity
import app.imalibrarian.domain.model.*
import app.imalibrarian.domain.repository.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepositoryImpl @Inject constructor(
    private val wishlistDao: WishlistDao
) : WishlistRepository {

    override fun getAllWishlistItems(): Flow<List<WishlistItem>> {
        return wishlistDao.getAllWishlistItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWishlistItemById(id: Long): WishlistItem? {
        return wishlistDao.getWishlistItemById(id)?.toDomain()
    }

    override suspend fun getWishlistItemByIsbn(isbn10: String, isbn13: String): WishlistItem? {
        return wishlistDao.getWishlistItemByIsbn(isbn10, isbn13)?.toDomain()
    }

    override fun searchWishlistItems(query: String): Flow<List<WishlistItem>> {
        return wishlistDao.searchWishlistItems(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addWishlistItem(item: WishlistItem): Long {
        return wishlistDao.insertWishlistItem(item.toEntity())
    }

    override suspend fun updateWishlistItem(item: WishlistItem) {
        wishlistDao.updateWishlistItem(item.toEntity())
    }

    override suspend fun deleteWishlistItem(item: WishlistItem) {
        wishlistDao.deleteWishlistItem(item.toEntity())
    }

    override suspend fun getWishlistCount(): Int = wishlistDao.getWishlistCount()

    private fun WishlistItemEntity.toDomain() = WishlistItem(
        id = id,
        title = title,
        subtitle = subtitle,
        isbn10 = isbn10,
        isbn13 = isbn13,
        authorNames = authorNames,
        publisher = publisher,
        genre = genre,
        subgenre = subgenre,
        priority = Priority.valueOf(priority),
        notes = notes,
        coverImagePath = coverImagePath,
        dateAdded = dateAdded
    )

    private fun WishlistItem.toEntity() = WishlistItemEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        isbn10 = isbn10,
        isbn13 = isbn13,
        authorNames = authorNames,
        publisher = publisher,
        genre = genre,
        subgenre = subgenre,
        priority = priority.name,
        notes = notes,
        coverImagePath = coverImagePath,
        dateAdded = dateAdded
    )
}