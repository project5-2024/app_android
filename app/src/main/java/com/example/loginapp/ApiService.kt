package com.example.loginapp

import com.example.loginapp.apiUsage.EmptyRequest
import com.example.loginapp.apiUsage.GetPreferencesResponse
import com.example.loginapp.apiUsage.LoginRequest
import com.example.loginapp.apiUsage.LoginResponse
import com.example.loginapp.apiUsage.RegisterRequest
import com.example.loginapp.apiUsage.SetPreferencesRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("users/register")
    fun register(@Body registerRequest: RegisterRequest): Call<Void>

    @PUT("users/set_parameters/{username}")
    fun setPreferences(
        @Path("username") username: String,
        @Body preferences: SetPreferencesRequest
    ): Call<Void>

    @POST("users/get_parameters/{userId}")
    fun getPreferences(
        @Path("userId") userId: String
    ): Call<GetPreferencesResponse>

}

