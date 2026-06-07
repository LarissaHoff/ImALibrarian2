package app.imalibrarian.data.local.db.entity

import androidx.room.Entity

@Entity(
    tableName = "book_author_cross_ref",
    primaryKeys = ["bookId", "authorId"]
)
data class BookAuthorCrossRef(
    val bookId: Long,
    val authorId: Long,
    val role: String
)