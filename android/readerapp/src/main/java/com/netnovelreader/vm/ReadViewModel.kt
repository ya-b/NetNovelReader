package com.netnovelreader.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.databinding.*
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.netnovelreader.R
import com.netnovelreader.repo.ChapterInfoRepo
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.http.paging.NetworkState
import com.netnovelreader.utils.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.io.File
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
    val networkState by lazy { MutableLiveData<NetworkState>() }                  //是否显示加载进度条
    val showDialogCommand by lazy { MutableLiveData<Boolean>() }             //显示目录
    val cacheDialogCommand by lazy { MutableLiveData<Void>() }
    val changeSourceCommand by lazy { MutableLiveData<StringBuilder>() }            //换源下载
    val brightnessCommand by lazy { MutableLiveData<Float>() }               //亮度
    val toastCommand = MutableLiveData<String>()
    val exitCommand = MutableLiveData<Void>()
    val initPageViewCommand = MutableLiveData<Int>()
    var chapterNum = AtomicInteger(1)              //章节数
    @Volatile
    var maxChapterNum = 0                                    //最大章节数
    lateinit var bookName: String                            //书名
    var remainNum = 3                                       //删除已读章节，但保留最近3章
    var cacheNum: Int = 0                                   //缓存后面章节数量
    var retry: (() -> Any)? = null
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

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
        repo.getChapterCount(bookName)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { int ->
                if (int < 1) {
                    repo.downloadCatalog(bookName)
                } else {
                    Single.create<List<ChapterInfoEntity>> { it.onSuccess(emptyList()) }
                }
            }.flatMap {
                if (it.isNotEmpty()) {
                    repo.saveCatalog(it)
                    initPageNum()
                    Single.just(it)
                } else {
                    initPageNum()
                    repo.getAllChapters(bookName).toSingle()
                }
            }
            .subscribe(
                { list ->
                    allChapters.clear()
                    allChapters.addAll(list)
                    allChapters.lastOrNull()?.chapterNum?.let { maxChapterNum = it }
                },
                {
                    LoggerFactory.getLogger(ReadViewModel::class.java).debug(it.toString())
                }).let { compositeDisposable.add(it) }
        getApplication<Application>().apply {
            sharedPreferences().get(getString(R.string.auto_download_key), false)
                .takeIf { it }
                ?.let { cacheNum = 3 }
        }
    }

    fun destroy() {
        compositeDisposable.clear()
    }

    //获取章节内容
    fun getChapter(chapterNum: Int) {
        if (showDialogCommand.value == true) showDialogCommand.postValue(false)
        networkState.postValue(NetworkState.LOADING)
        repo.getChapter(bookName, chapterNum)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                { pair ->
                    text.set("${pair.second.chapterName}|${pair.first}")
                    if (pair.first.isNotEmpty()) {
                        retry = null
                        networkState.postValue(NetworkState.LOADED)
                    } else {
                        retry = { getChapter(chapterNum) }
                        networkState.postValue(NetworkState.error("error"))
                    }
                },
                { t ->
                    LoggerFactory.getLogger(this.javaClass).warn("getChapter$t")
                })
            .let { compositeDisposable.add(it) }
        if(cacheNum > 0) {
            repo.downCacheChapter(bookName, chapterNum, cacheNum)  //下载chapterNum之后cacheNum章
                .subscribe()
                .let { compositeDisposable.add(it) }
        }
    }

    fun retryFailed() {
        val prevRetry = retry
        retry = null
        ioThread { prevRetry?.invoke() }
    }

    /**
     * 自动删除已读章节，但保留最近3章
     */
    fun autoDelCache() {
        getApplication<Application>().apply {
            sharedPreferences().get(getString(R.string.auto_remove_key), false)
                .takeIf { it && remainNum > 0 }
                ?.let { repo.delCacheChapter(bookName, chapterNum.get(), remainNum) }
                ?.subscribe { list ->
                    list.map { File(bookDir(bookName), it.chapterNum.toString()) }
                        .forEach { it.deleteRecursively() }
                }
        }
    }

    fun initPageNum() {
        repo.getRecord(bookName)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    chapterNum.set(it[0])
                    getChapter(it[0])
                    initPageViewCommand.postValue(it[1])
                },
                {
                    initPageViewCommand.postValue(1)
                }).let { compositeDisposable.add(it) }
    }

    fun onNextChapter() {
        if (chapterNum.get() < maxChapterNum) {
            val chapterNum = chapterNum.incrementAndGet()
            getChapter(chapterNum)
        }
    }

    fun onPreviousChapter() {
        if (chapterNum.get() > 1) {
            val chapterNum = chapterNum.decrementAndGet()
            getChapter(chapterNum)
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
        //保存阅读记录
        repo.setRecord(bookName, "${chapterNum.get()}#${if (pageNum < 1) 1 else pageNum}")
    }

    fun getChapterByCatalog(chapterName: String) {
        repo.getChapterInfo(bookName, chapterName)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                { chapterEntity ->
                    chapterNum.set(chapterEntity.chapterNum)
                    val title = allChapters.filter { it.chapterNum == chapterNum.get() }
                        .firstOrNull()?.chapterName ?: ""
                    text.set("${title}|")
                    getChapter(chapterEntity.chapterNum)
                },
                {
                    LoggerFactory.getLogger(this.javaClass).warn("getChapterByCatalog$it")
                    toastCommand.postValue("error!!!")
                }).let { compositeDisposable.add(it) }
    }

    fun changeSource() {
        isViewShow.forEach { it.value.set(false) }
        repo.getChapterInfo(bookName, chapterNum.get())
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    changeSourceCommand.postValue(StringBuilder(it.chapterName))
                },
                {
                    toastCommand.postValue("error on get chapter")
                    LoggerFactory.getLogger(this.javaClass).warn("changeSource$it")
                }).let { compositeDisposable.add(it) }
    }

    fun showCacheDialog() {
        isViewShow.forEach { it.value.set(false) }
        cacheDialogCommand.postValue(null)
    }

    fun cacheContent() {
        isViewShow.forEach { it.value.set(false) }
        repo.downCacheChapter(bookName, chapterNum.get() + 1, 99999)  //下载chapterNum之后cacheNum章
            .subscribe()
            .let { compositeDisposable.add(it) }
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

    fun exit() {
        exitCommand.postValue(null)
    }

    companion object {
        @JvmStatic
        val HEAD_VIEW = "headView"
        const val Font_SETTING = "fontSetting"
        const val BG_SETTING = "backgroundSetting"
        const val FOOT_VIEW = "footView"
    }
}