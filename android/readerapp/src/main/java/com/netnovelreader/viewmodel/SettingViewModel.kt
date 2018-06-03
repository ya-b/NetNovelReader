package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import com.google.gson.Gson
import com.netnovelreader.R
import com.netnovelreader.bean.ObservableSiteRule
import com.netnovelreader.bean.RuleType
import com.netnovelreader.common.get
import com.netnovelreader.common.put
import com.netnovelreader.common.sharedPreferences
import com.netnovelreader.common.tryIgnoreCatch
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.local.db.SitePreferenceBean
import com.netnovelreader.data.network.WebService
import kotlinx.coroutines.experimental.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class SettingViewModel(val context: Application) : AndroidViewModel(context) {
    val isLoading = ObservableBoolean(false)
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
        ReaderDbManager.sitePreferenceDao().getAll().let { siteList.addAll(it) }
    }

    //启动SiteEditorFragment
    fun editSite(hostName: String) = launch {
        siteList.first { it.hostname == hostName }
            .also { edittingSite!!.add(it) }
        editSiteCommand.postValue(hostName)
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
        val response = tryIgnoreCatch {
            WebService.novelReader.getSitePreference().execute().body()
        }?.takeIf { it.ret == 6 } ?: run { toastCommand.value = "获取配置文件错误"; return }
        if (!perferLocal) {
            response.rules
        } else {
            response.rules?.filter { bean -> siteList.none { it.hostname == bean.hostname } }
        }
            ?.let { ReaderDbManager.sitePreferenceDao().insert(*it.toTypedArray()) }

        showSiteList()
    }


    fun login(username: String, passwd: String) = launch {
        if (username.length < 4 || passwd.length < 4) {
            toastCommand.postValue("用户名或密码格式错误")
            return@launch
        }
        isLoading.set(true)
        val result = tryIgnoreCatch { WebService.novelReader.login(username, passwd).execute().body() }
        when (result?.ret) {
            1, 2 -> {
                this@SettingViewModel.context.apply {
                    sharedPreferences().put(getString(R.string.tokenKey), result.token!!)
                }
                exit()
            }
            3 -> toastCommand.postValue("登陆失败，用户名或密码错误")
            4 -> toastCommand.postValue("用户名或密码格式错误")
            else -> toastCommand.postValue("网络连接异常")
        }
        isLoading.set(false)
    }

    //保存至服务器
    fun saveRecord() = launch {
        isLoading.set(true)
        val token = "Bearer ${this@SettingViewModel.context.run { sharedPreferences().get(getString(R.string.tokenKey),"") }}"
        val records = ReaderDbManager.shelfDao().getAll() ?: return@launch
        val sb = StringBuilder()
        sb.append("{\"books\":[")
        for (i in 0 until records.size) {
            sb.append(Gson().toJson(records[i]))
            if (i != records.size - 1) {
                sb.append(",")
            }
        }
        sb.append("]}")
        val body = RequestBody.create(MediaType.parse("text/plain"), sb.toString())
        val filePart = MultipartBody.Part.createFormData("fileupload", "record", body)
        tryIgnoreCatch { WebService.novelReader.saveRecord(token, filePart).execute().body() }
            .let {
                if (it?.ret == 6) {
                    toastCommand.postValue("上传成功")
                } else {
                    toastCommand.postValue("上传失败")
                }
            }
        isLoading.set(false)
    }

    //从服务器恢复
    fun updateRecord() = launch {
        isLoading.set(true)
        val token = "Bearer ${this@SettingViewModel.context.run { sharedPreferences().get(getString(R.string.tokenKey),"") }}"
        val result = tryIgnoreCatch { WebService.novelReader.restoreRecord(token).execute().body() }
        result?.books?.let {
            if (it.size == 0) return@launch
            ReaderDbManager.shelfDao().getAll()?.forEach { ReaderDbManager.shelfDao().delete(it) }
            ReaderDbManager.shelfDao().insert(*it.toTypedArray())
            exit()
        }
        isLoading.set(false)
    }

    fun exit() {
        exitCommand.postValue(null)
    }
}