package com.netnovelreader.db

import com.netnovelreader.model.UserBean
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanHandler
import java.sql.Connection
import java.sql.SQLException

class UserDao(private val connection: () -> Connection?) {

    fun addUser(user: UserBean) {
        val sql =
            "replace into ${ReaderDatabase.TABLE_USER} (${ReaderDatabase.USERNAME}, ${ReaderDatabase.PASSWORD}, " +
                    "${ReaderDatabase.EMAIL}, ${ReaderDatabase.ROLE}) values (?, ?, ?, ?);"
        (connection() ?: throw SQLException())
            .also {
                QueryRunner().update(it, sql, user.username, user.password, user.email, user.role)
            }
            .close()

    }

    fun delUser(username: String) {
        (connection() ?: throw SQLException())
            .also {
                QueryRunner().update(
                    it, "delete from ${ReaderDatabase.TABLE_USER} where ${ReaderDatabase.USERNAME} = ?;", username
                )
            }
            .close()
    }

    fun getUser(username: String?): UserBean? {
        username ?: return null
        var result: UserBean? = null
        (connection() ?: throw SQLException())
            .also {
                result = QueryRunner().query(
                    it,
                    "select * from ${ReaderDatabase.TABLE_USER} where ${ReaderDatabase.USERNAME} = ?;",
                    BeanHandler(UserBean::class.java),
                    username
                )
            }
            .close()
        return result
    }
}