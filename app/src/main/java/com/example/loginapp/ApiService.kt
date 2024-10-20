package com.example.loginapp

import com.example.loginapp.apiUsage.LoginRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("users/register")
    fun register(@Body registerRequest: RegisterRequest): Call<Void>

    //@PUT("users/username")
    //fun updatePreferences(@Body preferencesRequest: PreferencesRequest): Call<Void> // Example if you need it later
}
