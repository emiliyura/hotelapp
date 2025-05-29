package com.example.hotelapp.model

import org.junit.Assert.*
import org.junit.Test

class HotelModelTest {
    @Test
    fun `test hotel model validation`() {
        // Test valid hotel
        val validHotel = Hotel(
            id = 1L,
            name = "Grand Hotel",
            pricePerNight = 100.0,
            description = "Luxury hotel in city center",
            roomCount = 50,
            imageUrl = "https://example.com/hotel.jpg"
        )
        assertNotNull(validHotel)
        assertEquals(1L, validHotel.id)
        assertEquals("Grand Hotel", validHotel.name)
        assertEquals(100.0, validHotel.pricePerNight, 0.01)
        assertEquals(50, validHotel.roomCount)
        assertEquals("https://example.com/hotel.jpg", validHotel.imageUrl)

        // Test hotel without optional fields
        val hotelWithoutOptionals = Hotel(
            name = "Simple Hotel",
            pricePerNight = 50.0,
            description = "Basic hotel",
            roomCount = 20
        )
        assertNull(hotelWithoutOptionals.id)
        assertNull(hotelWithoutOptionals.imageUrl)
    }
} 