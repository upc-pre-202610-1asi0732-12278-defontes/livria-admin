// TS14 – Core Integration Test
// Valida el flujo de autenticación del administrador en LoginViewModel:
// acceso con credenciales y pin válidos (AC1), rechazo por credenciales
// inválidas (AC2) y persistencia del token de sesión.
// Framework: JUnit4 + Kotlin (Instrumented Test)

package com.example.adminlivria.profilecontext

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.profilecontext.data.local.TokenManager
import com.example.adminlivria.profilecontext.data.model.AuthResponse
import com.example.adminlivria.profilecontext.data.model.LoginAdminRequest
import com.example.adminlivria.profilecontext.data.remote.AuthService
import com.example.adminlivria.profilecontext.presentation.LoginViewModel
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

// --------------------------------------------------------------------
// Fake de AuthService — simula las respuestas del backend IAM
// --------------------------------------------------------------------
class FakeAuthService(
    private val mode: Mode = Mode.SUCCESS
) : AuthService {

    enum class Mode { SUCCESS, INVALID_CREDENTIALS, FAILED_RESPONSE }

    var lastRequest: LoginAdminRequest? = null

    override suspend fun signInAdmin(request: LoginAdminRequest): Response<AuthResponse> {
        lastRequest = request
        return when (mode) {
            Mode.SUCCESS -> Response.success(
                AuthResponse(
                    token    = "valid-jwt-token",
                    success  = true,
                    message  = "Login successful.",
                    id       = 7,
                    userId   = 7,
                    userName = request.username
                )
            )
            Mode.INVALID_CREDENTIALS ->
                Response.error(401, "Unauthorized".toResponseBody())
            Mode.FAILED_RESPONSE -> Response.success(
                AuthResponse(
                    token    = null,
                    success  = false,
                    message  = "Invalid security pin.",
                    id       = null,
                    userId   = null,
                    userName = null
                )
            )
        }
    }
}

@RunWith(AndroidJUnit4::class)
class TS14_LoginViewModelTest {

    private lateinit var context: Context
    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        context      = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(context)
        tokenManager.clearAuthData()
    }

    private fun buildSut(mode: FakeAuthService.Mode): Pair<LoginViewModel, FakeAuthService> {
        val service = FakeAuthService(mode)
        return LoginViewModel(service, tokenManager) to service
    }

    private fun LoginViewModel.fillValidCredentials() {
        onUsernameChange("admin01")
        onPasswordChange("AdminPass123")
        onSecurityPinChange("9999")
    }

    // ----------------------------------------------------------------
    // AC1 – Autenticación exitosa y acceso al panel
    // ----------------------------------------------------------------

    @Test
    fun ts14_ac1_signInAdmin_whenCredentialsValid_shouldAuthenticate() = runBlocking {
        // Arrange
        val (sut, _) = buildSut(FakeAuthService.Mode.SUCCESS)
        sut.fillValidCredentials()

        // Act
        val result = sut.signInAdmin()

        // Assert — acceso otorgado al panel de administración
        assertTrue(result)
        assertTrue(sut.uiState.isAuthenticated)
        assertNull(sut.uiState.error)
        assertFalse(sut.uiState.isLoading)
    }

    @Test
    fun ts14_ac1_signInAdmin_whenSuccessful_shouldPersistAuthData() = runBlocking {
        // Arrange
        val (sut, _) = buildSut(FakeAuthService.Mode.SUCCESS)
        sut.fillValidCredentials()

        // Act
        sut.signInAdmin()

        // Assert — el token y el id del admin quedan persistidos
        assertEquals("valid-jwt-token", tokenManager.getToken())
        assertEquals(7, tokenManager.getAdminId())
    }

    @Test
    fun ts14_ac1_signInAdmin_shouldSendEnteredCredentials() = runBlocking {
        // Arrange
        val (sut, service) = buildSut(FakeAuthService.Mode.SUCCESS)
        sut.fillValidCredentials()

        // Act
        sut.signInAdmin()

        // Assert — la solicitud lleva las credenciales y el pin ingresados
        assertEquals("admin01",      service.lastRequest?.username)
        assertEquals("AdminPass123", service.lastRequest?.password)
        assertEquals("9999",         service.lastRequest?.securityPin)
    }

    // ----------------------------------------------------------------
    // AC2 – Rechazo por credenciales no válidas
    // ----------------------------------------------------------------

    @Test
    fun ts14_ac2_signInAdmin_whenFieldsEmpty_shouldSetErrorAndNotCallService() = runBlocking {
        // Arrange — formulario sin completar
        val (sut, service) = buildSut(FakeAuthService.Mode.SUCCESS)

        // Act
        val result = sut.signInAdmin()

        // Assert
        assertFalse(result)
        assertFalse(sut.uiState.isAuthenticated)
        assertEquals("Por favor, complete todos los campos.", sut.uiState.error)
        assertNull(service.lastRequest)
    }

    @Test
    fun ts14_ac2_signInAdmin_whenCredentialsInvalid_shouldDenyAccess() = runBlocking {
        // Arrange — el backend rechaza con 401
        val (sut, _) = buildSut(FakeAuthService.Mode.INVALID_CREDENTIALS)
        sut.fillValidCredentials()

        // Act
        val result = sut.signInAdmin()

        // Assert — se notifica que las credenciales son incorrectas
        assertFalse(result)
        assertFalse(sut.uiState.isAuthenticated)
        assertEquals(
            "Tus credenciales son incorrectas. Verifica tu información.",
            sut.uiState.error
        )
        assertNull(tokenManager.getToken())
    }

    @Test
    fun ts14_ac2_signInAdmin_whenBackendReportsFailure_shouldShowBackendMessage() = runBlocking {
        // Arrange — respuesta 200 pero success=false (ej. pin inválido)
        val (sut, _) = buildSut(FakeAuthService.Mode.FAILED_RESPONSE)
        sut.fillValidCredentials()

        // Act
        val result = sut.signInAdmin()

        // Assert
        assertFalse(result)
        assertFalse(sut.uiState.isAuthenticated)
        assertEquals("Invalid security pin.", sut.uiState.error)
        assertNull(tokenManager.getToken())
    }

    @Test
    fun ts14_ac2_onFieldChange_shouldClearPreviousError() = runBlocking {
        // Arrange — provocar un error primero
        val (sut, _) = buildSut(FakeAuthService.Mode.SUCCESS)
        sut.signInAdmin() // campos vacíos -> error
        assertNotNull(sut.uiState.error)

        // Act — el usuario corrige el formulario
        sut.onUsernameChange("admin01")

        // Assert
        assertNull(sut.uiState.error)
    }
}
