package com.netnovelreader.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(ShelfBean::class, SitePreferenceBean::class), version = 1)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun shelfDao(): ShelfDao
    abstract fun sitePreferenceDao(): SitePreferenceDao
}