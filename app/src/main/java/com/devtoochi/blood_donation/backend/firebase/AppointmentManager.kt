package com.devtoochi.blood_donation.backend.firebase

import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.models.Appointment
import com.devtoochi.blood_donation.backend.utils.Constants.APPOINTMENTS

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
        appointmentsCollection
            .whereEqualTo("donorId", auth.currentUser?.uid)
            .whereEqualTo("receiverId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val appointments = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Appointment::class.java)?.apply {
                        this.appointmentId = document.id
                    }
                }

                if (appointments.isEmpty()) {
                    onComplete.invoke(null, "Empty")
                } else {
                    onComplete.invoke(appointments, null)
                }
            }
            .addOnFailureListener {
                onComplete.invoke(null, it.message)
            }
    }
}