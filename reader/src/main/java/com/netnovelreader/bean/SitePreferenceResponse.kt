package com.netnovelreader.bean

import com.netnovelreader.data.local.db.SitePreferenceBean

data class SitePreferenceResponse(val arr: ArrayList<Bean>){
    data class Bean(
        var h: String,                 //域名
        var cs: String,  //目录选择器
        var hs: String,  //章节内容选择器
        var cf: String,      //过滤掉目录中的内容
        var hf: String,      //过滤掉章节中的内容
        var su: String,               //搜索网址
        var rf: String,     //重定至Header->field
        var ru: String,      //重定向搜索结果URL选择器
        var nu: String,  //搜索结果URL选择器
        var rn: String,         //重定向搜索结果书名选择器
        var nn: String,     //搜索结果书名选择器
        var ri: String,       //重定向搜索结果图片选择器
        var ni: String,   //搜索结果图片选择器
        var ct: String                    //URLEncode（"书名",charset）
    ){
        fun toSitePreferenceBean(): SitePreferenceBean {
            return SitePreferenceBean(
                hostname = h,
                catalogSelector = cs,
                chapterSelector = hs,
                catalogFilter = cf,
                chapterFilter = hf,
                searchUrl = su,
                redirectFileld = rf,
                redirectUrl = ru,
                noRedirectUrl = nu,
                redirectName = rn,
                noRedirectName = nn,
                redirectImage = ri,
                noRedirectImage = ni,
                charset = ct
            )
        }
    }
}