package com.netnovelreader.reader

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.netnovelreader.R
import com.netnovelreader.databinding.ActivityShelfBinding
import kotlinx.android.synthetic.main.activity_reader.*

class ReaderActivity : AppCompatActivity(),IReaderContract.IReaderView {
    var mViewModel: ReaderViewModel? = null
    var mReaderAdapter: ReaderPagerAdapter? = null
    var mPageChangeListener: ReaderOnPageChangeListener? = null
    var viewArray: Array<TextView>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(ReaderViewModel())
        initView()
        updateText(true)
    }

    override fun initView() {
        viewArray = Array<TextView>(3){TextView(this)}
        mReaderAdapter = ReaderPagerAdapter(viewArray!!)
        mViewPager.adapter = mReaderAdapter
        mPageChangeListener = ReaderOnPageChangeListener(mViewPager, this)
        mViewPager.addOnPageChangeListener(mPageChangeListener)
        mViewPager.setCurrentItem(1, false)
    }

    /**
     * @boolean true表示向后翻页 false表示向前翻页
     */
    override fun updateText(boolean: Boolean) {
        val texts = mViewModel?.getChapterText(boolean)
        if(boolean){
            for(i in 0..2) viewArray!![i].text = texts!![i]
        }else{
            for(i in 0..2) viewArray!![i].text = texts!![2 - i]
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewPager.removeOnPageChangeListener(mPageChangeListener)
    }

    override fun setViewModel(vm: ReaderViewModel) {
        mViewModel = vm
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_reader)
    }

    class ReaderPagerAdapter : PagerAdapter{
        var mViewArray: Array<TextView>? = null
        constructor(viewArray: Array<TextView>): super() {
            //viewArray 123  --> mViewArray 31231
            mViewArray = Array<TextView>(viewArray.size + 2){it -> initArrayItem(it, viewArray)}
        }

        override fun getCount(): Int {
            return mViewArray!!.size
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
            container?.removeView(mViewArray!![position])
            container?.addView(mViewArray!![position])
            mViewArray ?: return null
            return mViewArray!![position]
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        }

        fun initArrayItem(it: Int, viewArray: Array<TextView>): TextView{
            if(it == 0){
                return viewArray[viewArray.size -1]
            }else if(it == viewArray.size + 1){
                return viewArray[0]
            }
            return viewArray[it - 1]
        }
    }

    class ReaderOnPageChangeListener(val viewPager: ViewPager, val iView: IReaderContract.IReaderView) : ViewPager.OnPageChangeListener{

        var mPosition = 0

        override fun onPageScrollStateChanged(state: Int) {
            if (state != ViewPager.SCROLL_STATE_IDLE) return
            if(mPosition == 0){
                viewPager.setCurrentItem(3, false)
            }else if(mPosition == 4){
                viewPager.setCurrentItem(1, false)
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }

        override fun onPageSelected(position: Int) {
            if(mPosition == 3 && position == 4){
                iView.updateText(true)
            }else if(mPosition == 1 && position == 0){
                iView.updateText(false)
            }
            mPosition = position
        }

    }
}
