package com.netnovelreader.repo.http.resp

/**
 * [token] auth token
 */
data class RespMessage (
    var ret: Int,
    var dsc: String? = null,
    var token: String? = null
)