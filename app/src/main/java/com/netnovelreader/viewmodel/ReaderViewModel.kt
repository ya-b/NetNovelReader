package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.databinding.*
import android.graphics.Color
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication
import com.netnovelreader.bean.ChapterChangeType
import com.netnovelreader.bean.ReaderBean
import com.netnovelreader.common.NotDeleteNum
import com.netnovelreader.common.PREFERENCE_NAME
import com.netnovelreader.common.ReaderLiveData
import com.netnovelreader.common.replace
import com.netnovelreader.data.PreferenceManager
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.network.ChapterCache
import com.netnovelreader.data.network.DownloadCatalog
import kotlinx.coroutines.experimental.*
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
    val isViewShow by lazy {
        mapOf(
            Pair(HEAD_VIEW, ObservableBoolean(false)),                //是否显示HeadView
            Pair(Font_SETTING, ObservableBoolean(false)),             //是否显示FontSetting
            Pair(BG_SETTING, ObservableBoolean(false)),               //是否显示BackgroundSeting
            Pair(FOOT_VIEW, ObservableBoolean(false))                 //是否显示footview
        )                //是否显示FootView
    }

    val fontSizeSelected = List(5) { ObservableBoolean(false) }   //字体大小设置Button是否选中
    val fontTypeSelected = List(5) { ObservableBoolean(false) }   //字体大小设置Button是否选中
    val isLoading by lazy { ObservableBoolean(true) }                  //是否显示加载进度条
    val showDialogCommand by lazy { ReaderLiveData<Boolean>() }             //显示目录
    val changeSourceCommand by lazy { ReaderLiveData<String>() }            //换源下载
    val brightnessCommand by lazy { ReaderLiveData<Float>() }                 //亮度
    @Volatile
    var chapterName: String? = null                         //章节名
    var chapterNum = AtomicInteger(0)              //章节数
    @Volatile
    var maxChapterNum = 0                                    //最大章节数
    var chapterCache: ChapterCache? = null
    lateinit var bookName: String                            //书名
    var CACHE_NUM: Int = 0                                   //缓存后面章节数量
    var downloadJob: Job? = null
    var changeFontSizeFlag = false

    fun start() {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).apply {
            fontSizeChangeEvent(getFloat(context.getString(R.string.fontSizeKey), 50f))
            fontTypeChangeEvent(getString(context.getString(R.string.fontTypeKey), "default"))
            backgroundChangeEvent(getInt(context.getString(R.string.backgroundColorKey), 0))
        }
    }

    //获取章节内容
    fun getChapter(type: ChapterChangeType, chapterName: String?) {
        if (showDialogCommand.value == true) showDialogCommand.value = false
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
            downloadJob?.cancel()
            downloadJob = launch { downloadAndShow() }
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
        str.split("|")[1].takeIf { it != ChapterCache.FILENOTFOUND }?.run {
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
            ReaderDbManager.shelfDao().replace(
                bookName = bookName, readRecord = "$chapterNum#${if (pageNum < 1) 1 else pageNum}"
            )
        }
        if (changeFontSizeFlag == false) {
            isViewShow.forEach { it.value.set(false) }
        } else {
            changeFontSizeFlag = false
        }
    }

    /**
     * 重新读取目录
     */
    private fun getCatalog() {
        catalog.clear()
        catalog.addAll(ReaderDbManager.getAllChapter(bookName).map { ReaderBean(it) })
    }

    /**
     * 自动删除已读章节，但保留最近[NotDeleteNum]章
     */
    fun autoRemove() {
        PreferenceManager.isAutoRemove(context).takeIf { it }
        getRecord()[0].takeIf { it > NotDeleteNum }?.let {
            ReaderDbManager.setReaded(bookName, it - NotDeleteNum)
                .map { File("${ReaderApplication.dirPath}/$bookName/$it") }
                .forEach { it.delete() }
        }
    }

    fun drawPrepareTask(): Int = runBlocking {
        async {
            maxChapterNum = ReaderDbManager.getChapterCount(bookName).takeIf { it != 0 } ?:
                    return@async 0
            val record = async { getRecord() }.await()
            chapterNum.set(record[0])
            chapterCache = ChapterCache(CACHE_NUM, bookName).apply { init(maxChapterNum, bookName) }
            launch { getChapter(ChapterChangeType.BY_CATALOG, null) }
            record[1]
        }.await()
    }

    fun nextChapterTask() {
        isViewShow.forEach { it.value.set(false) }
        launch { getChapter(ChapterChangeType.NEXT, null) }
    }

    fun previousChapterTask() {
        isViewShow.forEach { it.value.set(false) }
        launch { getChapter(ChapterChangeType.PREVIOUS, null) }
    }

    fun centerClickTask() {
        if (isViewShow[FOOT_VIEW]!!.get()) {
            isViewShow.forEach { it.value.set(false) }
        } else {
            isViewShow[FOOT_VIEW]!!.set(true)
            isViewShow[HEAD_VIEW]!!.set(true)
        }
    }

    fun changeSourceTask() {
        isViewShow.forEach { it.value.set(false) }
        changeSourceCommand.value = chapterName
    }

    fun footViewClickEvent(which: String) = runBlocking {
        when (which) {
            "catalogButton" -> {
                isViewShow.forEach { it.value.set(false) }
                launch { getCatalog() }.join()
                showDialogCommand.value = true
            }
            "fontSizeButton" -> {
                isViewShow[BG_SETTING]!!.set(false)
                isViewShow[Font_SETTING]!!.set(!isViewShow[Font_SETTING]!!.get())
            }
            "backgroundButton" -> {
                isViewShow[Font_SETTING]!!.set(false)
                isViewShow[BG_SETTING]!!.set(!isViewShow[BG_SETTING]!!.get())
            }
        }
        Unit
    }

    fun fontSizeChangeEvent(float: Float) {
        changeFontSizeFlag = true
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
                fontType.set(ResourcesCompat.getFont(context, R.font.beiweikaishu))
                fontTypeSelected[1].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 1 }.forEach { it.set(false) }
            }
            "zedong" -> {
                fontType.set(ResourcesCompat.getFont(context, R.font.zedong))
                fontTypeSelected[2].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 2 }.forEach { it.set(false) }
            }
            "fzkatong" -> {
                fontType.set(ResourcesCompat.getFont(context, R.font.fzkatong))
                fontTypeSelected[3].set(true)
                fontTypeSelected.filterIndexed { i, _ -> i != 3 }.forEach { it.set(false) }
            }
            "jianzhi" -> {
                fontType.set(ResourcesCompat.getFont(context, R.font.jianzhi))
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
                //0
                fontColor.set(ContextCompat.getColor(context, R.color.read_font_default))
                backgroundColor.set(ContextCompat.getColor(context, R.color.read_bg_default))
            }
        }
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
            .putInt(context.getString(R.string.backgroundColorKey), which).apply()
    }

    fun changeBrightness(progress: Int) {
        brightnessCommand.value =
                (if (progress < 1) 1 else if (progress > 255) 255 else progress) / 255f
    }

    /**
     * 获取阅读记录
     */
    private fun getRecord(): Array<Int> {
        val queryResult = ReaderDbManager.shelfDao().getBookInfo(bookName)?.readRecord
            ?.split("#")?.map { it.toInt() }          //阅读记录 3#2 表示第3章第2页
        return arrayOf(queryResult?.get(0) ?: 1, queryResult?.get(1) ?: 1)
    }

    private fun updateCatalog() {
        try {
            DownloadCatalog(
                bookName, ReaderDbManager.shelfDao().getBookInfo(bookName)?.downloadUrl
                        ?: ""
            ).download("")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        maxChapterNum = ReaderDbManager.getChapterCount(bookName)
    }


    companion object {
        @JvmStatic
        val HEAD_VIEW = "headView"
        const val Font_SETTING = "fontSetting"
        const val BG_SETTING = "backgroundSetting"
        const val FOOT_VIEW = "footView"
    }

}