package com.devtoochi.blood_donation.backend.utils

import android.content.SharedPreferences
import android.text.InputType
import android.util.Patterns
import android.widget.ImageButton
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants.EMAIL
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.util.regex.Pattern

object Util {

    private var isPasswordVisible = false

    fun isValidEmailOrPhoneNumber(text: String, from: String): Boolean {
        return if (from == EMAIL) {
            Patterns.EMAIL_ADDRESS.matcher(text).matches()
        } else {
            val phonePattern = Pattern.compile(
                "^(\\+?234|0)?([789]\\d{9})\$"
            )
            phonePattern.matcher(text).matches()
        }
    }

    fun updateSharedPreferences(
        user: User,
        userType: String,
        sharedPreferences: SharedPreferences
    ) {
        val editor = sharedPreferences.edit()

        // Common properties for both Donor and Hospital
        editor.putString("user_id", user.userId)
        editor.putString("email", user.email)
        editor.putString("address", user.address)
        editor.putString("image_url", user.imageUrl)
        editor.putString("phone_number", user.phone)
        editor.putString("user_type", userType)

        // Specific properties for Donor
        if (user is Donor) {
            editor.putString("firstname", user.firstname)
            editor.putString("lastname", user.lastname)
            editor.putString("date_of_birth", user.birthDate)
        }

        // Specific properties for Hospital
        if (user is Hospital) {
            editor.putString("name", user.name)
        }

        editor.apply()
    }

    fun togglePasswordVisibility(
        passwordInputText: MaterialAutoCompleteTextView,
        imageButton: ImageButton
    ) {
        val selectionStart = passwordInputText.selectionStart
        val selectionEnd = passwordInputText.selectionEnd

        if (!isPasswordVisible) {
            // Show password
            passwordInputText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordInputText.setSelection(selectionStart, selectionEnd)
            imageButton.setImageResource(R.drawable.ic_eye_closed) // Update eye icon to closed
        } else {
            // Hide password
            passwordInputText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordInputText.setSelection(selectionStart, selectionEnd)
            imageButton.setImageResource(R.drawable.ic_eye_open) // Update eye icon to open
        }

        // Toggle visibility state
        isPasswordVisible = !isPasswordVisible
    }
}