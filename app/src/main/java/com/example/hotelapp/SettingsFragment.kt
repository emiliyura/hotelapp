package com.example.hotelapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.model.Booking
import com.example.hotelapp.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageView

class SettingsFragment : Fragment() {
    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var noBookingsTextView: TextView
    private val bookingsAdapter = BookingsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Log.d("SettingsFragment", "onViewCreated: SettingsFragment открыт")
        //Toast.makeText(requireContext(), "SettingsFragment открыт", Toast.LENGTH_SHORT).show()

        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView)
        noBookingsTextView = view.findViewById(R.id.noBookingsTextView)

        bookingsRecyclerView.layoutManager = LinearLayoutManager(context)
        bookingsRecyclerView.adapter = bookingsAdapter

        loadBookings()
        
        // Check user role and set FAB visibility
        val userRole = getUserRole()
        val fabAddHotel = view.findViewById<FloatingActionButton>(R.id.fab_add_hotel)
        if (userRole == "admin") {
            fabAddHotel.visibility = View.VISIBLE
        } else {
            fabAddHotel.visibility = View.GONE
        }
    }

    private fun loadBookings() {
        val userRole = getUserRole()
        Log.d("SettingsFragment", "Загрузка бронирований. Роль пользователя: $userRole")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bookings = if (userRole == "admin") {
                    Log.d("SettingsFragment", "Загружаем ВСЕ бронирования (админ)")
                    ApiService.create().getAllBookings()
                } else {
                    val username = getCurrentUsername()
                    Log.d("SettingsFragment", "Загружаем брони для пользователя: $username (не админ)")
                    ApiService.create().getUserBookings(username)
                }

                Log.d("SettingsFragment", "API запрос выполнен. Получено бронирований: ${bookings.size}")
                withContext(Dispatchers.Main) {
                    if (bookings.isEmpty()) {
                        Log.d("SettingsFragment", "Список бронирований пуст. Отображаем noBookingsTextView.")
                        noBookingsTextView.visibility = View.VISIBLE
                        bookingsRecyclerView.visibility = View.GONE
                    } else {
                        Log.d("SettingsFragment", "Список бронирований не пуст. Отображаем bookingsRecyclerView.")
                        noBookingsTextView.visibility = View.GONE
                        bookingsRecyclerView.visibility = View.VISIBLE
                        bookingsAdapter.submitList(bookings, userRole)
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Ошибка при загрузке бронирований", e)
                withContext(Dispatchers.Main) {
                    // Обработка ошибки
                    Toast.makeText(requireContext(), "Ошибка при загрузке бронирований: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getCurrentUsername(): String {
        val prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return prefs.getString("username", "") ?: ""
    }

    private fun getUserRole(): String {
        val prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return prefs.getString("role", "user") ?: "user"
    }

    private fun getToken(): String {
        val prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        return prefs.getString("token", "") ?: ""
    }
}

class BookingsAdapter : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {
    private var bookings: List<Booking> = emptyList()
    private val apiService = ApiService.create()
    private var userRole: String = "user"

    fun submitList(newBookings: List<Booking>, userRole: String) {
        bookings = newBookings
        this.userRole = userRole
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view, apiService)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position], userRole)
    }

    override fun getItemCount() = bookings.size

    class BookingViewHolder(itemView: View, private val apiService: ApiService) : RecyclerView.ViewHolder(itemView) {
        private val hotelNameTextView: TextView = itemView.findViewById(R.id.hotelNameTextView)
        private val roomNumberTextView: TextView = itemView.findViewById(R.id.roomNumberTextView)
        private val datesTextView: TextView = itemView.findViewById(R.id.datesTextView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(booking: Booking, userRole: String) {
            // Загружаем информацию об отеле
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val hotel = apiService.getHotel(booking.hotelId)
                    withContext(Dispatchers.Main) {
                        hotelNameTextView.text = hotel.name
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hotelNameTextView.text = "Отель #${booking.hotelId}"
                    }
                }
            }
            roomNumberTextView.text = "Номер: ${booking.roomNumber}"
            datesTextView.text = "${booking.checkInDate} - ${booking.checkOutDate}"
            
            // Отображаем имя пользователя и кнопку удаления только для администратора
            if (userRole == "admin") {
                usernameTextView.visibility = View.VISIBLE
                usernameTextView.text = "Пользователь: ${booking.username}"
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    // Обработчик нажатия на кнопку удаления
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiService.deleteBooking(booking.id!!)
                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    Toast.makeText(itemView.context, "Бронирование удалено", Toast.LENGTH_SHORT).show()
                                    // Здесь нужно обновить список бронирований в фрагменте
                                    // Это можно сделать через колбэк или LiveData/StateFlow
                                } else {
                                    Toast.makeText(itemView.context, "Ошибка при удалении бронирования", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("BookingViewHolder", "Ошибка при удалении бронирования", e)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(itemView.context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                usernameTextView.visibility = View.GONE
                deleteButton.visibility = View.GONE
            }
        }
    }
}