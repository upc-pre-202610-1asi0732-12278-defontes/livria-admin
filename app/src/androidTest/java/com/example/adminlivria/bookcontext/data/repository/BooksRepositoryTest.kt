@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.adminlivria.bookcontext.data.repository

import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.bookcontext.data.local.BookDao.GenreCount
import com.example.adminlivria.bookcontext.data.local.BookDao.GenreMonetaryValue
import com.example.adminlivria.bookcontext.data.remote.BookService
import com.example.adminlivria.bookcontext.data.local.BookEntity
import com.example.adminlivria.bookcontext.domain.Book
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class BooksRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeDao: FakeBookDao
    private lateinit var fakeService: FakeBookService
    private lateinit var repository: BooksRepository

    @Before
    fun setup() {
        fakeDao = FakeBookDao()
        fakeService = FakeBookService()

        repository = BooksRepository(fakeDao, fakeService)
    }
    @Test
    fun getGenreInventoryCount_debe_mapear_el_conteo_de_generos_correctamente() = testScope.runTest {
        fakeDao.genreCounts = listOf(
            GenreCount(genre = "Ficción", count = 50),
            GenreCount(genre = "Historia", count = 30),
            GenreCount(genre = "Ciencia", count = 20)
        )

        val result = repository.getGenreInventoryCount()

        val expected = mapOf(
            "Ficción" to 50,
            "Historia" to 30,
            "Ciencia" to 20
        )

        assertEquals(expected, result)
    }

    @Test
    fun getGenreInventoryValue_debe_mapear_el_valor_monetario_correctamente() = testScope.runTest {
        fakeDao.genreMonetaryValues = listOf(

            GenreMonetaryValue(genre = "Ficción", total_monetary_value = 550.50),
            GenreMonetaryValue(genre = "Historia", total_monetary_value = 300.00)
        )

        val result = repository.getGenreInventoryValue()

        val expected = mapOf(
            "Ficción" to 550.50f,
            "Historia" to 300.00f
        )

        assertEquals(expected, result)
    }

    @Test
    fun getTopBooksByStock_debe_devolver_libros_de_dominio_y_limitar_el_resultado() = testScope.runTest {
        fakeDao.topBooks = listOf(
            BookEntity(1, "Title A", "Desc", "Author A", "Ficción", "ES", 10.0, 5.0, 100, "cover1"),
            BookEntity(2, "Title B", "Desc", "Author B", "Ciencia", "EN", 20.0, 8.0, 50, "cover2"),
            BookEntity(3, "Title C", "Desc", "Author C", "Misterio", "ES", 15.0, 6.0, 20, "cover3")
        )

        val result = repository.getTopBooksByStock(limit = 2)

        assertEquals(2, result.size)
        assertEquals(1, result.first().id)
        assertEquals(50, result[1].stock)
    }

    private class FakeBookDao : BookDao {
        var genreCounts: List<GenreCount> = emptyList()
        var genreMonetaryValues: List<GenreMonetaryValue> = emptyList()
        var topBooks: List<BookEntity> = emptyList()
        var allBooks: List<BookEntity> = emptyList()

        override suspend fun countBooksByGenre(): List<GenreCount> = genreCounts

        override fun getGenreMonetaryValue(): List<GenreMonetaryValue> = genreMonetaryValues

        override suspend fun getTopByStock(limit: Int): List<BookEntity> = topBooks.take(limit)

        override fun getAll(): Flow<List<BookEntity>> = flowOf(allBooks)
        override fun search(query: String): Flow<List<BookEntity>> = throw NotImplementedError()
        override suspend fun upsertAll(entities: List<BookEntity>) = throw NotImplementedError()
        override suspend fun addStock(id: Int, delta: Int) = throw NotImplementedError()
        override suspend fun count(): Int = allBooks.size
        override suspend fun clearAll() = throw NotImplementedError()
        override fun getById(id: Int): Flow<BookEntity?> = throw NotImplementedError()
        override suspend fun replaceAll(entities: List<BookEntity>) = throw NotImplementedError()
    }


    private class FakeBookService : BookService {
        override suspend fun getAllBooks(): Response<List<com.example.adminlivria.bookcontext.data.remote.BookDto>> {
            throw NotImplementedError("Este método no se usa en este test.")
        }
        override suspend fun addStock(
            bookId: Int,
            body: com.example.adminlivria.bookcontext.data.remote.StockUpdateRequest
        ): Response<com.example.adminlivria.bookcontext.data.remote.BookDto> {
            throw NotImplementedError("Este método no se usa en este test.")
        }
        override suspend fun deactivateBook(bookId: Int): Response<Unit> {
            throw NotImplementedError("Este método no se usa en este test.")
        }
        override suspend fun reactivateBook(bookId: Int): Response<com.example.adminlivria.bookcontext.data.remote.BookDto> {
            throw NotImplementedError("Este método no se usa en este test.")
        }
        override suspend fun getDeactivatedBooks(): Response<List<com.example.adminlivria.bookcontext.data.remote.BookDto>> {
            throw NotImplementedError("Este método no se usa en este test.")
        }
    }
}