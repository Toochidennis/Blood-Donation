package com.devtoochi.blood_donation.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.ui.activities.BloodBankRequestActivity
import com.devtoochi.blood_donation.databinding.FragmentRequestBinding


class RequestFragment : Fragment() {

    private lateinit var binding: FragmentRequestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.postBloodRequestButton.setOnClickListener {
            PostBloodRequestDialogFragment {

            }.show(parentFragmentManager, getString(R.string.post_blood_request))
        }

        binding.bloodBankButton.setOnClickListener {
            startActivity(Intent(requireContext(), BloodBankRequestActivity::class.java))
        }
    }


}