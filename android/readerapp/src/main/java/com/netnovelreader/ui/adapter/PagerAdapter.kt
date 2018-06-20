package com.netnovelreader.ui.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class PagerAdapter(
        fm: FragmentManager,
        val titles: Array<String>,
        val types: Array<Class<out Fragment>>
) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int = titles.size

    override fun getItem(position: Int): Fragment = types[position].getConstructor().newInstance()

    override fun getPageTitle(position: Int): CharSequence? = titles[position]
}