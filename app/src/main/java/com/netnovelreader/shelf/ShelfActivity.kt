package com.netnovelreader.shelf

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.netnovelreader.IView
import com.netnovelreader.R
import com.netnovelreader.databinding.ActivityShelfBinding

import kotlinx.android.synthetic.main.activity_shelf.*

class ShelfActivity : AppCompatActivity(),IView<ShelfViewModel> {

    var booklist: ArrayList<BookInfoBean>? = null
    var mViewModel: ShelfViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(ShelfViewModel())
        initView()
    }

    fun initView(){
        setSupportActionBar(toolbar)
        booklist = ArrayList<BookInfoBean>()
        shelfRsclView.layoutManager = LinearLayoutManager(this)
        shelfRsclView.adapter = ShelfAdapter(booklist)
        shelfRsclView.setItemAnimator(DefaultItemAnimator())

        booklist?.add(BookInfoBean("aaaaaaaaaaaaa"))
        booklist?.add(BookInfoBean("bbbbbbbbbbbbb"))
    }

    override fun setViewModel(vm: ShelfViewModel) {
        mViewModel = vm
        val binding = DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_shelf)
        binding.shelfVM = vm
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_shelf, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
