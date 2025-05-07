package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hotelapp.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var emailText: TextView
    private lateinit var usernameText: TextView
    private lateinit var logoutButton: Button
    private lateinit var updateProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        emailText = findViewById(R.id.emailText)
        usernameText = findViewById(R.id.usernameText)
        logoutButton = findViewById(R.id.logoutButton)
        updateProfileButton = findViewById(R.id.updateProfileButton)

        // Загружаем данные пользователя
        loadUserProfile()

        updateProfileButton.setOnClickListener {
            showUpdateEmailDialog()
        }
        
        logoutButton.setOnClickListener {
            logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserProfile() {
        val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        
        // Получаем сохраненные данные
        val savedUsername = sharedPrefs.getString("username", "") ?: ""
        val savedEmail = sharedPrefs.getString("email", "") ?: ""
        
        Log.d("ProfileActivity", "Загружены данные из SharedPreferences: username=$savedUsername, email=$savedEmail")
        
        // Устанавливаем данные в UI
        usernameText.text = savedUsername
        emailText.text = savedEmail
        
        // Проверяем, есть ли email
        if (savedEmail.isBlank() || savedEmail == "Запрос профиля не удался") {
            Toast.makeText(
                this,
                "Email не удалось получить с сервера. Вы можете обновить его в настройках профиля.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun logout() {
        val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            clear()
            apply()
        }
    }

    private fun showUpdateEmailDialog() {
        // Создаем AlertDialog с полем ввода
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Обновить Email")
        
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = "Введите ваш email"
        builder.setView(input)
        
        builder.setPositiveButton("Сохранить") { dialog, _ ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putString("email", email)
                    apply()
                }
                emailText.text = email
                Toast.makeText(this, "Email обновлен", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        
        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.cancel()
        }
        
        builder.show()
    }
} 