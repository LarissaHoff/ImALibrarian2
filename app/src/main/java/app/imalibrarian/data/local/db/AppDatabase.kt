package app.imalibrarian.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.imalibrarian.data.local.db.dao.AuthorDao
import app.imalibrarian.data.local.db.dao.BookDao
import app.imalibrarian.data.local.db.dao.BookPhotoDao
import app.imalibrarian.data.local.db.dao.TagDao
import app.imalibrarian.data.local.db.dao.WishlistDao
import app.imalibrarian.data.local.db.entity.AuthorEntity
import app.imalibrarian.data.local.db.entity.BookAuthorCrossRef
import app.imalibrarian.data.local.db.entity.BookEntity
import app.imalibrarian.data.local.db.entity.BookPhotoEntity
import app.imalibrarian.data.local.db.entity.BookTagCrossRef
import app.imalibrarian.data.local.db.entity.TagEntity
import app.imalibrarian.data.local.db.entity.WishlistItemEntity

@Database(
    entities = [
        BookEntity::class,
        AuthorEntity::class,
        TagEntity::class,
        BookAuthorCrossRef::class,
        BookTagCrossRef::class,
        BookPhotoEntity::class,
        WishlistItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
    abstract fun tagDao(): TagDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun bookPhotoDao(): BookPhotoDao
}