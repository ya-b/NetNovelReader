package com.netnovelreader.reader

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.netnovelreader.R
import com.netnovelreader.databinding.ActivityShelfBinding
import kotlinx.android.synthetic.main.activity_reader.*

class ReaderActivity : AppCompatActivity(),IReaderContract.IReaderView {
    var mViewModel: ReaderViewModel? = null
    var viewArray: Array<TextView>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(ReaderViewModel())
        readerView.mViewModel = mViewModel
        initView()
    }

    override fun initView() {

    }

    /**
     * @boolean true表示向后翻页 false表示向前翻页
     */
    override fun updateText(boolean: Boolean) {
//        val texts = mViewModel?.getChapterText(boolean)
//        if(boolean){
//            for(i in 0..2) viewArray!![i].text = texts!![i]
//        }else{
//            for(i in 0..2) viewArray!![i].text = texts!![2 - i]
//        }
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
