package com.devtoochi.blood_donation.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.ui.adapters.BloodGroupAdapter
import com.devtoochi.blood_donation.backend.models.BloodGroup
import com.devtoochi.blood_donation.databinding.FragmentHospitalBloodGroupsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class HospitalBloodGroupsBottomSheetFragment(
    private val bloodGroupMap: HashMap<String, String>,
    private val onSelected: (HashMap<String, String>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentHospitalBloodGroupsBottomSheetBinding
    private var selectedItems = hashMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHospitalBloodGroupsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

        handleViewClick()
    }

    private fun initData() {
        setupAdapter()
    }

    private fun setupAdapter() {
        val bloodGroups = resources.getStringArray(R.array.blood_groups).toMutableList()
        val newBloodGroups = mutableListOf<BloodGroup>()

        bloodGroups.forEachIndexed { index, bloodGroup ->
            if (bloodGroup.isNotEmpty())
                newBloodGroups.add(BloodGroup((index + 1).toString(), bloodGroup))
        }

        selectedItems = bloodGroupMap

        newBloodGroups.forEach {
            val skillName = selectedItems[it.id]

            if (skillName != null) {
                it.isSelected = true
            }
        }

        newBloodGroups.sortBy { it.name }

        val skillsAdapter = BloodGroupAdapter(newBloodGroups, selectedItems)

        binding.bloodGroupRecyclerview.apply {
            hasFixedSize()
            adapter = skillsAdapter
        }
    }

    private fun handleViewClick() {
        binding.saveButton.setOnClickListener {
            onSelected.invoke(selectedItems)
        }
    }

}