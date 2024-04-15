package com.devtoochi.blood_donation.backend.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Donor(
    override var id: String = "",
    override var userId: String = "",
    var firstname: String = "",
    var lastname: String = "",
    override var email: String = "",
    override var imageUrl: String = "",
    override var password: String = "",
    override var bloodGroup: String = "",
    var birthDate: String = "",
    var genotype: String = "",
    override var country: String = "",
    override var state: String = "",
    override var city: String = "",
    override var phone: String = "",
    override var address: String = "",
    @ServerTimestamp
    override var recentDonation: Date = Date(),
    override var isAvailable: Boolean = true,
    override val token: String =""
) : User
