package com.netnovelreader.repo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
        tableName = ReaderDatabase.TABLE_SHELF,
        indices = [(Index(value = arrayOf(ReaderDatabase.BOOKNAME), unique = true))]
)
data class BookInfoEntity(
    @PrimaryKey @field:SerializedName("i")
    var _id: Int? = null,
    @ColumnInfo(name = ReaderDatabase.BOOKNAME) @field:SerializedName("b")
    var bookname: String,
    @ColumnInfo(name = ReaderDatabase.DOWNLOAD_URL) @field:SerializedName("d")
    var downloadUrl: String,
    @ColumnInfo(name = ReaderDatabase.READ_RECORD) @field:SerializedName("r")
    var readRecord: String,
    @ColumnInfo(name = ReaderDatabase.HAS_UPDATE) @field:SerializedName("s")
    var hasUpdate: Boolean,
    @ColumnInfo(name = ReaderDatabase.LATEST_CHAPTER) @field:SerializedName("l")
    var latestChapter: String,
    @ColumnInfo(name = ReaderDatabase.ORDER_NUM) @field:SerializedName("a")
    var orderNumber: Int,
    @ColumnInfo(name = ReaderDatabase.COVER_PATH) @field:SerializedName("c")
    var coverPath: String
)