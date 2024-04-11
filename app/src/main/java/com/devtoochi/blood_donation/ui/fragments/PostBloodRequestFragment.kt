package com.devtoochi.blood_donation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.databinding.FragmentPostBloodRequestBinding
import com.devtoochi.blood_donation.ui.dialogs.HospitalBloodGroupsBottomSheetFragment
import com.devtoochi.blood_donation.ui.dialogs.RequestSentDialog


class PostBloodRequestDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentPostBloodRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPostBloodRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleViewClick()
    }

    private fun handleViewClick() {
        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.bloodGroupTextInput.setOnClickListener {
            HospitalBloodGroupsBottomSheetFragment {

            }.show(parentFragmentManager, getString(R.string.blood_group))
        }

        binding.postRequestButton.setOnClickListener {
            RequestSentDialog(requireContext()).show()
        }
    }
}