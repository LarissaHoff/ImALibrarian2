package app.imalibrarian.data.remote.api

import app.imalibrarian.data.remote.model.OpenLibraryBookResponse
import app.imalibrarian.data.remote.model.OpenLibrarySearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("books")
    suspend fun getBookByIsbn(
        @Query("bibkeys") bibkeys: String,
        @Query("format") format: String = "json",
        @Query("jscmd") jscmd: String = "data"
    ): OpenLibraryBookResponse

    @GET("search.json")
    suspend fun searchByTitle(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): OpenLibrarySearchResponse
}