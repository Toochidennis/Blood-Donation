package com.devtoochi.blood_donation.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devtoochi.blood_donation.databinding.FragmentGenderBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textview.MaterialTextView

class GenderBottomSheetFragment(
    private val onSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGenderBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGenderBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView1.setOnClickListenerWithText()
        binding.textView2.setOnClickListenerWithText()

    }
    private fun View.setOnClickListenerWithText() {
        if (this is MaterialTextView) {
            this.setOnClickListener {
                onSelected.invoke(this.text.toString())
                dismiss()
            }
        }
    }

}