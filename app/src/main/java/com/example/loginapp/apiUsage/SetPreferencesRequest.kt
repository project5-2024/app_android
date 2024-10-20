package com.example.loginapp.apiUsage

data class SetPreferencesRequest(
    val userdata: Map<String, Int>
)

data class PreferencesData(
    val preferences: Map<String, Int>
)
