// TS04 – Core Integration Test
// Valida la vista de detalles completos de un libro: información esencial
// en el listado (AC1) y vista de detalles con sinopsis y costos
// financieros — precio de compra y de venta (AC2).
// Framework: JUnit4 + Kotlin (Instrumented Test)

package com.example.adminlivria.bookcontext

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.bookcontext.data.local.BookEntity
import com.example.adminlivria.bookcontext.data.remote.BookDto
import com.example.adminlivria.bookcontext.data.remote.BookService
import com.example.adminlivria.bookcontext.data.remote.StockUpdateRequest
import com.example.adminlivria.bookcontext.data.repository.BooksRepository
import com.example.adminlivria.bookcontext.presentation.detail.BookDetailViewModel
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
class TS04_BookDetailTest {

    private val storedBook = BookEntity(
        id = 7, title = "Clean Code", description = "A handbook of agile software craftsmanship",
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
    // AC1 – Información esencial del libro en el listado
    // ----------------------------------------------------------------

    @Test
    fun ts04_ac1_bookList_shouldExposeEssentialInformation() = runBlocking {
        // Act
        val book = repository.getBooks().first().first()

        // Assert
        assertEquals("Clean Code",       book.title)
        assertEquals("Robert C. Martin", book.author)
        assertEquals("cover.jpg",        book.cover)
        assertTrue(book.isActive)
    }

    // ----------------------------------------------------------------
    // AC2 – Vista de detalles con información completa del producto
    // ----------------------------------------------------------------

    @Test
    fun ts04_ac2_bookDetail_shouldExposeSynopsisAndFinancialCosts() = runBlocking {
        // Arrange
        val sut = BookDetailViewModel(repository, bookId = 7)

        // Act — la vista de detalles carga la información completa
        val book = withTimeout(5_000) { sut.book.first { it != null } }!!

        // Assert — sinopsis y costos financieros (compra y venta)
        assertEquals("A handbook of agile software craftsmanship", book.description)
        assertEquals(20.0, book.purchasePrice, 0.001)
        assertEquals(33.0, book.price,         0.001)
        assertEquals("non_fiction", book.genre)
        assertEquals("english",     book.language)
        assertEquals(10,            book.stock)
    }

    @Test
    fun ts04_ac2_deactivateBook_shouldMarkBookInactiveLocally() = runBlocking {
        // Act — gestión del producto desde la vista de detalles
        repository.deactivateBook(7)

        // Assert
        val saved = fakeDao.upserted.first()
        assertFalse(saved.isActive)
    }

    @Test
    fun ts04_ac2_reactivateBook_shouldMarkBookActiveLocally() = runBlocking {
        // Act
        repository.reactivateBook(7)

        // Assert
        val saved = fakeDao.upserted.first()
        assertTrue(saved.isActive)
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
        override suspend fun getAllBooks(): Response<List<BookDto>> =
            Response.error(500, "off".toResponseBody())

        override suspend fun addStock(bookId: Int, body: StockUpdateRequest): Response<BookDto> =
            Response.error(500, "off".toResponseBody())

        override suspend fun deactivateBook(bookId: Int): Response<Unit> =
            Response.success(Unit)

        override suspend fun reactivateBook(bookId: Int): Response<BookDto> =
            Response.success(
                BookDto(
                    id = bookId, title = "Clean Code",
                    description = "A handbook of agile software craftsmanship",
                    author = "Robert C. Martin", salePrice = 33.0, purchasePrice = 20.0,
                    stock = 10, cover = "cover.jpg", genre = "non_fiction", language = "english"
                )
            )

        override suspend fun getDeactivatedBooks(): Response<List<BookDto>> =
            Response.error(500, "off".toResponseBody())
    }
}
