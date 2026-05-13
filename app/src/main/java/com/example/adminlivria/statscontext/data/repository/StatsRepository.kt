package com.example.adminlivria.statscontext.data.repository

import com.example.adminlivria.statscontext.data.local.StatsDao
import com.example.adminlivria.statscontext.domain.BookInfoSource
import com.example.adminlivria.statscontext.domain.model.BookStatsDomain
import com.example.adminlivria.statscontext.domain.model.GenreStatsDomain
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

class StatsRepository(
    private val dao: StatsDao,
    private val bookInfoSource: BookInfoSource
) {

    /** Alineado con [Book.AllowedGenres] del backend + nombres legacy; el resto usa paleta por hash. */
    private val fallbackGenreColors = listOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFB74D),
        Color(0xFFBA68C8),
        Color(0xFF4DD0E1),
        Color(0xFFFF8A65),
        Color(0xFF9575CD),
        Color(0xFF4FC3F7),
        Color(0xFFAED581),
        Color(0xFFFFD54F),
        Color(0xFF90CAF9),
    )

    private fun mapGenreToColor(genreName: String): Color {
        val key = genreName.lowercase().trim()
        if (key.isEmpty()) return Color(0xFFB0BEC5)

        return when (key) {
            "literature" -> Color(0xFFF06292)
            "non_fiction" -> Color(0xFF78909C)
            "fiction" -> Color(0xFF7E57C2)
            "mangas_comics" -> Color(0xFF64B5F6)
            "juvenile" -> Color(0xFFFFCC80)
            "children" -> Color(0xFFFFCA28)
            "ebooks_audiobooks" -> Color(0xFF26A69A)
            // Valores antiguos / alternativos por si hay datos viejos
            "thriller" -> Color(0xFF81C784)
            "science_fiction" -> Color(0xFFBA68C8)
            else -> fallbackGenreColors[abs(key.hashCode()) % fallbackGenreColors.size]
        }
    }
    suspend fun fetchTopSellingBooks(): List<BookStatsDomain> {
        val topBooks = bookInfoSource.getTopBooksByStock(limit = 3)
        return topBooks.map { book ->
            BookStatsDomain(book.title, book.cover, book.stock)
        }
    }
    suspend fun fetchGenreDistributionByStock(): List<GenreStatsDomain> {
        val genreValueMap = bookInfoSource.getGenreInventoryCount()
        val totalStockCount = genreValueMap.values.sum().toFloat()

        if (totalStockCount == 0f) return emptyList()

        return genreValueMap.map { (genreName, genreCount) ->
            val share = genreCount.toFloat() / totalStockCount

            GenreStatsDomain(
                genreName = genreName,
                inventoryShare = share,
                color = mapGenreToColor(genreName)
            )
        }.sortedByDescending { it.inventoryShare }
    }

    suspend fun fetchInventoryValueByGenre(): List<GenreStatsDomain> {

        val genreStockCountMap = bookInfoSource.getGenreInventoryValue()

        return genreStockCountMap.map { (genreName, genreMonetaryValue) ->
            GenreStatsDomain(
                genreName = genreName,
                inventoryShare = genreMonetaryValue,
                color = mapGenreToColor(genreName)
            )
        }.sortedByDescending { it.inventoryShare }
    }
}
