package com.netnovelreader.shelf

import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.netnovelreader.R
import com.netnovelreader.base.BindingAdapter
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.databinding.ActivityShelfBinding
import com.netnovelreader.reader.ReaderActivity
import com.netnovelreader.search.SearchActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.android.synthetic.main.item_shelf_recycler_view.view.*

class ShelfActivity : AppCompatActivity(), IShelfContract.IShelfView {

    var mViewModel: ShelfViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(ShelfViewModel())
        if(!checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,1)
        }
        if(!checkPermission(android.Manifest.permission.INTERNET)){
            requirePermission(android.Manifest.permission.INTERNET,2)
        }
        initView()
    }

    override fun onResume() {
        super.onResume()
        updateShelf(null)
    }

    override fun setViewModel(vm: ShelfViewModel) {
        mViewModel = vm
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_shelf)
    }

    override fun initView(){
        setSupportActionBar({toolbar.setTitle(R.string.shelf_activity_title); toolbar}())
        shelfRecycler.layoutManager = LinearLayoutManager(this)
        shelfRecycler.setItemAnimator(DefaultItemAnimator())
        if(checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            shelfRecycler.adapter = BindingAdapter(mViewModel?.getModel()?.bookList,
                    R.layout.item_shelf_recycler_view, ShelfClickEvent())
        }else{
            shelfRecycler.adapter = BindingAdapter<Any>(null,
                    R.layout.item_shelf_recycler_view, ShelfClickEvent())
        }
    }

    override fun updateShelf(adapter: BindingAdapter<ShelfBean.BookInfoBean>?) {
        adapter?.changeDataSet(mViewModel?.getModel()?.bookList)
        if(mViewModel == null) return
        Observable.create<Boolean> { e -> e.onNext(mViewModel!!.updateBookList()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { shelfRecycler.adapter.notifyDataSetChanged() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_shelf, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_button -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateShelf(shelfRecycler.adapter as BindingAdapter<ShelfBean.BookInfoBean>)
            }else{
                Toast.makeText(this, R.string.permission_warnning, Toast.LENGTH_LONG).show()
            }
        }else if(requestCode == 2 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, R.string.permission_warnning, Toast.LENGTH_LONG).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun requirePermission(permission: String, reqCode: Int) {
        ActivityCompat.requestPermissions(this,Array<String>(1){ permission }, reqCode)
    }

    class ShelfClickEvent : IClickEvent {
        fun startReaderActivity(v: View){
            var intent = Intent(v.context, ReaderActivity::class.java)
            intent.putExtra("bookname", v.nameView.text.toString())
            v.context.startActivity(intent)
        }
    }
}
