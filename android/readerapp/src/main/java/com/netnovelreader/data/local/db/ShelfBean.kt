package com.netnovelreader.data.local.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
        tableName = ReaderDatabase.TABLE_SHELF,
        indices = [(Index(value = arrayOf(ReaderDatabase.BOOKNAME), unique = true))]
)
data class ShelfBean(
    @PrimaryKey @field:SerializedName("i")
    var _id: Int? = null,
    @ColumnInfo(name = ReaderDatabase.BOOKNAME) @field:SerializedName("b")
    var bookName: String?,
    @ColumnInfo(name = ReaderDatabase.DOWNLOADURL) @field:SerializedName("d")
    var downloadUrl: String? = null,
    @ColumnInfo(name = ReaderDatabase.READRECORD) @field:SerializedName("r")
    var readRecord: String? = null,
    @ColumnInfo(name = ReaderDatabase.ISUPDATE) @field:SerializedName("s")
    var isUpdate: String? = null,
    @ColumnInfo(name = ReaderDatabase.LATESTCHAPTER) @field:SerializedName("l")
    var latestChapter: String? = null,
    @ColumnInfo(name = ReaderDatabase.LATESTREAD) @field:SerializedName("a")
    var latestRead: Int? = null
)