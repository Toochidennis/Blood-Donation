package com.devtoochi.blood_donation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.databinding.FragmentHospitalHomeBinding
import com.devtoochi.blood_donation.ui.dialogs.WelcomeDialog


class HospitalHomeFragment : Fragment() {

    private lateinit var binding:FragmentHospitalHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHospitalHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WelcomeDialog(requireContext(), parentFragmentManager).show()

    }


}