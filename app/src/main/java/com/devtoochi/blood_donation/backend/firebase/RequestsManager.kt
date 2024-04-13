package com.devtoochi.blood_donation.backend.firebase

import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.backend.utils.Constants.GENERAL

object RequestsManager {

    private val requestsCollection = FirestoreDB.instance.collection(Constants.REQUESTS)

    fun postBloodRequest(bloodRequest: BloodRequest, onComplete: (Boolean, String?) -> Unit) {
        requestsCollection.add(bloodRequest)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete.invoke(true, "Request sent successfully")
                } else {
                    onComplete.invoke(false, task.exception?.message)
                }
            }
    }

    fun updateBloodRequest(
        data: HashMap<String, Any>,
        requestId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        requestsCollection.document(requestId)
            .update(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete.invoke(true, "Request updated successfully")
                } else {
                    onComplete.invoke(false, task.exception?.message)
                }
            }
    }

    fun getBloodRequestById(
        userId: String,
        onComplete: (List<BloodRequest>?, String?) -> Unit
    ) {
        requestsCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshots ->
                val requestDataList = querySnapshots.documents.mapNotNull { document ->
                    val requestId = document.id
                    document.toObject(BloodRequest::class.java)?.apply {
                        this.requestId = requestId
                    }
                }

                if (querySnapshots.isEmpty) {
                    onComplete(null, "Empty")
                } else {
                    onComplete(requestDataList, null)
                }
            }
            .addOnFailureListener { exception ->
                onComplete(null, exception.message)
            }
    }

    fun getAllBloodRequests(
        userId: String,
        onComplete: (List<BloodRequest?>?, String?) -> Unit
    ) {
        requestsCollection
            .whereNotEqualTo("userId", userId)
            .whereEqualTo("donorId", userId)
            .whereEqualTo("requestType", GENERAL)
            .get()
            .addOnSuccessListener { querySnapshots ->
                val requestDataList = querySnapshots.documents.mapNotNull { document ->
                    val requestId = document.id
                    document.toObject(BloodRequest::class.java)?.apply {
                        this.requestId = requestId
                    }
                }

                if (querySnapshots.isEmpty) {
                    onComplete(null, "Empty")
                } else {
                    onComplete(requestDataList, null)
                }
            }
            .addOnFailureListener { exception ->
                onComplete(null, exception.message)
            }
    }
}