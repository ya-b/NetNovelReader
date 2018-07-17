package com.netnovelreader.repo.db

import android.arch.paging.DataSource
import android.arch.persistence.room.*

@Dao
interface ChapterInfoDao {

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.BOOKNAME} LIKE " +
            ":bookname order by ${ReaderDatabase.CHAPTER_NUM} ASC")
    fun allChapters(bookname: String): DataSource.Factory<Int, ChapterInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.BOOKNAME} LIKE " +
            ":bookname order by ${ReaderDatabase.CHAPTER_NUM} ASC")
    fun getAll(bookname: String): List<ChapterInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.CHAPTER_NUM} " +
            "= :num AND ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getChapterInfo(bookname: String, num: Int): ChapterInfoEntity?

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.CHAPTER_NAME} " +
            "= :chapterName AND ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getChapterInfo(bookname: String, chapterName: String): ChapterInfoEntity?

    @Query("SELECT MAX(${ReaderDatabase.CHAPTER_NUM}) FROM ${ReaderDatabase.TABLE_CATALOG} " +
            "WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getMaxChapterNum(bookname: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entities: ChapterInfoEntity)

    @Update
    fun update(entity: ChapterInfoEntity)


    @Delete
    fun delete(entity: ChapterInfoEntity)
}