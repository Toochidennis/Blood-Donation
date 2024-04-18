package com.devtoochi.blood_donation.ui.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.getAllUsersDetails
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.databinding.FragmentEmergencyDonorsBinding
import com.devtoochi.blood_donation.ui.adapters.GenericAdapter
import com.devtoochi.blood_donation.ui.dialogs.LoadingDialog
import com.squareup.picasso.Picasso


class EmergencyDonorsDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentEmergencyDonorsBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEmergencyDonorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        getDonors()

        binding.navigateUp.setOnClickListener {
            dismiss()
        }
    }

    private fun getDonors() {
        loadingDialog.show()
        try {
            getAllUsersDetails(userType = DONOR) { donors, message ->
                if (donors != null && donors[0] is Donor) {
                    @Suppress("UNCHECKED_CAST")
                    setupAdapter(donors = donors as List<Donor>)
                    loadingDialog.dismiss()
                } else {
                    loadingDialog.dismiss()
                    Log.d("response", "$message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
        }
    }

    private fun setupAdapter(donors: List<Donor>) {
        val picasso = Picasso.get()
        val bloodBankAdapter = GenericAdapter(
            itemList = donors.toMutableList(),
            itemResLayout = R.layout.item_fragment_blood_banks,
            bindItem = { binding, model ->
                binding.setVariable(BR.hospital, model)
                binding.executePendingBindings()

                val imageview = binding.root.findViewById<ImageView>(R.id.imageview)
                if (model.imageUrl.isEmpty()) {
                    imageview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                } else {
                    picasso.load(model.imageUrl).into(imageview)
                }
            }
        ) { position ->
            EmergencyDonorDetailsFragment(donors[position]).show(
                parentFragmentManager,
                getString(R.string.emergency_donors)
            )
        }

        binding.emergencyDonorsRecyclerview.apply {
            hasFixedSize()
            adapter = bloodBankAdapter
        }
    }

}