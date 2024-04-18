package com.devtoochi.blood_donation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.databinding.FragmentDonorAppointmentsBinding
import com.devtoochi.blood_donation.ui.adapters.FragmentAdapter
import com.google.android.material.tabs.TabLayoutMediator


class DonorAppointmentsFragment : Fragment() {

    private lateinit var binding: FragmentDonorAppointmentsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDonorAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewPager()
    }

    private fun setUpViewPager() {
        val fragmentTitles = listOf("Scheduled", "Donated")

        val fragmentAdapter = FragmentAdapter(requireActivity()).apply {
            addFragment(DonorAppointmentHistoryFragment())
            addFragment(DonorDonationHistoryFragment())
        }

        binding.viewPager.adapter = fragmentAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitles[position]
        }.attach()
    }
}