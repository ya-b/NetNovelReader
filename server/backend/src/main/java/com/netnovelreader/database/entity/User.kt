package com.netnovelreader.database.entity

/**
 * 用户表
 */
data class User(
    var id: Int,
    var username: String,
    var password: String,
    var email: String
)