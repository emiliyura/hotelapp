package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        
        // Находим кнопки
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)
        val registerButton = findViewById<MaterialButton>(R.id.registerButton)
        
        // Устанавливаем обработчики нажатий
        loginButton.setOnClickListener {
            // Переход на экран входа
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        registerButton.setOnClickListener {
            // Переход на экран регистрации
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
} 