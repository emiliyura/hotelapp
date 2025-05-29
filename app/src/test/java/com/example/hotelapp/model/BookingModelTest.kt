package com.example.hotelapp.model

import org.junit.Assert.*
import org.junit.Test

class BookingModelTest {
    @Test
    fun `test booking model validation`() {
        // Test valid booking
        val validBooking = Booking(
            id = 1L,
            email = "guest@example.com",
            username = "guest",
            hotelId = 1L,
            roomNumber = 101,
            checkInDate = "2024-03-20",
            checkOutDate = "2024-03-25"
        )
        assertNotNull(validBooking)
        assertEquals(1L, validBooking.id)
        assertEquals("guest@example.com", validBooking.email)
        assertEquals(101, validBooking.roomNumber)
        assertEquals("2024-03-20", validBooking.checkInDate)
        assertEquals("2024-03-25", validBooking.checkOutDate)

        // Test booking without ID
        val bookingWithoutId = Booking(
            email = "guest2@example.com",
            username = "guest2",
            hotelId = 2L,
            roomNumber = 102,
            checkInDate = "2024-03-21",
            checkOutDate = "2024-03-26"
        )
        assertNull(bookingWithoutId.id)
    }
} 