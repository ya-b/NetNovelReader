package com.netnovelreader

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.netnovelreader.repo.*
import com.netnovelreader.vm.*

class ViewModelFactory private constructor(private val application: Application) :
        ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(ShelfViewModel::class.java) ->
                    ShelfViewModel(BookInfosRepo(application), application)
                isAssignableFrom(SearchViewModel::class.java) ->
                        SearchViewModel(SearchRepo(application), application)
                isAssignableFrom(SiteSelectorViewModel::class.java) ->
                        SiteSelectorViewModel(SiteSelectorRepo(application), application)
                isAssignableFrom(ReadViewModel::class.java) ->
                    ReadViewModel(ChapterInfoRepo(application), application)
                isAssignableFrom(LoginViewModel::class.java) ->
                    LoginViewModel(UserInfoRepo(application), application)
                isAssignableFrom(SyncRecordViewModel::class.java) ->
                    SyncRecordViewModel(UserInfoRepo(application), application)
                else ->
                    AndroidViewModel(application)
            }
        } as T
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: ViewModelFactory? = null

        fun getInstance(application: Application) =
                INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                    INSTANCE ?: ViewModelFactory(application)
                            .also { INSTANCE = it }
                }
    }
}