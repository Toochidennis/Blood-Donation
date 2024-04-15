package com.devtoochi.blood_donation.backend.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.util.Patterns
import android.widget.ImageButton
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.models.Notification
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants.BASE_URL
import com.devtoochi.blood_donation.backend.utils.Constants.CONTENT_TYPE
import com.devtoochi.blood_donation.backend.utils.Constants.EMAIL
import com.devtoochi.blood_donation.backend.utils.Constants.SCOPES
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
        editor.putString("id", user.id)
        editor.putString("email", user.email)
        editor.putString("address", user.address)
        editor.putString("image_url", user.imageUrl)
        editor.putString("phone_number", user.phone)
        editor.putString("user_type", userType)
        editor.putString("city", user.city)
        editor.putString("state", user.state)
        editor.putString("country", user.country)
        //editor.putString("last_donation", user.recentDonation)
        editor.putBoolean("is_available", user.isAvailable)
        editor.putString("blood_group", user.bloodGroup)

        // Specific properties for Donor
        if (user is Donor) {
            editor.putString("firstname", user.firstname)
            editor.putString("lastname", user.lastname)
            editor.putString("date_of_birth", user.birthDate)
            editor.putString("genotype", user.genotype)
        }

        // Specific properties for Hospital
        if (user is Hospital) {
            editor.putString("name", user.name)
            editor.putBoolean("eligible", user.eligibility)
            editor.putString("reg_no", user.regNo)
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

    fun getGreetingMessage(context: Context): String {
        val calender = Calendar.getInstance()
        val hourOfDay = calender[Calendar.HOUR_OF_DAY]

        return when {
            hourOfDay < 12 -> context.getString(R.string.good_morning)
            hourOfDay < 18 -> context.getString(R.string.good_afternoon)
            else -> context.getString(R.string.good_evening)
        }
    }

    fun formatTime(hourOfDay: Int, minute: Int): String {
        // Create a Calendar instance to set the selected time
        val selectedTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }

        // Format the time to display in AM/PM format (e.g., "11:30 AM")
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(selectedTime.time)
    }

    fun dateFormatter(date: String, format: String = "default"): String {
        return try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parseDate = simpleDateFormat.parse(date)!!

            val sdf = when (format) {
                "default" -> SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                else -> SimpleDateFormat("dd, MMM", Locale.getDefault())
            }

            sdf.format(parseDate)

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun sendNotification(
        context: Context,
        notification: Notification,
        onComplete: (String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val serverKey = getAccessToken(context)

        val url = URL(BASE_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", CONTENT_TYPE)
            setRequestProperty("Authorization", "Bearer $serverKey")
            doOutput = true
        }

        try {
            val payload = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", notification.token)

                    put("notification", JSONObject().apply {
                        put("title", notification.title)
                        put("body", notification.body)
                    })
                })
            }.toString()

            val outputStream = BufferedOutputStream(connection.outputStream)
            outputStream.write(payload.toByteArray())
            outputStream.flush()

            val responseCode = connection.responseCode

            withContext(Dispatchers.Main) {
                onComplete(responseCode.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onComplete(e.message)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun getAccessToken(context: Context): String? {
        return runBlocking {
            try {
                val inputStream = context.assets.open("service-account.json")

                val googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(listOf(SCOPES))

                val token = googleCredentials.refreshAccessToken()
                token.tokenValue

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}