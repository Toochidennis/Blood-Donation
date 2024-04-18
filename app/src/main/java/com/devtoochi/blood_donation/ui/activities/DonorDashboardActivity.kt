package com.devtoochi.blood_donation.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.databinding.ActivityDonorDashboardBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DonorDashboardActivity : AppCompatActivity() {

    private lateinit var binding:ActivityDonorDashboardBinding
    private var previousItemMenu: MenuItem? = null
    private val userActivityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityDonorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()

        userActivityScope.launch {
            saveTokenInBackground()
        }
    }

    private fun setupNavigation() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Set a darker background color for dark mode
            binding.bottomNavigation.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))
        } else {
            // Set the default background color for light mode
            binding.bottomNavigation.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.findNavController()
        binding.bottomNavigation.setupWithNavController(navController)

        // Set the first item to be checked programmatically
        updateIcon(navDestination = navController.currentDestination!!)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateIcon(navDestination = destination)
        }
    }

    private fun updateIcon(navDestination: NavDestination) {
        // Reset previous icon to normal state
        previousItemMenu?.setIcon(getNormalIcon(previousItemMenu!!))

        val selectedItem = binding.bottomNavigation.menu.findItem(navDestination.id)

        // Update current icon to filled state
        selectedItem.setIcon(getFilledIcon(selectedItem))

        // Update previousMenuItem to the current one
        previousItemMenu = selectedItem
    }

    private fun getFilledIcon(menuItem: MenuItem) = when (menuItem.itemId) {
        // Get the resource ID for the filled icon based on the selected menu item
        R.id.donorHomeFragment -> R.drawable.ic_home_filled
        R.id.donationFragment -> R.drawable.ic_donate_blood_filled
        R.id.donorAppointmentsFragment -> R.drawable.ic_appointment_filled
        else -> R.drawable.ic_person_filled
    }

    private fun getNormalIcon(menuItem: MenuItem) = when (menuItem.itemId) {
        // Get the resource ID for the normal icon based on the selected menu item
        R.id.donorHomeFragment -> R.drawable.ic_home
        R.id.donationFragment -> R.drawable.ic_donate_blood
        R.id.donorAppointmentsFragment -> R.drawable.ic_appointment
        else -> R.drawable.ic_person
    }

    private suspend fun saveTokenInBackground() {
        try {
            // Get the user's FCM token
            val userToken = FirebaseMessaging.getInstance().token.await()
            val profileId =
                getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getString("id", "")
            // Perform database update here using the obtained FCM token
            if (profileId != null) {
                PersonDetailsManager.updatePersonalDetails(
                    hashMapOf("token" to userToken),
                    userType = Constants.DONOR,
                    profileId = profileId
                ) { _, _ -> }
            }
        } catch (e: Exception) {
            // Handle exceptions, such as token retrieval failure or database update failure
            e.printStackTrace()
        }
    }
}