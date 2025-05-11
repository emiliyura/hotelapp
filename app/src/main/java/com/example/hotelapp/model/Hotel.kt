package com.example.hotelapp.model

data class Hotel(
    val id: Long? = null,
    val name: String,
    val pricePerNight: Double,
    val description: String,
    val roomCount: Int,
    val imageUrl: String? = null
) 