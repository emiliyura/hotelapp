package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.api.ApiClient
import com.example.hotelapp.api.LoginRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.hotelapp.ErrorResponse
import com.example.hotelapp.utils.ServerChecker
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Скрываем верхнюю панель действий (ActionBar)
        supportActionBar?.hide()

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        // Проверяем доступность сервера при запуске
        checkServerAvailability()
    }
    
    private fun checkServerAvailability() {
        CoroutineScope(Dispatchers.Main).launch {
            val isServerAvailable = ServerChecker.isEmulatorServerReachable()
            if (!isServerAvailable) {
                Toast.makeText(
                    this@LoginActivity,
                    "Внимание: Сервер недоступен. Убедитесь, что сервер запущен на порту 8080.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        // Показываем индикатор загрузки
        loginButton.isEnabled = false
        loginButton.text = "Вход..."
        
        CoroutineScope(Dispatchers.Main).launch {
            // Сначала проверяем доступность сервера
            val isServerAvailable = ServerChecker.isEmulatorServerReachable()
            if (!isServerAvailable) {
                Toast.makeText(
                    this@LoginActivity,
                    "Сервер недоступен. Убедитесь, что сервер запущен на порту 8080.",
                    Toast.LENGTH_LONG
                ).show()
                loginButton.isEnabled = true
                loginButton.text = "Войти"
                return@launch
            }
            
            try {
                // Используем email как username для запроса LoginRequest
                val loginRequest = LoginRequest(username = email, password = password)
                Log.d("LoginActivity", "Отправляем данные: $loginRequest")

                val response = withContext(Dispatchers.IO) {
                    ApiClient.api.login(loginRequest)
                }

                Log.d("LoginActivity", "Код ответа: ${response.code()}")
                Log.d("LoginActivity", "Тело ответа: ${response.body()}")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    
                    if (userResponse == null) {
                        Log.e("LoginActivity", "Пользователь не получен от сервера")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Ошибка аутентификации: данные пользователя не получены",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }
                    
                    Log.d("LoginActivity", "Получен пользователь: ${userResponse.username}, email: ${userResponse.email}")
                    
                    val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
                    with(sharedPrefs.edit()) {
                        putString("username", userResponse.username)
                        putString("email", userResponse.email)
                        putString("name", userResponse.username)
                        putString("lastLogin", java.util.Date().toString())
                        putBoolean("isLoggedIn", true)
                        putString("role", userResponse.role)
                        apply()
                    }
                    // Проверим, что сохранено в SharedPreferences перед переходом
                    val savedEmail = sharedPrefs.getString("email", "")
                    val savedUsername = sharedPrefs.getString("username", "")
                    Log.d("LoginActivity", "Сохранено в SharedPreferences: username=$savedUsername, email=$savedEmail")
                    
                    // Запускаем MainActivity вместо ProfileActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message
                    } catch (e: Exception) {
                        errorBody ?: "Неизвестная ошибка"
                    }

                    // Переводим сообщение об ошибке на русский
                    val russianMessage = when (errorMessage) {
                        "Invalid username or password" -> "Неверный email или пароль"
                        "User not found" -> "Пользователь не найден"
                        else -> "Ошибка входа: $errorMessage"
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            russianMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.e("LoginActivity", "Ошибка подключения к серверу", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Не удалось подключиться к серверу. Проверьте, запущен ли сервер на порту 8080.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: UnknownHostException) {
                Log.e("LoginActivity", "Сервер не найден", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Сервер не найден. Проверьте подключение к интернету и убедитесь, что сервер запущен.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Исключение при входе", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                // Восстанавливаем состояние кнопки
                withContext(Dispatchers.Main) {
                    loginButton.isEnabled = true
                    loginButton.text = "Войти"
                }
            }
        }
    }
} 