package com.devtoochi.blood_donation.ui.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.models.Country
import com.devtoochi.blood_donation.databinding.FragmentCountriesBottomSheetBinding
import com.devtoochi.blood_donation.ui.adapters.CountryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CountriesBottomSheetFragment(
    private val onCountrySelected: (String) -> Unit
) : BottomSheetDialogFragment(), CountryAdapter.OnCountrySelectedListener {

    private lateinit var binding: FragmentCountriesBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCountriesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val countries = resources.getStringArray(R.array.countries_array).toMutableList()
        val itemList = mutableListOf<Country>()

        countries.forEachIndexed { index, country ->
            itemList.add(Country("$index", country))
        }

        val countryAdapter = CountryAdapter(itemList, this)
        binding.countriesRecyclerview.apply {
            hasFixedSize()
            adapter = countryAdapter
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the adapter when text changes
                countryAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
    }

    override fun onCountrySelected(country: Country) {
        onCountrySelected.invoke(country.name)
        dismiss()
    }

}