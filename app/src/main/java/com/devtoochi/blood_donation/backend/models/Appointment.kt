package com.devtoochi.blood_donation.backend.models

data class Appointment(
    var appointmentId: String = "",
    var donorId: String = "",
    var receiverId: String = "",
    var requestId: String = "",
    var appointmentDate: String = "",
    var name: String = "",
    var address: String = "",
    var imageUrl: String = "",
    var phone: String = "",
    var email: String = "",
    var userType: String = "",
)
