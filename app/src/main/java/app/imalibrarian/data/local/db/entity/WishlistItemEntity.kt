package app.imalibrarian.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlist_items",
    indices = [
        Index("isbn10"),
        Index("isbn13"),
        Index("title"),
        Index("priority")
    ]
)
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val isbn10: String = "",
    val isbn13: String = "",
    val authorNames: String = "",
    val publisher: String = "",
    val genre: String = "",
    val subgenre: String = "",
    val priority: String = "MEDIUM",
    val notes: String = "",
    val coverImagePath: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)