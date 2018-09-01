package com.netnovelreader

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory private constructor(private val application: Application) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    @Throws(ReflectiveOperationException::class, SecurityException::class)
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val repoClass = modelClass.getDeclaredField("repo").type
        val repo = repoClass.getConstructor(Application::class.java).newInstance(application)
        return modelClass.getConstructor(repoClass, Application::class.java)
            .newInstance(repo, application)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(application: Application) =
            INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                INSTANCE ?: ViewModelFactory(application)
                    .also { INSTANCE = it }
            }
    }
}