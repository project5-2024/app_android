package com.example.loginapp.apiUsage

data class LoginResponse(
    val userId: String,
    val token: String,
) {
    override fun toString(): String {
        return "LoginResponse(userId='$userId', token='$token')"
    }
}
