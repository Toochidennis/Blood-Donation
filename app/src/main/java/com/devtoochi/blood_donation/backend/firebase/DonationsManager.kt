package com.devtoochi.blood_donation.backend.firebase

import com.devtoochi.blood_donation.backend.models.Donations
import com.devtoochi.blood_donation.backend.utils.Constants.DONATIONS

object DonationsManager {

    private val donationsCollection = FirestoreDB.instance.collection(DONATIONS)

    fun createDonations(donations: Donations, onComplete: (Boolean, String?) -> Unit) {
        donationsCollection.add(donations)
            .addOnSuccessListener {
                onComplete.invoke(true, "Donations saved")
            }
            .addOnFailureListener { exception ->
                onComplete.invoke(false, exception.message)
            }
    }

    fun checkIfDonationExists(requestId: String, onComplete: (Boolean, String?) -> Unit) {
        FirestoreDB.instance.collectionGroup(DONATIONS)
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


}