package app.imalibrarian.di

import android.content.Context
import androidx.room.Room
import app.imalibrarian.data.local.db.AppDatabase
import app.imalibrarian.data.local.db.dao.*
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
        ).build()
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