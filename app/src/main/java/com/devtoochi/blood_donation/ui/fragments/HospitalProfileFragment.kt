package com.devtoochi.blood_donation.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager
import com.devtoochi.blood_donation.backend.firebase.DonationManager.getDonations
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getPersonalDetails
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.updatePersonalDetails
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.backend.utils.Util.dateFormatter
import com.devtoochi.blood_donation.databinding.FragmentHospitalProfileBinding
import com.devtoochi.blood_donation.ui.activities.LoginActivity
import com.devtoochi.blood_donation.ui.dialogs.ContactInfoDialogFragment
import com.devtoochi.blood_donation.ui.dialogs.HospitalEditProfileDialogFragment
import com.squareup.picasso.Picasso


class HospitalProfileFragment : Fragment() {

    private lateinit var binding: FragmentHospitalProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isAvailable = true
    private var profileId: String? = null

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
        initData()
        handleViewsClick()
    }

    private fun initData() {
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        getInfo()
        with(sharedPreferences) {
            isAvailable = getBoolean("is_available", true)
            profileId = getString("id", "")
            val name = getString("name", "")
            val imageUrl = getString("image_url", "")

            binding.nameTextview.text = name

            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get().load(imageUrl).into(binding.imageview)
            } else {
                binding.imageview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            }

            val lastDonation = getString("last_donation", "")
            binding.lastDonationTextview.text =
                if (lastDonation.isNullOrEmpty()) {
                    "Last donation: NIL"
                } else {
                    "Last donation: ${dateFormatter(lastDonation)}"
                }

            binding.availableSwitchButton.isChecked = isAvailable
            livesSaved()
        }
    }

    private fun handleViewsClick() {
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
            updatePersonalDetails(
                data = hashMapOf("available" to isAvailable),
                userType = HOSPITAL,
                profileId = profileId!!,
            ) { success, _ ->
                if (success) {
                    sharedPreferences.edit().putBoolean("is_available", isAvailable).apply()
                }
            }
        }
    }

    private fun getInfo() {
        getPersonalDetails(
            userType = HOSPITAL,
            userId = "${AuthenticationManager.auth.currentUser?.uid}"
        ) { user, _ ->
            user?.let {
                Util.updateSharedPreferences(
                    user = user,
                    userType = HOSPITAL,
                    sharedPreferences
                )
            }
        }
    }

    private fun livesSaved() {
        try {
            getDonations { donations, _ ->
                donations?.let {
                    binding.donationsTextview.text = it.size.toString()
                    binding.livesSavedTextview.text = it.size.toString()
                } ?: updateLifeLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLifeLine() {
        binding.donationsTextview.text = "0"
        binding.livesSavedTextview.text = "0"
    }

    private fun logout() {
        AuthenticationManager.googleSignInClient(requireContext()).signOut()
        sharedPreferences.edit().clear().apply()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

}