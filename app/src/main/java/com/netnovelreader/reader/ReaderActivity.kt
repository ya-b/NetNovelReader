package com.netnovelreader.reader

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import com.netnovelreader.BR
import com.netnovelreader.R
import com.netnovelreader.databinding.ActivityReaderBinding
import kotlinx.android.synthetic.main.activity_reader.*

class ReaderActivity : AppCompatActivity(),IReaderContract.IReaderView {
    var mViewModel: ReaderViewModel? = null
    var detector: GestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(ReaderViewModel(intent.getStringExtra("bookname")))
        initView()
    }

    override fun initView() {
        readerView.background = getDrawable(R.drawable.bg_readbook_yellow)
        var listener = ViewGestureListener(mViewModel!!, readerView)
        listener.bookName = intent.getStringExtra("bookname")
        readerView.setFirstDrawListener(listener)
        detector = GestureDetector(this, listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel = null

    }

    override fun setViewModel(vm: ReaderViewModel) {
        mViewModel = vm
        val binding = DataBindingUtil.setContentView<ActivityReaderBinding>(this, R.layout.activity_reader)
        mViewModel?.text?.add("fjioewjofewa")
        binding.setVariable(BR.chapter, mViewModel)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return detector!!.onTouchEvent(event)
    }
}
