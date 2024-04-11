package com.devtoochi.blood_donation.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.models.BloodGroup
import com.devtoochi.blood_donation.backend.utils.Constants.DESELECTED
import com.devtoochi.blood_donation.backend.utils.Constants.SELECTED
import com.devtoochi.blood_donation.databinding.ItemFragmentHospitalBloodGroupsBottomSheetBinding

class BloodGroupAdapter(
    private val itemList: MutableList<BloodGroup>,
    private val selectedItems: HashMap<String, String>
) : RecyclerView.Adapter<BloodGroupAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            ItemFragmentHospitalBloodGroupsBottomSheetBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(private val binding: ItemFragmentHospitalBloodGroupsBottomSheetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(bloodGroup: BloodGroup) {
            binding.setVariable(BR.blood, bloodGroup)
            binding.executePendingBindings()

            val itemView = binding.root
            itemView.isSelected = bloodGroup.isSelected

            if (itemView.isSelected) {
                setBackgroundDrawable(binding.bloodGroupLayout, SELECTED)
            } else {
                setBackgroundDrawable(binding.bloodGroupLayout, DESELECTED)
            }

            itemView.setOnClickListener {
                bloodGroup.isSelected = !bloodGroup.isSelected
                itemView.isSelected = bloodGroup.isSelected

                if (itemView.isSelected) {
                    selectedItems[bloodGroup.id] = bloodGroup.name
                } else {
                    if (selectedItems.containsKey(bloodGroup.id)) {
                        selectedItems.remove(bloodGroup.id)
                    }
                }

                notifyDataSetChanged()
            }
        }

        private fun setBackgroundDrawable(view: View, selectionState: String) {
            if (selectionState == SELECTED)
                view.background =
                    ContextCompat.getDrawable(view.context, R.drawable.drawable_left_curved_2)
            else
                view.background =
                    ContextCompat.getDrawable(view.context, R.drawable.drawable_left_curved_1)
        }

    }

}