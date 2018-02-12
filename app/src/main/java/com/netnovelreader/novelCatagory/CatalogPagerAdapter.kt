package com.netnovelreader.novelCatagory

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * 文件： CatalogPagerAdapter
 * 描述：
 * 作者： YangJunQuan   2018-2-11.
 */

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


    override fun getCount(): Int {
        return typeList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return translateTile(typeList[position])
    }

    private fun translateTile(origin: String? = null) = when (origin) {
        "hot" -> "热门"
        "new" -> "新书"
        "reputation" -> "好评"
        "over" -> "完结"
        else -> "错误"
    }
}