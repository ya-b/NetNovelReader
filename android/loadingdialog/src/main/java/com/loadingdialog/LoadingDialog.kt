package com.loadingdialog

import android.app.Dialog
import android.content.Context
import android.widget.ProgressBar
import java.util.*
import kotlin.concurrent.schedule

class LoadingDialog(context: Context) : Dialog(context, R.style.LoadingDialog) {
    val progressBar by lazy { ProgressBar(context) }
    var timer: Timer? = null
    var TIME_OUT = -1L

    constructor(context: Context, timeout: Long): this(context) {
        TIME_OUT = timeout
    }

    init {
        setCancelable(false)
        setContentView(progressBar)
    }

    override fun show() {
        super.show()
        if(TIME_OUT > 0) {
            timer = Timer().apply {
                schedule(TIME_OUT) { dismiss() }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        if(TIME_OUT > 0) {
            timer?.cancel()
        }
    }
}