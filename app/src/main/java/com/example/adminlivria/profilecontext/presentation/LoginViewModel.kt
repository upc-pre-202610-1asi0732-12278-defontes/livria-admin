package com.example.adminlivria.profilecontext.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.adminlivria.profilecontext.data.local.TokenManager
import com.example.adminlivria.profilecontext.data.model.LoginAdminRequest
import com.example.adminlivria.profilecontext.data.remote.AuthService

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val securityPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

class LoginViewModel(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onUsernameChange(newUsername: String) {
        uiState = uiState.copy(username = newUsername, error = null)
    }

    fun onPasswordChange(newPassword: String) {
        uiState = uiState.copy(password = newPassword, error = null)
    }

    fun onSecurityPinChange(newPin: String) {
        uiState = uiState.copy(securityPin = newPin, error = null)
    }

    suspend fun signInAdmin(): Boolean {
        if (uiState.username.isBlank() || uiState.password.isBlank() || uiState.securityPin.isBlank()) {
            uiState = uiState.copy(error = "Por favor, complete todos los campos.", isAuthenticated = false)
            return false
        }

        uiState = uiState.copy(isLoading = true, error = null)

        return try {
            val request = LoginAdminRequest(
                username = uiState.username,
                password = uiState.password,
                securityPin = uiState.securityPin
            )
            val response = authService.signInAdmin(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.token != null) {
                    val adminId = authResponse.id ?: 0
                    tokenManager.saveAuthData(authResponse.token, adminId)
                    uiState = uiState.copy(isAuthenticated = true, isLoading = false)
                    true
                } else {
                    uiState = uiState.copy(error = authResponse?.message ?: "Respuesta inválida", isLoading = false, isAuthenticated = false)
                    false
                }
            } else {
                uiState = uiState.copy(error = "Tus credenciales son incorrectas. Verifica tu información.", isLoading = false, isAuthenticated = false)
                false
            }
        } catch (e: Exception) {
            uiState = uiState.copy(error = "Parece que no hay conexión a internet. Revisa tu red e intenta nuevamente.", isLoading = false, isAuthenticated = false)
            false
        }
    }
}