package com.devtoochi.blood_donation.backend.firebase

import com.google.firebase.auth.FirebaseAuth

object UserAuth {
    val instance:FirebaseAuth by lazy { FirebaseAuth.getInstance() }
}