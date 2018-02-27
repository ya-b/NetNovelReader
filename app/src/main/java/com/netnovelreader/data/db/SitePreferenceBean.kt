package com.netnovelreader.data.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "sitepreference", indices=arrayOf(Index(value=arrayOf("hostname"), unique=true)))
data class SitePreferenceBean(
        @PrimaryKey var _id: Int = 0,
        @ColumnInfo(name = "hostname") var hostname: String,                      //域名
        @ColumnInfo(name = "catalog_selector") var catalogSelector: String,       //目录选择器
        @ColumnInfo(name = "chapter_selector") var chapterSelector: String,       //章节内容选择器
        @ColumnInfo(name = "catalog_filter") var catalogFilter: String,           //过滤掉目录中的内容
        @ColumnInfo(name = "chapter_filter") var chapterFilter: String,           //过滤掉章节中的内容
        @ColumnInfo(name = "search_url") var searchUrl: String,                   //搜索网址
        @ColumnInfo(name = "redirect_fileld") var redirectFileld: String,         //重定至Header->field
        @ColumnInfo(name = "redirect_selector") var redirectSelector: String,     //重定向搜索结果URL选择器
        @ColumnInfo(name = "no_redirect_selector") var noRedirectSelector: String,//搜索结果URL选择器
        @ColumnInfo(name = "redirect_name") var redirectName: String,             //重定向搜索结果书名选择器
        @ColumnInfo(name = "no_redirect_name") var noRedirectName: String,        //搜索结果书名选择器
        @ColumnInfo(name = "redirect_image") var redirectImage: String,           //重定向搜索结果图片选择器
        @ColumnInfo(name = "no_redirect_image") var noRedirectImage: String,      //搜索结果图片选择器
        @ColumnInfo(name = "charset") var charset: String                         //URLEncode（"书名",charset）
)