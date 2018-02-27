package com.netnovelreader.data.db

import android.arch.persistence.room.*

@Dao
interface SitePreferenceDao {
    @Query("SELECT * FROM sitepreference")
    fun getAll(): List<SitePreferenceBean>

    @Query("SELECT * FROM sitepreference WHERE hostname LIKE :hostname")
    fun getRule(hostname: String): SitePreferenceBean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg beans: SitePreferenceBean)

    @Delete
    fun delete(bean: SitePreferenceBean)
}