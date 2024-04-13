package com.devtoochi.blood_donation.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getHospitalPersonalDetails
import com.devtoochi.blood_donation.backend.firebase.RequestsManager.getAllBloodRequests
import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.models.DonationRequest
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.backend.utils.Util.dateFormatter
import com.devtoochi.blood_donation.backend.utils.Util.formatTime
import com.devtoochi.blood_donation.backend.utils.Util.getGreetingMessage
import com.devtoochi.blood_donation.databinding.FragmentHospitalHomeBinding
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.devtoochi.blood_donation.ui.dialogs.WelcomeDialog
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale


class HospitalHomeFragment : Fragment() {

    private lateinit var binding: FragmentHospitalHomeBinding
    private lateinit var loadingDialog: LoadingDialog

    private val donationRequests = mutableListOf<DonationRequest>()
    private var userId: String? = null
    private var dateFormat: SimpleDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

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

        val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", "")

        loadingDialog = LoadingDialog(requireContext())

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

        getDonationRequests()
    }

    private fun getDonationRequests() {
        try {
            donationRequests.clear()
            loadingDialog.show()
            getAllBloodRequests(userId = "$userId") { bloodRequests, message ->

                if (bloodRequests != null) {
                    var requestsProcessed = 0 // Counter to track processed requests
                    val totalRequests = bloodRequests.size // Total number of requests

                    bloodRequests.forEach { bloodRequest ->
                        Log.d("response", "looping $bloodRequest")
                        getSenderPersonalDetails(bloodRequest) {
                            requestsProcessed++
                            if (requestsProcessed == totalRequests) {
                                // All requests processed, setup adapter
                                Log.d("response", "adapter reached")
                                setupAdapter()
                                Log.d("response", "adapter passed")
                                loadingDialog.dismiss()
                            }
                        }
                    }
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

    private fun getSenderPersonalDetails(bloodRequest: BloodRequest, onComplete: () -> Unit) {
        getHospitalPersonalDetails(
            userId = bloodRequest.userId,
        ) { hospital, message ->
            Log.d("response", "hospital $hospital $message")
            if (hospital != null) {
                Log.d("response", "hospital $hospital")
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
                    )
                )
            } else {
                Log.d("response", "$message")
            }

            onComplete.invoke()
        }
    }

    private fun formatRequestDate(requestDate: String): String {
        return try {
            val splitDate = requestDate.split(":")
            val date = splitDate[0]
            val hour = splitDate[1].toInt()
            val minutes = splitDate[2].toInt()

            val formattedDate = dateFormatter(date)
            val formattedTime = formatTime(hour, minutes)

            "$formattedDate $formattedTime"
        } catch (e: Exception) {
            Log.e("formatRequestDate", "Error formatting request date: $requestDate", e)
            requestDate // Return the original string in case of an error
        }
    }

    private fun setupAdapter() {
        Log.d("response", "adapter entered")
        Log.d("response", "$donationRequests")
        val donationAdapter = GenericAdapter(
            itemList = donationRequests,
            itemResLayout = R.layout.item_fragment_hospital_home,
            bindItem = { binding, model ->
                binding.setVariable(BR.request, model)
                binding.executePendingBindings()
                Log.d("response", "adapter executing")
            }
        ) {}

        binding.donationRequestRecyclerview.apply {
            hasFixedSize()
            adapter = donationAdapter
        }
    }

}