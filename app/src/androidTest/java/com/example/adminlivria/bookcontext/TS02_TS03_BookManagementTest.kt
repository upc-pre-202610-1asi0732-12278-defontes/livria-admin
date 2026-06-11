// TS02 / TS03 – Core Integration Tests
// Valida la gestión de libros del administrador sobre BooksManagementViewModel:
// - TS02: estadísticas de libros (total, géneros, precio promedio, stock)
// - TS03: búsqueda por título/autor, filtros por género/idioma y ordenamiento
// Framework: JUnit4 + Kotlin (Instrumented Test)

package com.example.adminlivria.bookcontext

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.bookcontext.data.local.BookEntity
import com.example.adminlivria.bookcontext.data.remote.BookDto
import com.example.adminlivria.bookcontext.data.remote.BookService
import com.example.adminlivria.bookcontext.data.remote.StockUpdateRequest
import com.example.adminlivria.bookcontext.data.repository.BooksRepository
import com.example.adminlivria.bookcontext.domain.BookFilters
import com.example.adminlivria.bookcontext.domain.SortOption
import com.example.adminlivria.bookcontext.presentation.BooksManagementViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class TS02_TS03_BookManagementTest {

    private fun entity(
        id: Int, title: String, author: String, genre: String,
        language: String, price: Double, stock: Int
    ) = BookEntity(
        id = id, title = title, description = "Desc", author = author,
        genre = genre, language = language, price = price,
        purchasePrice = price / 1.65, stock = stock, cover = "cover.jpg", isActive = true
    )

    private val catalog = listOf(
        entity(1, "Clean Code",    "Robert C. Martin", "non_fiction", "english", 30.0, 10),
        entity(2, "Don Quijote",   "Cervantes",        "literature",  "español", 40.0, 5),
        entity(3, "El Principito", "Saint-Exupéry",    "fiction",     "español", 20.0, 8),
        entity(4, "Atomic Habits", "James Clear",      "non_fiction", "english", 35.0, 12)
    )

    private lateinit var sut: BooksManagementViewModel

    @Before
    fun setUp() {
        val repository = BooksRepository(FakeDao(catalog), FakeService())
        sut = BooksManagementViewModel(repository)
    }

    private fun awaitBooks(predicate: (List<com.example.adminlivria.bookcontext.domain.Book>) -> Boolean) =
        runBlocking { withTimeout(5_000) { sut.books.first(predicate) } }

    // ----------------------------------------------------------------
    // TS02 – Estadísticas de libros
    // ----------------------------------------------------------------

    @Test
    fun ts02_ac1_stats_shouldCountTotalBooksAndGenres() = runBlocking {
        // Act — métricas de catálogo
        val stats = withTimeout(5_000) { sut.stats.first { it.totalBooks > 0 } }

        // Assert — total de libros y géneros distintos registrados
        assertEquals(4, stats.totalBooks)
        assertEquals(3, stats.totalGenres)
    }

    @Test
    fun ts02_ac2_stats_shouldComputeAveragePrice() = runBlocking {
        // Act — métricas financieras
        val stats = withTimeout(5_000) { sut.stats.first { it.totalBooks > 0 } }

        // Assert — precio promedio de venta: (30+40+20+35)/4 = 31.25
        assertEquals(31.25, stats.averagePrice, 0.001)
    }

    @Test
    fun ts02_ac3_stats_shouldSumBooksInStock() = runBlocking {
        // Act — métricas de inventario
        val stats = withTimeout(5_000) { sut.stats.first { it.totalBooks > 0 } }

        // Assert — unidades totales disponibles: 10+5+8+12 = 35
        assertEquals(35, stats.booksInStock)
    }

    // ----------------------------------------------------------------
    // TS03 – Búsqueda, filtros y ordenamiento del catálogo
    // ----------------------------------------------------------------

    @Test
    fun ts03_ac1_search_byTitleOrAuthor_shouldReturnMatches() {
        // Act — búsqueda por término (el fake filtra por título o autor)
        sut.onSearch("clean")
        val results = awaitBooks { it.isNotEmpty() && it.all { b -> b.title == "Clean Code" } }

        // Assert
        assertEquals(1, results.size)
        assertEquals("Robert C. Martin", results.first().author)
    }

    @Test
    fun ts03_ac2_filter_byGenreAndLanguage_shouldLimitResults() {
        // Act — filtro combinado por género e idioma
        sut.applyFilters(BookFilters(genre = "non_fiction", language = "english"))
        val results = awaitBooks { it.isNotEmpty() && it.all { b -> b.genre == "non_fiction" } }

        // Assert
        assertEquals(2, results.size)
        assertTrue(results.all { it.language == "english" })
    }

    @Test
    fun ts03_ac3_sort_byTitleAscending_shouldReorderCatalog() {
        // Act — ordenamiento por criterio seleccionado
        sut.applyFilters(BookFilters(sort = SortOption.TITLE_ASC))
        val results = awaitBooks { it.size == 4 && it.first().title == "Atomic Habits" }

        // Assert
        assertEquals(listOf("Atomic Habits", "Clean Code", "Don Quijote", "El Principito"),
            results.map { it.title })
    }

    @Test
    fun ts03_ac3_clearFilters_shouldRestoreFullCatalog() {
        // Arrange — con filtro activo solo hay 2 libros
        sut.applyFilters(BookFilters(genre = "non_fiction"))
        awaitBooks { it.size == 2 }

        // Act — restablecer filtros
        sut.clearFilters()
        val results = awaitBooks { it.size == 4 }

        // Assert
        assertEquals(4, results.size)
    }

    // ----------------------------------------------------------------
    // Fakes
    // ----------------------------------------------------------------

    private class FakeDao(private val books: List<BookEntity>) : BookDao {
        override fun getAll(): Flow<List<BookEntity>> = flowOf(books)
        override fun search(query: String): Flow<List<BookEntity>> = flowOf(
            books.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
            }
        )
        override suspend fun upsertAll(entities: List<BookEntity>) {}
        override suspend fun addStock(id: Int, delta: Int) {}
        override suspend fun count(): Int = books.size
        override suspend fun clearAll() {}
        override fun getById(id: Int): Flow<BookEntity?> = flowOf(books.find { it.id == id })
        override suspend fun replaceAll(entities: List<BookEntity>) {}
        override suspend fun getTopByStock(limit: Int): List<BookEntity> =
            books.sortedByDescending { it.stock }.take(limit)
        override suspend fun countBooksByGenre(): List<BookDao.GenreCount> = emptyList()
        override fun getGenreMonetaryValue(): List<BookDao.GenreMonetaryValue> = emptyList()
    }

    private class FakeService : BookService {
        override suspend fun getAllBooks(): Response<List<BookDto>> =
            Response.error(500, "off".toResponseBody())
        override suspend fun addStock(bookId: Int, body: StockUpdateRequest): Response<BookDto> =
            Response.error(500, "off".toResponseBody())
        override suspend fun deactivateBook(bookId: Int): Response<Unit> =
            Response.error(500, "off".toResponseBody())
        override suspend fun reactivateBook(bookId: Int): Response<BookDto> =
            Response.error(500, "off".toResponseBody())
        override suspend fun getDeactivatedBooks(): Response<List<BookDto>> =
            Response.error(500, "off".toResponseBody())
    }
}
