package com.devtoochi.blood_donation.ui.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devtoochi.blood_donation.BR
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.models.BloodGroup
import com.devtoochi.blood_donation.databinding.ItemFragmentBloodBankDetailsBinding

class BloodGroupAdapter2(
    private val itemList: MutableList<BloodGroup>,
    private var selectedBloodGroup: MutableList<String>
) : RecyclerView.Adapter<BloodGroupAdapter2.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            ItemFragmentBloodBankDetailsBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(private val binding: ItemFragmentBloodBankDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {


        @SuppressLint("NotifyDataSetChanged")
        fun bind(bloodGroup: BloodGroup) {
            binding.setVariable(BR.blood, bloodGroup)
            binding.executePendingBindings()

            val itemView = binding.root

            if (bloodGroup.isSelected) {
                itemView.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.drawable_button_5)
            } else {
                itemView.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.drawable_button_2)
            }

            itemView.setOnClickListener {
                deselectAll()
                bloodGroup.isSelected = true
                selectedBloodGroup.add(bloodGroup.name)
                notifyDataSetChanged()
            }
        }
    }

    private fun deselectAll(){
        selectedBloodGroup.clear()
        itemList.forEach {
            it.isSelected = false
        }
    }
}
