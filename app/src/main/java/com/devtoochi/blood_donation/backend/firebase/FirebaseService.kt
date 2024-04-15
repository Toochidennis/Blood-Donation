package com.devtoochi.blood_donation.backend.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.devtoochi.blood_donation.MainActivity
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.updatePersonalDetails
import com.devtoochi.blood_donation.backend.utils.Constants.CHANNEL_ID
import com.devtoochi.blood_donation.backend.utils.Constants.CHANNEL_NAME
import com.devtoochi.blood_donation.backend.utils.Constants.PREF_NAME
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        try {
            val profileId = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getString("id", "")

            if (profileId != null) {
                updatePersonalDetails(
                    hashMapOf("token" to token),
                    userType = "",
                    profileId = profileId
                ) { _, _ -> }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
            val notification = createNotification(message, pendingIntent)
            notificationManager.notify(notificationId, notification)
        } else {
            val notification = createLegacyNotification(message, pendingIntent)
            notificationManager.notify(notificationId, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            name = "Blood Request Accepted"
            description = "Notifications for blood request"
            enableLights(true)
            lightColor = ContextCompat.getColor(this@FirebaseService, R.color.red)
        }.also {
            notificationManager.createNotificationChannel(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(
        message: RemoteMessage,
        pendingIntent: PendingIntent
    ): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.app_logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createLegacyNotification(
        message: RemoteMessage,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.app_logo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()
    }
}