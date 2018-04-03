package com.netnovelreader.dao

import com.netnovelreader.dao.impl.ISitePreferenceDao
import com.netnovelreader.model.SitePreferenceBean
import com.netnovelreader.utils.DBHelper
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanHandler
import org.apache.commons.dbutils.handlers.BeanListHandler

class SitePreferenceDao : ISitePreferenceDao {
    override fun addPreference(bean: SitePreferenceBean): Int {
        val sql = "replace into ${DBHelper.TABLE_SITE} (${DBHelper.HOSTNAME}, ${DBHelper.CATALOG_SELECTOR}, ${DBHelper.CHAPTER_SELECTOR}, " +
                "${DBHelper.CATALOG_FILTER}, ${DBHelper.CHAPTER_FILTER}, ${DBHelper.SEARCHURL}, ${DBHelper.REDIRECTFILELD}, " +
                "${DBHelper.REDIRECTURL}, ${DBHelper.NOREDIRECTURL}, ${DBHelper.REDIRECTNAME}, ${DBHelper.NOREDIRECTNAME}, " +
                "${DBHelper.REDIRECTIMAGE}, ${DBHelper.NOREDIRECTIMAGE}, ${DBHelper.CHARSET}) values(" +
                "'${bean.hostname}'," +
                "'${bean.catalog_selector}'," +
                "'${bean.chapter_selector}'," +
                "'${bean.catalog_filter}'," +
                "'${bean.chapter_filter}'," +
                "'${bean.search_url}'," +
                "'${bean.redirect_fileld}'," +
                "'${bean.redirect_url}'," +
                "'${bean.no_redirect_url}'," +
                "'${bean.redirect_name}'," +
                "'${bean.no_redirect_name}'," +
                "'${bean.redirect_image}'," +
                "'${bean.no_redirect_image}'," +
                "'${bean.charset}')"
        val id = QueryRunner().update(DBHelper.getConn(), sql)
        DBHelper.closeConn()
        return id
    }

    override fun deletePreference(hostname: String) {
        val sql = "delete from ${DBHelper.TABLE_SITE} where ${DBHelper.HOSTNAME} = '$hostname';"
        QueryRunner().update(DBHelper.getConn(), sql)
        DBHelper.closeConn()
    }

    override fun deleteAllPreference() {
        val sql = "delete from ${DBHelper.TABLE_SITE};"
        QueryRunner().update(DBHelper.getConn(), sql)
        DBHelper.closeConn()
    }

    override fun getPreference(hostname: String): SitePreferenceBean? {
        val sql = "select * from ${DBHelper.TABLE_SITE} where ${DBHelper.HOSTNAME} = '$hostname';"
        val result = QueryRunner().query(DBHelper.getConn(), sql, BeanHandler(SitePreferenceBean::class.java))
        DBHelper.closeConn()
        return result
    }

    override fun getAllPreference(): List<SitePreferenceBean>? {
        val sql = "select * from ${DBHelper.TABLE_SITE};"
        val result = QueryRunner().query(DBHelper.getConn(), sql, BeanListHandler(SitePreferenceBean::class.java))
        DBHelper.closeConn()
        return result
    }
}
