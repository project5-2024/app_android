package com.example.loginapp.apiUsage;


data class GetPreferencesResponse(
    val sports: Int,
    val music: Int,
    val food: Int,
    val travel: Int,
    val movies: Int,
    val technology: Int,
    val fitness: Int,
    val gaming: Int,
    val books: Int,
    val fashion: Int
)