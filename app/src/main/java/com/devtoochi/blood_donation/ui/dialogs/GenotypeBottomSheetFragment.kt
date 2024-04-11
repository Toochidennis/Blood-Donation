package com.devtoochi.blood_donation.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.backend.models.Genotype
import com.devtoochi.blood_donation.databinding.FragmentGenotypeBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class GenotypeBottomSheetFragment(
    private val onSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGenotypeBottomSheetBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGenotypeBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareData()
    }

    private fun prepareData() {
        val bloodGroups = resources.getStringArray(R.array.genotypes).toMutableList()
        val newGenotype = mutableListOf<Genotype>()

        bloodGroups.forEach { genotype ->
            newGenotype.add(Genotype(genotype))
        }

        setUpAdapter(genotypes = newGenotype)
    }

    private fun setUpAdapter(genotypes: MutableList<Genotype>) {
        val bloodGroupAdapter = GenericAdapter(
            itemList = genotypes,
            itemResLayout = R.layout.item_fragment_genotype_bottom_sheet,
            bindItem = { binding, model ->
                binding.setVariable(BR.genotype, model)
                binding.executePendingBindings()
            }
        ) { position ->
            onSelected.invoke(genotypes[position].name)
            dismiss()
        }

        binding.genotypeRecyclerview.apply {
            hasFixedSize()
            adapter = bloodGroupAdapter
        }
    }

}