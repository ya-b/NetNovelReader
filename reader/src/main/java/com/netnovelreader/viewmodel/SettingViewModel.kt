package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import com.netnovelreader.bean.ObservableSiteRule
import com.netnovelreader.bean.RuleType
import com.netnovelreader.data.local.PreferenceManager
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.local.db.SitePreferenceBean
import com.netnovelreader.data.network.WebService
import kotlinx.coroutines.experimental.launch
import java.io.IOException

class SettingViewModel(val context: Application) : AndroidViewModel(context) {
    val siteList = ObservableArrayList<SitePreferenceBean>()          //显示的列表
    var edittingSite: ObservableSiteRule? = ObservableSiteRule()      //编辑的站点
    val exitCommand = MutableLiveData<Void>()                         //点击返回图标
    val toastCommand = MutableLiveData<String>()
    val editSiteCommand = MutableLiveData<String>()                   //编辑站点
    val editTextCommand = MutableLiveData<String>()                   //编辑具体规则
    val deleteAlertCommand = MutableLiveData<String>()                //删除对话框
    var typeTmp: RuleType? = null

    fun showSiteList() {
        siteList.clear()
        ReaderDbManager.sitePreferenceDao()
                .getAll()
                .let { siteList.addAll(it) }
    }

    //启动SiteEditorFragment
    fun editSite(hostName: String) = launch {
        siteList.first { it.hostname == hostName }
                .also { edittingSite!!.add(it) }
        editSiteCommand.value = hostName
    }

    //diaog删除对话框
    fun askDeleteSite(hostName: String): Boolean {
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

    fun editText(type: RuleType): Boolean {
        typeTmp = type
        editTextCommand.value = edittingSite!!.get(type).get() ?: ""
        return true
    }

    fun saveText(text: String?) {
        edittingSite!!.get(typeTmp!!).set(text)
        val editResult = edittingSite!!.toSitePreferenceBean()
        if (edittingSite!!.hostname.get().isNullOrEmpty()) return
        ReaderDbManager.sitePreferenceDao().insert(editResult)
        siteList.indexOfFirst { it.hostname == edittingSite!!.hostname.get() }.also {
            if (it == -1) {
                siteList.add(edittingSite!!.toSitePreferenceBean())
                siteList[siteList.size - 1] = editResult
            } else {
                siteList[it] = editResult
            }
        }
    }

    //与手动修改的规则冲突时
    fun updatePreference(perferLocal: Boolean) {
        val response = try {
            WebService.novelReader.getSitePreference().execute().body()
        } catch (e: IOException) {
            null
        } ?: return
        val updateList =
                if (!perferLocal) {
                    response.arr
                } else {
                    response.arr.filter { serverRule ->
                        siteList.none { it.hostname == serverRule.h }
                    }
                }.map { it.toSitePreferenceBean() }
        ReaderDbManager.sitePreferenceDao().insert(*updateList.toTypedArray())
        showSiteList()
    }


    fun login(username: String, passwd: String){
        if(username.length < 5 || passwd.length < 5){
            toastCommand.postValue("用户名或密码格式错误")
            return
        }
        launch {
            val result = try {
                WebService.novelReader.login(username, passwd).execute().body()
            }catch (e: IOException){
                null
            }
            when(result){
                "0","1" -> {
                    PreferenceManager.saveNamePasswd(this@SettingViewModel.context, username, passwd)
                    exit()
                }
                "2" -> {
                    toastCommand.postValue("用户名或密码格式错误")
                }
                "3" -> {
                    toastCommand.postValue("登陆失败，用户名或密码错误")
                }
                else -> {
                    toastCommand.postValue("网络连接异常")
                }
            }
        }
    }

    //保存至服务器
    fun saveRecord(){
        launch {

        }
    }

    //从服务器恢复
    fun updateRecord(){

    }

    fun exit() {
        exitCommand.postValue(null)
    }
}