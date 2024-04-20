package com.devtoochi.blood_donation.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.DonationManager.getDonations
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getPersonalDetails
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.backend.utils.Util.updateSharedPreferences
import com.devtoochi.blood_donation.databinding.FragmentDonorProfileBinding
import com.devtoochi.blood_donation.ui.activities.LoginActivity
import com.devtoochi.blood_donation.ui.dialogs.ContactInfoDialogFragment
import com.devtoochi.blood_donation.ui.dialogs.DonorProfileEditProfileDialogFragment
import com.squareup.picasso.Picasso


class DonorProfileFragment : Fragment() {

    private lateinit var binding: FragmentDonorProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isAvailable = true
    private var profileId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDonorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        handleViewsClick()
    }

    private fun initData() {
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        getInfo()
        with(sharedPreferences) {
            isAvailable = getBoolean("is_available", true)
            profileId = getString("id", "")
            val firstname = getString("firstname", "")
            val lastname = getString("lastname", "")
            val imageUrl = getString("image_url", "")
            val name = "$firstname $lastname"

            binding.nameTextview.text = name
            binding.bloodGroupTextview.text = getString("blood_group", "")

            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get().load(imageUrl).into(binding.imageview)
            } else {
                binding.imageview.setColorFilter(
                    Color.WHITE,
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
            }

            val lastDonation = getString("last_donation", "")
            binding.lastDonationTextview.text =
                if (lastDonation.isNullOrEmpty()) {
                    "Last donation: NIL"
                } else {
                    "Last donation: ${
                        Util.dateFormatter(
                            lastDonation
                        )
                    }"
                }

            binding.availableSwitchButton.isChecked = isAvailable

            livesSaved()
        }
    }

    private fun handleViewsClick() {
        binding.editProfileButton.setOnClickListener {
            DonorProfileEditProfileDialogFragment().show(
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

        binding.availableSwitchButton.setOnClickListener {
            updateAvailability(isAvailable = binding.availableSwitchButton.isChecked)
        }

        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.red)
            setOnRefreshListener {
                initData()
                isRefreshing = false
            }
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun updateAvailability(isAvailable: Boolean) {
        if (profileId != null) {
            PersonDetailsManager.updatePersonalDetails(
                data = hashMapOf("available" to isAvailable),
                userType = DONOR,
                profileId = profileId!!,
            ) { success, _ ->
                if (success) {
                    sharedPreferences.edit().putBoolean("is_available", isAvailable).apply()
                }
            }
        }
    }

    private fun getInfo() {
        getPersonalDetails(userType = DONOR, userId = "${auth.currentUser?.uid}") { user, _ ->
            user?.let {
                updateSharedPreferences(user = user, userType = DONOR, sharedPreferences)
            }
        }
    }

    private fun livesSaved() {
        try {
            getDonations { donations, _ ->
                val count = donations?.size!!
                binding.donationsTextview.text = count.toString()
                binding.livesSavedTextview.text = count.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun logout() {
        AuthenticationManager.googleSignInClient(requireContext()).signOut()
        sharedPreferences.edit().clear().apply()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }
}