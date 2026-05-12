package com.example.adminlivria.clientscontext.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateUserClientDto(
    @SerializedName("newSubscriptionPlan") val newSubscriptionPlan: String
)