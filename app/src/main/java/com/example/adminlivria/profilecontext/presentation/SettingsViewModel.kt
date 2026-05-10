package com.example.adminlivria.profilecontext.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminlivria.profilecontext.data.local.TokenManager
import com.example.adminlivria.profilecontext.data.remote.UserAdminService
import com.example.adminlivria.profilecontext.data.model.UserAdminDto
// NUEVOS IMPORTS para Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.util.Log
private const val TAG = "SettingsVM"

data class SettingsUiState(
    val display: String = "",
    val username: String = "",
    val email: String = "",
    val securityPin: String = "",
    val capital: Double = 0.0,
    val isLoading: Boolean = false,
    val initialLoadError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,

    val isProfileTabSelected: Boolean = true,
    val receiveNotifications: Boolean = true,
    val receiveEmailAlerts: Boolean = true,
    val autoSaveEnabled: Boolean = false,
)

class SettingsViewModel(
    private val userAdminService: UserAdminService,
    private val tokenManager: TokenManager
) : ViewModel() {


    private val _uiState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val adminId: Int = tokenManager.getAdminId()

    init {

        viewModelScope.launch {
            loadAdminData()
        }
    }
    fun spend(amount: Double) {
        if (amount <= 0) return
        val current = _uiState.value.capital
        _uiState.value = _uiState.value.copy(capital = (current - amount).coerceAtLeast(0.0))

    }

    fun updateField(field: String, value: String) {
        _uiState.update { currentState ->
            val updatedState = when (field) {
                "fullName" -> currentState.copy(display = value)
                "username" -> currentState.copy(username = value)
                "email" -> currentState.copy(email = value)
                "securityPin" -> currentState.copy(securityPin = value)
                else -> currentState
            }
            updatedState.copy(saveSuccess = false, saveError = null)
        }
    }

    fun setTab(isProfile: Boolean) {
        _uiState.update { it.copy(isProfileTabSelected = isProfile) }
    }

    fun updateApplicationSetting(setting: String, isEnabled: Boolean) {
        _uiState.update { currentState ->
            when (setting) {
                "notifications" -> currentState.copy(receiveNotifications = isEnabled, saveSuccess = false)
                "emailAlerts" -> currentState.copy(receiveEmailAlerts = isEnabled, saveSuccess = false)
                "autoSave" -> currentState.copy(autoSaveEnabled = isEnabled, saveSuccess = false)
                else -> currentState
            }
        }
    }

    fun logout() {
        tokenManager.clearAuthData()
        println("Cierre de sesión completado.")
    }


    suspend fun loadAdminData() {
        val adminId = tokenManager.getAdminId()

        if (adminId == 0) {
            _uiState.update { it.copy(initialLoadError = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, initialLoadError = null) }

        try {
            val response = userAdminService.getUserAdminData()
            Log.d(TAG, "response code=${response.code()} successful=${response.isSuccessful} body=${response.body()}")
            if (response.isSuccessful) {
                val adminDto = response.body()?.firstOrNull()
                adminDto?.let {
                    _uiState.update { state ->
                        state.copy(
                            display = it.display,
                            username = it.username,
                            email = it.email,
                            securityPin = it.securityPin,
                            capital = it.capital,
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update { it.copy(initialLoadError = "No hay datos de admin.", isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(initialLoadError = "No pudimos obtener tu información en este momento. Inténtalo más tarde.", isLoading = false) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception en loadAdminData: ${e.message}", e)
            val errorMsg = when (e) {
                is retrofit2.HttpException -> "Estamos teniendo problemas técnicos. Inténtalo más tarde."
                is java.io.IOException -> "Parece que no tienes conexión a internet. Verifica tu red."
                else -> "Ocurrió un error inesperado al cargar tus datos."
            }
            _uiState.update { it.copy(initialLoadError = errorMsg, isLoading = false) }
        }
    }

    fun saveChanges() {
        if (adminId == 0 || uiState.value.display.isBlank() || uiState.value.username.isBlank() || uiState.value.email.isBlank() || uiState.value.securityPin.isBlank()) {
            _uiState.update { it.copy(saveError = "Por favor completa todos tus datos antes de guardar.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveSuccess = false, saveError = null) }

        viewModelScope.launch {
            try {
                val currentState = uiState.value
                val requestDto = UserAdminDto(
                    id = adminId,
                    display = currentState.display,
                    username = currentState.username,
                    email = currentState.email,
                    adminAccess = true,
                    securityPin = currentState.securityPin,
                    capital = currentState.capital
                )

                val response = userAdminService.updateUserAdmin(adminId, requestDto)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, saveError = null) }
                } else {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = false, saveError = "No pudimos guardar tus cambios. Verifica la información e inténtalo de nuevo.") }
                }

                if (uiState.value.saveSuccess) {
                    delay(3000)
                    _uiState.update { it.copy(saveSuccess = false) }
                }

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is HttpException -> "Tuvimos un problema técnico al guardar. Inténtalo más tarde."
                    is IOException -> "Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar."
                    else -> "Ocurrió un error inesperado al guardar."
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = false, saveError = errorMsg) }
            }
        }
    }
}
