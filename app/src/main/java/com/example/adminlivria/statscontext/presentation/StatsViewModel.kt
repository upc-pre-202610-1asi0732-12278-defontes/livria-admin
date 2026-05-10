package com.example.adminlivria.statscontext.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminlivria.statscontext.data.repository.StatsRepository
import com.example.adminlivria.statscontext.domain.model.BookStatsDomain
import com.example.adminlivria.statscontext.domain.model.GenreStatsDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun BookStatsDomain.toUi() = BookStatsUi(
    title = this.bookTitle,
    imageUrl = this.imageUrl,
    units = this.unitsSold
)

fun GenreStatsDomain.toUi(): GenreStatsUi {
    val percentageFormatted = "${(this.inventoryShare * 100).toInt()}%"

    return GenreStatsUi(
        genre = this.genreName,
        inventoryShare = this.inventoryShare,
        percentageText = percentageFormatted,
        color = this.color
    )
}

fun GenreStatsDomain.toInventoryValueUi(): GenreStatsUi {
    val monetaryFormatted = "S/ %.0f".format(this.inventoryShare)

    return GenreStatsUi(
        genre = this.genreName,
        inventoryShare = this.inventoryShare,
        percentageText = monetaryFormatted,
        color = this.color
    )
}
class StatsViewModel(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val topBooksDomain = statsRepository.fetchTopSellingBooks()
                val genresDomain = statsRepository.fetchGenreDistributionByStock()
                val inventoryValueDomain = statsRepository.fetchInventoryValueByGenre()

                val topBooksUi = topBooksDomain.map { it.toUi() }
                val genresUi = genresDomain.map { it.toUi() }
                val inventoryValueUi = inventoryValueDomain.map { it.toInventoryValueUi() }

                _uiState.update {
                    it.copy(
                        topSellingBooks = topBooksUi,
                        revenueByGenre = genresUi,
                        inventoryValueByGenre = inventoryValueUi,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No pudimos cargar las estadísticas en este momento. Inténtalo más tarde."
                    )
                }
            }
        }
    }
}