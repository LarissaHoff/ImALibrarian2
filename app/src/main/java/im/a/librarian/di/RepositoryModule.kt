package im.a.librarian.di

import im.a.librarian.data.repository.BookRepositoryImpl
import im.a.librarian.data.repository.MetadataRepositoryImpl
import im.a.librarian.data.repository.WishlistRepositoryImpl
import im.a.librarian.domain.repository.BookRepository
import im.a.librarian.domain.repository.MetadataRepository
import im.a.librarian.domain.repository.WishlistRepository
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