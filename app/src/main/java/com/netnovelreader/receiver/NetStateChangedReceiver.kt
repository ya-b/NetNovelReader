package com.netnovelreader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class NetStateChangedReceiver(val block: () -> Unit) : BroadcastReceiver() {
    var stateTemp: Boolean = false
    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isAvailable = cm.activeNetworkInfo?.isAvailable ?: false
        if (isAvailable && !stateTemp) {
            block()
        }
        stateTemp = isAvailable
    }
}