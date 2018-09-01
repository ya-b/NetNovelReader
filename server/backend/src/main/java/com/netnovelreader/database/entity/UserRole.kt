package com.netnovelreader.database.entity

/**
 * 关系表：用户-角色
 */
data class UserRole(
    var id: Int,
    var userId: Int,
    var roleId: Int
)