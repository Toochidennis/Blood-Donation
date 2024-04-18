package com.devtoochi.blood_donation.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments = mutableListOf<Fragment>()

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
    }
}