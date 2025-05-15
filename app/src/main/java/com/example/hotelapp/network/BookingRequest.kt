package com.example.hotelapp.network

data class BookingRequest(
    val email: String,
    val username: String,
    val hotelId: Long,
    val roomNumber: Int,
    val checkInDate: String,
    val checkOutDate: String
) 