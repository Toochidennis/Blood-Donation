package com.devtoochi.blood_donation.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.RequestsManager.postBloodRequest
import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.utils.Util.dateFormatter
import com.devtoochi.blood_donation.backend.utils.Util.formatTime
import com.devtoochi.blood_donation.databinding.FragmentPostBloodRequestBinding
import com.devtoochi.blood_donation.ui.dialogs.DatePickerDialog
import com.devtoochi.blood_donation.ui.dialogs.DonorBloodGroupsBottomSheetFragment
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.devtoochi.blood_donation.ui.dialogs.RequestSentDialog
import java.util.Calendar


class PostBloodRequestDialogFragment(private val onPosted: () -> Unit) : DialogFragment() {

    private lateinit var binding: FragmentPostBloodRequestBinding
    private lateinit var loadingDialog: LoadingDialog
    private var time: String? = null
    private var date: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPostBloodRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        handleViewClick()
    }

    private fun handleViewClick() {
        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.bloodGroupTextInput.setOnClickListener {
            DonorBloodGroupsBottomSheetFragment { bloodGroup ->
                binding.bloodGroupTextInput.setText(bloodGroup)
            }.show(parentFragmentManager, getString(R.string.blood_group))
        }

        binding.postRequestButton.setOnClickListener {
            postRequest()
        }

        binding.timeInputText.setOnClickListener {
            showTimePickerDialog()
        }

        binding.dateTextInput.setOnClickListener {
            DatePickerDialog(requireContext()) { selectedDate ->
                date = selectedDate
                val formattedDate = dateFormatter(selectedDate)
                binding.dateTextInput.setText(formattedDate)
            }
        }
    }

    private fun postRequest() {
        try {
            if (isValidForm()) {
                loadingDialog.show()
                val bloodRequest = getDataFromForm()

                postBloodRequest(bloodRequest = bloodRequest) { success, message ->
                    if (success) {
                        loadingDialog.dismiss()
                        RequestSentDialog(requireContext()) {
                            onPosted.invoke()
                            dismiss()
                        }.show()
                    } else {
                        loadingDialog.dismiss()
                        showToast("$message")
                    }
                }
            }
        } catch (e: Exception) {
            loadingDialog.dismiss()
            e.printStackTrace()
            showToast("Something went wrong please try again")
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
                val formattedTime = formatTime(hourOfDay, minute)

                // Show the formatted time in the TextView
                binding.timeInputText.setText(formattedTime)
            },
            currentTime.get(Calendar.HOUR_OF_DAY), // Initial hour
            currentTime.get(Calendar.MINUTE), // Initial minute
            false // 24-hour format
        )

        // Show the TimePickerDialog
        timePickerDialog.show()
    }

    private fun isValidForm(): Boolean {
        return when {
            binding.bloodGroupTextInput.text.isBlank() -> {
                showToast("Please select blood group")
                false
            }

            binding.bloodUnitTextInput.text.isBlank() -> {
                showToast("Please provide unit")
                false
            }

            binding.dateTextInput.text.isBlank() -> {
                showToast("Please provide date")
                false
            }

            binding.timeInputText.text.isBlank() -> {
                showToast("Please provide time")
                false
            }

            binding.noteTextInput.text.isBlank() -> {
                showToast("Please provide a note")
                false
            }

            else -> true
        }
    }

    private fun getDataFromForm(): BloodRequest {
        return BloodRequest(
            userId = "${auth.currentUser?.uid}",
            bloodGroup = binding.bloodGroupTextInput.text.toString(),
            unit = binding.bloodUnitTextInput.text.toString(),
            requestDate = "$date:$time",
            note = binding.noteTextInput.text.toString()
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}