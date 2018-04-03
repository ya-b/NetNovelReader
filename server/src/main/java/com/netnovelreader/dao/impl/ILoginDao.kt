package com.netnovelreader.dao.impl

import com.netnovelreader.model.UserBean

interface ILoginDao {
    fun addUser(user: UserBean)
    fun delUser(username: String)
    fun getUser(username: String): UserBean?
}