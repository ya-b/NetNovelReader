package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.SiteSelectorEntity
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.ioThread
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory

class SiteSelectorRepo(app: Application) : Repo(app) {
    private val siteSelectorDao = db.siteSelectorDao()
    val allSiteSelectors = siteSelectorDao.allSelectors()

    fun getSelectorsFromNet() =
        WebService.readerAPI
            .getSiteSelectorList()
            .map {
                it.apply { forEach { it._id = null } }
            }

    fun getSelectorsFromLocal() =
        siteSelectorDao.getAll()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))

    fun saveAll(list: List<SiteSelectorEntity>) =
        ioThread { siteSelectorDao.insert(*list.toTypedArray()) }


    fun saveSelector(entity: SiteSelectorEntity) {
        siteSelectorDao.getItem(entity.hostname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    entity._id = it._id
                    siteSelectorDao.insert(entity)
                },
                {
                    LoggerFactory.getLogger(this.javaClass).warn("saveSelector$it")
                })
    }

}