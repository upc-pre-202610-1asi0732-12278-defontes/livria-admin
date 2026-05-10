package com.example.adminlivria.bookcontext.presentation.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adminlivria.common.data.local.AdminDatabase
import com.example.adminlivria.bookcontext.data.repository.BooksRepository
import com.example.adminlivria.bookcontext.domain.Book
import com.example.adminlivria.common.bookServiceInstance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val repository: BooksRepository,
    private val bookId: Int
) : ViewModel() {
    val book: StateFlow<Book?> =
        repository.getBookById(bookId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun toggleActivation() {
        val current = book.value ?: return
        viewModelScope.launch {
            try {
                if (current.isActive) {
                    repository.deactivateBook(bookId)
                } else {
                    repository.reactivateBook(bookId)
                }
            } catch (e: Exception) {
                android.util.Log.e("BookDetailVM", "Failed to toggle activation", e)
            }
        }
    }
}

class BookDetailViewModelFactory(
    context: Context,
    private val bookId: Int
) : ViewModelProvider.Factory {
    private val db = AdminDatabase.getInstance(context)
    private val repo = BooksRepository(db.bookDao(), bookServiceInstance)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BookDetailViewModel(repo, bookId) as T
    }
}