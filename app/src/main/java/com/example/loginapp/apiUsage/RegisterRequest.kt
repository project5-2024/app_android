package com.example.loginapp

data class RegisterRequest(
    val username: String,
    val password: String,
    val is_admin: Int
)
