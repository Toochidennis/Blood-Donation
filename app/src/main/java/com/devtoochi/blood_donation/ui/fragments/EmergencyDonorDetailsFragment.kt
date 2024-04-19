package com.devtoochi.blood_donation.ui.fragments

import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.RequestsManager.postBloodRequest
import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.backend.utils.Util.dateFormatter
import com.devtoochi.blood_donation.databinding.FragmentEmergencyDonorDetailsBinding
import com.devtoochi.blood_donation.ui.dialogs.DatePickerDialog
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.devtoochi.blood_donation.ui.dialogs.RequestSentDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import java.util.Calendar


class EmergencyDonorDetailsFragment(private val donor: Donor) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentEmergencyDonorDetailsBinding
    private lateinit var loadingDialog: LoadingDialog
    private var time: String? = null
    private var date: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEmergencyDonorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())
        initData()
        handleViewsClick()
    }

    private fun initData() {
        val name = "${donor.firstname} ${donor.lastname}"
        binding.nameTextview.text = name
        binding.addressTextview.text = donor.address
        binding.genderTextview.text = donor.gender
        binding.ageTextview.text = getAge(donor.birthDate)
        binding.bloodGroupTextview.text = donor.bloodGroup
        binding.recentDonationTextview.text = if (donor.recentDonation.isEmpty()) {
            "Last donation: NIL"
        } else {
            "Last donation: ${dateFormatter(donor.recentDonation)}"
        }

        if (donor.imageUrl.isEmpty()) {
            binding.imageview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        } else {
            Picasso.get().load(donor.imageUrl).into(binding.imageview)
        }
    }

    private fun getAge(birthDate: String): String {
        return try {
            val calendar = Calendar.getInstance()
            val currentYear = calendar[Calendar.YEAR]
            val splitDate = birthDate.split('-')
            val year = splitDate[0].toInt()

            "${currentYear - year} yrs"
        } catch (e: Exception) {
            e.printStackTrace()
            "NIL"
        }
    }

    private fun handleViewsClick() {
        binding.dateTextInput.setOnClickListener {
            DatePickerDialog(requireContext()) { selectedDate ->
                date = selectedDate
                binding.dateTextInput.setText(dateFormatter(selectedDate))
            }.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        binding.timeTextInput.setOnClickListener {
            showTimePickerDialog()
        }

        binding.sendRequestButton.setOnClickListener {
            sendRequest()
        }
    }

    private fun showTimePickerDialog() {
        // Get the current time
        val currentTime = Calendar.getInstance()

        // Create a TimePickerDialog with current time as the default time
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // Format the selected time to display in either AM or PM format
                time = "$hourOfDay:$minute"
                val formattedTime = Util.formatTime(hourOfDay, minute)

                // Show the formatted time in the TextView
                binding.timeTextInput.setText(formattedTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY), // Initial hour
            currentTime.get(Calendar.MINUTE), // Initial minute
            false // 24-hour format
        )

        // Show the TimePickerDialog
        timePickerDialog.show()
    }

    private fun sendRequest() {
        try {
            if (isValidForm()) {
                loadingDialog.show()
                postBloodRequest(
                    BloodRequest(
                        userId = "${auth.currentUser?.uid}",
                        donorId = donor.userId,
                        requestType = DONOR,
                        bloodGroup = donor.bloodGroup,
                        note = binding.noteTextInput.text.toString().trim(),
                        requestDate = "$date:$time",
                    )
                ) { success, message ->
                    if (success) {
                        RequestSentDialog(requireContext()) {
                            dismiss()
                        }.show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong please try again",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("response", "$message")
                    }
                    loadingDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun isValidForm(): Boolean {
        return when {
            time.isNullOrEmpty() -> {
                binding.timeTextInput.error = "Time is required"
                false
            }

            date.isNullOrEmpty() -> {
                binding.dateTextInput.error = "Date is required"
                false
            }

            binding.noteTextInput.text.isBlank() -> {
                binding.noteTextInput.error = "Note is required"
                false
            }

            else -> {
                binding.timeTextInput.error = null
                binding.dateTextInput.error = null
                binding.noteTextInput.error = null
                true
            }
        }
    }

}