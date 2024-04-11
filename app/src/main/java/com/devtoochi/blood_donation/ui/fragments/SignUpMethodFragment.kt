package com.devtoochi.blood_donation.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.databinding.FragmentSignUpMethodBinding



class SignUpMethodFragment : Fragment() {

    private lateinit var binding: FragmentSignUpMethodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignUpMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.navigateBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.hospitalButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpMethodFragment_to_hospitalSignUpFragment)
        }

        binding.donorButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpMethodFragment_to_donorSignUpFragment)
        }
    }


}