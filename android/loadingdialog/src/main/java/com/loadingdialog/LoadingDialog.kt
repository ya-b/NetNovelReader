package com.loadingdialog

import android.app.Dialog
import android.content.Context
import android.widget.ProgressBar
import java.util.*
import kotlin.concurrent.schedule

class LoadingDialog(context: Context) : Dialog(context, R.style.LoadingDialog) {
    val progressBar by lazy { ProgressBar(context) }
    var timer: Timer? = null

    init {
        setCancelable(false)
        setContentView(progressBar)
    }

    override fun show() {
        super.show()
        timer = Timer().apply {
            schedule(3000) { dismiss() }
        }
    }

    override fun dismiss() {
        super.dismiss()
        timer?.cancel()
    }
}