// TS12 / TS13 – Core Integration Tests
// Valida la sección de configuración del administrador en SettingsViewModel:
// - TS12: presentación y actualización de la información del perfil
// - TS13: control de notificaciones, alertas por email y autoguardado
// Framework: JUnit4 + Kotlin (Instrumented Test)

package com.example.adminlivria.profilecontext

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adminlivria.profilecontext.data.local.TokenManager
import com.example.adminlivria.profilecontext.data.model.UserAdminDto
import com.example.adminlivria.profilecontext.data.remote.UserAdminService
import com.example.adminlivria.profilecontext.presentation.SettingsViewModel
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

// --------------------------------------------------------------------
// Fake de UserAdminService
// --------------------------------------------------------------------
private class FakeUserAdminService(
    var admin: UserAdminDto? = UserAdminDto(
        id = 7, display = "Admin Livria", username = "admin01",
        email = "admin@livria.com", adminAccess = true,
        securityPin = "9999", capital = 5000.0
    ),
    var shouldFail: Boolean = false
) : UserAdminService {

    var lastUpdate: UserAdminDto? = null

    override suspend fun getUserAdminData(): Response<List<UserAdminDto>> =
        if (shouldFail) Response.error(500, "Server error".toResponseBody())
        else Response.success(listOfNotNull(admin))

    override suspend fun updateUserAdmin(id: Int, userAdmin: UserAdminDto): Response<Unit> {
        lastUpdate = userAdmin
        return if (shouldFail) Response.error(500, "Server error".toResponseBody())
        else Response.success(Unit)
    }
}

@RunWith(AndroidJUnit4::class)
class TS12_TS13_SettingsViewModelTest {

    private lateinit var context: Context
    private lateinit var tokenManager: TokenManager
    private lateinit var service: FakeUserAdminService

    @Before
    fun setUp() {
        context      = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(context)
        tokenManager.saveAuthData("valid-jwt-token", 7)
        service      = FakeUserAdminService()
    }

    private fun buildSut() = SettingsViewModel(service, tokenManager)

    // ----------------------------------------------------------------
    // TS12 / AC1 – Presentación de la información de la cuenta
    // ----------------------------------------------------------------

    @Test
    fun ts12_ac1_loadAdminData_shouldPresentProfileInformation() = runBlocking {
        // Arrange
        val sut = buildSut()

        // Act
        sut.loadAdminData()

        // Assert — nombre, usuario y correo del administrador
        assertEquals("Admin Livria",     sut.uiState.value.display)
        assertEquals("admin01",          sut.uiState.value.username)
        assertEquals("admin@livria.com", sut.uiState.value.email)
        assertEquals("9999",             sut.uiState.value.securityPin)
        assertNull(sut.uiState.value.initialLoadError)
    }

    @Test
    fun ts12_ac1_loadAdminData_whenSessionExpired_shouldShowError() = runBlocking {
        // Arrange — sin sesión activa (id = 0)
        tokenManager.clearAuthData()
        val sut = buildSut()

        // Act
        sut.loadAdminData()

        // Assert
        assertEquals(
            "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.",
            sut.uiState.value.initialLoadError
        )
    }

    // ----------------------------------------------------------------
    // TS12 / AC2 – Actualización de datos del perfil
    // ----------------------------------------------------------------

    @Test
    fun ts12_ac2_updateField_shouldModifyProfileData() {
        // Arrange
        val sut = buildSut()

        // Act
        sut.updateField("fullName",    "Nuevo Nombre")
        sut.updateField("username",    "nuevo_user")
        sut.updateField("email",       "nuevo@livria.com")
        sut.updateField("securityPin", "1234")

        // Assert
        assertEquals("Nuevo Nombre",     sut.uiState.value.display)
        assertEquals("nuevo_user",       sut.uiState.value.username)
        assertEquals("nuevo@livria.com", sut.uiState.value.email)
        assertEquals("1234",             sut.uiState.value.securityPin)
    }

    @Test
    fun ts12_ac2_saveChanges_whenFieldsBlank_shouldSetValidationError() {
        // Arrange — perfil sin completar
        val sut = buildSut()

        // Act
        sut.saveChanges()

        // Assert — el sistema valida antes de guardar
        assertEquals(
            "Por favor completa todos tus datos antes de guardar.",
            sut.uiState.value.saveError
        )
        assertNull(service.lastUpdate)
    }

    @Test
    fun ts12_logout_shouldClearAuthData() {
        // Arrange
        val sut = buildSut()
        assertEquals("valid-jwt-token", tokenManager.getToken())

        // Act
        sut.logout()

        // Assert — la sesión queda terminada en el dispositivo
        assertNull(tokenManager.getToken())
        assertEquals(0, tokenManager.getAdminId())
    }

    // ----------------------------------------------------------------
    // TS13 / AC1 – Control de notificaciones internas
    // ----------------------------------------------------------------

    @Test
    fun ts13_ac1_updateApplicationSetting_shouldToggleNotifications() {
        // Arrange — las notificaciones inician habilitadas
        val sut = buildSut()
        assertTrue(sut.uiState.value.receiveNotifications)

        // Act
        sut.updateApplicationSetting("notifications", false)

        // Assert
        assertFalse(sut.uiState.value.receiveNotifications)
    }

    // ----------------------------------------------------------------
    // TS13 / AC2 – Control de alertas por correo electrónico
    // ----------------------------------------------------------------

    @Test
    fun ts13_ac2_updateApplicationSetting_shouldToggleEmailAlerts() {
        // Arrange
        val sut = buildSut()
        assertTrue(sut.uiState.value.receiveEmailAlerts)

        // Act
        sut.updateApplicationSetting("emailAlerts", false)

        // Assert
        assertFalse(sut.uiState.value.receiveEmailAlerts)
    }

    // ----------------------------------------------------------------
    // TS13 / AC3 – Control de la función de autoguardado
    // ----------------------------------------------------------------

    @Test
    fun ts13_ac3_updateApplicationSetting_shouldToggleAutoSave() {
        // Arrange — el autoguardado inicia desactivado
        val sut = buildSut()
        assertFalse(sut.uiState.value.autoSaveEnabled)

        // Act
        sut.updateApplicationSetting("autoSave", true)

        // Assert
        assertTrue(sut.uiState.value.autoSaveEnabled)
    }

    @Test
    fun ts13_updateApplicationSetting_whenUnknownSetting_shouldNotChangeState() {
        // Arrange
        val sut = buildSut()
        val before = sut.uiState.value

        // Act
        sut.updateApplicationSetting("unknown", false)

        // Assert — un ajuste desconocido no altera la configuración
        assertEquals(before.receiveNotifications, sut.uiState.value.receiveNotifications)
        assertEquals(before.receiveEmailAlerts,   sut.uiState.value.receiveEmailAlerts)
        assertEquals(before.autoSaveEnabled,      sut.uiState.value.autoSaveEnabled)
    }
}
