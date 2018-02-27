package com.netnovelreader.data.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = ReaderDatabase.TABLE_SP,
        indices=arrayOf(Index(value=arrayOf(ReaderDatabase.HOSTNAME), unique=true))
)
data class SitePreferenceBean(
        @PrimaryKey var _id: Int = 0,
        @ColumnInfo(name = ReaderDatabase.HOSTNAME) var hostname: String,                 //域名
        @ColumnInfo(name = ReaderDatabase.CATALOG_SELECTOR) var catalogSelector: String,  //目录选择器
        @ColumnInfo(name = ReaderDatabase.CHAPTER_SELECTOR) var chapterSelector: String,  //章节内容选择器
        @ColumnInfo(name = ReaderDatabase.CATALOG_FILTER) var catalogFilter: String,      //过滤掉目录中的内容
        @ColumnInfo(name = ReaderDatabase.CHAPTER_FILTER) var chapterFilter: String,      //过滤掉章节中的内容
        @ColumnInfo(name = ReaderDatabase.SEARCHURL) var searchUrl: String,               //搜索网址
        @ColumnInfo(name = ReaderDatabase.REDIRECTFILELD) var redirectFileld: String,     //重定至Header->field
        @ColumnInfo(name = ReaderDatabase.REDIRECTURL) var redirectUrl: String,      //重定向搜索结果URL选择器
        @ColumnInfo(name = ReaderDatabase.NOREDIRECTURL) var noRedirectUrl: String,  //搜索结果URL选择器
        @ColumnInfo(name = ReaderDatabase.REDIRECTNAME) var redirectName: String,         //重定向搜索结果书名选择器
        @ColumnInfo(name = ReaderDatabase.NOREDIRECTNAME) var noRedirectName: String,     //搜索结果书名选择器
        @ColumnInfo(name = ReaderDatabase.REDIRECTIMAGE) var redirectImage: String,       //重定向搜索结果图片选择器
        @ColumnInfo(name = ReaderDatabase.NOREDIRECTIMAGE) var noRedirectImage: String,   //搜索结果图片选择器
        @ColumnInfo(name = ReaderDatabase.CHARSET) var charset: String                    //URLEncode（"书名",charset）
)