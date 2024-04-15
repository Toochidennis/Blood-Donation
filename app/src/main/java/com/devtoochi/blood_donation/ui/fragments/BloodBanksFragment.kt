package com.devtoochi.blood_donation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getAllHospitalDetails
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.databinding.FragmentBloodBanksBinding
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog


class BloodBanksFragment : Fragment() {

    private lateinit var binding: FragmentBloodBanksBinding
    private lateinit var loadingDialog: LoadingDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBloodBanksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        getBloodBanks()

        binding.navigateUp.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getBloodBanks() {
        loadingDialog.show()
        try {
            getAllHospitalDetails { hospitals, message ->
                if (hospitals != null) {
                    setupAdapter(hospitals = hospitals)
                    loadingDialog.dismiss()
                } else {
                    loadingDialog.dismiss()
                    Log.d("response", "$message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun setupAdapter(hospitals: List<Hospital>) {
        val bloodBankAdapter = GenericAdapter(
            itemList = hospitals.toMutableList(),
            itemResLayout = R.layout.item_fragment_blood_banks,
            bindItem = { binding, model ->
                binding.setVariable(BR.hospital, model)
                binding.executePendingBindings()
            }
        ) {

        }

        binding.bloodBankRecyclerview.apply {
            hasFixedSize()
            adapter = bloodBankAdapter
        }
    }

}