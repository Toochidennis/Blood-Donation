package com.devtoochi.blood_donation.backend.models

import com.devtoochi.blood_donation.backend.utils.Constants.GENERAL
import com.devtoochi.blood_donation.backend.utils.Constants.PENDING
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class BloodRequest(
    var requestId: String = "",
    var userId: String = "",
    var donorId: String = "",
    var requestType: String = GENERAL,
    var bloodGroup: String = "",
    var unit: String = "",
    var requestDate: String = "",
    var note: String = "",
    var status: String = PENDING,
    @ServerTimestamp
    var datePosted: Date = Date()
)
