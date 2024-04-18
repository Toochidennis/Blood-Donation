package com.devtoochi.blood_donation.ui.dialogs

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager
import com.devtoochi.blood_donation.backend.firebase.DonationManager
import com.devtoochi.blood_donation.backend.firebase.RequestsManager
import com.devtoochi.blood_donation.backend.models.Donation
import com.devtoochi.blood_donation.backend.models.DonationRequest
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.databinding.FragmentDonorBloodRequestDetailsBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso


class DonorBloodRequestDetailsBottomFragment(
    private val donationRequest: DonationRequest
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDonorBloodRequestDetailsBottomBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDonorBloodRequestDetailsBottomBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        initData()
        handleViewsClick()
    }

    private fun initData() {
        binding.nameTextview.text = donationRequest.name
        binding.bloodGroupTextview.text = donationRequest.bloodGroup
        binding.stateTextview.text = donationRequest.state
        binding.addressTextview.text = donationRequest.address
        binding.dateTextview.text = donationRequest.requestDate
        binding.noteTextview.text = donationRequest.note

        if (donationRequest.imageUrl.isNotEmpty()) {
            Picasso.get().load(donationRequest.imageUrl).into(binding.imageview)
        } else {
            binding.imageview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }

        try {
            DonationManager.checkIfDonationExists(donationRequest.requestId) { exists, feedback ->
                if (exists) {
                    binding.donateButton.isEnabled = false
                } else if (feedback == "Empty") {
                    binding.donateButton.isEnabled = true
                } else {
                    Log.d("response", "$feedback")
                    binding.donateButton.isEnabled = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleViewsClick() {
        binding.donateButton.setOnClickListener {
            CheckDonorEligibilityDialogFragment { isEligible ->
                if (isEligible) {
                    donate()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "You're not eligible to donate blood",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.show(
                parentFragmentManager,
                getString(R.string.check_eligibility)
            )
        }
    }

    private fun donate() {
        val donorId =
            donationRequest.donorId.ifEmpty { "${AuthenticationManager.auth.currentUser?.uid}" }
        try {
            loadingDialog.show()
            DonationManager.createDonations(
                Donation(
                    donorId = donorId,
                    receiverId = donationRequest.userId,
                    requestId = donationRequest.requestId,
                    receiverAddress = donationRequest.address,
                    receiverName = donationRequest.name,
                    bloodGroup = donationRequest.bloodGroup,
                    receiverImageUrl = donationRequest.imageUrl
                )
            ) { success, message ->
                if (success) {
                    updateRequest()
                } else {
                    loadingDialog.dismiss()
                    Log.d("response", "$message")
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun updateRequest() {
        RequestsManager.updateBloodRequest(
            data = hashMapOf("status" to Constants.FULFILLED),
            requestId = donationRequest.requestId
        ) { success, message ->
            if (success) {
                loadingDialog.dismiss()
                dismiss()
                DonationSuccessDialog(
                    requireContext(),
                    parentFragmentManager,
                    donationRequest,
                    Constants.DONOR
                ).show()
            } else {
                loadingDialog.dismiss()
                Log.d("response", "$message")
            }
        }
    }

}