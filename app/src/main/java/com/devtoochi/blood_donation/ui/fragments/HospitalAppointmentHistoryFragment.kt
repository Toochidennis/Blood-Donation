package com.devtoochi.blood_donation.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.AppointmentManager.getAppointments
import com.devtoochi.blood_donation.backend.firebase.AuthenticationManager.auth
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getPersonalDetails
import com.devtoochi.blood_donation.backend.models.Appointment
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.utils.Util
import com.devtoochi.blood_donation.databinding.FragmentHospitalAppointmentHistoryBinding
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.squareup.picasso.Picasso


class HospitalAppointmentHistoryFragment : Fragment() {

    private lateinit var binding: FragmentHospitalAppointmentHistoryBinding
    private lateinit var loadingDialog: LoadingDialog
    private var appointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHospitalAppointmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        getAppointmentsHistory()
        refreshAppointment()
    }

    private fun getAppointmentsHistory() {
        try {
            appointments.clear()
            loadingDialog.show()

            getAppointments { appointments, message ->
                binding.emptyTextview.isVisible = false
                appointments?.let {
                    var requestsProcessed = 0 // Counter to track processed requests
                    val totalRequests = appointments.size

                    appointments.forEach { appointment ->
                        getUserDetails(appointment = appointment) {
                            requestsProcessed++
                            if (requestsProcessed == totalRequests) {
                                // All requests processed, setup adapter
                                setupAdapter()
                            }
                        }
                    }
                } ?: handleGetAppointmentsError(message)
                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun getUserDetails(
        appointment: Appointment,
        onComplete: () -> Unit
    ) {
        val userId = if (auth.currentUser?.uid == appointment.receiverId) {
            appointment.donorId
        } else {
            appointment.receiverId
        }

        getPersonalDetails(userType = appointment.userType, userId = userId) { user, message ->
            user?.let { userDetails ->
                val name = when (userDetails) {
                    is Hospital -> userDetails.name
                    is Donor -> "${userDetails.firstname} ${userDetails.lastname}"
                    else -> ""
                }

                appointments.add(
                    Appointment(
                        appointmentId = appointment.appointmentId,
                        donorId = appointment.donorId,
                        receiverId = appointment.receiverId,
                        appointmentDate = formatRequestDate(appointment.appointmentDate),
                        requestId = appointment.requestId,
                        name = name,
                        address = userDetails.address,
                        imageUrl = userDetails.imageUrl,
                        phone = userDetails.phone,
                        email = userDetails.email,
                        userType = appointment.userType
                    )
                )
            } ?: Log.d("response", "$message")

            onComplete.invoke()
        }
    }

    private fun handleGetAppointmentsError(message: String?) {
        Log.d("response", "Failed to get donors: $message")
        binding.emptyTextview.isVisible = true
    }

    private fun setupAdapter() {
        val picasso = Picasso.get()

        val appointmentAdapter = GenericAdapter(
            itemList = appointments,
            itemResLayout = R.layout.item_fragment_hospital_appointment_history,
            bindItem = { binding, model ->
                binding.setVariable(BR.appointment, model)
                binding.executePendingBindings()

                val imageview = binding.root.findViewById<ImageView>(R.id.imageview)
                val callButton = binding.root.findViewById<LinearLayout>(R.id.call_button)
                val emailButton = binding.root.findViewById<LinearLayout>(R.id.email_button)

                if (model.imageUrl.isEmpty()) {
                    imageview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                } else {
                    picasso.load(model.imageUrl).into(imageview)
                }

                callButton.setOnClickListener {
                    makePhoneCall(model.phone)
                }

                emailButton.setOnClickListener {
                    sendEmail(model.email)
                }
            }
        ) {}

        binding.appointmentHistoryRecyclerview.apply {
            hasFixedSize()
            adapter = appointmentAdapter
        }
    }

    private fun formatRequestDate(requestDate: String): String {
        return try {
            val splitDate = requestDate.split(":")
            val date = splitDate[0]
            val hour = splitDate[1].toInt()
            val minutes = splitDate[2].toInt()

            val formattedDate = Util.dateFormatter(date)
            val formattedTime = Util.formatTime(hour, minutes)

            "$formattedDate $formattedTime"
        } catch (e: Exception) {
            Log.e("formatRequestDate", "Error formatting request date: $requestDate", e)
            requestDate // Return the original string in case of an error
        }
    }

    private fun sendEmail(email: String) {
        if (email.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "")
                putExtra(Intent.EXTRA_TEXT, "")
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                startActivity(Intent.createChooser(intent, "Send Us a mail"))
            }
        } else {
            showToast("Email address not available")
        }
    }

    private fun makePhoneCall(phone: String) {
        if (phone.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            }
        } else {
            showToast("Phone number not available")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun refreshAppointment() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.red)
            setOnRefreshListener {
                getAppointmentsHistory()
                isRefreshing = false
            }
        }
    }
}