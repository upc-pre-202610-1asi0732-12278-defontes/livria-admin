package com.example.adminlivria.clientscontext.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserClientDto(
    @SerializedName("id") val id: Int,
    @SerializedName("display") val display: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("phrase") val phrase: String?,
    @SerializedName("subscription") val subscription: String?,
    @SerializedName("planChangeDate") val planChangeDate: String?,
    @SerializedName("hasPayed") val hasPayed: Boolean?
)