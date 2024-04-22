package com.devtoochi.blood_donation.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.backend.models.Country
import com.devtoochi.blood_donation.databinding.ItemFragmentCountriesBinding

class CountryAdapter(
    private val itemList: MutableList<Country>,
    private val listener: OnCountrySelectedListener
) : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {

    private var filteredList: MutableList<Country> = mutableListOf()

    init {
        filteredList.addAll(itemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFragmentCountriesBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryAdapter.ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount() = filteredList.size

    inner class ViewHolder(private val binding: ItemFragmentCountriesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(country: Country) {
            binding.setVariable(BR.country, country)
            binding.root.setOnClickListener {
                val selectedCountry = filteredList[adapterPosition]
                listener.onCountrySelected(selectedCountry)
            }
            binding.executePendingBindings()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(text: String) {
        filteredList.clear()
        if (text.isEmpty()) {
            filteredList.addAll(itemList)
        } else {
            val searchText = text.lowercase().trim()
            for (item in itemList) {
                if (item.name.lowercase().contains(searchText)) {
                    filteredList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    interface OnCountrySelectedListener {
        fun onCountrySelected(country: Country)
    }
}