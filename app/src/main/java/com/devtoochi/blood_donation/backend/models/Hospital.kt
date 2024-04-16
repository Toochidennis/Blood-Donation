package com.devtoochi.blood_donation.backend.models

import com.devtoochi.blood_donation.backend.utils.Constants.NOT_ELIGIBLE
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.Date

data class Hospital(
    override var id: String = "",
    override var userId: String = "",
    var name: String = "",
    override var email: String = "",
    override var imageUrl: String = "",
    override var password: String = "",
    var regNo: String = "",
    override var bloodGroup: String = "",
    override var country: String = "",
    override var state: String = "",
    override var city: String = "",
    override var phone: String = "",
    override var address: String = "",
    var eligibility: Boolean = NOT_ELIGIBLE,
    @ServerTimestamp
    override var recentDonation: Date = Date(),
    override var isAvailable: Boolean = true,
    override val token: String = ""
) : User, Serializable
