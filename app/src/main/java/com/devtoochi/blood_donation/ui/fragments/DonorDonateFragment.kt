package com.devtoochi.blood_donation.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getPersonalDetails
import com.devtoochi.blood_donation.backend.firebase.RequestsManager.getAllBloodRequests
import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.models.DonationRequest
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.databinding.FragmentDonorDonateBinding
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.ui.dialogs.DonorBloodRequestDetailsBottomFragment
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale


class DonorDonateFragment : Fragment() {

    private lateinit var binding: FragmentDonorDonateBinding
    private lateinit var loadingDialog: LoadingDialog
    private val picasso = Picasso.get()

    private val donationRequests = mutableListOf<DonationRequest>()
    private var userId: String? = null
    private var dateFormat: SimpleDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDonorDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", "")


        getDonationRequests()
        refreshRequests()
    }

    private fun getDonationRequests() {
        try {
            donationRequests.clear()
            loadingDialog.show()
            getAllBloodRequests(userId = "$userId") { bloodRequests, message ->
                bloodRequests?.let {
                    binding.emptyTextview.isVisible = false
                    var requestsProcessed = 0 // Counter to track processed requests
                    val totalRequests = bloodRequests.size // Total number of requests

                    bloodRequests.forEach { bloodRequest ->
                        getSenderPersonalDetails(bloodRequest) {
                            requestsProcessed++
                            if (requestsProcessed == totalRequests) {
                                // All requests processed, setup adapter
                                setupAdapter()
                            }
                        }
                    }
                } ?: handleGetHospitalsError(message)

                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            loadingDialog.dismiss()
            e.printStackTrace()
        }
    }

    private fun getSenderPersonalDetails(bloodRequest: BloodRequest, onComplete: () -> Unit) {
        getPersonalDetails(
            userId = bloodRequest.userId,
            userType = Constants.HOSPITAL,
        ) { user, message ->
            val hospital = user as? Hospital
            hospital?.let {
                donationRequests.add(
                    DonationRequest(
                        requestId = bloodRequest.requestId,
                        userId = bloodRequest.userId,
                        donorId = bloodRequest.donorId,
                        name = hospital.name,
                        imageUrl = hospital.imageUrl,
                        address = hospital.address,
                        state = "${hospital.state} - ${hospital.city}",
                        note = bloodRequest.note,
                        bloodGroup = bloodRequest.bloodGroup,
                        unit = bloodRequest.unit,
                        requestDate = formatRequestDate(bloodRequest.requestDate),
                        datePosted = dateFormat.format(bloodRequest.datePosted),
                        token = hospital.token
                    )
                )
            } ?: Log.d("response", "Failed to get donors: $message")

            onComplete.invoke()
        }
    }

    private fun formatRequestDate(requestDate: String): String {
        return try {
            val splitDate = requestDate.split(":")
            val date = splitDate[0]
            val hour = splitDate[1].toInt()
            val minutes = splitDate[2].toInt()

            val formattedDate = Util.dateFormatter(date)
            val formattedTime = Util.formatTime(hour, minutes)

            "$formattedDate $formattedTime"
        } catch (e: Exception) {
            Log.e("formatRequestDate", "Error formatting request date: $requestDate", e)
            requestDate // Return the original string in case of an error
        }
    }

    private fun setupAdapter() {
        val donationAdapter = GenericAdapter(
            itemList = donationRequests,
            itemResLayout = R.layout.item_fragment_hospital_home,
            bindItem = { binding, model ->
                binding.setVariable(BR.request, model)
                binding.executePendingBindings()

                val imageview = binding.root.findViewById<ImageView>(R.id.imageview)
                if (model.imageUrl.isEmpty()) {
                    imageview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                } else {
                    picasso.load(model.imageUrl).into(imageview)
                }
            }
        ) { position ->
            showRequestDetails(donationRequest = donationRequests[position])
        }

        binding.donationRequestRecyclerview.apply {
            hasFixedSize()
            adapter = donationAdapter
        }
    }

    private fun refreshRequests() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.red)
            setOnRefreshListener {
                getDonationRequests()
                isRefreshing = false
            }
        }
    }

    private fun handleGetHospitalsError(message: String?) {
        Log.d("response", "Failed to get donors: $message")
        binding.emptyTextview.isVisible = true
    }

    private fun showRequestDetails(donationRequest: DonationRequest) {
        DonorBloodRequestDetailsBottomFragment(donationRequest)
            .show(parentFragmentManager, getString(R.string.request))
    }

}