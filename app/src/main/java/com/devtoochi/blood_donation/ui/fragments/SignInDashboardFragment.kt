package com.devtoochi.blood_donation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.databinding.FragmentSignInDashboardBinding


class SignInDashboardFragment : Fragment() {

    private lateinit var binding: FragmentSignInDashboardBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignInDashboardBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_signInDashboardFragment_to_signInFragment)
        }

        binding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_signInDashboardFragment_to_signUpMethodFragment)
        }
    }

}