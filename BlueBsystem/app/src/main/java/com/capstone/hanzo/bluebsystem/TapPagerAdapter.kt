package com.capstone.hanzo.bluebsystem

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class TapPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = arrayListOf(UserManagementFragment(), ReservationBusFragment(), PaymentManagementFragment())

    override fun getCount() = fragments.size

    override fun getItem(pos: Int) = fragments[pos]
}