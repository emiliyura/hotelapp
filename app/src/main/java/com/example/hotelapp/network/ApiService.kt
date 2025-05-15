package com.example.hotelapp.network

import com.example.hotelapp.model.Booking
import com.example.hotelapp.model.Hotel
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @GET("hotels")
    suspend fun getHotels(): List<Hotel>

    @GET("hotels/{id}")
    suspend fun getHotel(@Path("id") id: Long): Hotel

    @POST("bookings")
    suspend fun createBooking(@Body bookingRequest: BookingRequest): Booking

    @GET("bookings")
    suspend fun getUserBookings(@Query("username") username: String): List<Booking>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/api/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
} 