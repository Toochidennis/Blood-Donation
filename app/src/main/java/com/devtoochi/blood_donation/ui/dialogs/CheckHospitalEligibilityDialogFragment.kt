package com.devtoochi.blood_donation.ui.dialogs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.databinding.FragmentCheckHospitalEligibilityDialogBinding
import com.devtoochi.blood_donation.ui.activities.HospitalDashboardActivity


class CheckHospitalEligibilityDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentCheckHospitalEligibilityDialogBinding
    private var requiredScore = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCheckHospitalEligibilityDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.checkEligibilityButton.setOnClickListener {
            if (requiredScore > getScore()) {
                Toast.makeText(
                    requireContext(),
                    "You're not eligible to donate blood",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startActivity(Intent(requireContext(), HospitalDashboardActivity::class.java))
                dismiss()
            }
        }
    }

    private fun getScore(): Int {
        var score = 0

        // Update score based on the checked state of each button
        if (binding.yesButton1.isChecked) score += 2
        if (binding.yesButton2.isChecked) score += 2
        if (binding.yesButton3.isChecked) score += 2
        if (binding.yesButton4.isChecked) score += 2
        if (binding.yesButton5.isChecked) score += 2
        if (binding.yesButton6.isChecked) score += 2
        if (binding.yesButton7.isChecked) score += 2
        if (binding.yesButton8.isChecked) score += 2

        if (binding.noButton1.isChecked) score -= 2
        if (binding.noButton2.isChecked) score -= 2
        if (binding.noButton3.isChecked) score -= 2
        if (binding.noButton4.isChecked) score -= 2
        if (binding.noButton5.isChecked) score -= 2
        if (binding.noButton6.isChecked) score -= 2
        if (binding.noButton7.isChecked) score -= 2
        if (binding.noButton8.isChecked) score -= 2

        return score
    }

}