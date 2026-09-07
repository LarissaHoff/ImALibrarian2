package im.a.librarian.data.repository

import im.a.librarian.data.local.db.dao.WishlistDao
import im.a.librarian.data.local.db.entity.WishlistItemEntity
import im.a.librarian.domain.model.*
import im.a.librarian.domain.repository.WishlistRepository
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

    override suspend fun getWishlistItemByTitleAndAuthor(title: String, authorNames: String): WishlistItem? {
        return wishlistDao.getWishlistItemByTitleAndAuthor(title, authorNames)?.toDomain()
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
        pageCount = pageCount,
        language = language,
        originalPublicationYear = originalPublicationYear,
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
        pageCount = pageCount,
        language = language,
        originalPublicationYear = originalPublicationYear,
        genre = genre,
        subgenre = subgenre,
        priority = priority.name,
        notes = notes,
        coverImagePath = coverImagePath,
        dateAdded = dateAdded
    )
}