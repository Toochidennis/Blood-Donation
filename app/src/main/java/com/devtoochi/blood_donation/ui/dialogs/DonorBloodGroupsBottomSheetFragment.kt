package com.devtoochi.blood_donation.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.backend.models.BloodGroup
import com.devtoochi.blood_donation.databinding.FragmentDonorBloodGroupsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class DonorBloodGroupsBottomSheetFragment(
    private val onSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDonorBloodGroupsBottomSheetBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDonorBloodGroupsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareData()

    }

    private fun prepareData() {
        val bloodGroups = resources.getStringArray(R.array.blood_groups).toMutableList()
        val newBloodGroups = mutableListOf<BloodGroup>()

        bloodGroups.forEachIndexed { index, bloodGroup ->
            newBloodGroups.add(BloodGroup("${index + 1}", bloodGroup))
        }

        setUpAdapter(bloodGroups = newBloodGroups)

    }

    private fun setUpAdapter(bloodGroups: MutableList<BloodGroup>) {
        val bloodGroupAdapter = GenericAdapter(
            itemList = bloodGroups,
            itemResLayout = R.layout.item_fragment_donor_blood_groups_bottom_sheet,
            bindItem = { binding, model ->
                binding.setVariable(BR.blood, model)
                binding.executePendingBindings()
            }
        ) { position ->
            onSelected.invoke(bloodGroups[position].name)
            dismiss()
        }

        binding.bloodGroupRecyclerview.apply {
            hasFixedSize()
            adapter = bloodGroupAdapter
        }
    }


}