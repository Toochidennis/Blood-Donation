package com.devtoochi.blood_donation.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.fragment.app.FragmentManager
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class WelcomeDialog(from: String, context: Context, fragmentManager: FragmentManager) {

    private val dialog = Dialog(context)

    init {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_welcome)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val completeButton: ExtendedFloatingActionButton = findViewById(R.id.complete_button)

            completeButton.setOnClickListener {
                if (from == HOSPITAL) {
                    HospitalEditProfileDialogFragment().show(
                        fragmentManager,
                        context.getString(R.string.edit_profile)
                    )
                } else {
                    DonorProfileEditProfileDialogFragment().show(
                        fragmentManager,
                        context.getString(R.string.edit_profile)
                    )
                }
                dismiss()
            }

        }
    }

    fun show() = dialog.show()

    //fun dismiss() = dialog.dismiss()

}