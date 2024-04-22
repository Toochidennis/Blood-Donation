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
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.databinding.FragmentHospitalEditProfileDialogBinding
import org.json.JSONArray
import org.json.JSONObject


class HospitalEditProfileDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentHospitalEditProfileDialogBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sharedPreferences: SharedPreferences

    private var bloodGroupMap = hashMapOf<String, String>()
    private var profileId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHospitalEditProfileDialogBinding.inflate(inflater, container, false)
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
            binding.nameTextInput.setText(getString("name", ""))
            binding.regNoTextInput.setText(getString("reg_no", ""))

            processBloodGroup(getString("blood_group", ""))
            val bloodGroupString = bloodGroupMap.values.joinToString(separator = ", ") { it }
            binding.bloodGroupTextInput.setText(bloodGroupString)
            binding.countryTextInput.setText(getString("country", ""))
            binding.stateTextInput.setText(getString("state", ""))
            binding.cityTextInput.setText(getString("city", ""))
        }
    }

    private fun processBloodGroup(bloodGroup: String?) {
        try {
            if (bloodGroup != null)
                with(JSONArray(bloodGroup)) {
                    for (i in 0 until length()) {
                        getJSONObject(i).let {
                            val id = it.getString("id")
                            val name = it.getString("name")
                            bloodGroupMap[id] = name
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleViewsClick() {
        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.bloodGroupTextInput.setOnClickListener {
            HospitalBloodGroupsBottomSheetFragment(bloodGroupMap) { bloodGroups ->
                bloodGroupMap = bloodGroups
                val bloodGroupString = bloodGroupMap.values.joinToString(separator = ", ") { it }
                binding.bloodGroupTextInput.setText(bloodGroupString)
            }.show(parentFragmentManager, getString(R.string.blood_group))
        }

        binding.contactInfo.setOnClickListener {
            ContactInfoDialogFragment().show(
                parentFragmentManager,
                getString(R.string.contact_info)
            )
        }

        binding.stateTextInput.setOnClickListener {
            StatesBottomSheetFragment { state ->
                binding.stateTextInput.setText(state)
            }.show(parentFragmentManager, getString(R.string.state))
        }

        binding.saveButton.setOnClickListener {
            updateInfo()
        }

        binding.countryTextInput.setOnClickListener {
            CountriesBottomSheetFragment {
                binding.countryTextInput.setText(it)
            }.show(parentFragmentManager, getString(R.string.country))
        }
    }

    private fun updateInfo() {
        try {
            val data = getDataFromForm()
            if (isValidForm(data) && profileId != null) {
                loadingDialog.show()
                updatePersonalDetails(
                    data = hashMapOf(
                        "name" to data.name,
                        "regNo" to data.regNo,
                        "bloodGroup" to data.bloodGroup,
                        "country" to data.country,
                        "state" to data.state,
                        "city" to data.city,
                    ),
                    userType = HOSPITAL,
                    profileId = data.id
                ) { success, message ->
                    if (success) {
                        updateSharedPreferences(data)
                        loadingDialog.dismiss()
                        showToast("Saved successfully")
                        dismiss()
                    } else {
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

    private fun updateSharedPreferences(hospital: Hospital) {
        sharedPreferences.edit().run {
            putString("name", hospital.name)
            putString("country", hospital.country)
            putString("city", hospital.city)
            putString("state", hospital.state)
            putString("reg_no", hospital.regNo)
            putString("blood_group", hospital.bloodGroup)
            apply()
        }
    }

    private fun isValidForm(hospital: Hospital): Boolean {
        return when {
            hospital.name.isBlank() -> {
                showToast("Please provide hospital name")
                false
            }

            hospital.regNo.isBlank() -> {
                showToast("Please provide registration number")
                false
            }

            hospital.bloodGroup.isBlank() -> {
                showToast("Please select blood group")
                false
            }

            hospital.country.isBlank() -> {
                showToast("Please provide country")
                false
            }

            hospital.state.isBlank() -> {
                showToast("Please provide state")
                false
            }

            hospital.city.isBlank() -> {
                showToast("Please provide city")
                false
            }

            else -> true
        }
    }

    private fun getDataFromForm(): Hospital {
        return Hospital(
            id = "$profileId",
            name = binding.nameTextInput.text.toString(),
            regNo = binding.regNoTextInput.text.toString(),
            bloodGroup = convertMapToJsonString(),
            country = binding.countryTextInput.text.toString().trim(),
            state = binding.stateTextInput.text.toString(),
            city = binding.cityTextInput.text.toString().trim()
        )
    }

    private fun convertMapToJsonString(): String {
        return JSONArray().apply {
            bloodGroupMap.forEach { (key, value) ->
                JSONObject().apply {
                    put("id", key)
                    put("name", value)
                }.let {
                    put(it)
                }
            }
        }.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}