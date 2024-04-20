package com.devtoochi.blood_donation.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
        refreshRequests()
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
        loadingDialog.show()
        getBloodRequestById("$userId") { bloodRequests, message ->
            binding.emptyTextview.isVisible = false
            bloodRequests?.let {
                val processedRequests = mutableListOf<BloodRequest>()
                val requestsCount = bloodRequests.size
                var processedCount = 0
                bloodRequests.forEach { bloodRequest ->
                    processRequestData(bloodRequest) { processedRequest ->
                        processedRequests.add(processedRequest)
                        processedCount++
                        if (processedCount == requestsCount) {
                            setUpAdapter(processedRequests)
                        }
                    }
                }
            } ?: handleGetRequestsError(message)
            loadingDialog.dismiss()
        }
    }

    private fun processRequestData(data: BloodRequest, callback: (BloodRequest) -> Unit) {
        val bloodGroup = data.bloodGroup
        getUsername(data.donorId, data.requestType) { username ->
            data.bloodGroup = when (data.requestType) {
                DONOR -> "You urgently requested $bloodGroup blood from $username"
                HOSPITAL -> "You requested $bloodGroup blood from $username"
                else -> "You requested for $bloodGroup blood"
            }
            data.note = "Requested on ${dateFormat.format(data.datePosted)}"
            callback.invoke(data)
        }
    }

    private fun getUsername(userId: String, userType: String, callback: (String) -> Unit) {
        getPersonalDetails(userType = userType, userId = userId) { user, _ ->
            val name = when (user) {
                is Hospital -> user.name
                is Donor -> "${user.firstname} ${user.lastname}"
                else -> "Unknown name"
            }
            callback.invoke(name)
        }
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

    private fun handleGetRequestsError(message: String?) {
        Log.d("response", "Failed to get donors: $message")
        binding.emptyTextview.isVisible = true
    }

    private fun refreshRequests() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.red)
            setOnRefreshListener {
                getRequestHistory()
                isRefreshing = false
            }
        }
    }
}