package com.example.hotelapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hotelapp.HotelUploadActivity
import com.example.hotelapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        // Настройка FAB для добавления отеля
        val fabAddHotel = view.findViewById<FloatingActionButton>(R.id.fab_add_hotel)
        fabAddHotel.setOnClickListener {
            // Запуск активити для добавления отеля
            val intent = Intent(activity, HotelUploadActivity::class.java)
            startActivity(intent)
        }
        
        return view
    }
} 