package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.*
import android.graphics.Color
import android.support.v4.content.ContextCompat
import com.netnovelreader.R
import com.netnovelreader.repo.ChapterInfoRepo
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.get
import com.netnovelreader.utils.put
import com.netnovelreader.utils.sharedPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class ReadViewModel(val repo: ChapterInfoRepo, app: Application) : AndroidViewModel(app) {
    val allChapters by lazy { ObservableArrayList<ChapterInfoEntity>() }   //目录
    val text by lazy { ObservableField<String>("") }                  //一页显示的内容
    val fontSize by lazy { ObservableFloat(55f) }                     //字体大小
    val rowSpace by lazy { ObservableFloat(0.50f) }                    //字体大小
    val fontColor by lazy { ObservableInt(Color.BLACK) }                    //字体颜色
    val backgroundColor by lazy { ObservableInt(R.color.read_font_default) }//背景颜色
    val isViewShow by lazy {
        mapOf(
            Pair(HEAD_VIEW, ObservableBoolean(false)),                //是否显示HeadView
            Pair(Font_SETTING, ObservableBoolean(false)),             //是否显示FontSetting
            Pair(BG_SETTING, ObservableBoolean(false)),               //是否显示BackgroundSeting
            Pair(FOOT_VIEW, ObservableBoolean(false))                 //是否显示footview
        )
    }
    val fontSizeSelected = List(5) { ObservableBoolean(false) }   //字体大小设置Button是否选中
    val rowSpaceSelected = List(5) { ObservableBoolean(false) }   //行距Button是否选中
    val isLoading by lazy { ObservableBoolean(true) }                  //是否显示加载进度条
    val showDialogCommand by lazy { MutableLiveData<Boolean>() }             //显示目录
    val changeSourceCommand by lazy { MutableLiveData<String>() }            //换源下载
    val brightnessCommand by lazy { MutableLiveData<Float>() }               //亮度
    val toastCommand = MutableLiveData<String>()
    val initPageViewCommand = MutableLiveData<Int>()
    var chapterNum = AtomicInteger(1)              //章节数
    @Volatile
    var maxChapterNum = 0                                    //最大章节数
    lateinit var bookName: String                            //书名
    var cacheNum: Int = 0                                   //缓存后面章节数量
    val preserveSize = 3                                     //自动删除已读章节，但保留最近3章

    fun start() {
        val context = getApplication<Application>()
        changeFontSize(
            context.sharedPreferences().get(
                context.getString(R.string.fontSizeKey),
                50f
            )
        )
        changeRowSpace(
            context.sharedPreferences().get(
                context.getString(R.string.rowSpaceKey),
                0.50f
            )
        )
        changeBackground(
            context.sharedPreferences().get(
                context.getString(R.string.backgroundColorKey),
                0
            )
        )
        repo.getAllChapters(bookName)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe {
                allChapters.addAll(it)
                allChapters.lastOrNull()?.chapterNum?.let { maxChapterNum = it }
                if (allChapters.size == 0) {
                    downloadCatalog()
                } else {
                    initPageNum()
                }
            }
        getApplication<Application>().apply {
            sharedPreferences().get(getString(R.string.auto_download_key), false)
                .takeIf { it }
                ?.let { cacheNum = 3 }
        }
    }

    //获取章节内容
    fun getChapter(chapterNum: Int) {
        if (showDialogCommand.value == true) showDialogCommand.postValue(false)
        isLoading.set(true)
        repo.getChapter(bookName, chapterNum)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                { chapter ->
                    isLoading.set(false)
                    var chapterName = allChapters.filter { it.chapterNum == chapterNum }
                        .firstOrNull()?.chapterName ?: ""
                    text.set("${chapterName}|$chapter")
                },
                {
                    //todo 重新下载
                    LoggerFactory.getLogger(this.javaClass).warn("getChapter$it")
                })
        repo.downCacheChapter(bookName, chapterNum, cacheNum)  //下载chapterNum之后cacheNum章
    }

    /**
     * 重装app，恢复阅读记录时调用（此时有阅读记录，但没有目录）
     */
    fun downloadCatalog() {
        repo.downloadCatalog(bookName)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    allChapters.addAll(it)
                    initPageNum()
                    repo.saveCatalog(it)
                },
                {
                    toastCommand.postValue("error on get catalog")
                    LoggerFactory.getLogger(this.javaClass).warn("downloadCatalog$it")
                }
            )
    }

    //todo
    fun reloadCurrentChapter() {

    }

    /**
     * 自动删除已读章节，但保留最近3章
     */
    fun autoDelCache() {
        getApplication<Application>().apply {
            sharedPreferences().get(getString(R.string.auto_remove_key), false)
                .takeIf { it }
                ?.let { repo.delCacheChapter(bookName, chapterNum.get(), 3) }
        }
    }

    fun initPageNum() {
        repo.getRecord(bookName)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    getChapter(it[0].also { chapterNum.set(it) })
                    initPageViewCommand.postValue(it[1])
                },
                {
                    initPageViewCommand.postValue(1)
                })
    }

    fun onNextChapter() {
        if (isLoading.get()) return
        if (chapterNum.get() < maxChapterNum) {
            getChapter(chapterNum.incrementAndGet())
        }
    }

    fun onPreviousChapter() {
        if (isLoading.get()) return
        if (chapterNum.get() > 1) {
            getChapter(chapterNum.decrementAndGet())
        }
    }

    fun onCenterClick() {
        if (isViewShow[FOOT_VIEW]!!.get()) {
            isViewShow.forEach { it.value.set(false) }
        } else {
            isViewShow[FOOT_VIEW]!!.set(true)
            isViewShow[HEAD_VIEW]!!.set(true)
        }
    }

    fun onPageChange(pageNum: Int) {
        isViewShow.forEach { it.value.set(false) }
        repo.setRecord(bookName, chapterNum.get(), pageNum)
    }

    fun getChapterByCatalog(chapterName: String) {
        if (isLoading.get()) return
        repo.getChapterInfo(bookName, chapterName)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    chapterNum.set(it.chapterNum)
                    getChapter(it.chapterNum)
                },
                {
                    LoggerFactory.getLogger(this.javaClass).warn("getChapterByCatalog$it")
                })
    }

    fun changeSource() {
        isViewShow.forEach { it.value.set(false) }
        repo.getChapterInfo(bookName, chapterNum.get())
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    changeSourceCommand.value = it.chapterName
                },
                {
                    toastCommand.postValue("error on get chapter")
                    LoggerFactory.getLogger(this.javaClass).warn("changeSource$it")
                })
    }

    fun clickFootView(which: String) {
        when (which) {
            "catalogButton" -> {
                isViewShow.forEach { it.value.set(false) }
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
    }

    fun changeFontSize(float: Float) {
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
        val context = getApplication<Application>()
        context.sharedPreferences().put(context.getString(R.string.fontSizeKey), float)
    }

    fun changeRowSpace(float: Float) {
        rowSpace.set(float + 1)
        when (float) {
            0.25f -> {
                rowSpaceSelected[0].set(true)
                rowSpaceSelected.filterIndexed { i, _ -> i != 0 }.forEach { it.set(false) }
            }
            0.50f -> {
                rowSpaceSelected[1].set(true)
                rowSpaceSelected.filterIndexed { i, _ -> i != 1 }.forEach { it.set(false) }
            }
            0.75f -> {
                rowSpaceSelected[2].set(true)
                rowSpaceSelected.filterIndexed { i, _ -> i != 2 }.forEach { it.set(false) }
            }
            1.00f -> {
                rowSpaceSelected[3].set(true)
                rowSpaceSelected.filterIndexed { i, _ -> i != 3 }.forEach { it.set(false) }
            }
            1.50f -> {
                rowSpaceSelected[4].set(true)
                rowSpaceSelected.filterIndexed { i, _ -> i != 4 }.forEach { it.set(false) }
            }
            else -> {
                rowSpaceSelected[1].set(true)
                rowSpaceSelected.filterIndexed { i, _ -> i != 1 }.forEach { it.set(false) }
            }
        }
        val context = getApplication<Application>()
        context.sharedPreferences().put(context.getString(R.string.rowSpaceKey), float)
    }

    fun changeBackground(which: Int) {
        val context = getApplication<Application>()
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
        context.sharedPreferences().put(context.getString(R.string.backgroundColorKey), which)
    }

    fun changeBrightness(progress: Int) {
        brightnessCommand.value =
                (if (progress < 1) 1 else if (progress > 255) 255 else progress) / 255f
    }

    companion object {
        @JvmStatic
        val HEAD_VIEW = "headView"
        const val Font_SETTING = "fontSetting"
        const val BG_SETTING = "backgroundSetting"
        const val FOOT_VIEW = "footView"
    }
}