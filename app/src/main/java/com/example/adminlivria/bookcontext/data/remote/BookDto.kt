package com.example.adminlivria.bookcontext.data.remote

import com.google.gson.annotations.SerializedName
import com.example.adminlivria.bookcontext.domain.Book


data class BookDto(
    val id: Int? = null,
    val title: String,
    val description: String,
    val author: String,
    @SerializedName("salePrice") val salePrice: Double?,
    @SerializedName("purchasePrice") val purchasePrice: Double?,
    val stock: Int,
    val cover: String,
    val genre: String,
    val language: String
)

fun BookDto.toDomain(isActiveStatus: Boolean = true) = Book(
    id = id ?: 0,
    title = title,
    description = description,
    author = author,
    genre = genre,
    language = language,
    price = salePrice ?: 0.0,
    purchasePrice = purchasePrice ?: 0.0,
    stock = stock,
    cover = cover,
    isActive = isActiveStatus
)