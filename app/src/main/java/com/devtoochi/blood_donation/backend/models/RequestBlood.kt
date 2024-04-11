package com.devtoochi.blood_donation.backend.models

import com.devtoochi.blood_donation.backend.utils.Constants.PENDING

data class RequestBlood(
    var id: String = "",
    var donorId: String = "",
    var receiverId: String = "",
    var bloodGroup: String = "",
    var unit: String = "",
    var requestDate: String = "",
    var status: String = PENDING,
)
