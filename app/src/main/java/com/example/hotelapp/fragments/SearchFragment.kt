package com.example.hotelapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hotelapp.HotelDetailActivity
import com.example.hotelapp.R
import com.example.hotelapp.adapters.HotelAdapter
import com.example.hotelapp.adapters.SearchHistoryAdapter
import com.example.hotelapp.api.ApiClient
import com.example.hotelapp.model.Hotel
import com.example.hotelapp.utils.ServerChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SearchFragment : Fragment(), HotelAdapter.OnHotelClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchHistoryRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchProgressBar: ProgressBar
    private lateinit var emptyView: TextView

    private val hotelAdapter = HotelAdapter(this)
    private val searchHistoryAdapter = SearchHistoryAdapter(
        onItemClick = { query ->
            searchView.setQuery(query, true)
            hideSearchHistory()
        },
        onDeleteClick = { query ->
            removeFromSearchHistory(query)
        }
    )

    private val MAX_HISTORY_ITEMS = 10
    private var currentQuery: String = ""
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.hotels_recycler_view)
        searchHistoryRecyclerView = view.findViewById(R.id.search_history_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        searchView = view.findViewById(R.id.search_view)
        progressBar = view.findViewById(R.id.progress_bar)
        searchProgressBar = view.findViewById(R.id.search_progress_bar)
        emptyView = view.findViewById(R.id.empty_view)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = hotelAdapter

        searchHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchHistoryRecyclerView.adapter = searchHistoryAdapter

        swipeRefreshLayout.setOnRefreshListener {
            loadHotels()
        }

        searchView.queryHint = "Введите название отеля"
        searchView.isIconified = false

        // Инициализация SearchView без автоматического фокуса
        searchView.clearFocus()
        updateCloseButtonVisibility()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        currentQuery = it
                        addToSearchHistory(it)
                        hideSearchHistory()
                        performSearch(it)
                    }
                }
                updateCloseButtonVisibility()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                updateCloseButtonVisibility()
                if (newText.isNullOrBlank()) {
                    showSearchHistory()
                    hotelAdapter.filter.filter("")
                } else {
                    hideSearchHistory()
                    performSearch(newText)
                }
                return false
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchView.query.isNullOrBlank()) {
                showSearchHistory()
            } else {
                hideSearchHistory()
            }
        }

        // Обработка нажатия на кнопку "Крестик"
        val closeButton = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn)
        closeButton?.setOnClickListener {
            currentQuery = ""
            searchView.setQuery("", false)
            searchView.clearFocus() // Убираем фокус
            hotelAdapter.filter.filter("")
            showSearchHistory()
            updateCloseButtonVisibility()

            // Скрываем клавиатуру
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchView.windowToken, 0)

            // Обновляем список отелей
            loadHotels()
        }

        loadHotels()

        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString("search_query", "")
            searchView.setQuery(currentQuery, false)
            searchView.clearFocus() // Убираем фокус после восстановления
            hotelAdapter.filter.filter(currentQuery)
            updateCloseButtonVisibility()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // При возвращении во фрагмент очищаем фокус и обновляем видимость кнопки
        searchView.clearFocus()
        updateCloseButtonVisibility()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("search_query", currentQuery)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString("search_query", "")
            searchView.setQuery(currentQuery, false)
            searchView.clearFocus() // Убираем фокус после восстановления
            hotelAdapter.filter.filter(currentQuery)
            updateCloseButtonVisibility()
        }
    }

    private fun loadHotels() {
        showLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val isServerAvailable = ServerChecker.isEmulatorServerReachable()
                if (!isServerAvailable) {
                    Log.w(TAG, "Сервер недоступен, загружаем тестовые данные")
                    showHotels(getTestHotels())
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getAllHotels()
                }

                if (response.isSuccessful) {
                    val hotels = response.body() ?: emptyList()
                    if (hotels.isEmpty()) {
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
            emptyView.text = "Нет доступных отелей"
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
        val intent = Intent(requireContext(), HotelDetailActivity::class.java)
        intent.putExtra(HotelDetailActivity.EXTRA_HOTEL, hotel)
        startActivity(intent)
    }

    private fun addToSearchHistory(query: String) {
        val sharedPrefs = requireContext().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
        val history = getSearchHistory().toMutableList()

        history.remove(query)
        history.add(0, query)

        if (history.size > MAX_HISTORY_ITEMS) {
            history.removeAt(history.size - 1)
        }

        sharedPrefs.edit().putString("history", history.joinToString(",")).apply()
        searchHistoryAdapter.setSearchHistory(history)
    }

    private fun removeFromSearchHistory(query: String) {
        val sharedPrefs = requireContext().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
        val history = getSearchHistory().toMutableList()

        history.remove(query)
        sharedPrefs.edit().putString("history", history.joinToString(",")).apply()
        searchHistoryAdapter.setSearchHistory(history)
    }

    private fun getSearchHistory(): List<String> {
        val sharedPrefs = requireContext().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
        val historyString = sharedPrefs.getString("history", "") ?: ""
        return if (historyString.isBlank()) {
            emptyList()
        } else {
            historyString.split(",")
        }
    }

    private fun showSearchHistory() {
        val history = getSearchHistory()
        if (history.isNotEmpty()) {
            searchHistoryAdapter.setSearchHistory(history)
            searchHistoryRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun hideSearchHistory() {
        searchHistoryRecyclerView.visibility = View.GONE
    }

    private fun performSearch(query: String) {
        if (isSearching) return

        isSearching = true
        showSearchProgress(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                delay(300)
                hotelAdapter.filter.filter(query)

                if (hotelAdapter.filteredHotels.isEmpty()) {
                    showNoResults(query)
                } else {
                    hideNoResults()
                }
            } finally {
                isSearching = false
                showSearchProgress(false)
            }
        }
    }

    private fun showNoResults(query: String) {
        emptyView.text = "По запросу \"$query\" ничего не найдено"
        emptyView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideNoResults() {
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showSearchProgress(show: Boolean) {
        searchProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateCloseButtonVisibility() {
        val closeButton = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn)
        closeButton?.visibility = if (searchView.query.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    companion object {
        private const val TAG = "SearchFragment"
    }
}