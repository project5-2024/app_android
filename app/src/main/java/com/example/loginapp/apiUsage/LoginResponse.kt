package com.example.loginapp.apiUsage

data class LoginResponse(
    val userId: String,
    val token: String,
    // Other fields as per your API response
) {
    override fun toString(): String {
        return "LoginResponse(userId='$userId', token='$token')"
    }
}
