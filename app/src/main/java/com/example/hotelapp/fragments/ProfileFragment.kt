package com.example.hotelapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.hotelapp.LoginActivity
import com.example.hotelapp.R
import com.example.hotelapp.HelpActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class ProfileFragment : Fragment() {
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var logoutButton: Button
    private lateinit var helpButton: Button
    private lateinit var themeSwitch: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameText = view.findViewById(R.id.username_text)
        emailText = view.findViewById(R.id.email_text)
        logoutButton = view.findViewById(R.id.logout_button)
        helpButton = view.findViewById(R.id.help_button)
        themeSwitch = view.findViewById(R.id.theme_switch)

        // Загружаем данные пользователя из SharedPreferences
        loadUserData()

        // Загружаем состояние темы
        loadThemeState()

        // Обработчик переключения темы
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveThemeState(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Обработчик кнопки выхода из аккаунта
        logoutButton.setOnClickListener {
            logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // Обработчик кнопки справки
        helpButton.setOnClickListener {
            showHelp()
        }

        return view
    }

    private fun loadUserData() {
        val sharedPrefs = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPrefs.getString("username", "") ?: ""
        val email = sharedPrefs.getString("email", "") ?: ""

        usernameText.text = username
        emailText.text = email
    }

    private fun loadThemeState() {
        val sharedPrefs = requireActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkTheme = sharedPrefs.getBoolean("isDarkTheme", false)
        themeSwitch.isChecked = isDarkTheme
    }

    private fun saveThemeState(isDarkTheme: Boolean) {
        val sharedPrefs = requireActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("isDarkTheme", isDarkTheme).apply()
    }

    private fun logout() {
        val sharedPrefs = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            clear()
            apply()
        }
    }

    private fun showHelp() {
        val intent = Intent(requireContext(), HelpActivity::class.java)
        startActivity(intent)
    }
}