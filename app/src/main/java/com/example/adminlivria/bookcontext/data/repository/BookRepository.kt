package com.example.adminlivria.bookcontext.data.repository

import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.bookcontext.data.local.toDomain
import com.example.adminlivria.bookcontext.data.local.toEntity
import com.example.adminlivria.bookcontext.data.remote.BookService
import com.example.adminlivria.bookcontext.data.remote.StockUpdateRequest
import com.example.adminlivria.bookcontext.data.remote.toDomain
import com.example.adminlivria.bookcontext.domain.Book
import kotlinx.coroutines.flow.*
import com.example.adminlivria.statscontext.domain.BookInfoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BooksRepository(
    private val dao: BookDao,
    private val service: BookService
) : BookInfoSource {
    fun getBooks(): Flow<List<Book>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }


    fun streamBooks(queryFlow: Flow<String>): Flow<List<Book>> =
        queryFlow
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { q ->
                if (q.isBlank()) dao.getAll() else dao.search(q)
            }
            .map { list -> list.map { it.toDomain() } }

    suspend fun refreshBooks() {
        try {
            val responseActive = service.getAllBooks()
            val responseDeactivated = service.getDeactivatedBooks()

            val allBooks = mutableListOf<Book>()

            if (responseActive.isSuccessful) {
                allBooks.addAll(responseActive.body().orEmpty().map { it.toDomain(isActiveStatus = true) })
            } else {
                android.util.Log.e("BooksRepo", "Active HTTP ${responseActive.code()} ${responseActive.message()}")
            }

            if (responseDeactivated.isSuccessful) {
                allBooks.addAll(responseDeactivated.body().orEmpty().map { it.toDomain(isActiveStatus = false) })
            } else {
                android.util.Log.e("BooksRepo", "Deactivated HTTP ${responseDeactivated.code()} ${responseDeactivated.message()}")
            }

            if (allBooks.isNotEmpty() || (responseActive.isSuccessful && responseDeactivated.isSuccessful)) {
                dao.replaceAll(allBooks.map { it.toEntity() })
            }
        } catch (e: Exception) {
            android.util.Log.e("BooksRepo", "refreshBooks error", e)
        }
    }

    suspend fun deactivateBook(id: Int) {
        val res = service.deactivateBook(id)
        if (!res.isSuccessful) {
            throw IllegalStateException("deactivateBook failed: ${res.code()} ${res.message()}")
        }
        val localBook = dao.getById(id).firstOrNull()
        if (localBook != null) {
            val deactivatedBook = localBook.copy(isActive = false)
            dao.upsertAll(listOf(deactivatedBook))
        }
    }

    suspend fun reactivateBook(id: Int) {
        val res = service.reactivateBook(id)
        if (!res.isSuccessful) {
            throw IllegalStateException("reactivateBook failed: ${res.code()} ${res.message()}")
        }
        val localBook = dao.getById(id).firstOrNull()
        if (localBook != null) {
            val reactivatedBook = localBook.copy(isActive = true)
            dao.upsertAll(listOf(reactivatedBook))
        }
    }

    suspend fun insertBook(book: Book) {
        dao.upsertAll(listOf(book.toEntity()))
    }
    fun getBookById(id: Int): Flow<Book?> =
        dao.getById(id).map { it?.toDomain() }
    suspend fun addStock(id: Int, qty: Int): Book {
        val res = service.addStock(id, StockUpdateRequest(quantityToAdd = qty))
        if (!res.isSuccessful) {
            throw IllegalStateException("addStock failed: ${res.code()} ${res.message()}")
        }
        val updated = res.body() ?: error("Empty body")
        val entity = updated.toDomain().toEntity()
        dao.upsertAll(listOf(entity))
        return entity.toDomain()
    }


    suspend fun isEmpty(): Boolean = dao.count() == 0

    override suspend fun getTopBooksByStock(limit: Int): List<Book> {
        val entities = dao.getTopByStock(limit)
        return entities.map { it.toDomain() }
    }

    override suspend fun getGenreInventoryValue(): Map<String, Float> {
        return withContext(Dispatchers.IO) {
            val genreMonetaryValues = dao.getGenreMonetaryValue()

            genreMonetaryValues.associate {
                it.genre to it.total_monetary_value.toFloat()
            }
        }
    }

    override suspend fun getGenreInventoryCount(): Map<String, Int> {
        val counts = dao.countBooksByGenre()
        return counts.associate { it.genre to it.count }
    }
}
