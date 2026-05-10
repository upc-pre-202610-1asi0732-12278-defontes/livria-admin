package com.example.adminlivria.searchcontext.presentation

import com.example.adminlivria.profilecontext.domain.AdminUser
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminlivria.profilecontext.data.local.TokenManager
import com.example.adminlivria.profilecontext.data.remote.UserAdminService

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class HomeUiState(
    val user: AdminUser = AdminUser.mock(),
    val isLoading: Boolean = true,
    val loadError: String? = null,
)

class HomeViewModel(
    private val userAdminService: UserAdminService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val adminId: Int = tokenManager.getAdminId()

    init {
        viewModelScope.launch {
            loadAdminData()
        }
    }

    private suspend fun loadAdminData() {
        if (adminId == 0) {
            _uiState.update { it.copy(loadError = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, loadError = null) }

        try {
            val response = userAdminService.getUserAdminData()

            if (response.isSuccessful) {
                val adminList = response.body()
                val adminDto = adminList?.firstOrNull()

                adminDto?.let {
                    val adminUser = AdminUser(
                        id = it.id.toString(),
                        username = it.username,
                        fullName = it.display,
                        email = it.email,
                        capital = it.capital,
                        adminAccess = if (it.adminAccess) 1 else 0
                    )

                    _uiState.update { state ->
                        state.copy(
                            user = adminUser,
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update { it.copy(
                        loadError = "No se encontraron datos del administrador.",
                        isLoading = false
                    ) }
                }
            } else {
                _uiState.update { it.copy(
                    loadError = "No pudimos obtener tu información en este momento. Inténtalo más tarde.",
                    isLoading = false
                ) }
            }
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is HttpException -> "Estamos teniendo problemas técnicos con el servidor. Inténtalo más tarde."
                is IOException -> "Parece que no tienes conexión a internet. Verifica tu red."
                else -> "Ocurrió un error inesperado al cargar los datos."
            }
            _uiState.update { it.copy(loadError = errorMsg, isLoading = false) }
        }
    }
}