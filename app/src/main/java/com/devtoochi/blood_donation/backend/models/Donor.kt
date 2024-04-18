package com.devtoochi.blood_donation.backend.models

import java.io.Serializable

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
    var gender: String = "",
    override var country: String = "",
    override var state: String = "",
    override var city: String = "",
    override var phone: String = "",
    override var address: String = "",
    override var recentDonation: String = "",
    override var isAvailable: Boolean = true,
    override val token: String = ""
) : User, Serializable
