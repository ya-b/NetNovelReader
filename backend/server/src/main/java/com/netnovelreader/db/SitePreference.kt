package com.netnovelreader.db

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class SitePreference(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SitePreference>(SitePreferences)
        var hostname by SitePreferences.hostname
        var catalog_selector by SitePreferences.catalog_selector
        var chapter_selector by SitePreferences.chapter_selector
        var catalog_filter by SitePreferences.catalog_filter
        var chapter_filter by SitePreferences.chapter_filter
        var search_url by SitePreferences.search_url
        var redirect_fileld by SitePreferences.redirect_fileld
        var redirect_url by SitePreferences.redirect_url
        var no_redirect_url by SitePreferences.no_redirect_url
        var redirect_name by SitePreferences.redirect_name
        var no_redirect_name by SitePreferences.no_redirect_name
        var redirect_image by SitePreferences.redirect_image
        var no_redirect_image by SitePreferences.no_redirect_image
        var charset by SitePreferences.charset
}