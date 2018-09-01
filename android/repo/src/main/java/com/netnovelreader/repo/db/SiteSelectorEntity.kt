package com.netnovelreader.repo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
        tableName = ReaderDatabase.TABLE_SS,
        indices = [(Index(value = arrayOf(ReaderDatabase.HOSTNAME), unique = true))]
)
data class SiteSelectorEntity(
        @PrimaryKey @field:SerializedName("i")
        var _id: Int? = null,
        @ColumnInfo(name = ReaderDatabase.HOSTNAME) @field:SerializedName("h")
        var hostname: String,                 //域名
        @ColumnInfo(name = ReaderDatabase.CATALOG_SELECTOR) @field:SerializedName("cs")
        var catalogSelector: String,  //目录选择器
        @ColumnInfo(name = ReaderDatabase.CHAPTER_SELECTOR) @field:SerializedName("hs")
        var chapterSelector: String,  //章节内容选择器
        @ColumnInfo(name = ReaderDatabase.CATALOG_FILTER) @field:SerializedName("cf")
        var catalogFilter: String,      //过滤掉目录中的内容
        @ColumnInfo(name = ReaderDatabase.CHAPTER_FILTER) @field:SerializedName("hf")
        var chapterFilter: String,      //过滤掉章节中的内容
        @ColumnInfo(name = ReaderDatabase.SEARCH_URL) @field:SerializedName("su")
        var searchUrl: String,               //搜索网址
        @ColumnInfo(name = ReaderDatabase.REDIRECT_FILELD) @field:SerializedName("rf")
        var redirectFileld: String,     //重定至Header->field
        @ColumnInfo(name = ReaderDatabase.REDIRECT_URL) @field:SerializedName("ru")
        var redirectUrl: String,      //重定向搜索结果URL选择器
        @ColumnInfo(name = ReaderDatabase.NO_REDIRECT_URL) @field:SerializedName("nu")
        var noRedirectUrl: String,  //搜索结果URL选择器
        @ColumnInfo(name = ReaderDatabase.REDIRECT_NAME) @field:SerializedName("rn")
        var redirectName: String,         //重定向搜索结果书名选择器
        @ColumnInfo(name = ReaderDatabase.NO_REDIRECT_NAME) @field:SerializedName("nn")
        var noRedirectName: String,     //搜索结果书名选择器
        @ColumnInfo(name = ReaderDatabase.REDIRECT_IMAGE) @field:SerializedName("ri")
        var redirectImage: String,       //重定向搜索结果图片选择器
        @ColumnInfo(name = ReaderDatabase.NO_REDIRECT_IMAGE) @field:SerializedName("ni")
        var noRedirectImage: String,   //搜索结果图片选择器
        @ColumnInfo(name = ReaderDatabase.CHARSET) @field:SerializedName("ct")
        var charset: String                    //URLEncode（"书名",charset）
)