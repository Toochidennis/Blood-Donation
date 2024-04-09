package com.devtoochi.blood_donation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.adapters.GenericAdapter
import com.devtoochi.blood_donation.backend.models.State
import com.devtoochi.blood_donation.databinding.FragmentStatesBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class StatesBottomSheetFragment(
    private val onSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentStatesBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStatesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareData()
    }

    private fun prepareData() {
        val bloodGroups = resources.getStringArray(R.array.nigeria_states).toMutableList()
        val newStates = mutableListOf<State>()

        bloodGroups.forEach { state ->
            newStates.add(State(state))
        }

        setUpAdapter(states = newStates)
    }

    private fun setUpAdapter(states: MutableList<State>) {
        val bloodGroupAdapter = GenericAdapter(
            itemList = states,
            itemResLayout = R.layout.item_fragment_states_bottom_sheet,
            bindItem = { binding, model ->
                binding.setVariable(BR.blood, model)
                binding.executePendingBindings()
            }
        ) { position ->
            onSelected.invoke(states[position].name)
            dismiss()
        }

        binding.statesRecyclerview.apply {
            hasFixedSize()
            adapter = bloodGroupAdapter
        }
    }
}