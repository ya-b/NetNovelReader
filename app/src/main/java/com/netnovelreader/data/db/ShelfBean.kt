package com.netnovelreader.data.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = ReaderDatabase.TABLE_SHELF,
        indices = arrayOf(Index(value = arrayOf(ReaderDatabase.BOOKNAME), unique = true))
)
data class ShelfBean(
        @PrimaryKey var _id: Int? = null,
        @ColumnInfo(name = ReaderDatabase.BOOKNAME) var bookName: String?,
        @ColumnInfo(name = ReaderDatabase.DOWNLOADURL) var downloadUrl: String? = null,
        @ColumnInfo(name = ReaderDatabase.READRECORD) var readRecord: String? = null,
        @ColumnInfo(name = ReaderDatabase.ISUPDATE) var isUpdate: String? = null,
        @ColumnInfo(name = ReaderDatabase.LATESTCHAPTER) var latestChapter: String? = null,
        @ColumnInfo(name = ReaderDatabase.LATESTREAD) var latestRead: Int? = null
)