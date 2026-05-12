package com.example.adminlivria.clientscontext.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminlivria.clientscontext.data.repository.UserClientRepository
import com.example.adminlivria.clientscontext.domain.model.UserClient
import com.example.adminlivria.common.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserClientListUiState(
    val isLoading: Boolean = false,
    val clients: List<UserClient> = emptyList(),
    val errorMessage: String? = null
)

class UserClientListViewModel(
    private val repository: UserClientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserClientListUiState())
    val uiState: StateFlow<UserClientListUiState> = _uiState.asStateFlow()

    init {
        loadClients()
    }

    fun loadClients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getAllUserClients()) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, clients = result.data ?: emptyList())
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun toggleHasPayed(client: UserClient) {
        viewModelScope.launch {
            val newValue = !client.hasPayed
            when (val result = repository.updateHasPayed(client.id, newValue)) {
                is Resource.Success -> result.data?.let { updated ->
                    _uiState.update { state ->
                        state.copy(
                            clients = state.clients.map { if (it.id == updated.id) updated else it }
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun updateSubscription(client: UserClient, newPlan: String) {
        viewModelScope.launch {
            when (val result = repository.updateSubscription(client.id, newPlan)) {
                is Resource.Success -> result.data?.let { updated ->
                    _uiState.update { state ->
                        state.copy(
                            clients = state.clients.map { if (it.id == updated.id) updated else it }
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }
}