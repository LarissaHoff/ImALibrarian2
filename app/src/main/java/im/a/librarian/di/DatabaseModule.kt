package im.a.librarian.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import im.a.librarian.data.local.db.AppDatabase
import im.a.librarian.data.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "imalibrarian_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
         .build()
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE books ADD COLUMN translator TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE wishlist_items ADD COLUMN pageCount INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE wishlist_items ADD COLUMN language TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE wishlist_items ADD COLUMN originalPublicationYear INTEGER")
        }
    }

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao = database.bookDao()

    @Provides
    fun provideAuthorDao(database: AppDatabase): AuthorDao = database.authorDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    fun provideWishlistDao(database: AppDatabase): WishlistDao = database.wishlistDao()

    @Provides
    fun provideBookPhotoDao(database: AppDatabase): BookPhotoDao = database.bookPhotoDao()
}