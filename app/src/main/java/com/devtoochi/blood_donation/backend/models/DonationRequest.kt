package com.devtoochi.blood_donation.backend.models

data class DonationRequest(
    var requestId: String = "",
    var userId: String = "",
    var donorId: String = "",
    var name: String = "",
    var address: String = "",
    var state: String = "",
    var imageUrl: String = "",
    var bloodGroup: String = "",
    var unit: String = "",
    var note: String = "",
    var requestDate: String = "",
    var datePosted: String = "",
    val token:String=""
)
