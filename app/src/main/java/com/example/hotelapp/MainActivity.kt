package com.example.hotelapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hotelapp.fragments.ProfileFragment
import com.example.hotelapp.fragments.SearchFragment
import com.example.hotelapp.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Проверяем, вошел ли пользователь в систему
        val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false)
        
        if (!isLoggedIn) {
            // Если пользователь не вошел в систему, отправляем его на экран входа
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        bottomNavigation = findViewById(R.id.bottom_navigation)
        
        // Настраиваем слушатель для нижней навигации
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                R.id.navigation_search -> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        
        // По умолчанию загружаем фрагмент профиля
        loadFragment(ProfileFragment())
        bottomNavigation.selectedItemId = R.id.navigation_profile
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}