package com.devtoochi.blood_donation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.databinding.FragmentHospitalProfileBinding
import com.devtoochi.blood_donation.dialogs.ContactInfoDialogFragment
import com.devtoochi.blood_donation.dialogs.HospitalEditProfileDialogFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HospitalProfileFragment : Fragment() {

    private lateinit var binding: FragmentHospitalProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHospitalProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleButtonClicks()

    }

    private fun handleButtonClicks() {
        binding.editProfileButton.setOnClickListener {
            HospitalEditProfileDialogFragment().show(
                parentFragmentManager,
                getString(R.string.edit_profile)
            )
        }

        binding.contactInfoButton.setOnClickListener {
            ContactInfoDialogFragment().show(
                parentFragmentManager,
                getString(R.string.contact_info)
            )
        }
    }
}