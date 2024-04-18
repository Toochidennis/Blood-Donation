package com.devtoochi.blood_donation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.DonationManager.getDonations
import com.devtoochi.blood_donation.backend.models.Donation
import com.devtoochi.blood_donation.databinding.FragmentHospitalDonationHistoryBinding
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog

class HospitalDonationHistoryFragment : Fragment() {

    private lateinit var binding: FragmentHospitalDonationHistoryBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHospitalDonationHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        getDonationsHistory()
    }

    private fun getDonationsHistory() {
        try {
            loadingDialog.show()
            getDonations { donations, message ->
                if (donations != null) {
                    setupAdapter(donations.toMutableList())
                    loadingDialog.dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("response", "$message")
                    loadingDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun setupAdapter(donations: MutableList<Donation>) {
        val donationAdapter = GenericAdapter(
            itemList = donations,
            itemResLayout = R.layout.item_fragment_hospital_donation_history,
            bindItem = { binding, model ->
                binding.setVariable(BR.donation, model)
                binding.executePendingBindings()
            }
        ) {}

        binding.donationHistoryRecyclerview.apply {
            hasFixedSize()
            adapter = donationAdapter
        }
    }

}