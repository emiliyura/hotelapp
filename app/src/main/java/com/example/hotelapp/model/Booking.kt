package com.example.hotelapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Booking(
    val id: Long? = null,
    val email: String,
    val username: String,
    val hotelId: Long,
    val roomNumber: Int,
    val checkInDate: String,
    val checkOutDate: String
) : Parcelable 