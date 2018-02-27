package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.databinding.*
import android.graphics.Color
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import com.netnovelreader.R
import com.netnovelreader.bean.ChapterChangeType
import com.netnovelreader.bean.ReaderBean
import com.netnovelreader.common.*
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ShelfBean
import com.netnovelreader.data.network.ChapterCache
import com.netnovelreader.data.network.DownloadCatalog
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by yangbo on 18-1-13.Typeface
 */

class ReaderViewModel(val context: Application) : AndroidViewModel(context) {

    val catalog by lazy { ObservableArrayList<ReaderBean>() }               //目录
    val text by lazy { ObservableField<String>("") }                   //一页显示的内容
    val fontSize by lazy { ObservableFloat(55f) }                      //字体大小
    val fontType by lazy { ObservableField<Typeface>(Typeface.DEFAULT) }    //字体
    val fontColor by lazy { ObservableInt(Color.BLACK) }                    //字体颜色
    val backgroundColor by lazy { ObservableInt(R.color.read_font_default) }//背景颜色
    val isHeadViewShow by lazy { ObservableBoolean(false) }            //是否显示HeadView
    val isFontSettingShow by lazy { ObservableBoolean(false) }         //是否显示FontSetting
    val isBackgroundSetingShow by lazy { ObservableBoolean(false) }    //是否显示BackgroundSeting
    val isFootViewShow by lazy { ObservableBoolean(false) }            //是否显示FootView
    val fontSizeSelected = List(5) { ObservableBoolean(false) }   //字体大小设置Button是否选中
    val fontTypeSelected = List(5) { ObservableBoolean(false) }   //字体大小设置Button是否选中
    val isLoading by lazy { ObservableBoolean(true) }                  //是否显示加载进度条
    val showDialogCommand by lazy { ReaderLiveData<Boolean>() }             //显示目录
    val changeSourceCommand by lazy { ReaderLiveData<String>() }            //换源下载
    @Volatile
    var chapterName: String? = null
    var chapterNum = AtomicInteger(0)              //章节数
    @Volatile
    var maxChapterNum = 0                                    //最大章节数
    var chapterCache: ChapterCache? = null
    lateinit var bookName: String
    var CACHE_NUM: Int = 0

    //获取章节内容
    fun getChapter(type: ChapterChangeType, chapterName: String?) {
        when (type) {
            ChapterChangeType.NEXT -> if (chapterNum.get() >= maxChapterNum) return else chapterNum.incrementAndGet()
            ChapterChangeType.PREVIOUS -> if (chapterNum.get() < 2) return else chapterNum.decrementAndGet()
            ChapterChangeType.BY_CATALOG -> chapterName?.run {
                ReaderDbManager.getChapterId(bookName, chapterName).also { chapterNum.set(it) }
            }
        }
        isLoading.set(true)
        launch { if (chapterNum.get() == maxChapterNum) updateCatalog() }
        val str = chapterCache!!.getChapter(chapterNum.get(), false)
        text.set(str)
        this.chapterName = str.substring(0, str.indexOf("|"))
        if (str.substring(str.indexOf("|") + 1) == ChapterCache.FILENOTFOUND) {
            launch { downloadAndShow() }
        } else {
            isLoading.set(false)
        }
    }

    //下载并显示，阅读到未下载章节时调用
    suspend fun downloadAndShow() {
        chapterCache ?: return
        var str = ChapterCache.FILENOTFOUND
        var times = 0
        while (str == ChapterCache.FILENOTFOUND && times++ < 10) {
            str = chapterCache!!.getChapter(chapterNum.get())
            delay(500)
        }
        if (str != ChapterCache.FILENOTFOUND && str.isNotEmpty()) {
            isLoading.set(false)
            getChapter(ChapterChangeType.BY_CATALOG, null)
        }
    }

    fun reloadCurrentChapter() {
        chapterCache ?: return
        updateCatalog()
        getCatalog()
        chapterCache!!.clearCache()
        getChapter(ChapterChangeType.BY_CATALOG, null)
    }

    /**
     * 保存阅读记录
     */
    fun setRecord(pageNum: Int) {
        if (chapterNum.get() < 1) return
        launch {
            ReaderDbManager.getRoomDB().shelfDao().replace(
                ShelfBean(
                    bookName = bookName,
                    readRecord = "$chapterNum#${if (pageNum < 1) 1 else pageNum}"
                )
            )
        }
    }

    /**
     * 重新读取目录
     */
    fun getCatalog() {
        catalog.clear()
        catalog.addAll(ReaderDbManager.getAllChapter(bookName).map { ReaderBean(it) })
    }

    /**
     * 自动删除已读章节，但保留最近[NotDeleteNum]章
     */
    fun autoRemove() {
        val num = getRecord()[0]
        if (num < NotDeleteNum) return
        val id = num - NotDeleteNum
        ReaderDbManager.setReaded(bookName, id)
            .forEach { File("${getSavePath()}/$bookName/$it").delete() }
    }

    fun pageByCatalog(chapterName: String) {
        showDialogCommand.value = false
        getChapter(ChapterChangeType.BY_CATALOG, chapterName)
    }

    fun drawPrepareTask(): Int = runBlocking {
        maxChapterNum = ReaderDbManager.getChapterCount(bookName).takeIf { it != 0 } ?:
                return@runBlocking 0
        val record = async { getRecord() }.await()
        chapterNum.set(record[0])
        chapterCache = ChapterCache(CACHE_NUM, bookName).apply { init(maxChapterNum, bookName) }
        launch { getChapter(ChapterChangeType.BY_CATALOG, null) }
        record[1]
    }

    fun nextChapterTask() {
        isHeadViewShow.set(false)
        isFontSettingShow.set(false)
        isBackgroundSetingShow.set(false)
        isFootViewShow.set(false)
        launch { getChapter(ChapterChangeType.NEXT, null) }
    }

    fun previousChapterTask() {
        isHeadViewShow.set(false)
        isFontSettingShow.set(false)
        isBackgroundSetingShow.set(false)
        isFootViewShow.set(false)
        launch { getChapter(ChapterChangeType.PREVIOUS, null) }
    }

    fun centerClickTask() {
        if (isFootViewShow.get()) {
            isHeadViewShow.set(false)
            isFontSettingShow.set(false)
            isBackgroundSetingShow.set(false)
            isFootViewShow.set(false)
        } else {
            isFootViewShow.set(true)
            isHeadViewShow.set(true)
        }
    }

    fun changeSourceTask() {
        isHeadViewShow.set(false)
        isFontSettingShow.set(false)
        isBackgroundSetingShow.set(false)
        isFootViewShow.set(false)
        changeSourceCommand.value = chapterName
    }

    fun footViewClickEvent(which: String) {
        when (which) {
            "catalogButton" -> {
                isHeadViewShow.set(false)
                isFontSettingShow.set(false)
                isBackgroundSetingShow.set(false)
                isFootViewShow.set(false)
                showDialogCommand.value = true
            }
            "fontSizeButton" -> {
                isBackgroundSetingShow.set(false)
                isFontSettingShow.set(!isFontSettingShow.get())
            }
            "backgroundButton" -> {
                isFontSettingShow.set(false)
                isBackgroundSetingShow.set(!isBackgroundSetingShow.get())
            }
        }
    }

    fun fontSizeChangeEvent(float: Float) {
        fontSize.set(float)
        when (float) {
            45f -> {
                fontSizeSelected[0].set(true)
                fontSizeSelected.filterIndexed { i, _ -> i != 0 }.forEach { it.set(false) }
            }
            50f -> {
                fontSizeSelected[1].set(true)
                fontSizeSelected.filterIndexed { i, _ -> i != 1 }.forEach { it.set(false) }
            }
            55f -> {
                fontSizeSelected[2].set(true)
                fontSizeSelected.filterIndexed { i, _ -> i != 2 }.forEach { it.set(false) }
            }
            60f -> {
                fontSizeSelected[3].set(true)
                fontSizeSelected.filterIndexed { i, _ -> i != 3 }.forEach { it.set(false) }
            }
            65f -> {
                fontSizeSelected[4].set(true)
                fontSizeSelected.filterIndexed { i, _ -> i != 4 }.forEach { it.set(false) }
            }
            else -> {
                //55f
                fontSizeSelected[2].set(true)
                fontSizeSelected.filterIndexed { i, _ -> i != 2 }.forEach { it.set(false) }
            }
        }
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
            .putFloat(context.getString(R.string.fontSizeKey), float).apply()
    }

    fun fontTypeChangeEvent(which: String) {
        when (which) {
            "beiweikai" -> {
                fontType.set(Typeface.createFromAsset(context.assets, "font/beiweikaishu.ttf"))
                fontTypeSelected[1].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 1 }.forEach { it.set(false) }
            }
            "bysong" -> {
                fontType.set(Typeface.createFromAsset(context.assets, "font/bysong.ttf"))
                fontTypeSelected[2].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 2 }.forEach { it.set(false) }
            }
            "fzkatong" -> {
                fontType.set(Typeface.createFromAsset(context.assets, "font/fzkatong.ttf"))
                fontTypeSelected[3].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 3 }.forEach { it.set(false) }
            }
            "chenguang" -> {
                fontType.set(Typeface.createFromAsset(context.assets, "font/chenguang.ttf"))
                fontTypeSelected[4].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 4 }.forEach { it.set(false) }
            }
            else -> {
                fontType.set(Typeface.DEFAULT)
                fontTypeSelected[0].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 0 }.forEach { it.set(false) }
            }
        }
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
            .putString(context.getString(R.string.fontTypeKey), which).apply()
    }

    fun backgroundChangeEvent(which: Int) {
        when (which) {
            1 -> {
                fontColor.set(ContextCompat.getColor(context, R.color.read_font_1))
                backgroundColor.set(ContextCompat.getColor(context, R.color.read_bg_1))
            }
            2 -> {
                fontColor.set(ContextCompat.getColor(context, R.color.read_font_2))
                backgroundColor.set(ContextCompat.getColor(context, R.color.read_bg_2))
            }
            3 -> {
                fontColor.set(ContextCompat.getColor(context, R.color.read_font_3))
                backgroundColor.set(ContextCompat.getColor(context, R.color.read_bg_3))
            }
            4 -> {
                fontColor.set(ContextCompat.getColor(context, R.color.read_font_4))
                backgroundColor.set(ContextCompat.getColor(context, R.color.read_bg_4))
            }
            else -> {
                fontColor.set(ContextCompat.getColor(context, R.color.read_font_default))
                backgroundColor.set(ContextCompat.getColor(context, R.color.read_bg_default))
            }
        }
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
            .putInt(context.getString(R.string.backgroundColorKey), which).apply()
    }

    /**
     * 获取阅读记录
     */
    private fun getRecord(): Array<Int> {

        val queryResult = ReaderDbManager.getRoomDB().shelfDao().getBookInfo(bookName)?.readRecord
            ?.split("#")?.map { it.toInt() }          //阅读记录 3#2 表示第3章第2页
        return arrayOf(queryResult?.get(0) ?: 1, queryResult?.get(1) ?: 1)
    }

    private fun updateCatalog() {
        try {
            DownloadCatalog(
                bookName, ReaderDbManager.getRoomDB().shelfDao().getBookInfo(bookName)?.downloadUrl
                        ?: ""
            ).download()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        maxChapterNum = ReaderDbManager.getChapterCount(bookName)
    }
}