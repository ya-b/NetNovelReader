package com.netnovelreader.bean

import com.netnovelreader.data.local.db.ShelfBean
import com.netnovelreader.data.local.db.SitePreferenceBean

/**
 * [ret] 返回码：1 登陆
 *              2 注册
 *              3 用户名或密码错误
 *              4 用户名或密码格式错误
 *              5 用户logou
 *              6 操作成功
 *              7 操作失败
 *              8 权限不足
 * [desc] 说明
 * [token] auth token
 */
data class RespMessage (
    var ret: Int,
    var dsc: String? = null,
    var token: String? = null,
    var rules: List<Bean>? = null,
    var rule: Bean? = null,
    var books: ArrayList<ReadRecordBean>? = null
){
    data class Bean(
        var h: String,    //域名
        var cs: String,   //目录选择器
        var hs: String,   //章节内容选择器
        var cf: String,   //过滤掉目录中的内容
        var hf: String,   //过滤掉章节中的内容
        var su: String,   //搜索网址
        var rf: String,   //重定至Header->field
        var ru: String,   //重定向搜索结果URL选择器
        var nu: String,   //搜索结果URL选择器
        var rn: String,   //重定向搜索结果书名选择器
        var nn: String,   //搜索结果书名选择器
        var ri: String,   //重定向搜索结果图片选择器
        var ni: String,   //搜索结果图片选择器
        var ct: String    //URLEncode（"书名",charset）
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
    data class ReadRecordBean(
        var i: Int? = null,
        var b: String?,
        var d: String? = null,
        var r: String? = null,
        var s: String? = null,
        var l: String? = null,
        var a: Int? = null
    ){
        fun toSitePreferenceBean(): ShelfBean {
            return ShelfBean(i, b, d, r, s, l, a)
        }
    }
}