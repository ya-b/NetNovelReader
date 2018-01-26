package com.netnovelreader

import android.app.Application
import android.content.Context
import com.netnovelreader.common.MAINDB_NAME
import com.netnovelreader.data.orm.DaoMaster
import com.netnovelreader.data.orm.DaoSession
import com.netnovelreader.data.orm.HtmlParseRules
import com.netnovelreader.data.orm.SearchRules

/**
 * Created by yangbo on 2018/1/11.
 */
class ReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        daoSession = DaoMaster(DaoMaster.DevOpenHelper(this, MAINDB_NAME).writableDb).newSession()
        Thread { initDB() }.start()
    }

    companion object {
        lateinit var appContext: Context
        lateinit var daoSession: DaoSession
    }

    fun initDB() {
        if (daoSession.htmlParseRulesDao.count() == 0L) {
            val r1 = HtmlParseRules(null, "qidian.com", ".volume-wrap", ".read-content")
            val r2 = HtmlParseRules(null, "yunlaige.com", "#contenttable", "#content")
            daoSession.htmlParseRulesDao.insert(r1)
            daoSession.htmlParseRulesDao.insert(r2)
        }
        if (daoSession.searchRulesDao.count() == 0L) {
            val s1 = SearchRules(null,
                    "qidian.com",
                    "https://www.qidian.com/search/?kw=${SearchRules.SEARCH_NAME}",
                    "utf-8",
                    "0",
                    "",
                    "",
                    ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)",
                    "",
                    ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)",
                    "",
                    ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)")
            val s2 = SearchRules(null,
                    "yunlaige.com",
                    "http://www.yunlaige.com/modules/article/search.php?searchkey=${SearchRules.SEARCH_NAME}&action=login&submit=",
                    "gbk",
                    "1",
                    "location",
                    ".readnow",
                    "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)",
                    "#content > div.book-info > div.info > h2 > a",
                    "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)",
                    "",
                    "")
            daoSession.searchRulesDao.insert(s1)
            daoSession.searchRulesDao.insert(s2)
        }
    }
}