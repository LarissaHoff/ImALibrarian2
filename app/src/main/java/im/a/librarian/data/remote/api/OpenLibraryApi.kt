package im.a.librarian.data.remote.api

import im.a.librarian.data.remote.model.OpenLibraryBookData
import im.a.librarian.data.remote.model.OpenLibrarySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("/api/books")
    suspend fun getBookByIsbn(
        @Query("bibkeys") bibkeys: String,
        @Query("format") format: String = "json",
        @Query("jscmd") jscmd: String = "data"
    ): Map<String, OpenLibraryBookData>

    @GET("/search.json")
    suspend fun searchByTitle(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): OpenLibrarySearchResponse
}