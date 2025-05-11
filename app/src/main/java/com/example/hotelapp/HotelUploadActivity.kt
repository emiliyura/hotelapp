package com.example.hotelapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hotelapp.api.ApiClient
import com.example.hotelapp.model.Hotel
import com.example.hotelapp.utils.ServerChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class HotelUploadActivity : AppCompatActivity() {
    
    private lateinit var imageContainer: FrameLayout
    private lateinit var hotelImage: ImageView
    private lateinit var hotelNameEditText: EditText
    private lateinit var hotelPriceEditText: EditText
    private lateinit var hotelDescriptionEditText: EditText
    private lateinit var hotelRoomsEditText: EditText
    private lateinit var saveButton: Button
    
    private var selectedImageUri: Uri? = null
    
    // Используем новый API для выбора изображений
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        Log.d(TAG, "Изображение выбрано: $uri")
        uri?.let {
            selectedImageUri = it
            hotelImage.setImageURI(it)
            hotelImage.scaleType = ImageView.ScaleType.CENTER_CROP
            Toast.makeText(this, "Изображение выбрано", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Запрос разрешений с использованием нового API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Разрешение предоставлено через requestPermissionLauncher")
            openGallery()
        } else {
            Log.d(TAG, "Разрешение отклонено через requestPermissionLauncher")
            Toast.makeText(
                this,
                "Для выбора изображения необходимо разрешение на доступ к хранилищу",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_upload)
        
        // Инициализация UI элементов
        imageContainer = findViewById(R.id.image_container)
        hotelImage = findViewById(R.id.hotel_image)
        hotelNameEditText = findViewById(R.id.hotel_name_edittext)
        hotelPriceEditText = findViewById(R.id.hotel_price_edittext)
        hotelDescriptionEditText = findViewById(R.id.hotel_description_edittext)
        hotelRoomsEditText = findViewById(R.id.hotel_rooms_edittext)
        saveButton = findViewById(R.id.save_button)
        
        // Настройка выбора изображения
        imageContainer.setOnClickListener {
            Log.d(TAG, "Нажатие на контейнер изображения")
            requestStoragePermissionAndOpenGallery()
        }
        
        // Для совместимости также добавим обработчик на само изображение
        hotelImage.setOnClickListener {
            Log.d(TAG, "Нажатие на изображение")
            requestStoragePermissionAndOpenGallery()
        }
        
        // Настройка кнопки сохранения
        saveButton.setOnClickListener {
            // Проверяем доступность сервера перед отправкой
            checkServerAndUpload()
        }
    }
    
    private fun requestStoragePermissionAndOpenGallery() {
        when {
            // Для Android 13+ (API 33+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                Log.d(TAG, "Android 13+, запрашиваем READ_MEDIA_IMAGES")
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
            // Для Android 10-12 (API 29-32)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                Log.d(TAG, "Android 10-12, открываем галерею напрямую")
                // В Android 10+ разрешение на чтение не требуется для выбора файлов через системный пикер
                openGallery()
            }
            // Для Android 9 и ниже (API < 29)
            else -> {
                Log.d(TAG, "Android 9 и ниже, запрашиваем READ_EXTERNAL_STORAGE")
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
    
    private fun openGallery() {
        Log.d(TAG, "Открываем галерею")
        try {
            pickImage.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при открытии галереи", e)
            Toast.makeText(this, "Не удалось открыть галерею: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkServerAndUpload() {
        // Показываем индикатор загрузки
        saveButton.isEnabled = false
        saveButton.text = "Проверка сервера..."
        
        CoroutineScope(Dispatchers.Main).launch {
            val isServerAvailable = ServerChecker.isEmulatorServerReachable()
            if (!isServerAvailable) {
                Toast.makeText(
                    this@HotelUploadActivity,
                    "Сервер недоступен. Убедитесь, что сервер запущен на порту 8080.",
                    Toast.LENGTH_LONG
                ).show()
                saveButton.isEnabled = true
                saveButton.text = "Сохранить"
            } else {
                uploadHotel()
            }
        }
    }
    
    private fun uploadHotel() {
        val name = hotelNameEditText.text.toString()
        val priceText = hotelPriceEditText.text.toString()
        val description = hotelDescriptionEditText.text.toString()
        val roomsText = hotelRoomsEditText.text.toString()
        
        // Валидация полей
        if (name.isEmpty() || priceText.isEmpty() || description.isEmpty() || roomsText.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            saveButton.text = "Сохранить"
            return
        }
        
        val price = priceText.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(this, "Некорректная цена", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            saveButton.text = "Сохранить"
            return
        }
        
        val rooms = roomsText.toIntOrNull()
        if (rooms == null) {
            Toast.makeText(this, "Некорректное количество комнат", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            saveButton.text = "Сохранить"
            return
        }
        
        if (selectedImageUri == null) {
            Toast.makeText(this, "Пожалуйста, выберите изображение", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            saveButton.text = "Сохранить"
            return
        }
        
        // Показываем индикатор загрузки
        saveButton.text = "Сохранение..."
        
        // Подготовка данных для отправки
        val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceRequestBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val roomsRequestBody = rooms.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        
        var imageMultipart: MultipartBody.Part? = null
        
        try {
            selectedImageUri?.let { uri ->
                Log.d(TAG, "Подготовка изображения для отправки: $uri")
                // Конвертация Uri в File
                val imageFile = File(cacheDir, "temp_image")
                val inputStream = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(imageFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                Log.d(TAG, "Файл изображения создан: ${imageFile.exists()}, размер: ${imageFile.length()} байт")
                
                // Создание MultipartBody.Part из файла
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                imageMultipart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                Log.d(TAG, "MultipartBody.Part создан")
            } ?: run {
                Log.d(TAG, "Изображение не выбрано")
                Toast.makeText(this, "Изображение не выбрано", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
                saveButton.text = "Сохранить"
                return
            }
            
            // Отправка запроса на сервер
            Log.d(TAG, "Отправка запроса на сервер")
            val apiService = ApiClient.apiService
            
            val call = apiService.addHotel(
                nameRequestBody,
                priceRequestBody,
                descriptionRequestBody,
                roomsRequestBody,
                imageMultipart
            )
            
            call.enqueue(object : Callback<Hotel> {
                override fun onResponse(call: Call<Hotel>, response: Response<Hotel>) {
                    // Восстанавливаем состояние кнопки
                    saveButton.isEnabled = true
                    saveButton.text = "Сохранить"
                    
                    if (response.isSuccessful) {
                        Log.d(TAG, "Отель успешно добавлен: ${response.body()}")
                        Toast.makeText(this@HotelUploadActivity, "Отель успешно добавлен", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.e(TAG, "Ошибка при добавлении отеля: ${response.code()} - ${response.message()}")
                        Toast.makeText(this@HotelUploadActivity, "Ошибка: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onFailure(call: Call<Hotel>, t: Throwable) {
                    // Восстанавливаем состояние кнопки
                    saveButton.isEnabled = true
                    saveButton.text = "Сохранить"
                    
                    Log.e(TAG, "Ошибка при отправке запроса", t)
                    
                    when (t) {
                        is SocketTimeoutException -> {
                            Toast.makeText(
                                this@HotelUploadActivity, 
                                "Не удалось подключиться к серверу. Проверьте, запущен ли сервер на порту 8080.", 
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is UnknownHostException -> {
                            Toast.makeText(
                                this@HotelUploadActivity, 
                                "Сервер не найден. Проверьте подключение к интернету и убедитесь, что сервер запущен.", 
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(this@HotelUploadActivity, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            // Восстанавливаем состояние кнопки
            saveButton.isEnabled = true
            saveButton.text = "Сохранить"
            
            Log.e(TAG, "Ошибка при подготовке данных", e)
            Toast.makeText(this, "Ошибка при подготовке данных: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    companion object {
        private const val TAG = "HotelUploadActivity"
    }
} 