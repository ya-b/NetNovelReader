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
import com.netnovelreader.utils.get
import com.netnovelreader.utils.put
import com.netnovelreader.utils.sharedPreferences
import java.util.concurrent.atomic.AtomicInteger

class ReadViewModel(val repo: ChapterInfoRepo, app: Application) : AndroidViewModel(app) {
    val allChapters by lazy { ObservableArrayList<ChapterInfoEntity>() }   //目录
    val text by lazy { ObservableField<String>("") }                  //一页显示的内容
    val fontSize by lazy { ObservableFloat(55f) }                     //字体大小
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
    val isLoading by lazy { ObservableBoolean(true) }                  //是否显示加载进度条
    val showDialogCommand by lazy { MutableLiveData<Boolean>() }             //显示目录
    val changeSourceCommand by lazy { MutableLiveData<String>() }            //换源下载
    val brightnessCommand by lazy { MutableLiveData<Float>() }               //亮度
    val toastCommand = MutableLiveData<String>()
    var chapterNum = AtomicInteger(1)              //章节数
    @Volatile
    var maxChapterNum = 0                                    //最大章节数
    lateinit var bookName: String                            //书名
    var cacheNum: Int = 0                                   //缓存后面章节数量
    val notDeleteNum = 3                                     //自动删除已读章节，但保留最近3章
    var changeFontSizeFlag = false

    fun start() {
        val context = getApplication<Application>()
        fontSizeChangeEvent(
            context.sharedPreferences().get(
                context.getString(R.string.fontSizeKey),
                50f
            )
        )
        backgroundChangeEvent(
            context.sharedPreferences().get(
                context.getString(R.string.backgroundColorKey),
                0
            )
        )
        allChapters.addAll(repo.getAllChapters(bookName) ?: emptyList())
        allChapters.lastOrNull()?.chapterNum?.let { maxChapterNum = it }
    }

    //获取章节内容
    fun getChapter(chapterNum: Int) {
        val block = {
            isLoading.set(true)
            repo.getChapter(bookName, chapterNum) { chapter, isError ->
                if(isError) {
                    //todo 重新下载
                }
                isLoading.set(false)
                val chapterName = repo.getChapterInfo(bookName, chapterNum)?.chapterName
                text.set("${chapterName ?: ""}|$chapter")
            }
        }
        if (showDialogCommand.value == true) showDialogCommand.postValue(false)
        if(allChapters.size < 1) {
            repo.downloadCatalog(bookName) {
                if(it.isNotEmpty()) {
                    allChapters.addAll(it)
                    block.invoke()
                } else {
                    toastCommand.postValue("error")
                }
            }
        } else {
            block.invoke()
        }
    }

    //todo
    fun reloadCurrentChapter() {

    }

    /**
     * 保存阅读记录
     */
    fun setRecord(pageNum: Int) {
        repo.setRecord(bookName, chapterNum.get(), pageNum)
    }

    /**
     * todo 自动删除已读章节，但保留最近[notDeleteNum]章
     */
    fun autoRemove() {

    }

    fun prepare(): Int {
        val arr = repo.getRecord(bookName)
        getChapter(arr[0].also { chapterNum.set(it) })
        return arr[1]
    }

    fun onNextChapter() {
        isViewShow.forEach { it.value.set(false) }
        if(isLoading.get()) return
        if(chapterNum.get() < maxChapterNum) {
            getChapter(chapterNum.incrementAndGet())
        }
    }

    fun onPreviousChapter() {
        isViewShow.forEach { it.value.set(false) }
        if(isLoading.get()) return
        if(chapterNum.get() > 1) {
            getChapter(chapterNum.decrementAndGet())
        }
    }

    fun centerClick() {
        if (isViewShow[FOOT_VIEW]!!.get()) {
            isViewShow.forEach { it.value.set(false) }
        } else {
            isViewShow[FOOT_VIEW]!!.set(true)
            isViewShow[HEAD_VIEW]!!.set(true)
        }
    }

    fun getChapterByCatalog(chapterName: String) {
        if(isLoading.get()) return
        repo.getChapterInfo(bookName, chapterName) {
            chapterNum.set(it)
            getChapter(it)
        }
    }

    fun changeSource() {
        isViewShow.forEach { it.value.set(false) }
        val chapterName = repo.getChapterInfo(bookName, chapterNum.get())?.chapterName
        if(chapterName.isNullOrEmpty()) {
            //todo 章节错误
        } else {
            changeSourceCommand.value = chapterName
        }
    }

    fun footViewClickEvent(which: String) {
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
        val context = getApplication<Application>()
        context.sharedPreferences().put(context.getString(R.string.fontSizeKey), float)
    }

    fun backgroundChangeEvent(which: Int) {
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