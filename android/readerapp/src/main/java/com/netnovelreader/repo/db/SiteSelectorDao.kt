package com.netnovelreader.repo.db

import android.arch.paging.DataSource
import android.arch.persistence.room.*

@Dao
interface SiteSelectorDao {

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SS}")
    fun allSelectors(): DataSource.Factory<Int, SiteSelectorEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SS}")
    fun getAll(): List<SiteSelectorEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SS} WHERE ${ReaderDatabase.HOSTNAME} LIKE :hostname")
    fun getItem(hostname: String): SiteSelectorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entities: SiteSelectorEntity)

    @Delete
    fun delete(entity: SiteSelectorEntity)

    @Update
    fun update(entity: SiteSelectorEntity)
}