package com.netnovelreader.data.local

import android.content.Context
import com.netnovelreader.R

object PreferenceManager {
    val PREFERENCE_NAME = "com.netnovelreader_preferences"

    fun isFullScreen(context: Context): Boolean =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getBoolean(context.getString(R.string.full_screen_key), false)

    fun getAutoDownNum(context: Context): Int {
        val boolean = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(context.getString(R.string.auto_download_key), true)
        return compareValues(boolean, false) * 3
    }

    fun isAutoRemove(context: Context): Boolean =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getBoolean(context.getString(R.string.auto_remove_key), true)


    fun getThemeId(context: Context): Int =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getString(context.getString(R.string.themeKey), "black")
                    .let {
                        when (it) {
                            "blue" -> R.style.AppThemeBlue
                            "gray" -> R.style.AppThemeGray
                            else -> R.style.AppThemeBlack
                        }
                    }

    fun getRowSpace(context: Context): Float =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getString(context.getString(R.string.rowspaceKey), "1.50").toFloat()

    fun saveBackground(context: Context, which: Int) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putInt(context.getString(R.string.backgroundColorKey), which).apply()
    }

    fun getBackground(context: Context): Int =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getInt(context.getString(R.string.backgroundColorKey), 0)

    fun saveFontSize(context: Context, float: Float) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putFloat(context.getString(R.string.fontSizeKey), float).apply()
    }

    fun getFontSize(context: Context): Float =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getFloat(context.getString(R.string.fontSizeKey), 50f)

    fun saveFontType(context: Context, which: String) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putString(context.getString(R.string.fontTypeKey), which).apply()
    }

    fun getFontType(context: Context): String =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getString(context.getString(R.string.fontTypeKey), "default")
}