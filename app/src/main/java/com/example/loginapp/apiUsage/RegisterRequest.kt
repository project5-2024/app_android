package com.example.loginapp.apiUsage

data class RegisterRequest(
    val username: String,
    val password: String,
    val is_admin: Int
)
