package com.devtoochi.blood_donation.backend.firebase

import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.models.Donation
import com.devtoochi.blood_donation.backend.utils.Constants.DONATIONS

object DonationManager {

    private val donationsCollection = FirestoreDB.instance.collection(DONATIONS)

    fun createDonations(donation: Donation, onComplete: (Boolean, String?) -> Unit) {
        donationsCollection.add(donation)
            .addOnSuccessListener {
                onComplete.invoke(true, "Donations saved")
            }
            .addOnFailureListener { exception ->
                onComplete.invoke(false, exception.message)
            }
    }

    fun checkIfDonationExists(requestId: String, onComplete: (Boolean, String?) -> Unit) {
        FirestoreDB.instance.collection(DONATIONS)
            .whereEqualTo("requestId", requestId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onComplete(false, "Empty")
                } else {
                    onComplete(true, "")
                }
            }
            .addOnFailureListener {
                onComplete(false, it.message)
            }
    }

    fun getDonations(onComplete: (List<Donation>?, String?) -> Unit) {
        donationsCollection
            .whereEqualTo("donorId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val donations = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Donation::class.java)?.apply {
                        this.donationId = document.id
                    }
                }
                if (querySnapshot.isEmpty) {
                    onComplete.invoke(null, "Empty")
                } else {
                    onComplete.invoke(donations, null)
                }
            }
            .addOnFailureListener {
                onComplete.invoke(null, it.message)
            }
    }
}