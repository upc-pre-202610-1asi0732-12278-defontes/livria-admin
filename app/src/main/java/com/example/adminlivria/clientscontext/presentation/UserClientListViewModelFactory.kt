package com.example.adminlivria.clientscontext.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.adminlivria.clientscontext.data.repository.UserClientRepository
import com.example.adminlivria.common.userClientServiceInstance

class UserClientListViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserClientListViewModel::class.java)) {
            val repository = UserClientRepository(userClientServiceInstance)
            @Suppress("UNCHECKED_CAST")
            return UserClientListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
