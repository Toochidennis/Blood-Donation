package com.devtoochi.blood_donation.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.googleSignInClient
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.registerWithEmailAndPassword
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.registerWithGoogle
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getPersonalDetails
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants.EMAIL
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.NOT_AVAILABLE
import com.devtoochi.blood_donation.backend.utils.Constants.PHONE_NUMBER
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.backend.utils.Constants.SIGN_UP
import com.devtoochi.blood_donation.backend.utils.Util.isValidEmailOrPhoneNumber
import com.devtoochi.blood_donation.backend.utils.Util.togglePasswordVisibility
import com.devtoochi.blood_donation.backend.utils.Util.updateSharedPreferences
import com.devtoochi.blood_donation.databinding.FragmentHospitalSignUpBinding
import com.devtoochi.blood_donation.ui.activities.HospitalDashboardActivity
import com.devtoochi.blood_donation.ui.dialogs.CheckHospitalEligibilityDialogFragment
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.MaterialAutoCompleteTextView


class HospitalSignUpFragment : Fragment() {

    private lateinit var binding: FragmentHospitalSignUpBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHospitalSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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
            togglePasswordVisibility(binding.passwordInputText, binding.passwordVisibilityToggle)
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
        val googleSignInClient = googleSignInClient(requireContext())
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            account?.let {
                registerWithGoogle(
                    account = it,
                    userType = HOSPITAL
                ) { success, message ->
                    if (success) {
                        if (message == HOSPITAL) {
                            getPersonalDetails(userType = HOSPITAL) { user, exception ->
                                handleAuthenticationResult(exception, user)
                            }
                        } else {
                            loadingDialog.dismiss()
                            googleSignInClient(requireContext()).signOut()
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
        editTextWatcher(binding.emailTextInput, EMAIL)
        editTextWatcher(binding.phoneNumberTextInput, PHONE_NUMBER)

        try {
            val user = getUserFromForm()

            if (isValidSignUpForm(user)) {
                loadingDialog.show()
                registerWithEmailAndPassword(user) { success, errorMessage ->
                    if (success) {
                        getPersonalDetails(userType = HOSPITAL) { result, error ->
                            handleAuthenticationResult(error, result as Hospital)
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
                updateSharedPreferences(
                    user = user,
                    userType = HOSPITAL,
                    sharedPreferences = sharedPreferences
                )
                loadingDialog.dismiss()
                navigateTo(user = user)

            }

            errorMessage == NOT_AVAILABLE -> showToastAndDismiss("User details not available")
            else -> showToastAndDismiss(errorMessage.toString())
        }
    }

    private fun navigateTo(user: User) {
        if ((user as Hospital).eligibility) {
            startActivity(Intent(requireContext(), HospitalDashboardActivity::class.java))
            requireActivity().finish()
        } else {
            CheckHospitalEligibilityDialogFragment(SIGN_UP).show(
                parentFragmentManager,
                getString(R.string.check_eligibility)
            )
        }
    }

    private fun showToastAndDismiss(message: String) {
        loadingDialog.dismiss()
        showToast(message)
    }

    private fun isValidSignUpForm(hospital: Hospital): Boolean {
        return when {
            hospital.name.isBlank() -> {
                showToast(getString(R.string.please_provide_name))
                false
            }

            hospital.regNo.isBlank() -> {
                showToast(getString(R.string.please_provide_registration_number))
                false
            }

            hospital.phone.isBlank() -> {
                showToast(getString(R.string.please_provide_phone_number))
                false
            }

            hospital.email.isBlank() -> {
                showToast(getString(R.string.please_provide_email))
                false
            }

            hospital.password.isBlank() -> {
                showToast(getString(R.string.please_provide_password))
                false
            }

            else -> true
        }
    }

    private fun getUserFromForm(): Hospital {
        return Hospital(
            email = binding.emailTextInput.text.toString().trim(),
            password = binding.passwordInputText.text.toString().trim(),
            name = binding.hospitalNameTextInput.text.toString().trim(),
            phone = binding.phoneNumberTextInput.text.toString().trim(),
            regNo = binding.regNoTextInput.text.toString().trim(),
        )
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

                if (isValidEmailOrPhoneNumber(email, from)) {
                    completeTextView.error = null
                } else {
                    completeTextView.error = if (from == EMAIL) {
                        getString(R.string.invalid_email_address)
                    } else {
                        getString(R.string.invalid_phone_number)
                    }
                }
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}