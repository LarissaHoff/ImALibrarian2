package app.imalibrarian.di

import app.imalibrarian.data.repository.BookRepositoryImpl
import app.imalibrarian.data.repository.MetadataRepositoryImpl
import app.imalibrarian.data.repository.WishlistRepositoryImpl
import app.imalibrarian.domain.repository.BookRepository
import app.imalibrarian.domain.repository.MetadataRepository
import app.imalibrarian.domain.repository.WishlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(impl: WishlistRepositoryImpl): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindMetadataRepository(impl: MetadataRepositoryImpl): MetadataRepository
}