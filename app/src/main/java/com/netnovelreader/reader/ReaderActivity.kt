package com.netnovelreader.reader

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.netnovelreader.R
import kotlinx.android.synthetic.main.activity_reader.*

class ReaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)
        testText.text =  intent.getStringExtra("bookname")
    }
}
