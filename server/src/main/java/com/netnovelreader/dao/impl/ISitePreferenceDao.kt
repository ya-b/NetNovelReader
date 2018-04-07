package com.netnovelreader.dao.impl

import com.netnovelreader.model.SitePreferenceBean

interface ISitePreferenceDao {
    fun addPreference(bean: SitePreferenceBean): Int
    fun deletePreference(hostname: String)
    fun deleteAllPreference()
    fun getPreference(hostname: String): SitePreferenceBean?
    fun getAllPreference(): List<SitePreferenceBean>?
}