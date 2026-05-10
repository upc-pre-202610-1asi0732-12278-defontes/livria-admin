package com.example.adminlivria.stockcontext.presentation

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminlivria.stockcontext.data.model.CreateBookRequest
import com.example.adminlivria.stockcontext.data.remote.InventoryService
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.core.net.toUri


object BookOptions {
    val LANGUAGE_OPTIONS = listOf("español", "english")
    val GENRE_OPTIONS = listOf(
        "literature", "non_fiction", "fiction", "mangas_comics",
        "juvenile", "children", "ebooks_audiobooks"
    )
}



data class AddBookUiState(
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val stock: String = "",
    val cover: String = "",
    val genre: String = "",
    val language: String = "",



    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)


class AddBookViewModel(
    private val inventoryService: InventoryService,
    private val context: Context
) : ViewModel() {

    var uiState by mutableStateOf(AddBookUiState())
        private set

    fun onTitleChange(value: String) { 
        if (value.length <= 255) uiState = uiState.copy(title = value) 
    }
    fun onDescriptionChange(value: String) { 
        if (value.length <= 1000) uiState = uiState.copy(description = value) 
    }
    fun onAuthorChange(value: String) { 
        if (value.length <= 100) uiState = uiState.copy(author = value) 
    }
    fun onStockChange(value: String) {
        if ((value.all { it.isDigit() } || value.isEmpty()) && value.length <= 5) {
            uiState = uiState.copy(stock = value)
        }
    }

    fun onCoverUriSelected(value: String) { uiState = uiState.copy(cover = value) }


    fun onGenreSelected(value: String) {
        if (BookOptions.GENRE_OPTIONS.contains(value)) {
            uiState = uiState.copy(genre = value)
        }
    }


    fun onLanguageSelected(value: String) {
        if (BookOptions.LANGUAGE_OPTIONS.contains(value)) {
            uiState = uiState.copy(language = value)
        }
    }


    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }


    fun submitBook() {
        if (!validateForm()) return

        uiState = uiState.copy(isLoading = true, successMessage = null, errorMessage = null)

        viewModelScope.launch {
            try {
                val base64Image = convertUriToBase64(uiState.cover)
                if (base64Image == null) {
                    uiState = uiState.copy(
                        errorMessage = "No pudimos procesar la imagen seleccionada. Intenta con otra.",
                        isLoading = false
                    )
                    return@launch
                }
                val request = CreateBookRequest(
                    title = uiState.title,
                    description = uiState.description,
                    author = uiState.author,
                    stock = uiState.stock.toInt(),
                    cover = "data:image/jpeg;base64,$base64Image",
                    genre = uiState.genre,
                    language = uiState.language
                )

                val response = inventoryService.createBook(request)

                if (response.isSuccessful) {
                    uiState = AddBookUiState(
                        successMessage = "¡El libro '${uiState.title}' se agregó a tu inventario con éxito!"
                    )
                } else {
                    uiState = uiState.copy(
                        errorMessage = "Oops, no pudimos guardar el libro. Verifica la información e inténtalo de nuevo.",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar.",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun convertUriToBase64(uriString: String): String? = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext null

        return@withContext try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uriString.toUri())

            inputStream?.use { stream ->
                val bytes = stream.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun validateForm(): Boolean {
        if (uiState.title.isBlank() || uiState.description.isBlank() || uiState.author.isBlank() ||
            uiState.cover.isBlank() || uiState.genre.isBlank() || uiState.language.isBlank() ||
            uiState.stock.isBlank()) {

            uiState = uiState.copy(errorMessage = "Todos los campos son obligatorios.")
            return false
        }

        if (uiState.stock.toIntOrNull() == null || uiState.stock.toInt() < 0) {
            uiState = uiState.copy(errorMessage = "El Stock debe ser un número entero positivo.")
            return false
        }

        return true
    }
}
