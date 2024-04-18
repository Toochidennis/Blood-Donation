package com.devtoochi.blood_donation.ui.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.updatePersonalDetails
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.databinding.FragmentDonorEditProfileDialogBinding


class DonorProfileEditProfileDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentDonorEditProfileDialogBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sharedPreferences: SharedPreferences
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
        binding = FragmentDonorEditProfileDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        sharedPreferences = requireActivity().getSharedPreferences(
            Constants.PREF_NAME,
            Context.MODE_PRIVATE
        )

        initData()
        handleViewsClick()
    }

    private fun initData() {
        with(sharedPreferences) {
            profileId = getString("id", "")
            binding.firstNameTextInput.setText(getString("firstname", ""))
            binding.lastNameTextInput.setText(getString("lastname", ""))
            binding.dateOfBirthTextInput.setText(getString("date_of_birth", ""))
            binding.genderInputText.setText(getString("gender", ""))
            binding.genderInputText.setText(getString("gender", ""))
            binding.bloodGroupTextInput.setText(getString("blood_group", ""))
            binding.genotypeTextInput.setText(getString("genotype", ""))
            binding.countryTextInput.setText(getString("country", ""))
            binding.stateTextInput.setText(getString("state", ""))
            binding.cityTextInput.setText(getString("city", ""))
        }
    }

    private fun handleViewsClick() {
        binding.navigateUp.setOnClickListener {
            dismiss()
        }


        binding.contactInfoButton.setOnClickListener {
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

        binding.dateOfBirthTextInput.setOnClickListener {
            DatePickerDialog(requireContext()) { date ->
                binding.dateOfBirthTextInput.setText(date)
            }.window?.setLayout(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        binding.genderInputText.setOnClickListener {
            GenderBottomSheetFragment { gender ->
                binding.genderInputText.setText(gender)
            }.show(parentFragmentManager, getString(R.string.gender))
        }

        binding.genotypeTextInput.setOnClickListener {
            GenotypeBottomSheetFragment {
                binding.genotypeTextInput.setText(it)
            }.show(parentFragmentManager, getString(R.string.genotype))
        }

        binding.bloodGroupTextInput.setOnClickListener {
            DonorBloodGroupsBottomSheetFragment{
                binding.bloodGroupTextInput.setText(it)
            }.show(parentFragmentManager, getString(R.string.blood_group))
        }

    }

    private fun updateInfo() {
        try {
            val data = getDataFromForm()
            if (isValidForm(data) && profileId != null) {
                loadingDialog.show()
                updatePersonalDetails(
                    data = hashMapOf(
                        "firstname" to data.firstname,
                        "lastname" to data.lastname,
                        "gender" to data.gender,
                        "birthDate" to data.birthDate,
                        "genotype" to data.genotype,
                        "bloodGroup" to data.bloodGroup,
                        "country" to data.country,
                        "state" to data.state,
                        "city" to data.city,
                    ),
                    userType = Constants.DONOR,
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

    private fun updateSharedPreferences(donor: Donor) {
        sharedPreferences.edit().run {
            putString("firstname", donor.firstname)
            putString("lastname", donor.lastname)
            putString("gender", donor.gender)
            putString("genotype", donor.genotype)
            putString("date_of_birth", donor.genotype)
            putString("country", donor.country)
            putString("city", donor.city)
            putString("state", donor.state)
            putString("blood_group", donor.bloodGroup)
            apply()
        }
    }

    private fun isValidForm(donor: Donor): Boolean {
        return when {
            donor.firstname.isBlank() -> {
                showToast("Please provide firstname")
                false
            }

            donor.lastname.isBlank() -> {
                showToast("Please provide lastname")
                false
            }

            donor.birthDate.isBlank() -> {
                showToast("Please provide date of birth")
                false
            }

            donor.gender.isBlank() -> {
                showToast("Please provide gender")
                false
            }

            donor.genotype.isBlank() -> {
                showToast("Please provide genotype")
                false
            }

            donor.bloodGroup.isBlank() -> {
                showToast("Please select blood group")
                false
            }

            donor.country.isBlank() -> {
                showToast("Please provide country")
                false
            }

            donor.state.isBlank() -> {
                showToast("Please provide state")
                false
            }

            donor.city.isBlank() -> {
                showToast("Please provide city")
                false
            }

            else -> true
        }
    }

    private fun getDataFromForm(): Donor {
        return Donor(
            id = "$profileId",
            firstname = binding.firstNameTextInput.text.toString(),
            lastname = binding.lastNameTextInput.text.toString(),
            birthDate = binding.dateOfBirthTextInput.text.toString(),
            bloodGroup = binding.bloodGroupTextInput.text.toString(),
            gender = binding.genderInputText.text.toString(),
            genotype = binding.genotypeTextInput.text.toString(),
            country = binding.countryTextInput.text.toString().trim(),
            state = binding.stateTextInput.text.toString(),
            city = binding.cityTextInput.text.toString().trim()
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}