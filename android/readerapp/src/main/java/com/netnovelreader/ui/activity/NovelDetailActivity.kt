package com.netnovelreader.ui.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.netnovelreader.R
import com.netnovelreader.bean.NovelIntroduce
import com.netnovelreader.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*

class NovelDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(intent.getIntExtra("themeid", R.style.AppThemeBlack))
        super.onCreate(savedInstanceState)
        val binding =
                DataBindingUtil.setContentView<ActivityDetailBinding>(this, R.layout.activity_detail)
        binding.searchEvent = this
        setSupportActionBar({ toolbar.title = "书籍详情";toolbar }())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mydetail = intent.getSerializableExtra("data") as NovelIntroduce

        with(mydetail) {
            //对数据进行进一步的处理再显示
            serialState.text = if (mydetail.isSerial!!) "连载中" else "已完结"
            updated = updated?.replace("T", "  ")
            updated = "上次更新： $updated"
            updated = updated!!.substring(0, updated!!.lastIndexOf(":"))
            wordCount += "字"
            chaptersCount += "章"
            longIntro = longIntro?.replace("  ", "")
            longIntro = longIntro?.replace("\n", "\n\u3000\u3000")
            longIntro = "\u3000\u3000" + longIntro                           //首行缩进两个空格
            retentionRatio = "$retentionRatio%"
        }
        binding.detail = mydetail

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()   //点击回退按钮结束当前Activity
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun search(bookname: String) {
        val intent = Intent(this, SearchActivity::class.java).apply {
            putExtra("bookname", bookname)
            putExtra("chapterName", "")
            putExtra("themeid", intent.getIntExtra("themeid", R.style.AppThemeBlack))
        }
        startActivity(intent)
        setResult(2333)
        this.finish()
    }
}