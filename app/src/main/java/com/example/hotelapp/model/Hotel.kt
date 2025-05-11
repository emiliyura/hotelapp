package com.example.hotelapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Hotel(
    val id: Long? = null,
    val name: String,
    val pricePerNight: Double,
    val description: String,
    val roomCount: Int,
    val imageUrl: String? = null
) : Parcelable 