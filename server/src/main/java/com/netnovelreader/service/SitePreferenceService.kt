package com.netnovelreader.service

import com.netnovelreader.dao.SitePreferenceDao
import com.netnovelreader.model.SitePreferenceBean
import javax.servlet.http.HttpServletRequest

class SitePreferenceService {
    fun addPreference(bean: SitePreferenceBean?) =
        if (bean == null) {
            false
        } else {
            SitePreferenceDao().addPreference(bean)
            true
        }

    fun getPreference(hostname: String?): List<SitePreferenceBean>? =
        if (hostname.isNullOrEmpty() || hostname == "*") {
            SitePreferenceDao().getAllPreference()
        } else {
            SitePreferenceDao().getPreference(hostname!!)?.let { listOf(it) }
        }

    fun deletePreference(hostname: String?) {
        if (hostname == "*") {
            SitePreferenceDao().deleteAllPreference()
        } else if (!hostname.isNullOrEmpty()) {
            SitePreferenceDao().deletePreference(hostname!!)
        }
    }

    fun getBean(req: HttpServletRequest): SitePreferenceBean? {
        val hostname = req.getParameter("hostname") ?: return null
        return SitePreferenceBean().also {
            it.hostname = hostname
            it.catalog_selector = req.getParameter("catalog_selector")
            it.chapter_selector = req.getParameter("chapter_selector")
            it.catalog_filter = req.getParameter("catalog_filter")
            it.chapter_filter = req.getParameter("chapter_filter")
            it.search_url = req.getParameter("search_url")
            it.redirect_fileld = req.getParameter("redirect_fileld")
            it.redirect_url = req.getParameter("redirect_url")
            it.no_redirect_url = req.getParameter("no_redirect_url")
            it.redirect_name = req.getParameter("redirect_name")
            it.no_redirect_name = req.getParameter("no_redirect_name")
            it.redirect_image = req.getParameter("redirect_image")
            it.no_redirect_image = req.getParameter("no_redirect_image")
            it.charset = req.getParameter("charset")
        }
    }
}