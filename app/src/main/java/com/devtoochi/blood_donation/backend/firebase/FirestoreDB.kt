package com.devtoochi.blood_donation.backend.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreDB {
    val instance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}