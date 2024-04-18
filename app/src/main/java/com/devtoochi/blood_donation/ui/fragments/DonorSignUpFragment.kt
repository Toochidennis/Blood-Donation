package com.devtoochi.blood_donation.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.databinding.FragmentDonorSignUpBinding
import com.devtoochi.blood_donation.ui.activities.DonorDashboardActivity
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.MaterialAutoCompleteTextView


class DonorSignUpFragment : Fragment() {

    private lateinit var binding: FragmentDonorSignUpBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDonorSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        loadingDialog = LoadingDialog(requireContext())

        handleViewClick()
    }

    private fun handleViewClick() {
        binding.signUpButton.setOnClickListener {
            signUpWithEmailAndPassword()
        }

        binding.signInWithGoogle.setOnClickListener {
            signUpWithGoogle()
        }

        binding.navigateBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.passwordVisibilityToggle.setOnClickListener {
            Util.togglePasswordVisibility(
                binding.passwordInputText,
                binding.passwordVisibilityToggle
            )
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result?.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            } else {
                loadingDialog.dismiss()
                showToast("Login failed. Please use another method to create account")
            }
        }

    private fun signUpWithGoogle() {
        loadingDialog.show()
        val googleSignInClient = AuthenticationManager.googleSignInClient(requireContext())
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            account?.let {
                AuthenticationManager.registerWithGoogle(
                    account = it,
                    userType = DONOR
                ) { success, message ->
                    if (success) {
                        if (message == DONOR) {
                            PersonDetailsManager.getPersonalDetails(userType = DONOR) { user, exception ->
                                handleAuthenticationResult(exception, user)
                            }
                        } else {
                            loadingDialog.dismiss()
                            AuthenticationManager.googleSignInClient(requireContext()).signOut()
                            showToast("Email address already in use by another account")
                        }
                    } else {
                        loadingDialog.dismiss()
                        showToast(message.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred. ${e.message}")
        }
    }

    private fun signUpWithEmailAndPassword() {
        editTextWatcher(binding.emailTextInput, Constants.EMAIL)
        editTextWatcher(binding.phoneNumberTextInput, Constants.PHONE_NUMBER)

        try {
            val user = getUserFromForm()

            if (isValidSignUpForm(user)) {
                loadingDialog.show()
                AuthenticationManager.registerWithEmailAndPassword(user) { success, errorMessage ->
                    if (success) {
                        PersonDetailsManager.getPersonalDetails(userType = DONOR) { result, error ->
                            handleAuthenticationResult(error, result)
                        }
                    } else {
                        loadingDialog.dismiss()
                        showToast(errorMessage.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun handleAuthenticationResult(errorMessage: String?, user: User?) {
        when {
            user != null -> {
                Util.updateSharedPreferences(
                    user = user,
                    userType = DONOR,
                    sharedPreferences = sharedPreferences
                )
                loadingDialog.dismiss()
                navigateTo()
            }

            errorMessage == Constants.NOT_AVAILABLE -> showToastAndDismiss("User details not available")
            else -> showToastAndDismiss(errorMessage.toString())
        }
    }

    private fun navigateTo() {
        startActivity(Intent(requireContext(), DonorDashboardActivity::class.java))
        requireActivity().finish()
    }

    private fun showToastAndDismiss(message: String) {
        loadingDialog.dismiss()
        showToast(message)
    }

    private fun editTextWatcher(
        completeTextView: MaterialAutoCompleteTextView,
        from: String
    ) {
        completeTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()

                if (Util.isValidEmailOrPhoneNumber(email, from)) {
                    completeTextView.error = null
                } else {
                    completeTextView.error = if (from == Constants.EMAIL) {
                        getString(R.string.invalid_email_address)
                    } else {
                        getString(R.string.invalid_phone_number)
                    }
                }
            }
        })
    }

    private fun getUserFromForm(): Donor {
        return Donor(
            email = binding.emailTextInput.text.toString().trim(),
            password = binding.passwordInputText.text.toString().trim(),
            firstname = binding.firstNameTextInput.text.toString().trim(),
            lastname = binding.lastNameTextInput.text.toString().trim(),
            phone = binding.phoneNumberTextInput.text.toString().trim(),
        )
    }

    private fun isValidSignUpForm(donor: Donor): Boolean {
        return when {
            donor.firstname.isBlank() -> {
                showToast("Please provide firstname")
                false
            }

            donor.lastname.isBlank() -> {
                showToast("Please provide lastname")
                false
            }

            donor.phone.isBlank() -> {
                showToast(getString(R.string.please_provide_phone_number))
                false
            }

            donor.email.isBlank() -> {
                showToast(getString(R.string.please_provide_email))
                false
            }

            donor.password.isBlank() -> {
                showToast(getString(R.string.please_provide_password))
                false
            }

            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}