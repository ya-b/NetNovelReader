package com.netnovelreader.repo

import android.app.Application
import android.util.Log
import com.netnovelreader.repo.db.SiteSelectorEntity
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.ioThread
import io.reactivex.schedulers.Schedulers

class SiteSelectorRepo(app: Application) : Repo(app) {
    private val siteSelectorDao = db.siteSelectorDao()
    val allSiteSelectors = siteSelectorDao.allSelectors()

    fun getSelectorsFromNet(block: (List<SiteSelectorEntity>) -> Unit) {
        WebService.readerAPI
            .getSiteSelectorList()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe (
                {
                    block.invoke(it.apply { forEach { it._id = null } })
                },
                {
                    block.invoke(emptyList())
                    Log.w("${app.packageName}:${this.javaClass.simpleName}", it)
                }
            )
    }

    fun getSelectorSFromLocal(block: (List<SiteSelectorEntity>) -> Unit) {
        block.invoke(siteSelectorDao.getAll())
    }

    fun saveAll(list: List<SiteSelectorEntity>) = ioThread { siteSelectorDao.insert(*list.toTypedArray()) }


    fun saveSelector(entity: SiteSelectorEntity) {
        ioThread {
            entity._id = siteSelectorDao.getItem(entity.hostname)?._id
            siteSelectorDao.insert(entity)
        }
    }

}