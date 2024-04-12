package com.devtoochi.blood_donation.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.devtoochi.blood_donation.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class RequestSentDialog(context: Context, onDismiss:()->Unit) {
    private val dialog = Dialog(context)

    init {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.dialog_sent_successful)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val loadingView: ImageView = findViewById(R.id.imageview)
            val okayButton: ExtendedFloatingActionButton = findViewById(R.id.okay_button)

            when (val drawable = loadingView.drawable) {
                is AnimatedVectorDrawable -> drawable.start()

                is AnimatedVectorDrawableCompat -> drawable.start()
            }

            okayButton.setOnClickListener {
                onDismiss.invoke()
                dismiss()
            }
        }
    }

    fun show() = dialog.show()
}