package app.imalibrarian.data.local.db.entity

import androidx.room.Entity

@Entity(
    tableName = "book_tag_cross_ref",
    primaryKeys = ["bookId", "tagId"]
)
data class BookTagCrossRef(
    val bookId: Long,
    val tagId: Long
)