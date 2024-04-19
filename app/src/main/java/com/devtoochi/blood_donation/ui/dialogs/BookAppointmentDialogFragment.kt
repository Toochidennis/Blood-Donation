package com.devtoochi.blood_donation.ui.dialogs

import android.app.TimePickerDialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AppointmentManager.bookAppointment
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.updatePersonalDetails
import com.devtoochi.blood_donation.backend.models.Appointment
import com.devtoochi.blood_donation.backend.models.DonationRequest
import com.devtoochi.blood_donation.backend.models.Notification
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.backend.utils.Util.dateFormatter
import com.devtoochi.blood_donation.backend.utils.Util.sendNotification
import com.devtoochi.blood_donation.databinding.FragmentBookAppointmentDialogBinding
import java.util.Calendar


class BookAppointmentDialogFragment(
    private val donationRequest: DonationRequest,
    private val userType: String
) : DialogFragment() {

    private lateinit var binding: FragmentBookAppointmentDialogBinding
    private lateinit var loadingDialog: LoadingDialog

    private var date: String? = null
    private var time: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBookAppointmentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.dateTextInput.setOnClickListener {
            DatePickerDialog(requireContext()) { selectedDate ->
                date = selectedDate
                binding.dateTextInput.setText(dateFormatter(selectedDate))
            }.window?.setLayout(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        binding.timeTextInput.setOnClickListener {
            showTimePickerDialog()
        }

        binding.scheduleAppointmentButton.setOnClickListener {
            scheduleAppointment()
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


    private fun scheduleAppointment() {
        try {
            when {
                time.isNullOrEmpty() -> {
                    binding.timeTextInput.error = "Time is required"
                }

                date.isNullOrEmpty() -> {
                    binding.dateTextInput.error = "Date is required"
                }

                else -> {
                    binding.timeTextInput.error = null
                    binding.dateTextInput.error = null

                    loadingDialog.show()

                    bookAppointment(
                        Appointment(
                            donorId = donationRequest.donorId,
                            receiverId = donationRequest.userId,
                            requestId = donationRequest.requestId,
                            appointmentDate = "$date:$time",
                            userType = userType
                        )
                    ) { success, message ->
                        if (success) {
                            updateInfo()
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateInfo() {
        val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val profileId = sharedPreferences.getString("id", "")

        updatePersonalDetails(
            data = hashMapOf("recentDonation" to getDate()),
            userType = userType,
            profileId = "$profileId"
        ) { success, message ->
            if (success) {
                sendNotification(
                    requireContext(),
                    Notification(
                        token = donationRequest.token,
                        title = "Donation Request Accepted!",
                        body = getString(R.string.request_accepted)
                    ),
                ) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Appointment booked successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            } else {
                loadingDialog.dismiss()
                Log.d("response", "$message")
            }
        }
    }

    private fun getDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH] + 1
        val day = calendar[Calendar.DAY_OF_MONTH]

        return "$year-$month-$day"
    }
}