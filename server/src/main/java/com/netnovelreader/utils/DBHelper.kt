package com.netnovelreader.utils

import org.apache.commons.dbutils.QueryRunner
import java.sql.*

object DBHelper {

    private val DRIVENAME = "org.mariadb.jdbc.Driver"
    private val URL = "jdbc:mariadb://localhost:3306/reader_server?useUnicode=true&characterEncoding=UTF-8"
    private val USER = "reader"
    private val PASSWORD = "reader"

    private var conn: Connection? = null
    private var st: Statement? = null
    private var ppst: PreparedStatement? = null
    private var rs: ResultSet? = null

    const val TABLE_SITE = "sitepreference"
    const val TABLE_USER = "user"
    const val ID = "_id"
    const val HOSTNAME = "hostname"
    const val CATALOG_SELECTOR = "catalog_selector"
    const val CHAPTER_SELECTOR = "chapter_selector"
    const val CATALOG_FILTER = "catalog_filter"
    const val CHAPTER_FILTER = "chapter_filter"
    const val SEARCHURL = "search_url"
    const val REDIRECTFILELD = "redirect_fileld"
    const val REDIRECTURL = "redirect_url"
    const val NOREDIRECTURL = "no_redirect_url"
    const val REDIRECTNAME = "redirect_name"
    const val NOREDIRECTNAME = "no_redirect_name"
    const val REDIRECTIMAGE = "redirect_image"
    const val NOREDIRECTIMAGE = "no_redirect_image"
    const val CHARSET = "charset"

    const val USERNAME = "username"
    const val PASSWD = "password"
    const val EMAIL = "email"
    const val ROLE = "role"

    init {
        try {
            Class.forName(DRIVENAME).getDeclaredConstructor().newInstance()
            initTable()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initTable() {
        QueryRunner().apply {
            update(
                getConn(), "CREATE TABLE IF NOT EXISTS $TABLE_SITE (`$ID` INTEGER NOT NULL AUTO_INCREMENT, " +
                        "`$HOSTNAME` varchar(128) UNIQUE, `$CATALOG_SELECTOR` TEXT, `$CHAPTER_SELECTOR` TEXT, `$CATALOG_FILTER` TEXT, " +
                        "`$CHAPTER_FILTER` TEXT, `$SEARCHURL` TEXT, `$REDIRECTFILELD` TEXT, `$REDIRECTURL` TEXT, " +
                        "`$NOREDIRECTURL` TEXT, `$REDIRECTNAME` TEXT, `$NOREDIRECTNAME` TEXT, `$REDIRECTIMAGE` TEXT, " +
                        "`$NOREDIRECTIMAGE` TEXT, `$CHARSET` TEXT, PRIMARY KEY ($ID));"
            )
            update(
                getConn(), "CREATE TABLE IF NOT EXISTS $TABLE_USER (`$ID` INTEGER NOT NULL AUTO_INCREMENT, " +
                        "`$USERNAME` varchar(128) UNIQUE, `$PASSWD` varchar(128), `$EMAIL` varchar(128), `$ROLE` INTEGER, " +
                        "PRIMARY KEY ($ID));"
            )
        }
        DBHelper.closeConn()
    }

    fun getConn(): Connection {
        if (conn != null) return conn!!
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return conn!!
    }


    fun closeConn(
        resultSet: ResultSet? = this.rs,
        statement: Statement? = this.st,
        preparedStatement: PreparedStatement? = this.ppst,
        connection: Connection? = this.conn
    ) {
        try {
            resultSet?.close()
            this.rs = null
        } catch (e: SQLException) {
            println(e.message)
        }
        try {
            statement?.close()
            this.st = null
        } catch (e: SQLException) {
            println(e.message)
        }
        try {
            preparedStatement?.close()
            this.ppst = null
        } catch (e: SQLException) {
            println(e.message)
        }
        try {
            connection?.close()
            this.conn = null
        } catch (e: SQLException) {
            println(e.message)
        }
    }
}
