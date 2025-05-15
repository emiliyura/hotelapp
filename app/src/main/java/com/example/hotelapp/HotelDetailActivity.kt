package com.example.hotelapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.hotelapp.api.ApiClient
import com.example.hotelapp.model.Hotel
import com.example.hotelapp.utils.GlideApp
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ImageView
import android.content.Intent

class HotelDetailActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var hotelImage: ImageView
    private lateinit var hotelName: TextView
    private lateinit var roomCount: TextView
    private lateinit var hotelDescription: TextView
    private lateinit var bookButton: MaterialButton
    
    private val TAG = "HotelDetailActivity"
    
    // Базовый URL для изображений
    private val BASE_URL = "http://10.0.2.2:8080"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_detail)
        
        // Инициализация UI элементов
        toolbar = findViewById(R.id.toolbar)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        hotelImage = findViewById(R.id.hotel_image)
        hotelName = findViewById(R.id.hotel_name)
        roomCount = findViewById(R.id.room_count)
        hotelDescription = findViewById(R.id.hotel_description)
        bookButton = findViewById(R.id.book_button)
        
        // Настройка toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        // Получение ID отеля из Intent
        val hotelId = intent.getLongExtra(EXTRA_HOTEL_ID, -1)
        
        if (hotelId != -1L) {
            // Загрузка информации об отеле
            loadHotelDetails(hotelId)
        } else {
            // Получение объекта Hotel из Intent
            intent.getParcelableExtra<Hotel>(EXTRA_HOTEL)?.let {
                displayHotelDetails(it)
            } ?: run {
                Toast.makeText(this, "Ошибка: Информация об отеле отсутствует", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
        // Обработка нажатия на кнопку
        // bookButton.setOnClickListener {
        //     Toast.makeText(this, "Функция бронирования в разработке", Toast.LENGTH_SHORT).show()
        // }
    }
    
    private fun loadHotelDetails(hotelId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getHotelById(hotelId)
                }
                
                if (response.isSuccessful) {
                    val hotel = response.body()
                    if (hotel != null) {
                        displayHotelDetails(hotel)
                    } else {
                        Toast.makeText(this@HotelDetailActivity, "Ошибка: Информация об отеле не найдена", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@HotelDetailActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке информации об отеле", e)
                Toast.makeText(this@HotelDetailActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun displayHotelDetails(hotel: Hotel) {
        // Отображение информации об отеле
        collapsingToolbar.title = hotel.name
        hotelName.text = hotel.name
        roomCount.text = hotel.roomCount.toString()
        hotelDescription.text = hotel.description
        
        // Отображение цены в кнопке бронирования
        bookButton.text = "Забронировать - ${hotel.pricePerNight} ₽ за ночь"
        
        // Добавляем обработчик нажатия на кнопку бронирования
        bookButton.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra(BookingActivity.EXTRA_HOTEL, hotel)
            }
            startActivity(intent)
        }
        
        // Загрузка изображения
        if (!hotel.imageUrl.isNullOrEmpty()) {
            // Формируем полный URL изображения
            val imageUrl = if (hotel.imageUrl.startsWith("http")) {
                hotel.imageUrl
            } else {
                "$BASE_URL${hotel.imageUrl}"
            }
            
            try {
                GlideApp.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_hotel)
                    .error(R.drawable.placeholder_hotel)
                    .into(hotelImage)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке изображения", e)
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    companion object {
        const val EXTRA_HOTEL_ID = "hotel_id"
        const val EXTRA_HOTEL = "hotel"
    }
} 