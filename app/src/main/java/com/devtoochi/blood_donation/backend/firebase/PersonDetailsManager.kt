package com.devtoochi.blood_donation.backend.firebase

import android.util.Log
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.donorCollection
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.hospitalCollection
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.NOT_AVAILABLE
import com.devtoochi.blood_donation.backend.utils.Constants.PERSONAL_DETAILS
import com.google.firebase.firestore.CollectionReference

object PersonDetailsManager {

    fun createNewUser(
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val collection = if (user is Hospital) hospitalCollection else donorCollection
        addToPersonalDetails(collection, user, onComplete)
    }

    private fun addToPersonalDetails(
        collection: CollectionReference,
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        collection.document(user.userId)
            .collection(PERSONAL_DETAILS)
            .add(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete.invoke(true, null)
                } else {
                    onComplete.invoke(false, task.exception?.message)
                }
            }
    }

    fun checkIfUserExists(
        userId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        Log.d("response", "user Id $userId")
        val donorsQuery = donorCollection.document(userId).collection(PERSONAL_DETAILS).get()
        val hospitalsQuery = hospitalCollection.document(userId).collection(PERSONAL_DETAILS).get()

        // Add success and failure listeners to the donors query
        donorsQuery.addOnSuccessListener { donorResult ->
            // Check if the document exists in the "donors" collection
            val donorExists = !donorResult.isEmpty

            // Add success and failure listeners to the hospitals query
            hospitalsQuery.addOnSuccessListener { hospitalResult ->
                // Check if the document exists in the "hospitals" collection
                val hospitalExists = !hospitalResult.isEmpty

                Log.d("response", "$hospitalExists $donorExists")

                if (donorExists || hospitalExists) {
                    // User exists in either "donors" or "hospitals" collection
                    val userType = if (donorExists) DONOR else HOSPITAL
                    onComplete(true, userType)
                } else {
                    // User does not exist in either collection
                    onComplete(false, null)
                }
            }.addOnFailureListener { hospitalError ->
                // Handle any errors that occur during the hospitals query
                Log.d("response", "${hospitalError.message}")
                onComplete(false, hospitalError.message)

            }
        }.addOnFailureListener { donorError ->
            // Handle any errors that occur during the donors query
            Log.d("response", "${donorError.message}")
            onComplete(false, donorError.message)
        }
    }

    fun getPersonalDetails(
        userType: String,
        userId: String = "",
        onComplete: (User?, String?) -> Unit
    ) {
        val collection = if (userType == HOSPITAL) hospitalCollection else donorCollection

        // Reference to the user's personal details collection
        val userRef = if (userId.isEmpty()) {
            // Get the current user's ID
            val currentId = auth.currentUser?.uid
            currentId?.let {
                collection.document(it)
                    .collection(PERSONAL_DETAILS)
            }
        } else {
            collection.document(userId).collection(PERSONAL_DETAILS)
        }

        // Retrieve personal details
        userRef?.get()
            ?.addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val user = if (userType == HOSPITAL) {
                        querySnapshot.documents.firstOrNull()?.toObject(Hospital::class.java)
                    } else {
                        querySnapshot.documents.firstOrNull()?.toObject(Donor::class.java)
                    }
                    onComplete.invoke(user, null)
                } else {
                    onComplete.invoke(null, NOT_AVAILABLE)
                }
            }
            ?.addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
            }
    }
}