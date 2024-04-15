package com.devtoochi.blood_donation.backend.models

data class Notification(
    var token: String = "",
    val title: String = "",
    val body: String = "",
)
