package com.devtoochi.blood_donation.backend.firebase

import com.devtoochi.blood_donation.backend.models.BloodRequest
import com.devtoochi.blood_donation.backend.utils.Constants
import com.devtoochi.blood_donation.backend.utils.Constants.GENERAL
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot

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
        requestType:String,
        userId: String,
        onComplete: (List<BloodRequest>?, String?) -> Unit
    ) {
        val query1 = requestsCollection
            .whereNotEqualTo("userId", userId)
            .whereEqualTo("requestType", GENERAL)
            .whereEqualTo("status", "Pending")
            .get()

        val query2 = requestsCollection
            .whereEqualTo("donorId", userId)
            .whereEqualTo("requestType", requestType)
            .whereEqualTo("status", "Pending")
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(query1, query2)
            .addOnSuccessListener { results ->
                val requestDataList = mutableListOf<BloodRequest>()
                val uniqueIds = HashSet<String>()

                results.forEach { querySnapshot ->
                    querySnapshot.documents.forEach { document ->
                        val requestId = document.id
                        if (uniqueIds.add(requestId)) {
                            // Only add the document to the list if its ID is not already in the set
                            val bloodRequest = document.toObject(BloodRequest::class.java)?.apply {
                                this.requestId = requestId
                            }
                            bloodRequest?.let { requestDataList.add(it) }
                        }
                    }
                }

                if (requestDataList.isEmpty()) {
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