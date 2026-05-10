package com.example.adminlivria.bookcontext.data.remote

import retrofit2.http.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface BookService {
    @GET("books")
    suspend fun getAllBooks(): retrofit2.Response<List<BookDto>>

    @PUT("books/{bookId}/stock")
    suspend fun addStock(
        @Path("bookId") bookId: Int,
        @Body body: StockUpdateRequest
    ): Response<BookDto>

    @PATCH("books/{bookId}/deactivate")
    suspend fun deactivateBook(@Path("bookId") bookId: Int): Response<Unit>

    @PATCH("books/{bookId}/reactivate")
    suspend fun reactivateBook(@Path("bookId") bookId: Int): Response<BookDto>

    @GET("books/deactivated")
    suspend fun getDeactivatedBooks(): Response<List<BookDto>>
}