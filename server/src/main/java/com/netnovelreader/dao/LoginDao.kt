package com.netnovelreader.dao

import com.netnovelreader.dao.impl.ILoginDao
import com.netnovelreader.model.UserBean
import com.netnovelreader.utils.DBHelper
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanHandler

class LoginDao : ILoginDao {

    override fun addUser(user: UserBean) {
        val sql = "replace into ${DBHelper.TABLE_USER} (${DBHelper.USERNAME}, ${DBHelper.PASSWD}, ${DBHelper.EMAIL}, ${DBHelper.ROLE}) values " +
                "('${user.username}','${user.password}','${user.email}','${user.role}');"
        QueryRunner().update(DBHelper.getConn(), sql)
        DBHelper.closeConn()
    }

    override fun delUser(username: String) {
        QueryRunner().update(DBHelper.getConn(),"delete from ${DBHelper.TABLE_USER} where ${DBHelper.USERNAME} = " +
                "'$username';")
        DBHelper.closeConn()
    }

    override fun getUser(username: String): UserBean? {
        val result = QueryRunner().query(DBHelper.getConn(), "select * from ${DBHelper.TABLE_USER} where ${DBHelper.USERNAME} " +
                "= '$username';", BeanHandler(UserBean::class.java))
        DBHelper.closeConn()
        return result
    }
}