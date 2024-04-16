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
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.registerWithGoogle
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.signInWithEmailAndPassword
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getPersonalDetails
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Constants.EMAIL
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.NOT_AVAILABLE
import com.devtoochi.blood_donation.backend.utils.Util.isValidEmailOrPhoneNumber
import com.devtoochi.blood_donation.backend.utils.Util.togglePasswordVisibility
import com.devtoochi.blood_donation.backend.utils.Util.updateSharedPreferences
import com.devtoochi.blood_donation.databinding.FragmentSignInBinding
import com.devtoochi.blood_donation.ui.activities.DonorDashboardActivity
import com.devtoochi.blood_donation.ui.activities.HospitalDashboardActivity
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        loadingDialog = LoadingDialog(requireContext())

        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.navigateBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.signInButton.setOnClickListener {
            logInWithEmailAndPassword()
        }

        binding.signInWithGoogle.setOnClickListener {
            signUpWithGoogle()
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
                showToast("Login failed. Please try using another method")
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
                registerWithGoogle(account = it) { success, message ->
                    if (success) {
                        getPersonalDetails(userType = "$message") { user, error ->
                            handleAuthenticationResult(error, user)
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
            showToast("An error occurred.")
        }
    }

    private fun logInWithEmailAndPassword() {
        val email = binding.emailInputText.text.toString().trim()
        val password = binding.passwordInputText.text.toString().trim()

        emailEditTextWatcher()

        try {
            if (isValidSignInForm(email, password)) {
                loadingDialog.show()

                signInWithEmailAndPassword(
                    email,
                    password
                ) { success, message ->
                    if (success) {
                        getPersonalDetails(userType = "$message") { user, error ->
                            handleAuthenticationResult(error, user)
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
            showToast("An error occurred.")
        }
    }

    private fun handleAuthenticationResult(errorMessage: String?, user: User?) {
        when {
            user != null -> {
                loadingDialog.dismiss()

                when (user) {
                    is Hospital -> {
                        updateSharedPreferences(
                            user = user,
                            userType = HOSPITAL,
                            sharedPreferences = sharedPreferences
                        )
                        launchDashboardActivity(HOSPITAL)
                    }

                    is Donor -> {
                        updateSharedPreferences(
                            user = user,
                            userType = DONOR,
                            sharedPreferences = sharedPreferences
                        )
                        launchDashboardActivity(DONOR)
                    }

                    else -> {
                        showToast("$user is not an instance of any of the users")
                    }
                }
            }

            errorMessage == NOT_AVAILABLE -> showToastAndDismiss("User details not available")
            else -> showToastAndDismiss(errorMessage.toString())
        }
    }

    private fun showToastAndDismiss(message: String) {
        loadingDialog.dismiss()
        showToast(message)
    }

    private fun emailEditTextWatcher() {
        binding.emailInputText.run {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val email = s.toString().trim()

                    error = if (isValidEmailOrPhoneNumber(email, EMAIL)) {
                        null
                    } else {
                        getString(R.string.invalid_email_address)
                    }
                }
            })
        }
    }

    private fun isValidSignInForm(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                showToast(getString(R.string.please_provide_email))
                false
            }

            password.isBlank() -> {
                showToast(getString(R.string.please_provide_password))
                false
            }

            else -> true
        }
    }

    private fun launchDashboardActivity(who: String) {
        val intent = if (who == HOSPITAL) {
            Intent(requireContext(), HospitalDashboardActivity::class.java)
        } else {
            Intent(requireContext(), DonorDashboardActivity::class.java)
        }

        startActivity(intent)
        requireActivity().finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}