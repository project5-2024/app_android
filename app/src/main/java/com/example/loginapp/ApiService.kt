package com.example.loginapp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("login/username/{username}/password/{password}")
    fun login(@Path("username") username: String, @Path("password") password: String): Call<Void>

    @GET("register/username/{username}/password/{password}")
    fun register(@Path("username") username: String, @Path("password") password: String): Call<Void>
}

