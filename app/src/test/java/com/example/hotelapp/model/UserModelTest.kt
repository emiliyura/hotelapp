package com.example.hotelapp.model

import org.junit.Assert.*
import org.junit.Test

class UserModelTest {
    @Test
    fun `test user model validation`() {
        // Test valid user
        val validUser = User(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            role = "user"
        )
        assertNotNull(validUser)
        assertEquals("testuser", validUser.username)
        assertEquals("test@example.com", validUser.email)
        assertEquals("password123", validUser.password)
        assertEquals("user", validUser.role)

        // Test default role
        val userWithDefaultRole = User(
            username = "testuser2",
            email = "test2@example.com",
            password = "password123"
        )
        assertEquals("user", userWithDefaultRole.role)
    }
} 