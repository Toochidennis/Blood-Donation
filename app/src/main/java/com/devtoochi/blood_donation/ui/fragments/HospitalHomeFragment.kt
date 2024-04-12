package com.devtoochi.blood_donation.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.backend.utils.Util.getGreetingMessage
import com.devtoochi.blood_donation.databinding.FragmentHospitalHomeBinding
import com.devtoochi.blood_donation.ui.dialogs.WelcomeDialog
import com.squareup.picasso.Picasso


class HospitalHomeFragment : Fragment() {

    private lateinit var binding: FragmentHospitalHomeBinding

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

        initData()
    }

    private fun initData() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPreferences) {
            val name = getString("name", "")
            val photoUrl = getString("image_url", "")
            val city = getString("city", "")
            val bloodGroup = getString("blood_group", "")

            binding.greetingsTextview.text = getGreetingMessage(requireContext())
            binding.usernameTextview.text = name

            if (!photoUrl.isNullOrEmpty()) {
                Picasso.get().load(photoUrl).into(binding.imageview)
            } else {
                binding.imageview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            }

            if (city.isNullOrEmpty() || bloodGroup.isNullOrEmpty()) {
                WelcomeDialog(requireContext(), parentFragmentManager).show()
            }
        }
    }

}