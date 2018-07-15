package com.netnovelreader.repo.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = ReaderDatabase.TABLE_CATALOG)
data class ChapterInfoEntity (
        @PrimaryKey
        var _id: Long? = null,
        @ColumnInfo(name = ReaderDatabase.CHAPTER_NUM)
        var chapterNum: Int,
        @ColumnInfo(name = ReaderDatabase.BOOKNAME)
        var bookname: String,
        @ColumnInfo(name = ReaderDatabase.CHAPTER_NAME)
        var chapterName: String,
        @ColumnInfo(name = ReaderDatabase.CHAPTER_URL)
        var chapterUrl: String,
        @ColumnInfo(name = ReaderDatabase.IS_DOWNLOADED)
        var isDownloaded: Int
)