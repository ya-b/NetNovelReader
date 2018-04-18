package com.netnovelreader.db

import org.apache.commons.dbutils.QueryRunner
import java.sql.DriverManager
import java.sql.SQLException

class ReaderDatabase {
    private val DRIVENAME = "org.mariadb.jdbc.Driver"
    private val URL = "jdbc:mariadb://localhost:3306/reader_server?useUnicode=true&characterEncoding=UTF-8"
    private var USER: String = ""
    private var PASSWD: String = ""
    private var userDao: UserDao? = null
    private var sitePreferenceDao: SitePreferenceDao? = null

    init {
        try {
            Class.forName(DRIVENAME).getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun init(username: String, password: String){
        USER = username
        PASSWD = password
        getConn().also {
            QueryRunner().update(
                it, "CREATE TABLE IF NOT EXISTS $TABLE_SITE (`$ID` INTEGER NOT NULL AUTO_INCREMENT, " +
                        "`$HOSTNAME` varchar(128) UNIQUE, `$CATALOG_SELECTOR` TEXT, `$CHAPTER_SELECTOR` TEXT, `$CATALOG_FILTER` TEXT, " +
                        "`$CHAPTER_FILTER` TEXT, `$SEARCHURL` TEXT, `$REDIRECTFILELD` TEXT, `$REDIRECTURL` TEXT, " +
                        "`$NOREDIRECTURL` TEXT, `$REDIRECTNAME` TEXT, `$NOREDIRECTNAME` TEXT, `$REDIRECTIMAGE` TEXT, " +
                        "`$NOREDIRECTIMAGE` TEXT, `$CHARSET` TEXT, PRIMARY KEY ($ID));"
            )
            QueryRunner().update(
                it, "CREATE TABLE IF NOT EXISTS $TABLE_USER (`$ID` INTEGER NOT NULL AUTO_INCREMENT, " +
                        "`$USERNAME` varchar(128) UNIQUE, `$PASSWORD` varchar(128), `$EMAIL` varchar(128), `$ROLE` INTEGER, " +
                        "PRIMARY KEY ($ID));"
            )
        }?.close()
    }

    fun getConn() = try {
        DriverManager.getConnection(URL, USER, PASSWD)
    } catch (e: SQLException) {
        e.printStackTrace()
        null
    }

    fun userDao(): UserDao =
        userDao ?: synchronized(ReaderDatabase::class.java){
            userDao ?: run { UserDao { getConn() }.also { userDao = it } }
        }

    fun sitePreferenceDao(): SitePreferenceDao =
        sitePreferenceDao ?: synchronized(ReaderDatabase::class.java){
            sitePreferenceDao ?: run { SitePreferenceDao { getConn() }.also { sitePreferenceDao = it } }
        }

    companion object {

        val TABLE_SITE = "sitepreference"
        val TABLE_USER = "user"
        val ID = "_id"
        val HOSTNAME = "hostname"
        val CATALOG_SELECTOR = "catalog_selector"
        val CHAPTER_SELECTOR = "chapter_selector"
        val CATALOG_FILTER = "catalog_filter"
        val CHAPTER_FILTER = "chapter_filter"
        val SEARCHURL = "search_url"
        val REDIRECTFILELD = "redirect_fileld"
        val REDIRECTURL = "redirect_url"
        val NOREDIRECTURL = "no_redirect_url"
        val REDIRECTNAME = "redirect_name"
        val NOREDIRECTNAME = "no_redirect_name"
        val REDIRECTIMAGE = "redirect_image"
        val NOREDIRECTIMAGE = "no_redirect_image"
        val CHARSET = "charset"

        val USERNAME = "username"
        val PASSWORD = "password"
        val EMAIL = "email"
        val ROLE = "role"
    }
}