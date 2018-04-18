package com.netnovelreader.db

import com.netnovelreader.model.SitePreferenceBean
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanHandler
import org.apache.commons.dbutils.handlers.BeanListHandler
import java.sql.Connection
import java.sql.SQLException

class SitePreferenceDao(private val connection: () -> Connection?) {
    fun addPreference(vararg bean: SitePreferenceBean): Int {
        var id = 0
        val sql = "replace into ${ReaderDatabase.TABLE_SITE} (${ReaderDatabase.HOSTNAME}, ${ReaderDatabase.CATALOG_SELECTOR}, " +
                "${ReaderDatabase.CHAPTER_SELECTOR}, ${ReaderDatabase.CATALOG_FILTER}, ${ReaderDatabase.CHAPTER_FILTER}, " +
                "${ReaderDatabase.SEARCHURL}, ${ReaderDatabase.REDIRECTFILELD}, ${ReaderDatabase.REDIRECTURL}, " +
                "${ReaderDatabase.NOREDIRECTURL}, ${ReaderDatabase.REDIRECTNAME}, ${ReaderDatabase.NOREDIRECTNAME}, " +
                "${ReaderDatabase.REDIRECTIMAGE}, ${ReaderDatabase.NOREDIRECTIMAGE}, ${ReaderDatabase.CHARSET}) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
        (connection() ?: throw SQLException())
            .also {
                bean.forEach { item ->
                    id = QueryRunner().update(
                        it,
                        sql,
                        item.hostname,
                        item.catalog_selector,
                        item.chapter_selector,
                        item.catalog_filter,
                        item.chapter_filter,
                        item.search_url,
                        item.redirect_fileld,
                        item.redirect_url,
                        item.no_redirect_url,
                        item.redirect_name,
                        item.no_redirect_name,
                        item.redirect_image,
                        item.no_redirect_image,
                        item.charset
                    )
                }
            }
            .close()
        return id
    }

    fun deletePreference(hostname: String) {
        val sql = "delete from ${ReaderDatabase.TABLE_SITE} where ${ReaderDatabase.HOSTNAME} = ?;"
        (connection() ?: throw SQLException())
            .also {
                QueryRunner().update(it, sql, hostname)
            }
            .close()
    }

    fun deleteAllPreference() {
        val sql = "delete from ${ReaderDatabase.TABLE_SITE};"
        (connection() ?: throw SQLException())
            .also {
                QueryRunner().update(it, sql)
            }
            .close()
    }

    fun getPreference(hostname: String): SitePreferenceBean? {
        var result: SitePreferenceBean? = null
        val sql = "select * from ${ReaderDatabase.TABLE_SITE} where ${ReaderDatabase.HOSTNAME} = ?;"
        (connection() ?: throw SQLException())
            .also {
                result = QueryRunner().query(it, sql, BeanHandler(SitePreferenceBean::class.java), hostname)
            }
            .close()
        return result
    }

    fun getAllPreference(): List<SitePreferenceBean>? {
        var result: List<SitePreferenceBean>? = null
        val sql = "select * from ${ReaderDatabase.TABLE_SITE};"
        (connection() ?: throw SQLException())
            .also {
                result = QueryRunner().query(it, sql, BeanListHandler(SitePreferenceBean::class.java))
            }
            .close()
        return result
    }
}
