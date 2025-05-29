package com.example.hotelapp.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HotelPriceCalculationTest {
    @Test
    fun `test hotel price calculation`() {
        val hotel = Hotel(
            name = "Test Hotel",
            pricePerNight = 100.0,
            description = "Test description",
            roomCount = 10
        )

        // Test price calculation for 3 nights
        val checkIn = LocalDate.parse("2024-03-20")
        val checkOut = LocalDate.parse("2024-03-23")
        val nights = checkOut.toEpochDay() - checkIn.toEpochDay()
        val totalPrice = hotel.pricePerNight * nights

        assertEquals(300.0, totalPrice, 0.01)
    }
} 