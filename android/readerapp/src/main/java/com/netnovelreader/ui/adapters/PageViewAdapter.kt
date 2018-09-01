package com.netnovelreader.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PageViewAdapter(
    fm: androidx.fragment.app.FragmentManager,
    val titles: Array<String>,
    val types: Array<Class<out androidx.fragment.app.Fragment>>
) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    override fun getCount(): Int = titles.size

    override fun getItem(position: Int): androidx.fragment.app.Fragment = types[position].getConstructor().newInstance()

    override fun getPageTitle(position: Int): CharSequence? = titles[position]
}