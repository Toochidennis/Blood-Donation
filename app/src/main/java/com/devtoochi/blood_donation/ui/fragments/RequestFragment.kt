package com.devtoochi.blood_donation.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getPersonalDetails
import com.devtoochi.blood_donation.backend.firebase.RequestsManager.getBloodRequestById
import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.databinding.FragmentRequestBinding
import com.devtoochi.blood_donation.ui.activities.BloodBankRequestActivity
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import java.text.SimpleDateFormat
import java.util.Locale


class RequestFragment : Fragment() {

    private lateinit var binding: FragmentRequestBinding
    private lateinit var loadingDialog: LoadingDialog
    private var dateFormat: SimpleDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    private var userId: String? = null

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

        val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", "")

        loadingDialog = LoadingDialog(requireContext())

        handleViewsClick()

        getRequestHistory()
    }

    private fun handleViewsClick() {
        binding.postBloodRequestButton.setOnClickListener {
            PostBloodRequestDialogFragment().show(
                parentFragmentManager,
                getString(R.string.post_blood_request)
            )
        }

        binding.bloodBankButton.setOnClickListener {
            startActivity(Intent(requireContext(), BloodBankRequestActivity::class.java))
        }

        binding.emergencyDonorsButton.setOnClickListener {
            EmergencyDonorsDialogFragment().show(
                parentFragmentManager,
                getString(R.string.emergency_donors)
            )
        }
    }

    private fun getRequestHistory() {
        try {
            loadingDialog.show()
            getBloodRequestById("$userId") { bloodRequests, message ->
                if (bloodRequests != null) {
                    loadingDialog.dismiss()
                    bloodRequests.forEach { bloodRequest ->
                        processRequestData(bloodRequest)
                    }
                    setUpAdapter(bloodRequests)
                } else {
                    loadingDialog.dismiss()
                    Log.d("response", "$message")
                }
            }
        } catch (e: Exception) {
            loadingDialog.dismiss()
            e.printStackTrace()
        }
    }

    private fun processRequestData(data: BloodRequest) {
        val bloodGroup = data.bloodGroup
        data.bloodGroup = when (data.requestType) {
            DONOR -> "You urgently requested $bloodGroup blood from ${
                getUsername(
                    data.donorId,
                    data.requestType
                )
            }"

            HOSPITAL -> "You requested $bloodGroup blood from ${
                getUsername(
                    data.donorId,
                    data.requestType
                )
            }"

            else -> "You requested for $bloodGroup blood"
        }
        data.note = "Requested on ${dateFormat.format(data.datePosted)}"
    }

    private fun getUsername(userId: String, userType: String): String {
        var name = ""
        getPersonalDetails(userType = userType, userId = userId) { user, _ ->
            if (user != null) {
                name = when (user) {
                    is Hospital -> user.name
                    is Donor -> "${user.firstname} ${user.lastname}"
                    else -> "Unknown name"
                }
            }
        }

        return name
    }

    private fun setUpAdapter(bloodRequest: List<BloodRequest>) {
        val requestAdapter = GenericAdapter(
            itemList = bloodRequest.toMutableList(),
            itemResLayout = R.layout.item_fragment_request,
            bindItem = { binding, model ->
                binding.setVariable(BR.request, model)
                binding.executePendingBindings()
            }
        ) {}

        binding.requestHistoryRecyclerview.apply {
            hasFixedSize()
            adapter = requestAdapter
        }
    }

}