package com.example.hotelapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.hotelapp.fragments.ProfileFragment
import com.example.hotelapp.fragments.SearchFragment
import com.example.hotelapp.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileFragment: ProfileFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var settingsFragment: SettingsFragment
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем сохраненную тему
        applyTheme()
        
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
        
        // Инициализируем фрагменты
        profileFragment = ProfileFragment()
        searchFragment = SearchFragment()
        settingsFragment = SettingsFragment()
        
        bottomNavigation = findViewById(R.id.bottom_navigation)
        
        // Настраиваем слушатель для нижней навигации
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    loadFragment(profileFragment)
                    true
                }
                R.id.navigation_search -> {
                    loadFragment(searchFragment)
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
        
        // Восстанавливаем выбранный фрагмент или устанавливаем профиль по умолчанию
        if (savedInstanceState == null) {
            loadFragment(profileFragment)
            bottomNavigation.selectedItemId = R.id.navigation_profile
        } else {
            // Восстанавливаем выбранный элемент навигации
            val selectedItemId = savedInstanceState.getInt("selected_nav_item", R.id.navigation_profile)
            bottomNavigation.selectedItemId = selectedItemId
            
            // Восстанавливаем фрагмент, если он еще не восстановлен системой
            if (supportFragmentManager.fragments.isEmpty()) {
                when (selectedItemId) {
                    R.id.navigation_profile -> loadFragment(profileFragment)
                    R.id.navigation_search -> loadFragment(searchFragment)
                    R.id.navigation_settings -> loadFragment(settingsFragment)
                }
            }
        }
    }
    
    private fun applyTheme() {
        val sharedPrefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkTheme = sharedPrefs.getBoolean("isDarkTheme", false)
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем выбранный элемент навигации
        outState.putInt("selected_nav_item", bottomNavigation.selectedItemId)
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}