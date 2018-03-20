package com.netnovelreader.common

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class PagerAdapter(
        fm: FragmentManager,
        val titles: Array<String>,
        val types: Array<Class<out Fragment>>
) :
        FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return titles.size
    }

    override fun getItem(position: Int): Fragment = types[position].getConstructor().newInstance()

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}