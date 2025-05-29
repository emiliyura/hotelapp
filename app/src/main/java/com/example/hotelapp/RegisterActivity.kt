package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.api.ApiClient
import com.example.hotelapp.model.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.hotelapp.ErrorResponse

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Скрываем верхнюю панель действий (ActionBar)
        supportActionBar?.hide()

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        // Set up click listeners
        registerButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (validateInput(username, email, password)) {
                registerUser(username, email, password)
            }
        }

        loginButton.setOnClickListener {
            finish()
        }
    }

    public fun validateInput(username: String, email: String, password: String): Boolean { //private
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser(username: String, email: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = User(username = username, email = email, password = password)
                Log.d("RegisterActivity", "Отправляем данные: $user")
                
                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.register(user)
                }
                
                Log.d("RegisterActivity", "Код ответа: ${response.code()}")
                Log.d("RegisterActivity", "Тело ответа: ${response.body()}")

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.let {
                        // Сохраняем только токен и данные, которые мы отправляли
                        val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
                        with(sharedPrefs.edit()) {
                            putString("token", it.token)
                            putString("email", email)
                            putString("username", username)
                            putBoolean("isLoggedIn", true)
                            apply()
                        }
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            // Используем Gson для парсинга JSON ошибки
                            val gson = com.google.gson.Gson()
                            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                            errorResponse.message
                        } catch (e: Exception) {
                            errorBody ?: "Неизвестная ошибка"
                        }
                        
                        // Переводим сообщение об ошибке на русский
                        val russianMessage = when (errorMessage) {
                            "Username already exists" -> "Пользователь с таким именем уже существует"
                            "Email already exists" -> "Email уже зарегистрирован"
                            else -> "Ошибка регистрации: $errorMessage"
                        }
                        
                        Toast.makeText(this@RegisterActivity, 
                            russianMessage, 
                            Toast.LENGTH_LONG).show()
                        Log.e("RegisterActivity", "Ошибка регистрации: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Исключение при регистрации", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, 
                        "Ошибка: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
} 