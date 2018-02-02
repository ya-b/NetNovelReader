package com.netnovelreader.reader

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import com.netnovelreader.R

/**
 * 文件： Config
 * 描述：
 * 作者： YangJunQuan   2018/2/2.
 */
class Config {
    companion object {

        val FONTTYPE_DEFAULT = ""
        val FONTTYPE_BEIWEIKAISHU = "font/beiweikaishu.ttf"
        val FONTTYPE_CHENGUANG = "font/chenguang.ttf"
        val FONTTYPE_FZKATONG = "font/fzkatong.ttf"
        val FONTTYPE_BYSONG = "font/bysong.ttf"

        /**
         * 根据字体路径返回字体类型
         */
        fun getTypeface(context: Context, typeFacePath: String): Typeface {
            return if (typeFacePath == FONTTYPE_DEFAULT) {
                Typeface.DEFAULT
            } else {
                Typeface.createFromAsset(context.assets, typeFacePath)
            }
        }

        /**
         *   设置字体设置按钮选择的背景
         */
        fun setTextViewSelect(textView: TextView, isSelect: Boolean?) {
            val context = textView.context
            if (isSelect!!) {
                textView.setBackgroundDrawable(context.resources.getDrawable(R.drawable.button_select_bg))
                textView.setTextColor(context.resources.getColor(R.color.read_dialog_button_select))
            } else {
                textView.setBackgroundDrawable(context.resources.getDrawable(R.drawable.button_bg))
                textView.setTextColor(context.resources.getColor(R.color.white))
            }
        }

    }
}