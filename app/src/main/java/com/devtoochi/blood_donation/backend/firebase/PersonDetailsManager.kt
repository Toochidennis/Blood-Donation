package com.devtoochi.blood_donation.backend.firebase

import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.donorsCollection
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.hospitalsCollection
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
        val collection = if (user is Hospital) hospitalsCollection else donorsCollection
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
        val donorsQuery = donorsCollection.document(userId).collection(PERSONAL_DETAILS).get()
        val hospitalsQuery = hospitalsCollection.document(userId).collection(PERSONAL_DETAILS).get()

        // Add success and failure listeners to the donors query
        donorsQuery.addOnSuccessListener { donorResult ->
            // Check if the document exists in the "donors" collection
            val donorExists = !donorResult.isEmpty

            // Add success and failure listeners to the hospitals query
            hospitalsQuery.addOnSuccessListener { hospitalResult ->
                // Check if the document exists in the "hospitals" collection
                val hospitalExists = !hospitalResult.isEmpty

                if (donorExists || hospitalExists) {
                    // User exists in either "donors" or "hospitals" collection
                    val userType = if (donorExists) DONOR else HOSPITAL
                    onComplete.invoke(true, userType)
                } else {
                    // User does not exist in either collection
                    onComplete.invoke(false, null)
                }
            }.addOnFailureListener { hospitalError ->
                // Handle any errors that occur during the hospitals query
                onComplete.invoke(false, hospitalError.message)

            }
        }.addOnFailureListener { donorError ->
            // Handle any errors that occur during the donors query
            onComplete.invoke(false, donorError.message)
        }
    }

    fun getPersonalDetails(
        userType: String,
        userId: String = "",
        onComplete: (User?, String?) -> Unit
    ) {
        val collection = if (userType == HOSPITAL) hospitalsCollection else donorsCollection

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
                        val hospital =
                            querySnapshot.documents.firstOrNull()?.toObject(Hospital::class.java)
                        hospital?.id = querySnapshot.documents.firstOrNull()?.id.toString()
                        hospital
                    } else {
                        val donor =
                            querySnapshot.documents.firstOrNull()?.toObject(Donor::class.java)
                        donor?.id = querySnapshot.documents.firstOrNull()?.id.toString()
                        donor
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

    fun getHospitalPersonalDetails(
        userId: String = "",
        onComplete: (Hospital?, String?) -> Unit
    ) {
        // Reference to the user's personal details collection
        val collection = hospitalsCollection.document(userId).collection(PERSONAL_DETAILS)


        // Retrieve personal details
        collection.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val hospital =
                        querySnapshot.documents.firstOrNull()?.toObject(Hospital::class.java)
                    hospital?.id = querySnapshot.documents.firstOrNull()?.id.toString()
                    onComplete.invoke(hospital, null)
                } else {
                    onComplete.invoke(null, NOT_AVAILABLE)
                }
            }
            .addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
            }
    }


    fun getAllHospitalDetails(
        onComplete: (List<Hospital>?, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        // Reference to the user's personal details collection
        val collection = FirestoreDB.instance
            .collectionGroup(PERSONAL_DETAILS)
            .whereNotEqualTo("userId", userId)
            .whereEqualTo("available", true)

        // Retrieve personal details
        collection.get()
            .addOnSuccessListener { querySnapshots ->
                val hospitals = querySnapshots.documents.mapNotNull { document ->
                    document.toObject(Hospital::class.java).apply {
                        this?.id = document.id
                    }
                }

                if (!querySnapshots.isEmpty) {
                    onComplete.invoke(hospitals, null)
                } else {
                    onComplete.invoke(null, NOT_AVAILABLE)
                }
            }
            .addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
            }
    }

    fun updatePersonalDetails(
        data: HashMap<String, Any>,
        userType: String,
        profileId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val collection = if (userType == HOSPITAL) hospitalsCollection else donorsCollection
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val updateQuery = collection.document(userId).collection(PERSONAL_DETAILS)
            updateQuery.document(profileId)
                .update(data)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener {
                    onComplete.invoke(false, it.message)
                }
        }
    }
}