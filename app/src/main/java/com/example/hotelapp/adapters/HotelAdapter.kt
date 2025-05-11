package com.example.hotelapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hotelapp.R
import com.example.hotelapp.model.Hotel
import com.example.hotelapp.utils.GlideApp
import java.util.*

class HotelAdapter(private val onHotelClickListener: OnHotelClickListener) : 
    RecyclerView.Adapter<HotelAdapter.HotelViewHolder>(), Filterable {
    
    private var hotels: List<Hotel> = listOf()
    private var filteredHotels: List<Hotel> = listOf()
    private val TAG = "HotelAdapter"
    
    // Базовый URL для изображений
    private val BASE_URL = "http://10.0.2.2:8080"
    
    interface OnHotelClickListener {
        fun onHotelClick(hotel: Hotel)
    }
    
    fun setHotels(hotels: List<Hotel>) {
        this.hotels = hotels
        this.filteredHotels = hotels
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = filteredHotels[position]
        holder.bind(hotel)
    }
    
    override fun getItemCount(): Int = filteredHotels.size
    
    inner class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hotelImage: ImageView = itemView.findViewById(R.id.hotel_image)
        private val hotelName: TextView = itemView.findViewById(R.id.hotel_name)
        private val hotelPrice: TextView = itemView.findViewById(R.id.hotel_price)
        private val hotelDescription: TextView = itemView.findViewById(R.id.hotel_description)
        
        fun bind(hotel: Hotel) {
            hotelName.text = hotel.name
            hotelPrice.text = "${hotel.pricePerNight} ₽"
            hotelDescription.text = hotel.description
            
            // Загрузка изображения с помощью Glide
            if (!hotel.imageUrl.isNullOrEmpty()) {
                // Формируем полный URL изображения
                val imageUrl = if (hotel.imageUrl.startsWith("http")) {
                    hotel.imageUrl
                } else {
                    "$BASE_URL${hotel.imageUrl}"
                }
                
                Log.d(TAG, "Загрузка изображения: $imageUrl")
                
                try {
                    GlideApp.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_hotel)
                        .error(R.drawable.placeholder_hotel)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(hotelImage)
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при загрузке изображения: ${e.message}", e)
                    hotelImage.setImageResource(R.drawable.placeholder_hotel)
                }
            } else {
                hotelImage.setImageResource(R.drawable.placeholder_hotel)
            }
            
            // Обработка нажатия на элемент
            itemView.setOnClickListener {
                onHotelClickListener.onHotelClick(hotel)
            }
        }
    }
    
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                filteredHotels = if (charString.isEmpty()) {
                    hotels
                } else {
                    val filteredList = mutableListOf<Hotel>()
                    hotels.forEach { hotel ->
                        if (hotel.name.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(hotel)
                        }
                    }
                    filteredList
                }
                return FilterResults().apply { values = filteredHotels }
            }
            
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredHotels = results?.values as? List<Hotel> ?: listOf()
                notifyDataSetChanged()
            }
        }
    }
} 