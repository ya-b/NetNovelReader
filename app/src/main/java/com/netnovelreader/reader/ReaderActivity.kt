package com.netnovelreader.reader

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.netnovelreader.R
import com.netnovelreader.databinding.ActivityShelfBinding
import kotlinx.android.synthetic.main.activity_reader.*

class ReaderActivity : AppCompatActivity(),IReaderContract.IReaderView {
    var mViewModel: ReaderViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(ReaderViewModel(intent.getStringExtra("bookname")))
        initView()
    }

    override fun initView() {
        readerView.mViewModel = mViewModel
        readerView.bookName = intent.getStringExtra("bookname")
        readerView.background = getDrawable(R.drawable.bg_readbook_yellow)
    }


    override fun onDestroy() {
        super.onDestroy()
        mViewModel = null
        readerView.mViewModel = null
    }

    override fun setViewModel(vm: ReaderViewModel) {
        mViewModel = vm
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_reader)
    }


}
