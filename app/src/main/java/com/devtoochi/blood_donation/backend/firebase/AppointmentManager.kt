package com.devtoochi.blood_donation.backend.firebase

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

}