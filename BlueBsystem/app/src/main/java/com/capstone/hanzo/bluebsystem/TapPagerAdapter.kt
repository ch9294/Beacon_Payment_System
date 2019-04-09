package com.capstone.hanzo.bluebsystem

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class TapPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = ArrayList<Fragment>()

    init {
        fragments.apply {
            add(UserManagementFragment())
            add(ReservationBusFragment())
            add(PaymentManagementFragment())
        }
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(pos: Int): Fragment {
        return fragments[pos]
    }
}