package com.example.hotelapp

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.model.Hotel
import com.example.hotelapp.network.ApiService
import com.example.hotelapp.network.BookingRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BookingActivity : AppCompatActivity() {
    private lateinit var hotel: Hotel
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var checkInDateEditText: EditText
    private lateinit var checkOutDateEditText: EditText
    private lateinit var roomNumberEditText: EditText
    private lateinit var bookButton: Button
    private lateinit var checkInDate: String
    private lateinit var checkOutDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        hotel = intent.getParcelableExtra(EXTRA_HOTEL) ?: throw IllegalArgumentException("Hotel is required")

        checkInDateEditText = findViewById(R.id.checkInDateEditText)
        checkOutDateEditText = findViewById(R.id.checkOutDateEditText)
        roomNumberEditText = findViewById(R.id.roomNumberEditText)
        bookButton = findViewById(R.id.bookButton)

        // Убираем ручной ввод дат, только через календарь
        checkInDateEditText.isFocusable = false
        checkOutDateEditText.isFocusable = false
        checkInDateEditText.setOnClickListener { showDatePickerDialog(true) }
        checkOutDateEditText.setOnClickListener { showDatePickerDialog(false) }

        bookButton.setOnClickListener {
            createBooking()
        }
    }

    private fun getCurrentUserEmail(): String {
        val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return prefs.getString("email", "") ?: ""
    }

    private fun getCurrentUsername(): String {
        val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return prefs.getString("username", "") ?: ""
    }

    private fun getToken(): String {
        val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return prefs.getString("token", "") ?: ""
    }

    private fun showDatePickerDialog(isCheckIn: Boolean) {
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            if (isCheckIn) {
                checkInDateEditText.setText(date)
                checkInDate = date
            } else {
                checkOutDateEditText.setText(date)
                checkOutDate = date
            }
        }
        val dialog = DatePickerDialog(this, listener, 2024, 0, 1)
        dialog.show()
    }

    private fun createBooking() {
        val email = getCurrentUserEmail()
        val username = getCurrentUsername()
        val roomNumber = roomNumberEditText.text.toString().toIntOrNull()
        val checkInDate = checkInDateEditText.text.toString()
        val checkOutDate = checkOutDateEditText.text.toString()

        if (email.isEmpty() || username.isEmpty() || checkInDate.isEmpty() || checkOutDate.isEmpty() || roomNumber == null) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingRequest = BookingRequest(
            email = email,
            username = username,
            hotelId = hotel.id!!,
            roomNumber = roomNumber,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiService.create().createBooking(bookingRequest)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookingActivity, "Бронирование успешно создано", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookingActivity, "Ошибка при создании бронирования: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val EXTRA_HOTEL = "hotel"
    }
} 