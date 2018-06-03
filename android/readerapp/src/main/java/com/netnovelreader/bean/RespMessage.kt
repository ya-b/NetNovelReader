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
    var rules: List<SitePreferenceBean>? = null,
    var rule: SitePreferenceBean? = null,
    var books: ArrayList<ShelfBean>? = null
)