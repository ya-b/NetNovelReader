package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import com.netnovelreader.bean.ObservableSiteBean
import com.netnovelreader.common.ReaderLiveData
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.SitePreferenceBean
import kotlinx.coroutines.experimental.launch

class SettingViewModel(val context: Application) : AndroidViewModel(context) {
    val siteList = ObservableArrayList<SitePreferenceBean>()         //显示的列表
    val editedSite = ObservableSiteBean()                            //编辑的站点
    val exitCommand = ReaderLiveData<Void>()                         //点击返回图标
    val editSiteCommand = ReaderLiveData<String>()                   //编辑站点
    val deleteAlertCommand = ReaderLiveData<String>()                //删除对话框

    fun showSiteListTask() {
        siteList.clear()
        siteList.addAll(ReaderDbManager.getRoomDB().sitePreferenceDao().getAll())
    }

    fun editSiteTask(hostName: String) = launch {
        siteList.first { it.hostname == hostName }.also { editedSite.addAll(it) }
        editSiteCommand.value = hostName
    }

    fun askDeleteSiteTask(hostName: String): Boolean {
        deleteAlertCommand.value = hostName
        return true
    }

    fun deleteSiteTask(hostName: String) {
        //TODO 在数据库删除
        siteList.first { it.hostname == hostName }.also { siteList.remove(it) }
    }

    fun exitTask() {
        exitCommand.call()
    }
}