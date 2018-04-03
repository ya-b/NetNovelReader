package com.netnovelreader.service

import com.netnovelreader.dao.LoginDao
import com.netnovelreader.model.UserBean
import javax.servlet.http.Cookie

class UserAuthorityService {
    val TEXT_IS_NULL = 0
    val LOGIN_FAIL = -1
    val LOGIN_SUCCESS = 1
    val REGISTER_SUCCESS = 2

    private val dao = LoginDao()

    fun loginOrRegister(userName: String?, passwd: String?, email: String? = null): Int {
        if (userName == null || userName.length < 4 || passwd == null || passwd.length < 4) return TEXT_IS_NULL
        var user = dao.getUser(userName)
        if (user == null) {
            user = UserBean()
            user.username = userName
            user.password = passwd
            user.email = email
            user.role = 1
            dao.addUser(user)
            return REGISTER_SUCCESS
        }
        if (user.password == passwd) {
            return LOGIN_SUCCESS
        } else {
            return LOGIN_FAIL
        }
    }

    fun getRole(cookies: Array<Cookie>?): Int? {
        val arr = cookies?.firstOrNull { it.name == "name" }?.value?.split("=|=") ?: return null
        if(arr.size < 2) return null
        val user = dao.getUser(arr[0])
        if (user?.password != arr[1]) return null
        return user.role
    }

    fun getUserName(cookies: Array<Cookie>?): String? {
        val arr = cookies?.firstOrNull { it.name == "name" }?.value?.split("=|=") ?: return null
        if(arr.size < 2) return null
        val user = dao.getUser(arr[0])
        if (user?.password != arr[1]) return null
        return arr[0]
    }
}