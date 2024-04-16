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
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.RequestsManager.postBloodRequest
import com.devtoochi.blood_donation.backend.models.BloodGroup
import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.databinding.FragmentBloodBankDetailsBinding
import com.devtoochi.blood_donation.ui.adapters.BloodGroupAdapter2
import com.devtoochi.blood_donation.ui.dialogs.DatePickerDialog
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.devtoochi.blood_donation.ui.dialogs.RequestSentDialog
import org.json.JSONArray
import java.util.Calendar


class BloodBankDetailsFragment : Fragment() {

    private lateinit var binding: FragmentBloodBankDetailsBinding
    private lateinit var loadingDialog: LoadingDialog
    private var bloodBankDetails: Hospital? = null
    private var bloodGroup = BloodGroup()
    private var time: String? = null
    private var date: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            bloodBankDetails = it.getSerializable("details") as Hospital
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBloodBankDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        initData()
        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.navigateUp.setOnClickListener {
            findNavController().popBackStack()
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

    private fun initData() {
        if (bloodBankDetails != null) {
            binding.nameTextview.text = bloodBankDetails?.name
            binding.stateTextview.text = bloodBankDetails?.state
            binding.addressTextview.text = bloodBankDetails?.address
            binding.cityTextview.text=  bloodBankDetails?.city

            bloodBankDetails?.bloodGroup?.let { processJson(it) }
        }
    }

    private fun processJson(bloodGroup: String) {
        val bloodGroups = mutableListOf<BloodGroup>()
        try {
            with(JSONArray(bloodGroup)) {
                for (i in 0 until length()) {
                    bloodGroups.add(BloodGroup().fromJsonObject(getJSONObject(i)))
                }
                setupAdapter(bloodGroups = bloodGroups)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAdapter(bloodGroups: MutableList<BloodGroup>) {
        val bloodGroupAdapter2 = BloodGroupAdapter2(bloodGroups, bloodGroup)
        binding.bloodBankStockRecyclerview.apply {
            hasFixedSize()
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = bloodGroupAdapter2
        }
    }

    private fun sendRequest() {
        try {
            if (isValidForm()) {
                loadingDialog.show()
                postBloodRequest(
                    BloodRequest(
                        userId = "${auth.currentUser?.uid}",
                        donorId = bloodBankDetails?.userId!!,
                        requestType = HOSPITAL,
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
    }

    private fun isValidForm(): Boolean {
        return when {
            bloodGroup.name.isEmpty() -> {
                Toast.makeText(
                    requireContext(), "Please select blood group",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

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