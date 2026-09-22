package im.a.librarian.data.remote.api

import im.a.librarian.data.remote.model.OpenLibrarySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("/search.json")
    suspend fun getBookByIsbn(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1
    ): OpenLibrarySearchResponse

    @GET("/search.json")
    suspend fun searchByTitle(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): OpenLibrarySearchResponse
}