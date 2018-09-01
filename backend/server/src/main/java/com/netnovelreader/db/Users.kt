package com.netnovelreader.db

import org.jetbrains.exposed.dao.IntIdTable

object Users : IntIdTable("user")  {
    var username = varchar("username", 50).uniqueIndex()
    var password = varchar("password", 50)
    var email = varchar("email", 50)
    var role = integer("role")
}