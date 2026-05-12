package com.example.adminlivria.clientscontext.domain.model

data class UserClient(
    val id: Int,
    val display: String,
    val username: String,
    val email: String,
    val icon: String,
    val phrase: String,
    val subscription: String,
    val planChangeDate: String,
    val hasPayed: Boolean
)