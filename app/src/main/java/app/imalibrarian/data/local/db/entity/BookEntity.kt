package app.imalibrarian.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index("isbn10"),
        Index("isbn13"),
        Index("title"),
        Index("authorNames"),
        Index("genre"),
        Index("readStatus"),
        Index("seriesName")
    ]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val authorNames: String = "",
    val isbn10: String = "",
    val isbn13: String = "",
    val publisher: String = "",
    val placeOfPublication: String = "",
    val pageCount: Int = 0,
    val language: String = "",
    val originalPublicationYear: Int? = null,
    val editionPublicationYear: Int? = null,
    val editionNumber: Int? = null,
    val printingNumber: Int? = null,
    val genre: String = "",
    val subgenre: String = "",
    val dateAcquired: Long? = null,
    val purchasePrice: String = "",
    val sourceOfPurchase: String = "",
    val shelfLocation: String = "",
    val readStatus: String = "UNREAD",
    val rating: Int = 0,
    val personalNotes: String = "",
    val isFavourite: Boolean = false,
    val seriesName: String = "",
    val seriesNumber: Int? = null,
    val coverImagePath: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)