package com.devtoochi.blood_donation.ui.dialogs

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.updatePersonalDetails
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.databinding.FragmentContactInfoDialogBinding


class ContactInfoDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentContactInfoDialogBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sharedPreferences: SharedPreferences

    private var profileId: String? = null
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContactInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        initData()
        handleViewsClick()
    }

    private fun initData() {
        with(sharedPreferences) {
            profileId = getString("id", "")
            userType = getString("user_type", "")
            binding.emailTextview.text = getString("email", "")
            binding.phoneNumberTextInput.setText(getString("phone_number", ""))
            binding.addressTextInput.setText(getString("address", ""))
        }
    }

    private fun handleViewsClick() {
        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            updateContactInfo()
        }
    }

    private fun updateContactInfo() {
        try {
            if (isValidForm() && profileId != null) {
                loadingDialog.show()

                val phoneNumber = binding.phoneNumberTextInput.text.toString()
                val address = binding.addressTextInput.text.toString().trim()

                updatePersonalDetails(
                    data = hashMapOf(
                        "phone" to phoneNumber,
                        "address" to address
                    ),
                    userType = "$userType",
                    profileId = "$profileId",
                ) { success, message ->
                    if (success) {
                        sharedPreferences.edit().apply {
                            putString("phone_number", phoneNumber)
                            putString("address", address)
                            apply()
                        }
                        loadingDialog.dismiss()
                        showToast("Saved successfully")
                    }else{
                        loadingDialog.dismiss()
                        showToast("Something went wrong please try again: $message")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun isValidForm(): Boolean {
        return if (binding.phoneNumberTextInput.text.isBlank()) {
            binding.phoneNumberTextInput.error = "Phone number is required"
            false
        } else if (binding.addressTextInput.text.isBlank()) {
            binding.addressTextInput.error = "Address is required"
            false
        } else {
            binding.phoneNumberTextInput.error = null
            binding.addressTextInput.error = null
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}