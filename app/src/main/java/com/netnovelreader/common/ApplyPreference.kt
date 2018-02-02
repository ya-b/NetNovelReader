package com.netnovelreader.common

import android.content.Context
import com.netnovelreader.R

/**
 * Created by yangbo on 18-1-27.
 */
object ApplyPreference {
    fun isFullScreen(context: Context): Boolean {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getBoolean(context.getString(R.string.full_screen_key), false)
    }

    fun getAutoDownNum(context: Context): Int {
        val boolean = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getBoolean(context.getString(R.string.auto_download), true)
        return compareValues(boolean, false) * 3
    }

    fun setTheme(context: Context) {
        val color = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(context.getString(R.string.themeKey), "black")

        when (color) {
            "blue" -> context.setTheme(R.style.AppThemeBlue)
            "gray" -> context.setTheme(R.style.AppThemeGray)
            else -> context.setTheme(R.style.AppThemeBlack)
        }
    }

    fun getRowSpace(context: Context): Float {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(context.getString(R.string.rowspaceKey), "1.50").toFloat()
    }
}