package com.netnovelreader.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.netnovelreader.R
import com.netnovelreader.repo.SiteSelectorRepo
import com.netnovelreader.utils.get
import com.netnovelreader.utils.put
import com.netnovelreader.utils.sharedPreferences

class SplashActivity : AppCompatActivity() {
    val req = 217

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val isInit = sharedPreferences().get(getString(R.string.isInitKey), false)
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        if(!isInit) {
            SiteSelectorRepo(application).apply {
                getSelectorsFromNet {
                    sharedPreferences().put(getString(R.string.isInitKey), it.size > 0)
                    saveAll(it)
                    if(hasPermission()) {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    } else {
                        requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, req)
                    }
                }
            }
        } else {
            application.cacheDir.listFiles().forEach { it.deleteRecursively() }
            if(hasPermission()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            } else {
                requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, req)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == req) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            finish()
        }
    }

    private fun hasPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun requirePermission(permission: String, reqCode: Int) {
        ActivityCompat.requestPermissions(this, Array(1) { permission }, reqCode)
    }
}
