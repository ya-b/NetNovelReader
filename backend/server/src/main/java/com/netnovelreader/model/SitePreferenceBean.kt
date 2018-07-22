package com.netnovelreader.model

import com.google.gson.annotations.SerializedName

class SitePreferenceBean {

    @field:SerializedName("i") var _id: Int? = null
    @field:SerializedName("h") var hostname: String? = null           //域名
        get() = field ?: ""
    @field:SerializedName("cs") var catalog_selector: String? = null  //目录选择器
        get() = field ?: ""
    @field:SerializedName("hs") var chapter_selector: String? = null  //章节内容选择器
        get() = field ?: ""
    @field:SerializedName("cf") var catalog_filter: String? = null    //过滤掉目录中的内容
        get() = field ?: ""
    @field:SerializedName("hf") var chapter_filter: String? = null   //过滤掉章节中的内容
        get() = field ?: ""
    @field:SerializedName("su") var search_url: String? = null       //搜索网址
        get() = field ?: ""
    @field:SerializedName("rf") var redirect_fileld: String? = null   //重定至Header->field
        get() = field ?: ""
    @field:SerializedName("ru") var redirect_url: String? = null      //重定向搜索结果URL选择器
        get() = field ?: ""
    @field:SerializedName("nu") var no_redirect_url: String? = null  //搜索结果URL选择器
        get() = field ?: ""
    @field:SerializedName("rn") var redirect_name: String? = null     //重定向搜索结果书名选择器
        get() = field ?: ""
    @field:SerializedName("nn") var no_redirect_name: String? = null  //搜索结果书名选择器
        get() = field ?: ""
    @field:SerializedName("ri") var redirect_image: String? = null     //重定向搜索结果图片选择器
        get() = field ?: ""
    @field:SerializedName("ni") var no_redirect_image: String? = null  //搜索结果图片选择器
        get() = field ?: ""
    @field:SerializedName("ct") var charset: String? = null            //URLEncode（"书名"charset）
        get() = field ?: ""
}