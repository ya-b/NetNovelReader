package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import com.netnovelreader.bean.ObservableSiteBean
import com.netnovelreader.bean.RuleType
import com.netnovelreader.common.ReaderLiveData
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.SitePreferenceBean
import com.netnovelreader.data.network.ApiManager
import kotlinx.coroutines.experimental.launch

class SettingViewModel(context: Application) : AndroidViewModel(context) {
    val siteList = ObservableArrayList<SitePreferenceBean>()         //显示的列表
    val edittingSite = ObservableSiteBean()                            //编辑的站点
    val exitCommand = ReaderLiveData<Void>()                         //点击返回图标
    val editSiteCommand = ReaderLiveData<String>()                   //编辑站点
    val editTextCommand = ReaderLiveData<String>()                   //编辑具体规则
    val deleteAlertCommand = ReaderLiveData<String>()                //删除对话框
    var typeTmp: RuleType? = null

    fun showSiteList() {
        siteList.clear()
        ReaderDbManager.sitePreferenceDao()
            .getAll()
            .let { siteList.addAll(it) }
    }

    //启动SiteEditorFragment
    fun editSiteTask(hostName: String) = launch {
        siteList.first { it.hostname == hostName }
            .also { edittingSite.addAll(it) }
        editSiteCommand.value = hostName
    }

    //diaog删除对话框
    fun askDeleteSiteTask(hostName: String): Boolean {
        deleteAlertCommand.value = hostName
        return true
    }

    fun deleteSite(hostName: String) {
        siteList.first { it.hostname == hostName }
            .also {
                siteList.remove(it)
                ReaderDbManager.sitePreferenceDao().delete(it)
            }
    }

    fun editTextTask(type: RuleType): Boolean {
        typeTmp = type
        editTextCommand.value = edittingSite.get(type).get()
        return true
    }

    fun saveText(text: String?) {
        edittingSite.get(typeTmp!!).set(text)
        val editResult = edittingSite.toSitePreferenceBean()
        ReaderDbManager.sitePreferenceDao().insert(editResult)
        siteList.indexOfFirst { it.hostname == edittingSite.hostname.get() }
            .apply { siteList[this] = editResult }
    }

    //与手动修改的规则冲突时
    fun updatePreference(perferLocal: Boolean) {
        ApiManager.novelReader.getSitePreference().enqueueCall {
            it ?: return@enqueueCall
            val updateList =
                if (!perferLocal) {
                    it.arr
                } else {
                    it.arr.filter { serverRule ->
                        siteList.none { it.hostname == serverRule.hostname }
                    }
                }
            ReaderDbManager.sitePreferenceDao().insert(*updateList.toTypedArray())
            showSiteList()
        }
    }

    fun exitTask() {
        exitCommand.call()
    }
}