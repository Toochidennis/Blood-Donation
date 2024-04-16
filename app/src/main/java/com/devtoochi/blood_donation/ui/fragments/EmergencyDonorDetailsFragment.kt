package com.devtoochi.blood_donation.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager
import com.devtoochi.blood_donation.backend.firebase.RequestsManager
import com.devtoochi.blood_donation.backend.models.BloodGroup
import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.databinding.FragmentEmergencyDonorDetailsBinding
import com.devtoochi.blood_donation.ui.adapters.BloodGroupAdapter2
import com.devtoochi.blood_donation.ui.dialogs.DatePickerDialog
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.devtoochi.blood_donation.ui.dialogs.RequestSentDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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

    }

    private fun handleViewsClick() {
      /*  binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.dateTextInput.setOnClickListener {
            DatePickerDialog(requireContext()) { selectedDate ->
                date = selectedDate
                binding.dateTextInput.setText(Util.dateFormatter(selectedDate))
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
        }*/
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
               // binding.timeTextInput.setText(formattedTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY), // Initial hour
            currentTime.get(Calendar.MINUTE), // Initial minute
            false // 24-hour format
        )

        // Show the TimePickerDialog
        timePickerDialog.show()
    }



/*    private fun sendRequest() {
        try {
            if (isValidForm()) {
                loadingDialog.show()
                RequestsManager.postBloodRequest(
                    BloodRequest(
                        userId = "${AuthenticationManager.auth.currentUser?.uid}",
                        donorId = bloodBankDetails?.userId!!,
                        requestType = Constants.HOSPITAL,
                        bloodGroup = bloodGroup.name,
                        note = binding.noteTextInput.text.toString().trim(),
                        requestDate = "$date:$time",
                    )
                ) { success, message ->
                    if (success) {
                        loadingDialog.dismiss()
                        RequestSentDialog(requireContext()) {
                            findNavController().popBackStack()
                        }.show()
                    } else {
                        loadingDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong please try again",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("response", "$message")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }*/



}