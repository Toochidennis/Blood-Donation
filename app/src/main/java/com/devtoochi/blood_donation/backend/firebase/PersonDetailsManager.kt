package com.devtoochi.blood_donation.backend.firebase

import android.util.Log
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.donorsCollection
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.hospitalsCollection
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.devtoochi.blood_donation.backend.utils.Constants.NOT_AVAILABLE
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
        collection.add(user)
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
        val donorsQuery = donorsCollection.get()
        val hospitalsQuery = hospitalsCollection.get()

        // Add success and failure listeners to the donors query
        donorsQuery.addOnSuccessListener { donorResult ->
            // Check if the document exists in the "donors" collection
            var donorExists = false
            donorResult.documents.mapNotNull { document ->
                document.toObject(Donor::class.java)?.also {
                    if (it.userId == userId) donorExists = true
                }
            }

            // Add success and failure listeners to the hospitals query
            hospitalsQuery.addOnSuccessListener { hospitalResult ->
                // Check if the document exists in the "hospitals" collection
                var hospitalExists = false

                hospitalResult.documents.mapNotNull { document ->
                    document.toObject(Hospital::class.java)?.also {
                        if (it.userId == userId) hospitalExists = true
                    }
                }

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
                collection.whereEqualTo("userId", it)
            }
        } else {
            collection.whereEqualTo("userId", userId)
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

    fun getAllUsersDetails(
        userType: String,
        onComplete: (List<User>?, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        val db = FirestoreDB.instance
        // Reference to the user's personal details collection
        val query = if (userType == HOSPITAL) {
            db.collection(HOSPITAL).whereNotEqualTo("userId", userId)
                .whereNotEqualTo("available", false)
                .get()
        } else {
            db.collection(DONOR).whereNotEqualTo("userId", userId)
                .whereNotEqualTo("available", false)
                .get()
        }

        query
            .addOnSuccessListener { querySnapshot ->
                Log.d("response", "${querySnapshot.documents.size}  ${querySnapshot.isEmpty}")
                val user = if (userType == HOSPITAL) {
                    querySnapshot.documents.mapNotNull { documentSnapshot ->
                        documentSnapshot.toObject(Hospital::class.java)?.apply {
                            this.id = documentSnapshot.id
                        }
                    }
                } else {
                    querySnapshot.documents.mapNotNull { documentSnapshot ->
                        documentSnapshot.toObject(Donor::class.java)?.apply {
                            this.id = documentSnapshot.id
                        }
                    }
                }

                if (querySnapshot.isEmpty) {
                    onComplete.invoke(null, "Empty")
                } else {
                    onComplete.invoke(user, "Empty")
                }
            }
            .addOnFailureListener {
                onComplete.invoke(null, it.message)
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
            val updateQuery = collection.document(profileId)
            updateQuery.update(data)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener {
                    onComplete.invoke(false, it.message)
                }
        }
    }
}