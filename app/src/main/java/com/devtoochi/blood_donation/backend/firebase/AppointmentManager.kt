package com.devtoochi.blood_donation.backend.firebase

import android.util.Log
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.models.Appointment
import com.devtoochi.blood_donation.backend.utils.Constants.APPOINTMENTS
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot

object AppointmentManager {

    private val appointmentsCollection = FirestoreDB.instance.collection(APPOINTMENTS)

    fun bookAppointment(appointment: Appointment, onComplete: (Boolean, String?) -> Unit) {
        appointmentsCollection.add(appointment)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete.invoke(true, null)
                } else {
                    onComplete.invoke(false, task.exception?.message)
                }
            }
    }

    fun getAppointments(onComplete: (List<Appointment>?, String?) -> Unit) {
        Log.d("response", "id :${auth.currentUser?.uid}")
        val query1 = appointmentsCollection
            .whereEqualTo("donorId", auth.currentUser?.uid)
            .get()
        val query2 = appointmentsCollection
            .whereEqualTo("receiverId", auth.currentUser?.uid)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(query1, query2)
            .addOnSuccessListener { results ->
                Log.d("response", "result :${results.size}")

                val appointmentList = mutableListOf<Appointment>()
                val uniqueIds = HashSet<String>()

                results.forEach { querySnapshot ->
                    querySnapshot.documents.mapNotNull { document ->
                        val appointmentId = document.id
                        if (uniqueIds.add(appointmentId)) {
                            val appointments = document.toObject(Appointment::class.java)?.apply {
                                this.appointmentId = document.id
                            }
                            appointments?.let { appointmentList.add(it) }
                        }
                    }
                }

                if (appointmentList.isEmpty()) {
                    onComplete.invoke(null, "Empty")
                } else {
                    onComplete.invoke(appointmentList, null)
                }
            }
            .addOnFailureListener {
                onComplete.invoke(null, it.message)
            }
    }
}