package com.example.hotelapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.R

class SearchHistoryAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    private var searchHistory: List<String> = listOf()

    fun setSearchHistory(history: List<String>) {
        searchHistory = history
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val query = searchHistory[position]
        holder.bind(query)
    }

    override fun getItemCount(): Int = searchHistory.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val searchQuery: TextView = itemView.findViewById(R.id.search_query)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(query: String) {
            searchQuery.text = query
            
            itemView.setOnClickListener {
                onItemClick(query)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(query)
            }
        }
    }
} 