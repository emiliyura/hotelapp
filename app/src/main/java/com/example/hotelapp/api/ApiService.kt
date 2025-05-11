package com.example.hotelapp.api

import com.example.hotelapp.model.Hotel
import com.example.hotelapp.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body user: User): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<UserResponse>

    @GET("api/users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<User>
    
    @Multipart
    @POST("api/hotels")
    fun addHotel(
        @Part("name") name: RequestBody,
        @Part("pricePerNight") pricePerNight: RequestBody,
        @Part("description") description: RequestBody,
        @Part("roomCount") roomCount: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<Hotel>
    
    @GET("api/hotels")
    suspend fun getAllHotels(): Response<List<Hotel>>
    
    @GET("api/hotels/{id}")
    suspend fun getHotelById(@Path("id") id: Long): Response<Hotel>
}

data class LoginRequest(
    val username: String,
    val password: String
) {
    override fun toString(): String {
        return "LoginRequest(username='$username', password='***')"
    }
}

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