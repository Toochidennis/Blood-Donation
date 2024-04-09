package com.devtoochi.blood_donation.dialogs

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import com.devtoochi.blood_donation.R
import com.google.android.material.card.MaterialCardView

class LoadingDialog(context: Context) {

    private val dialog = Dialog(context)

    init {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialog_loading)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val loadingView: MaterialCardView = findViewById(R.id.loading_view)

            animateView(loadingView, "scaleX")
            animateView(loadingView, "scaleY")
        }
    }

    fun show() = dialog.show()

    fun dismiss() = dialog.dismiss()

    private fun animateView(view: View, propertyName: String) {
        ObjectAnimator.ofFloat(view, propertyName, 1f, 1.2f).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

}