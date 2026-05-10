package com.example.adminlivria.clientscontext.data.repository

import com.example.adminlivria.clientscontext.data.remote.UserClientService
import com.example.adminlivria.clientscontext.domain.model.UserClient
import com.example.adminlivria.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserClientRepository(
    private val api: UserClientService
) {
    suspend fun getAllUserClients(): Resource<List<UserClient>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllUserClients()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val clients = body.map { dto ->
                            UserClient(
                                id = dto.id,
                                display = dto.display,
                                username = dto.username,
                                email = dto.email,
                                icon = dto.icon ?: "",
                                phrase = dto.phrase ?: "",
                                subscription = dto.subscription ?: "Free Plan"
                            )
                        }
                        Resource.Success(clients)
                    } else {
                        Resource.Error("El servidor devolvió una lista vacía.")
                    }
                } else {
                    Resource.Error("Tuvimos un problema al obtener la lista de clientes. Inténtalo más tarde.")
                }
            } catch (e: Exception) {
                Resource.Error("Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar.")
            }
        }
    }
}
