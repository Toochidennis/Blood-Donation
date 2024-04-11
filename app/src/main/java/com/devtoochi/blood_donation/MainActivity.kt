package com.devtoochi.blood_donation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.devtoochi.blood_donation.ui.activities.LoginActivity
import com.devtoochi.blood_donation.databinding.ActivityMainBinding
import com.devtoochi.blood_donation.ui.activities.DonorDashboardActivity
import com.devtoochi.blood_donation.ui.activities.HospitalDashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        val intent = when (sharedPreferences.getString("user_type", "")) {
            HOSPITAL -> Intent(this, HospitalDashboardActivity::class.java)
            DONOR -> Intent(this, DonorDashboardActivity::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }

        Log.d("usertype", "${sharedPreferences.getString("user_type", "")}")

        startActivity(intent)
        finish()
    }

}