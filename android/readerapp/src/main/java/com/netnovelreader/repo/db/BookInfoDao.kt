package com.netnovelreader.repo.db

import android.arch.paging.DataSource
import android.arch.persistence.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface BookInfoDao {

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} order by ${ReaderDatabase.ORDER_NUM} DESC")
    fun allBooks(): DataSource.Factory<Int, BookInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} order by ${ReaderDatabase.ORDER_NUM} DESC")
    fun getAll(): Maybe<List<BookInfoEntity>>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_SHELF} WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getBookInfo(bookname: String): Single<BookInfoEntity>

    @Query("SELECT max(${ReaderDatabase.ORDER_NUM}) from ${ReaderDatabase.TABLE_SHELF}")
    fun getMaxOrderNum(): Maybe<Int>

    @Query("update ${ReaderDatabase.TABLE_SHELF} set ${ReaderDatabase.ORDER_NUM}=(" +
            "select * from (select ifnull((" +
            "select max(${ReaderDatabase.ORDER_NUM}) from ${ReaderDatabase.TABLE_SHELF}), 0)) as t" +
            ") + 1, ${ReaderDatabase.HAS_UPDATE} = 0 where  ${ReaderDatabase.BOOKNAME} LIKE :bookname;")
    fun setMaxOrderToBook(bookname: String)

    @Query("update ${ReaderDatabase.TABLE_SHELF} set ${ReaderDatabase.READ_RECORD}=:record " +
            "WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun setRecord(bookname: String, record: String)

    @Query("DELETE FROM ${ReaderDatabase.TABLE_SHELF} WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun delete(bookname: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entities: BookInfoEntity)

    @Delete
    fun delete(entity: BookInfoEntity)

    @Update
    fun update(entity: BookInfoEntity)
}