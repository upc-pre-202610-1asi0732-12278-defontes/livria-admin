package com.example.adminlivria.clientscontext.data.remote

import com.example.adminlivria.clientscontext.data.remote.dto.UserClientDto
import retrofit2.Response
import retrofit2.http.GET

interface UserClientService {
    @GET("userclients")
    suspend fun getAllUserClients(): Response<List<UserClientDto>>
}
