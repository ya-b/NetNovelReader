package com.netnovelreader.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.netnovelreader.R
import com.netnovelreader.repo.SiteSelectorRepo
import com.netnovelreader.utils.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File

class SplashActivity : AppCompatActivity() {
    val req = 217
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val isInit = sharedPreferences().get(getString(R.string.isInitKey), false)
        val repo = SiteSelectorRepo(application)
        if (!isInit) {
            repo.getSelectorsFromNet()
                    .subscribeOn(Schedulers.from(IO_EXECUTOR))
                    .subscribe(
                            {
                                sharedPreferences().put(getString(R.string.isInitKey), it.size > 0)
                                repo.saveAll(it)
                                startMainActivity()
                            },
                            {
                                startMainActivity()
                            })
                    .also { compositeDisposable.add(it) }
        } else {
            val oldDir = File(booksDirOld())
            if (oldDir.exists()) {
                oldDir.copyRecursively(File(booksDir()), true)
                oldDir.deleteRecursively()
            }
            application.cacheDir.listFiles().forEach { it.deleteRecursively() }
            startMainActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == req) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            finish()
        }
    }

    private fun startMainActivity() {
        if (hasPermission()) {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        } else {
            requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, req)
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
