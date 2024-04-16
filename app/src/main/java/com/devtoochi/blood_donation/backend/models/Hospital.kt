package com.devtoochi.blood_donation.backend.models

import com.devtoochi.blood_donation.backend.utils.Constants.NOT_ELIGIBLE
import java.io.Serializable

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
    override var recentDonation: String = "",
    override var isAvailable: Boolean = true,
    override val token: String = ""
) : User, Serializable
