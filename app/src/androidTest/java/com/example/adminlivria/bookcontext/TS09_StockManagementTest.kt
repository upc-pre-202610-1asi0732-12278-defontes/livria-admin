// TS09 – Core Integration Test
// Valida la visualización del inventario y el aumento de stock:
// presentación estructurada de los detalles del libro (AC1) y
// procesamiento del aumento de stock con cálculo del costo total (AC2).
// Framework: JUnit4 + Kotlin (Instrumented Test)

package com.example.adminlivria.bookcontext

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.bookcontext.data.local.BookEntity
import com.example.adminlivria.bookcontext.data.remote.BookDto
import com.example.adminlivria.bookcontext.data.remote.BookService
import com.example.adminlivria.bookcontext.data.remote.StockUpdateRequest
import com.example.adminlivria.bookcontext.data.repository.BooksRepository
import com.example.adminlivria.bookcontext.presentation.stock.StockViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class TS09_StockManagementTest {

    private val storedBook = BookEntity(
        id = 1, title = "Clean Code", description = "A handbook of agile software craftsmanship",
        author = "Robert C. Martin", genre = "non_fiction", language = "english",
        price = 33.0, purchasePrice = 20.0, stock = 10, cover = "cover.jpg", isActive = true
    )

    private lateinit var fakeDao: FakeDao
    private lateinit var fakeService: FakeService
    private lateinit var repository: BooksRepository

    @Before
    fun setUp() {
        fakeDao = FakeDao(storedBook)
        fakeService = FakeService()
        repository = BooksRepository(fakeDao, fakeService)
    }

    // ----------------------------------------------------------------
    // AC1 – Presentación estructurada de los detalles de inventario
    // ----------------------------------------------------------------

    @Test
    fun ts09_ac1_inventoryList_shouldExposeStructuredBookDetails() = runBlocking {
        // Act
        val books = repository.getBooks().first()
        val book = books.first()

        // Assert — título, autor, género, idioma, stock y precio de compra
        assertEquals("Clean Code",       book.title)
        assertEquals("Robert C. Martin", book.author)
        assertEquals("non_fiction",      book.genre)
        assertEquals("english",          book.language)
        assertEquals(10,                 book.stock)
        assertEquals(20.0,               book.purchasePrice, 0.001)
    }

    // ----------------------------------------------------------------
    // AC2 – Procesamiento de aumento de stock
    // ----------------------------------------------------------------

    @Test
    fun ts09_ac2_setQty_whenZeroOrNegative_shouldCoerceToOne() {
        // Arrange
        val sut = StockViewModel(repository, bookId = 1)

        // Act & Assert — la cantidad debe ser un entero positivo
        sut.setQty(0)
        assertEquals(1, sut.qty.value)
        sut.setQty(-5)
        assertEquals(1, sut.qty.value)
    }

    @Test
    fun ts09_ac2_totalToPay_shouldBePurchasePriceTimesQuantity() = runBlocking {
        // Arrange
        val sut = StockViewModel(repository, bookId = 1)

        // Act — costo total de la adquisición (precio de compra * cantidad)
        sut.setQty(5)
        val total = withTimeout(5_000) {
            sut.totalToPay.first { it > 0.0 }
        }

        // Assert — 20.0 * 5 = 100.0
        assertEquals(100.0, total, 0.001)
    }

    @Test
    fun ts09_ac2_addStock_shouldUpdateLocalInventory() = runBlocking {
        // Act — el backend confirma y el inventario local se actualiza
        val updated = repository.addStock(id = 1, qty = 5)

        // Assert
        assertEquals(15, updated.stock)
        assertEquals(15, fakeDao.upserted.first().stock)
        assertEquals(5, fakeService.lastRequest?.quantityToAdd)
    }

    @Test
    fun ts09_ac2_addStock_whenServerFails_shouldThrow() = runBlocking {
        // Arrange
        fakeService.shouldFail = true

        // Act & Assert — el fallo remoto no actualiza el inventario
        try {
            repository.addStock(id = 1, qty = 5)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(fakeDao.upserted.isEmpty())
        }
    }

    // ----------------------------------------------------------------
    // Fakes
    // ----------------------------------------------------------------

    private class FakeDao(private val book: BookEntity) : BookDao {
        val upserted = mutableListOf<BookEntity>()

        override fun getAll(): Flow<List<BookEntity>> = flowOf(listOf(book))
        override fun search(query: String): Flow<List<BookEntity>> = flowOf(emptyList())
        override suspend fun upsertAll(entities: List<BookEntity>) { upserted.addAll(entities) }
        override suspend fun addStock(id: Int, delta: Int) {}
        override suspend fun count(): Int = 1
        override suspend fun clearAll() {}
        override fun getById(id: Int): Flow<BookEntity?> = flowOf(book)
        override suspend fun replaceAll(entities: List<BookEntity>) {}
        override suspend fun getTopByStock(limit: Int): List<BookEntity> = listOf(book)
        override suspend fun countBooksByGenre(): List<BookDao.GenreCount> = emptyList()
        override fun getGenreMonetaryValue(): List<BookDao.GenreMonetaryValue> = emptyList()
    }

    private class FakeService : BookService {
        var shouldFail = false
        var lastRequest: StockUpdateRequest? = null

        override suspend fun getAllBooks(): Response<List<BookDto>> =
            Response.error(500, "off".toResponseBody())

        override suspend fun addStock(bookId: Int, body: StockUpdateRequest): Response<BookDto> {
            lastRequest = body
            if (shouldFail) return Response.error(500, "fail".toResponseBody())
            return Response.success(
                BookDto(
                    id = bookId, title = "Clean Code",
                    description = "A handbook of agile software craftsmanship",
                    author = "Robert C. Martin", salePrice = 33.0, purchasePrice = 20.0,
                    stock = 10 + body.quantityToAdd, cover = "cover.jpg",
                    genre = "non_fiction", language = "english"
                )
            )
        }

        override suspend fun deactivateBook(bookId: Int): Response<Unit> =
            Response.success(Unit)

        override suspend fun reactivateBook(bookId: Int): Response<BookDto> =
            Response.error(500, "off".toResponseBody())

        override suspend fun getDeactivatedBooks(): Response<List<BookDto>> =
            Response.error(500, "off".toResponseBody())
    }
}
