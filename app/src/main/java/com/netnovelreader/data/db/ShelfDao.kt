package com.netnovelreader.data.db

import android.arch.persistence.room.*

@Dao
interface ShelfDao {

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} order by ${ReaderDatabase.LATESTREAD} DESC")
    fun getAll(): List<ShelfBean>?

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getBookInfo(bookname: String): ShelfBean?

    @Query("SELECT max(${ReaderDatabase.LATESTREAD}) from ${ReaderDatabase.TABLE_SHELF}")
    fun getLatestReaded(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg beans: ShelfBean)

    @Delete
    fun delete(bean: ShelfBean)
}