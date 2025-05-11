package com.example.hotelapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelapp.R
import com.example.hotelapp.adapters.HotelAdapter
import com.example.hotelapp.api.ApiClient
import com.example.hotelapp.model.Hotel
import com.example.hotelapp.utils.ServerChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SearchFragment : Fragment(), HotelAdapter.OnHotelClickListener {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    
    private val hotelAdapter = HotelAdapter(this)
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        
        // Инициализация UI элементов
        recyclerView = view.findViewById(R.id.hotels_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        searchView = view.findViewById(R.id.search_view)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.empty_view)
        
        // Настройка RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = hotelAdapter
        
        // Настройка SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadHotels()
        }
        
        // Настройка SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hotelAdapter.filter.filter(query)
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                hotelAdapter.filter.filter(newText)
                return false
            }
        })
        
        // Загрузка отелей при создании фрагмента
        loadHotels()
        
        return view
    }
    
    private fun loadHotels() {
        showLoading(true)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Проверка доступности сервера
                val isServerAvailable = ServerChecker.isEmulatorServerReachable()
                if (!isServerAvailable) {
                    Log.w(TAG, "Сервер недоступен, загружаем тестовые данные")
                    // Если сервер недоступен, показываем тестовые данные
                    showHotels(getTestHotels())
                    return@launch
                }
                
                // Загрузка отелей с сервера
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getAllHotels()
                }
                
                if (response.isSuccessful) {
                    val hotels = response.body() ?: emptyList()
                    if (hotels.isEmpty()) {
                        // Если список пуст, показываем тестовые данные
                        showHotels(getTestHotels())
                    } else {
                        showHotels(hotels)
                    }
                } else {
                    showError("Ошибка при загрузке отелей: ${response.code()} - ${response.message()}")
                }
            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "Таймаут соединения, загружаем тестовые данные", e)
                showHotels(getTestHotels())
            } catch (e: UnknownHostException) {
                Log.w(TAG, "Хост не найден, загружаем тестовые данные", e)
                showHotels(getTestHotels())
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке отелей", e)
                showError("Ошибка: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun getTestHotels(): List<Hotel> {
        // Создаем тестовые данные для отображения
        return listOf(
            Hotel(
                id = 1,
                name = "Гранд Отель",
                pricePerNight = 5000.0,
                description = "Роскошный отель в центре города с видом на реку",
                roomCount = 100,
                imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=1000"
            ),
            Hotel(
                id = 2,
                name = "Комфорт Инн",
                pricePerNight = 3500.0,
                description = "Уютный отель для бизнес-путешественников",
                roomCount = 75,
                imageUrl = "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?q=80&w=1000"
            ),
            Hotel(
                id = 3,
                name = "Морской Бриз",
                pricePerNight = 4500.0,
                description = "Пляжный отель с панорамным видом на море",
                roomCount = 85,
                imageUrl = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?q=80&w=1000"
            ),
            Hotel(
                id = 4,
                name = "Горный Курорт",
                pricePerNight = 6000.0,
                description = "Отель в горах с видом на заснеженные вершины",
                roomCount = 60,
                imageUrl = "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?q=80&w=1000"
            )
        )
    }
    
    private fun showHotels(hotels: List<Hotel>) {
        if (hotels.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            hotelAdapter.setHotels(hotels)
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun showError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        emptyView.text = message
        emptyView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    override fun onHotelClick(hotel: Hotel) {
        // Обработка нажатия на отель
        Toast.makeText(requireContext(), "Выбран отель: ${hotel.name}", Toast.LENGTH_SHORT).show()
        
        // Здесь можно добавить переход на экран детальной информации об отеле
        // val intent = Intent(requireContext(), HotelDetailActivity::class.java)
        // intent.putExtra("hotel_id", hotel.id)
        // startActivity(intent)
    }
    
    companion object {
        private const val TAG = "SearchFragment"
    }
} 