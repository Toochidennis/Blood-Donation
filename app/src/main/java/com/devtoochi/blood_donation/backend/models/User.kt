package com.devtoochi.blood_donation.backend.models

import java.util.Date

interface User {
    val id:String
    val userId:String
    val email: String
    val password: String
    val imageUrl: String
    val address: String
    val phone: String
    var country: String
    var state: String
    var city: String
    var recentDonation: String
    val isAvailable:Boolean
    var bloodGroup: String
    val token:String
}