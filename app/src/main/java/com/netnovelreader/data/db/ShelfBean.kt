package com.netnovelreader.data.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "shelf", indices=arrayOf(Index(value=arrayOf("book_name"), unique=true)))
data class ShelfBean(
        @PrimaryKey var _id: Int? = null,
        @ColumnInfo(name = "book_name") var bookName: String?,
        @ColumnInfo(name = "download_url") var downloadUrl: String? = null,
        @ColumnInfo(name = "read_Record") var readRecord: String? = null,
        @ColumnInfo(name = "is_update") var isUpdate: String? = null,
        @ColumnInfo(name = "latest_Chapter") var latestChapter: String? = null,
        @ColumnInfo(name = "latest_read") var latestRead: Int? = null
)