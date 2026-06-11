// TS08 – Core Integration Test
// Valida la búsqueda y el filtrado de libros dentro del inventario
// (Book Collection) a través de BooksRepository.streamBooks:
// búsqueda por término (AC1) y filtro por género/idioma (AC2).
// Framework: JUnit4 + Kotlin (Instrumented Test)

package com.example.adminlivria.bookcontext

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.bookcontext.data.local.BookEntity
import com.example.adminlivria.bookcontext.data.remote.BookDto
import com.example.adminlivria.bookcontext.data.remote.BookService
import com.example.adminlivria.bookcontext.data.remote.StockUpdateRequest
import com.example.adminlivria.bookcontext.data.repository.BooksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class TS08_InventorySearchTest {

    private fun entity(
        id: Int, title: String, author: String, genre: String, language: String
    ) = BookEntity(
        id = id, title = title, description = "Desc", author = author,
        genre = genre, language = language, price = 30.0,
        purchasePrice = 18.0, stock = 10, cover = "cover.jpg", isActive = true
    )

    private val inventory = listOf(
        entity(1, "Clean Code",    "Robert C. Martin", "non_fiction", "english"),
        entity(2, "Don Quijote",   "Cervantes",        "literature",  "español"),
        entity(3, "El Principito", "Saint-Exupéry",    "fiction",     "español")
    )

    private lateinit var repository: BooksRepository

    @Before
    fun setUp() {
        repository = BooksRepository(FakeDao(inventory), FakeService())
    }

    // ----------------------------------------------------------------
    // AC1 – Búsqueda por término dentro del inventario
    // ----------------------------------------------------------------

    @Test
    fun ts08_ac1_streamBooks_whenQueryBlank_shouldReturnFullInventory() = runBlocking {
        // Act — sin término de búsqueda se presenta todo el inventario
        val books = repository.streamBooks(flowOf("")).first()

        // Assert
        assertEquals(3, books.size)
    }

    @Test
    fun ts08_ac1_streamBooks_whenQueryMatchesTitle_shouldReturnOnlyMatches() = runBlocking {
        // Act
        val books = repository.streamBooks(flowOf("quijote")).first()

        // Assert — solo los libros que coinciden con el término
        assertEquals(1, books.size)
        assertEquals("Don Quijote", books.first().title)
    }

    @Test
    fun ts08_ac1_streamBooks_whenQueryMatchesAuthor_shouldReturnOnlyMatches() = runBlocking {
        // Act
        val books = repository.streamBooks(flowOf("martin")).first()

        // Assert
        assertEquals(1, books.size)
        assertEquals("Robert C. Martin", books.first().author)
    }

    @Test
    fun ts08_ac1_streamBooks_whenNoMatch_shouldReturnEmptyList() = runBlocking {
        // Act
        val books = repository.streamBooks(flowOf("inexistente")).first()

        // Assert
        assertTrue(books.isEmpty())
    }

    // ----------------------------------------------------------------
    // AC2 – Aplicación de filtro por género o idioma
    // ----------------------------------------------------------------

    @Test
    fun ts08_ac2_filterByGenre_shouldShowOnlyMatchingBooks() = runBlocking {
        // Act — mismo criterio de filtrado que aplica la vista de inventario
        val books = repository.getBooks().first()
            .filter { it.genre.equals("fiction", ignoreCase = true) }

        // Assert
        assertEquals(1, books.size)
        assertEquals("El Principito", books.first().title)
    }

    @Test
    fun ts08_ac2_filterByLanguage_shouldShowOnlyMatchingBooks() = runBlocking {
        // Act
        val books = repository.getBooks().first()
            .filter { it.language.equals("español", ignoreCase = true) }

        // Assert
        assertEquals(2, books.size)
        assertTrue(books.all { it.language == "español" })
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
