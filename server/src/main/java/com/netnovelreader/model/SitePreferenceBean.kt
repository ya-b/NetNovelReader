package com.netnovelreader.model

class SitePreferenceBean{
    var _id: Int? = null
    var hostname: String? = null                 //域名
        get() = field ?: ""
    var catalog_selector: String? = null  //目录选择器
        get() = field ?: ""
    var chapter_selector: String? = null  //章节内容选择器
        get() = field ?: ""
    var catalog_filter: String? = null      //过滤掉目录中的内容
        get() = field ?: ""
    var chapter_filter: String? = null      //过滤掉章节中的内容
        get() = field ?: ""
    var search_url: String? = null               //搜索网址
        get() = field ?: ""
    var redirect_fileld: String? = null     //重定至Header->field
        get() = field ?: ""
    var redirect_url: String? = null      //重定向搜索结果URL选择器
        get() = field ?: ""
    var no_redirect_url: String? = null  //搜索结果URL选择器
        get() = field ?: ""
    var redirect_name: String? = null         //重定向搜索结果书名选择器
        get() = field ?: ""
    var no_redirect_name: String? = null     //搜索结果书名选择器
        get() = field ?: ""
    var redirect_image: String? = null       //重定向搜索结果图片选择器
        get() = field ?: ""
    var no_redirect_image: String? = null   //搜索结果图片选择器
        get() = field ?: ""
    var charset: String? = null                    //URLEncode（"书名"charset）
        get() = field ?: ""

    override fun toString(): String =
        "SitePreferenceBean[_id=$_id,hostname=$hostname,catalog_selector=$catalog_selector,chapter_selector=$chapter_selector," +
                "catalog_filter=$catalog_filter,chapter_filter=$chapter_filter,search_url=$search_url,redirect_fileld=$redirect_fileld," +
                "redirect_url=$redirect_url,no_redirect_url=$no_redirect_url,redirect_name=$redirect_name,no_redirect_name=$no_redirect_name," +
                "redirect_image=$redirect_image,no_redirect_image=$no_redirect_image,charset=$charset]"
}