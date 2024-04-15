package com.devtoochi.blood_donation.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.models.DonationRequest
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class DonationSuccessDialog(
    context: Context,
    fragmentManager: FragmentManager,
    donationRequest: DonationRequest
) {
    private val dialog = Dialog(context)

    init {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_donation_successful)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val dismissButton: TextView = findViewById(R.id.dismiss_button)
            val okayButton: ExtendedFloatingActionButton = findViewById(R.id.okay_button)

            dismissButton.setOnClickListener {
                dismiss()
            }

            okayButton.setOnClickListener {
                BookAppointmentDialogFragment(donationRequest).show(
                    fragmentManager,
                    context.getString(R.string.appointment)
                )
                dismiss()
            }
        }
    }

    fun show() = dialog.show()
}