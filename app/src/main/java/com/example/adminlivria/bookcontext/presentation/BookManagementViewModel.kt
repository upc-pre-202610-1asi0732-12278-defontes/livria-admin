package com.example.adminlivria.bookcontext.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminlivria.bookcontext.data.repository.BooksRepository
import com.example.adminlivria.bookcontext.domain.Book
import com.example.adminlivria.bookcontext.domain.BookFilters
import com.example.adminlivria.bookcontext.domain.BooksStats
import com.example.adminlivria.bookcontext.domain.SortOption
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class BooksManagementViewModel(
    private val repository: BooksRepository
) : ViewModel() {

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search
    fun onSearch(newValue: String) { _search.value = newValue }

    private val _filters = MutableStateFlow(BookFilters())
    val filters: StateFlow<BookFilters> = _filters
    fun applyFilters(newFilters: BookFilters) { _filters.value = newFilters }
    fun clearFilters() { _filters.value = BookFilters() }

    private val baseBooks: StateFlow<List<Book>> =
        repository.streamBooks(_search.debounce(300))
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val genres: StateFlow<List<String>> =
        baseBooks.map { it.map { b -> b.genre }.filter { it.isNotBlank() }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val languages: StateFlow<List<String>> =
        baseBooks.map { it.map { b -> b.language }.filter { it.isNotBlank() }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val books: StateFlow<List<Book>> =
        combine(baseBooks, _filters) { list, f ->
            var r = list
            if (f.genre != null)    r = r.filter { it.genre.equals(f.genre, true) }
            if (f.language != null) r = r.filter { it.language.equals(f.language, true) }
            r = r.filter { it.isActive == !f.showInactive }
            when (f.sort) {
                SortOption.TITLE_ASC  -> r.sortedBy { it.title.lowercase() }
                SortOption.TITLE_DESC -> r.sortedByDescending { it.title.lowercase() }
                SortOption.NONE       -> r
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val stats: StateFlow<BooksStats> =
        baseBooks.map { list ->
            val total = list.size
            val genres = list.map { it.genre }.distinct().size
            val priced = list.map { it.price }.filter { it > 0.0 }
            val avg = if (priced.isNotEmpty()) priced.average() else 0.0
            val inStock = list.sumOf { it.stock }
            BooksStats(totalBooks = total, totalGenres = genres, averagePrice = avg, booksInStock = inStock)
        }.stateIn(viewModelScope, SharingStarted.Lazily, BooksStats())


    fun refresh() {
        viewModelScope.launch { repository.refreshBooks() }
    }

    init { refresh() }
}
