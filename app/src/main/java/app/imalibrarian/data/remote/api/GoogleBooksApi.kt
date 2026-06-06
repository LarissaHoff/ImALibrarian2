package app.imalibrarian.data.remote.api

import app.imalibrarian.data.remote.model.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun searchByIsbn(
        @Query("q") query: String,
        @Query("key") apiKey: String? = null
    ): GoogleBooksResponse

    @GET("volumes")
    suspend fun searchByTitle(
        @Query("q") query: String,
        @Query("key") apiKey: String? = null,
        @Query("maxResults") maxResults: Int = 20
    ): GoogleBooksResponse
}