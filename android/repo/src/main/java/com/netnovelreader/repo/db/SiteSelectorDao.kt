package com.netnovelreader.repo.db

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Single

@Dao
interface SiteSelectorDao {

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SS}")
    fun allSelectors(): DataSource.Factory<Int, SiteSelectorEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SS}")
    fun getAll(): Single<List<SiteSelectorEntity>>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SS} WHERE ${ReaderDatabase.HOSTNAME} LIKE :hostname")
    fun getItem(hostname: String): Single<SiteSelectorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entities: SiteSelectorEntity)

    @Delete
    fun delete(entity: SiteSelectorEntity)

    @Update
    fun update(entity: SiteSelectorEntity)
}