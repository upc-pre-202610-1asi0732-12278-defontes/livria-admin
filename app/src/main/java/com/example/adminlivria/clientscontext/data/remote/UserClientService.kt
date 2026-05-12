package com.example.adminlivria.clientscontext.data.remote

import com.example.adminlivria.clientscontext.data.remote.dto.UpdateHasPayedDto
import com.example.adminlivria.clientscontext.data.remote.dto.UpdateUserClientDto
import com.example.adminlivria.clientscontext.data.remote.dto.UserClientDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserClientService {

    @GET("userclients")
    suspend fun getAllUserClients(): Response<List<UserClientDto>>

    @PUT("userclients/{id}/subscription")
    suspend fun updateUserClient(
        @Path("id") id: Int,
        @Body body: UpdateUserClientDto
    ): Response<UserClientDto>

    @PUT("userclients/{id}/hasPayed")
    suspend fun updateHasPayed(
        @Path("id") id: Int,
        @Body body: UpdateHasPayedDto
    ): Response<UserClientDto>
}