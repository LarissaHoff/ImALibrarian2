package im.a.librarian.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import im.a.librarian.data.local.db.dao.AuthorDao
import im.a.librarian.data.local.db.dao.BookDao
import im.a.librarian.data.local.db.dao.BookPhotoDao
import im.a.librarian.data.local.db.dao.TagDao
import im.a.librarian.data.local.db.dao.WishlistDao
import im.a.librarian.data.local.db.entity.AuthorEntity
import im.a.librarian.data.local.db.entity.BookAuthorCrossRef
import im.a.librarian.data.local.db.entity.BookEntity
import im.a.librarian.data.local.db.entity.BookPhotoEntity
import im.a.librarian.data.local.db.entity.BookTagCrossRef
import im.a.librarian.data.local.db.entity.TagEntity
import im.a.librarian.data.local.db.entity.WishlistItemEntity

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
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
    abstract fun tagDao(): TagDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun bookPhotoDao(): BookPhotoDao
}