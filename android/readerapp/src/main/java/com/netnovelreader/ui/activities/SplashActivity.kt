package com.netnovelreader.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netnovelreader.R
import com.netnovelreader.repo.SiteSelectorRepo
import com.netnovelreader.utils.get
import com.netnovelreader.utils.put
import com.netnovelreader.utils.sharedPreferences

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val isInit = sharedPreferences().get(getString(R.string.isInit), false)
        if(!isInit) {
            SiteSelectorRepo(application).apply {
                getSelectorsFromNet {
                    sharedPreferences().put(getString(R.string.isInit), it.size > 0)
                    saveAll(it)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        } else {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            application.cacheDir.listFiles().forEach { it.deleteRecursively() }
            finish()
        }
    }
}
