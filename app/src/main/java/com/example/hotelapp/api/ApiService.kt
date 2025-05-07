package com.example.hotelapp.api

import com.example.hotelapp.model.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body user: User): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<UserResponse>

    @GET("api/users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<User>
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: User? = null
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val password: String? = null
) 