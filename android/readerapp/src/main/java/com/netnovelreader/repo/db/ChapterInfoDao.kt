package com.netnovelreader.repo.db

import android.arch.paging.DataSource
import android.arch.persistence.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface ChapterInfoDao {

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.BOOKNAME} LIKE " +
            ":bookname order by ${ReaderDatabase.CHAPTER_NUM} ASC")
    fun allChapters(bookname: String): DataSource.Factory<Int, ChapterInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.BOOKNAME} LIKE " +
            ":bookname order by ${ReaderDatabase.CHAPTER_NUM} ASC")
    fun getAll(bookname: String): Maybe<List<ChapterInfoEntity>>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.CHAPTER_NUM} " +
            "= :num AND ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getChapterInfo(bookname: String, num: Int): Single<ChapterInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG} WHERE ${ReaderDatabase.CHAPTER_NAME} " +
            "= :chapterName AND ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun getChapterInfo(bookname: String, chapterName: String): Single<ChapterInfoEntity>

    @Query("SELECT * FROM ${ReaderDatabase.TABLE_CATALOG}  WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname AND ${ReaderDatabase.CHAPTER_NUM} BETWEEN :start AND :end")
    fun getRangeChapter(bookname: String, start: Int, end: Int): Maybe<List<ChapterInfoEntity>>

    @Query("DELETE FROM ${ReaderDatabase.TABLE_CATALOG}  WHERE ${ReaderDatabase.BOOKNAME} LIKE :bookname")
    fun deleteBook(bookname: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg entities: ChapterInfoEntity)

    @Update
    fun update(entity: ChapterInfoEntity)


    @Delete
    fun delete(entity: ChapterInfoEntity)
}