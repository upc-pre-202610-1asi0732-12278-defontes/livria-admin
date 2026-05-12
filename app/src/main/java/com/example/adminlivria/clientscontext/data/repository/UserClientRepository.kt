package com.example.adminlivria.clientscontext.data.repository

import android.util.Log
import com.example.adminlivria.clientscontext.data.remote.UserClientService
import com.example.adminlivria.clientscontext.data.remote.dto.UpdateHasPayedDto
import com.example.adminlivria.clientscontext.data.remote.dto.UpdateUserClientDto
import com.example.adminlivria.clientscontext.domain.model.UserClient
import com.example.adminlivria.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserClientRepository(
    private val api: UserClientService
) {

    private fun mapDto(dto: com.example.adminlivria.clientscontext.data.remote.dto.UserClientDto) =
        UserClient(
            id             = dto.id,
            display        = dto.display,
            username       = dto.username,
            email          = dto.email,
            icon           = dto.icon           ?: "",
            phrase         = dto.phrase         ?: "",
            subscription   = dto.subscription   ?: "freeplan",
            planChangeDate = dto.planChangeDate  ?: "",
            hasPayed       = dto.hasPayed        ?: false
        )

    suspend fun getAllUserClients(): Resource<List<UserClient>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllUserClients()
                Log.d("UserClientRepo", "GET userclients → ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("UserClientRepo", "Body size: ${body?.size}")
                    if (body != null) {
                        Resource.Success(body.map { mapDto(it) })
                    } else {
                        Resource.Error("El servidor devolvió una lista vacía.")
                    }
                } else {
                    Log.e("UserClientRepo", "Error body: ${response.errorBody()?.string()}")
                    Resource.Error("Tuvimos un problema al obtener la lista de clientes. Inténtalo más tarde.")
                }
            } catch (e: Exception) {
                Log.e("UserClientRepo", "Exception GET: ${e::class.simpleName} → ${e.message}", e)
                Resource.Error("Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar.")
            }
        }
    }

    suspend fun updateSubscription(client: Int, newPlan: String): Resource<UserClient> {
        return withContext(Dispatchers.IO) {
            try {
                val body = UpdateUserClientDto(newSubscriptionPlan = newPlan)
                Log.d("UserClientRepo", "PUT userclients/${client}/subscription body=$body")
                val response = api.updateUserClient(client, body)
                Log.d("UserClientRepo", "PUT plan → ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    val dto = response.body()
                    if (dto != null) {
                        Resource.Success(mapDto(dto))
                    } else {
                        Resource.Error("No se recibió respuesta del servidor.")
                    }
                } else {
                    val err = response.errorBody()?.string()
                    Log.e("UserClientRepo", "PUT plan error body: $err")
                    Resource.Error("Error al actualizar el plan. Inténtalo más tarde.")
                }
            } catch (e: Exception) {
                Log.e("UserClientRepo", "Exception PUT plan: ${e.message}", e)
                Resource.Error("Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar.")
            }
        }
    }

    suspend fun updateHasPayed(id: Int, hasPayed: Boolean): Resource<UserClient> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateHasPayed(id, UpdateHasPayedDto(hasPayed))
                Log.d("UserClientRepo", "PUT userclients/$id/hasPayed → ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    val dto = response.body()
                    if (dto != null) {
                        Resource.Success(mapDto(dto))
                    } else {
                        Resource.Error("No se recibió respuesta del servidor.")
                    }
                } else {
                    val err = response.errorBody()?.string()
                    Log.e("UserClientRepo", "PUT hasPayed error body: $err")
                    Resource.Error("Error al actualizar el estado de pago. Inténtalo más tarde.")
                }
            } catch (e: Exception) {
                Log.e("UserClientRepo", "Exception PUT hasPayed: ${e.message}", e)
                Resource.Error("Parece que no tienes conexión a internet. Verifica tu red y vuelve a intentar.")
            }
        }
    }
}