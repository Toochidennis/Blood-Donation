package com.devtoochi.blood_donation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devtoochi.blood_donation.R

/**
 * A simple [Fragment] subclass.
 * Use the [DonorBloodGroupsBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DonorBloodGroupsBottomSheetFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_donor_blood_groups_bottom_sheet, container, false)
    }


}