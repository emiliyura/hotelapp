package com.example.hotelapp.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BookingDateValidationTest {
    @Test
    fun `test booking date validation`() {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        
        // Test valid date range
        val checkIn = LocalDate.parse("2024-03-20", formatter)
        val checkOut = LocalDate.parse("2024-03-25", formatter)
        assertTrue(checkOut.isAfter(checkIn))

        // Test invalid date range
        val invalidCheckIn = LocalDate.parse("2024-03-25", formatter)
        val invalidCheckOut = LocalDate.parse("2024-03-20", formatter)
        assertFalse(invalidCheckOut.isAfter(invalidCheckIn))
    }
} 