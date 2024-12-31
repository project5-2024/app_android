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

    @PUT("users/set_geo_parameters/{username}")
    fun setGeoPreferences(
        @Path("username") username: String,
        @Body preferences: SetPreferencesRequest
    ): Call<Void>

    @POST("users/get_geo_parameters/{userId}")
    fun getGeoPreferences(
        @Path("userId") userId: String
    ): Call<GetPreferencesResponse>



}

//curl -X PUT "http://185.94.45.58:7832/users/set_geo_parameters/name" -H "Content-Type: application/json" -d "{\"userdata\": {\"sports\": 12, \"music\": 20, \"food\": 23, \"travel\": 54, \"movies\": 64, \"technology\": 75, \"fitness\": 99, \"gaming\": 56, \"books\": 75, \"fashion\": 36}}"
