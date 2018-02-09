package com.netnovelreader.noveldetail

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.netnovelreader.GlideApp
import com.netnovelreader.R
import com.netnovelreader.api.bean.NovelIntroduce
import com.netnovelreader.databinding.ActivityDetailBinding
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_detail.*

/**
 * 文件： NovelDetailActivity
 * 描述：
 * 作者： YangJunQuan   2018-2-8.
 */
class NovelDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityDetailBinding>(this, R.layout.activity_detail)
        setSupportActionBar({ toolbar.title = "书籍详情";toolbar }())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mydetail = intent.getSerializableExtra("data") as NovelIntroduce
        with(mydetail) {
            //对数据进行进一步的处理再显示
            if (!cover!!.contains("http://")) {
                cover = "http://statics.zhuishushenqi.com$cover-covers"     //补全图片Url,并且加载图片
            }
            updated = updated?.replace("T", "  ")
            updated = "上次更新： $updated"
            updated = updated!!.substring(0, updated!!.lastIndexOf(":"))
            wordCount += "字"
            longIntro = longIntro?.replace("  ", "")
            longIntro = longIntro?.replace("\n", "\n\u3000\u3000")
            longIntro = "\u3000\u3000" + longIntro                           //首行缩进两个空格
            retentionRatio = "$retentionRatio%"
            GlideApp.with(this@NovelDetailActivity)
                    .load(cover)
                    .error(ContextCompat.getDrawable(this@NovelDetailActivity, R.drawable.cover_default))
                    .centerCrop()
                    .into(novelCover)
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


}