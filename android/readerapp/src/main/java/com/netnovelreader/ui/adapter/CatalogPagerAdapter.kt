package com.netnovelreader.ui.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.netnovelreader.ui.fragment.NovelListFragment

class CatalogPagerAdapter(fm: FragmentManager, major: String) : FragmentPagerAdapter(fm) {

    private val typeList = arrayOf("hot", "new", "reputation", "over")
    private val mMajor = major

    override fun getItem(position: Int): Fragment {
        val fragment = NovelListFragment()
        val bundle = Bundle()
        bundle.putString("type", typeList[position])
        bundle.putString("major", mMajor)
        fragment.arguments = bundle
        return fragment
    }


    override fun getCount(): Int = typeList.size

    override fun getPageTitle(position: Int): CharSequence? = when (typeList[position]) {
        "hot" -> "热门"
        "new" -> "新书"
        "reputation" -> "好评"
        "over" -> "完结"
        else -> "错误"
    }
}