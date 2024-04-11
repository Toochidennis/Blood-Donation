package com.devtoochi.blood_donation.backend.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Donor(
    var id: String = "",
    override var userId: String = "",
    var firstname: String = "",
    var lastname: String = "",
    override var email: String = "",
    override var imageUrl: String = "",
    override var password: String = "",
    var bloodGroup: String = "",
    var birthDate: String = "",
    override var country: String = "",
    override var state: String = "",
    override var city: String = "",
    override var phone: String = "",
    override var address: String = "",
    @ServerTimestamp
    override var recentDonation: Date = Date(),
) : User
