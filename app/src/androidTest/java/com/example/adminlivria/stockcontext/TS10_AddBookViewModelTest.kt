// TS10 – Core Integration Test
// Valida que AddBookViewModel implementa correctamente la validación
// del formulario antes de enviar al backend (AC1 y AC2).
// Framework: JUnit4 + Kotlin (Instrumented Test)
// Carpeta: app/src/androidTest/java/com/example/adminlivria/stockcontext/

package com.example.adminlivria.stockcontext

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.stockcontext.data.model.CreateBookRequest
import com.example.adminlivria.stockcontext.data.remote.BookResponse
import com.example.adminlivria.stockcontext.data.remote.InventoryService
import com.example.adminlivria.stockcontext.data.remote.UpdateStockRequest
import com.example.adminlivria.stockcontext.presentation.AddBookViewModel
import com.example.adminlivria.stockcontext.presentation.BookOptions
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

// --------------------------------------------------------------------
// Fake de InventoryService — implementa los 4 métodos de la interfaz
// --------------------------------------------------------------------
class FakeInventoryService(
    private val shouldSucceed: Boolean = true
) : InventoryService {

    var lastRequest: CreateBookRequest? = null

    override suspend fun createBook(request: CreateBookRequest): Response<BookResponse> {
        lastRequest = request
        return if (shouldSucceed) {
            Response.success(
                BookResponse(
                    id          = 1,
                    title       = request.title,
                    description = request.description,
                    author      = request.author,
                    stock       = request.stock,
                    cover       = request.cover,
                    genre       = request.genre,
                    language    = request.language
                )
            )
        } else {
            Response.error(400, "Bad Request".toResponseBody())
        }
    }

    override suspend fun getAllBooks(): Response<List<BookResponse>> {
        return Response.success(emptyList())
    }

    override suspend fun getBookById(bookId: Int): Response<BookResponse> {
        return Response.success(
            BookResponse(
                id = bookId, title = "Test", description = "Desc",
                author = "Author", stock = 1, cover = "cover.jpg",
                genre = "fiction", language = "english"
            )
        )
    }

    override suspend fun updateBookStock(
        bookId: Int,
        request: UpdateStockRequest
    ): Response<BookResponse> {
        return Response.success(
            BookResponse(
                id = bookId, title = "Test", description = "Desc",
                author = "Author", stock = request.quantityToAdd,
                cover = "cover.jpg", genre = "fiction", language = "english"
            )
        )
    }
}

@RunWith(AndroidJUnit4::class)
class TS10_AddBookViewModelTest {

    private lateinit var fakeService: FakeInventoryService
    private lateinit var sut: AddBookViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        context     = ApplicationProvider.getApplicationContext()
        fakeService = FakeInventoryService(shouldSucceed = true)
        sut         = AddBookViewModel(fakeService, context)
    }

    // ----------------------------------------------------------------
    // AC1 – Formulario presenta todos los campos requeridos
    // ----------------------------------------------------------------

    @Test
    fun ts10_ac1_initialState_allFieldsShouldBeEmpty() {
        // Assert — el formulario inicia en blanco, listo para entrada
        assertEquals("", sut.uiState.title)
        assertEquals("", sut.uiState.description)
        assertEquals("", sut.uiState.author)
        assertEquals("", sut.uiState.stock)
        assertEquals("", sut.uiState.genre)
        assertEquals("", sut.uiState.language)
        assertEquals("", sut.uiState.cover)
        assertFalse(sut.uiState.isLoading)
        assertNull(sut.uiState.errorMessage)
    }

    @Test
    fun ts10_ac1_genreOptions_shouldContainAllValidGenres() {
        // Assert — el dropdown de géneros presenta todas las opciones válidas
        val expected = listOf(
            "literature", "non_fiction", "fiction", "mangas_comics",
            "juvenile", "children", "ebooks_audiobooks"
        )
        assertEquals(expected, BookOptions.GENRE_OPTIONS)
    }

    @Test
    fun ts10_ac1_languageOptions_shouldContainBothLanguages() {
        // Assert — el dropdown de idiomas presenta español e inglés
        assertTrue(BookOptions.LANGUAGE_OPTIONS.contains("español"))
        assertTrue(BookOptions.LANGUAGE_OPTIONS.contains("english"))
        assertEquals(2, BookOptions.LANGUAGE_OPTIONS.size)
    }

    @Test
    fun ts10_ac1_onTitleChange_shouldUpdateUiState() {
        // Arrange & Act
        sut.onTitleChange("Clean Code")

        // Assert
        assertEquals("Clean Code", sut.uiState.title)
    }

    @Test
    fun ts10_ac1_onTitleChange_whenExceeds255Chars_shouldNotUpdate() {
        // Arrange
        val longTitle = "A".repeat(256)

        // Act
        sut.onTitleChange(longTitle)

        // Assert — el campo no acepta más de 255 caracteres
        assertEquals("", sut.uiState.title)
    }

    // ----------------------------------------------------------------
    // AC2 – Validación de integridad antes del registro
    // ----------------------------------------------------------------

    @Test
    fun ts10_ac2_validateForm_whenAllFieldsEmpty_shouldSetErrorMessage() {
        // Act
        sut.submitBook()

        // Assert — AC2: el sistema rechaza el envío con campos vacíos
        assertNotNull(sut.uiState.errorMessage)
        assertEquals(
            "Todos los campos son obligatorios.",
            sut.uiState.errorMessage
        )
    }

    @Test
    fun ts10_ac2_onStockChange_whenNonNumeric_shouldNotUpdate() {
        // Arrange & Act
        sut.onStockChange("abc")

        // Assert — stock solo acepta dígitos
        assertEquals("", sut.uiState.stock)
    }

    @Test
    fun ts10_ac2_onStockChange_whenNegativeString_shouldNotUpdate() {
        // El campo solo acepta dígitos — el signo '-' no es dígito
        // Arrange & Act
        sut.onStockChange("-5")

        // Assert
        assertEquals("", sut.uiState.stock)
    }

    @Test
    fun ts10_ac2_onStockChange_whenValidNumber_shouldUpdateUiState() {
        // Arrange & Act
        sut.onStockChange("10")

        // Assert
        assertEquals("10", sut.uiState.stock)
    }

    @Test
    fun ts10_ac2_onGenreSelected_whenValidGenre_shouldUpdateUiState() {
        // Arrange & Act
        sut.onGenreSelected("fiction")

        // Assert
        assertEquals("fiction", sut.uiState.genre)
    }

    @Test
    fun ts10_ac2_onGenreSelected_whenInvalidGenre_shouldNotUpdate() {
        // Arrange & Act
        sut.onGenreSelected("drama") // no está en GENRE_OPTIONS

        // Assert
        assertEquals("", sut.uiState.genre)
    }

    @Test
    fun ts10_ac2_onLanguageSelected_whenValidLanguage_shouldUpdateUiState() {
        // Arrange & Act
        sut.onLanguageSelected("english")

        // Assert
        assertEquals("english", sut.uiState.language)
    }

    @Test
    fun ts10_ac2_onLanguageSelected_whenInvalidLanguage_shouldNotUpdate() {
        // Arrange & Act
        sut.onLanguageSelected("french") // no está en LANGUAGE_OPTIONS

        // Assert
        assertEquals("", sut.uiState.language)
    }

    @Test
    fun ts10_ac2_validateForm_whenStockIsBlank_shouldSetErrorMessage() {
        // Arrange — todos los campos excepto stock
        sut.onTitleChange("Clean Code")
        sut.onDescriptionChange("A great book")
        sut.onAuthorChange("Robert C. Martin")
        sut.onGenreSelected("non_fiction")
        sut.onLanguageSelected("english")
        sut.onCoverUriSelected("content://image/1")
        // stock vacío

        // Act
        sut.submitBook()

        // Assert
        assertNotNull(sut.uiState.errorMessage)
        assertEquals(
            "Todos los campos son obligatorios.",
            sut.uiState.errorMessage
        )
    }

    @Test
    fun ts10_ac2_clearMessages_shouldResetErrorAndSuccess() {
        // Arrange — provocar un error
        sut.submitBook()
        assertNotNull(sut.uiState.errorMessage)

        // Act
        sut.clearMessages()

        // Assert
        assertNull(sut.uiState.errorMessage)
        assertNull(sut.uiState.successMessage)
    }
}
