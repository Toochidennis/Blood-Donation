package com.devtoochi.blood_donation.backend.models

data class Donations(
    var donationId: String = "",
    var donorId: String = "",
    var receiverId: String = "",
    var requestId: String = "",
    var receiverName: String = "",
    var receiverImageUrl: String = "",
    var receiverAddress: String = "",
    var bloodGroup: String = "",
    var isScheduled: Boolean = false
)
