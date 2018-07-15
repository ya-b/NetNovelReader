package com.netnovelreader.repo.db

import android.arch.paging.DataSource
import android.arch.persistence.room.*

@Dao
interface BookInfoDao {

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} order by ${ReaderDatabase.ORDER_NUM} DESC")
    fun allBooks(): DataSource.Factory<Int, BookInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} order by ${ReaderDatabase.ORDER_NUM} DESC")
    fun getAll(): List<BookInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getBookInfo(bookname: String): BookInfoEntity?

    @Query("SELECT max(${ReaderDatabase.ORDER_NUM}) from ${ReaderDatabase.TABLE_SHELF}")
    fun getMaxOrderNum(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entities: BookInfoEntity)

    @Delete
    fun delete(entity: BookInfoEntity)

    @Update
    fun update(entity: BookInfoEntity)
}