package com.example.adminlivria.clientscontext.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateHasPayedDto(
    @SerializedName("hasPayed") val hasPayed: Boolean
)