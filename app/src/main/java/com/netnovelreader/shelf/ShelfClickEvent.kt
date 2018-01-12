package com.netnovelreader.shelf

import android.content.Intent
import android.view.View
import com.netnovelreader.reader.ReaderActivity
import kotlinx.android.synthetic.main.item_shelf_recycler_view.view.*

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfClickEvent {
    fun startReaderActivity(v: View){
        val intent = Intent(v.context, ReaderActivity::class.java)
        intent.putExtra("bookname", v.nameView.text)
        v.context.startActivity(intent)
    }
}