package com.netnovelreader.db

import org.jetbrains.exposed.dao.IntIdTable

object SitePreferences : IntIdTable("sitepreference") {
    var hostname = varchar("hostname", 50).uniqueIndex()           //域名
    var catalog_selector = varchar("catalog_selector", 200)  //目录选择器
    var chapter_selector = varchar("chapter_selector", 200)  //章节内容选择器
    var catalog_filter = varchar("catalog_filter", 200)    //过滤掉目录中的内容
    var chapter_filter = varchar("chapter_filter", 200)   //过滤掉章节中的内容
    var search_url = varchar("search_url", 200)       //搜索网址
    var redirect_fileld = varchar("redirect_fileld", 200)   //重定至Header->field
    var redirect_url = varchar("redirect_url", 200)      //重定向搜索结果URL选择器
    var no_redirect_url = varchar("no_redirect_url", 200)  //搜索结果URL选择器
    var redirect_name = varchar("redirect_name", 200)     //重定向搜索结果书名选择器
    var no_redirect_name = varchar("no_redirect_name", 200)  //搜索结果书名选择器
    var redirect_image = varchar("redirect_image", 200)     //重定向搜索结果图片选择器
    var no_redirect_image = varchar("no_redirect_image", 200)  //搜索结果图片选择器
    var charset = varchar("charset", 50)            //URLEncode（"书名"charset）
}