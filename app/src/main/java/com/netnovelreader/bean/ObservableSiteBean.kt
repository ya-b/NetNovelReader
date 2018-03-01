package com.netnovelreader.bean

import android.databinding.ObservableField
import com.netnovelreader.data.db.SitePreferenceBean

data class ObservableSiteBean(
    var hostname: ObservableField<String> = ObservableField(),
    var catalogSelector: ObservableField<String> = ObservableField(),
    var chapterSelector: ObservableField<String> = ObservableField(),
    var catalogFilter: ObservableField<String> = ObservableField(),
    var chapterFilter: ObservableField<String> = ObservableField(),
    var searchUrl: ObservableField<String> = ObservableField(),
    var redirectFileld: ObservableField<String> = ObservableField(),
    var redirectUrl: ObservableField<String> = ObservableField(),
    var noRedirectUrl: ObservableField<String> = ObservableField(),
    var redirectName: ObservableField<String> = ObservableField(),
    var noRedirectName: ObservableField<String> = ObservableField(),
    var redirectImage: ObservableField<String> = ObservableField(),
    var noRedirectImage: ObservableField<String> = ObservableField(),
    var charset: ObservableField<String> = ObservableField()

) {
    fun addAll(bean: SitePreferenceBean) {
        hostname.set(bean.hostname)
        catalogSelector.set(bean.catalogSelector)
        chapterSelector.set(bean.chapterSelector)
        catalogFilter.set(bean.catalogFilter)
        chapterFilter.set(bean.chapterFilter)
        searchUrl.set(bean.searchUrl)
        redirectFileld.set(bean.redirectFileld)
        redirectUrl.set(bean.redirectUrl)
        noRedirectUrl.set(bean.noRedirectUrl)
        redirectName.set(bean.redirectName)
        noRedirectName.set(bean.noRedirectName)
        redirectImage.set(bean.redirectImage)
        noRedirectImage.set(bean.noRedirectImage)
        charset.set(bean.charset)
    }
}