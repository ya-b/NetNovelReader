package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import com.netnovelreader.common.id2TableName
import com.netnovelreader.data.SQLHelper
import java.util.Vector
import kotlin.collections.ArrayList

/**
 * Created by yangbo on 18-1-13.
 */

class ReaderViewModel(private val bookName: String, private val CACHE_NUM: Int) :
    IReaderContract.IReaderViewModel {
    var catalog = ObservableArrayList<ReaderBean.Catalog>()
    /**
     * 一页显示的内容
     */
    var text = ObservableArrayList<String>()
    /**
     * 一章的所有内容
     */
    private var chapterText = Vector<ArrayList<String>>()

    @Volatile
    private var dirName: String? = null

    @Volatile
    private var chapterName: String? = null
    /**
     * 章节数，页码，最大章节数，最大页码，例如1,1,4,5 == 第一章第一页,总共4章，这一章有5页
     */
    @Volatile
    var pageIndicator = IntArray(4) { _ -> 1 }

    private var tableName = ""
    private val chapterCache: ChapterCache

    init {
        val cursor = SQLHelper.getDB().rawQuery(
            "select ${SQLHelper.ID} from " +
                    "${SQLHelper.TABLE_SHELF} where ${SQLHelper.BOOKNAME}='$bookName';",
            null
        )
        if (cursor.moveToFirst()) {
            tableName = id2TableName(cursor.getInt(0))
        }
        cursor.close()
        chapterCache = ChapterCache(CACHE_NUM, tableName)
    }

    /**
     * readerView第一次绘制时执行
     */
    override fun initData(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float) {
        pageIndicator[2] = getChapterCount()
        if (pageIndicator[2] == 0) {
            for (i in 0..3) {
                pageIndicator[i] = 0
            }
            return
        }
        val array = getRecord()
        pageIndicator[0] = array[0]
        pageIndicator[1] = array[1]
        chapterCache.prepare(pageIndicator[0], pageIndicator[2], dirName!!)
        getPage(pageIndicator, textAreaWidth, textAreaHeight, txtFontSize)
        pageIndicator[3] = chapterText.size
        updateTextAndRecord(pageIndicator)
    }

    override fun pageToNext(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float) {
        if (pageIndicator[1] == pageIndicator[3] || pageIndicator[3] < 2) {
            if (pageIndicator[0] == pageIndicator[2]) {
                return
            } else {
                //下一章
                pageIndicator[0] += 1
                getPage(pageIndicator, textAreaWidth, textAreaHeight, txtFontSize)
                pageIndicator[3] = chapterText.size
                if (pageIndicator[3] != 0) {
                    pageIndicator[1] = 1
                } else {
                    pageIndicator[1] = 0
                }
                updateTextAndRecord(pageIndicator)
            }
        } else {
            //同一章下一页
            pageIndicator[1] += 1
            updateTextAndRecord(pageIndicator)
        }
    }

    override fun pageToPrevious(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float) {
        if (pageIndicator[1] < 2) {
            if (pageIndicator[0] < 2) {
                return
            } else {
                //上一章
                pageIndicator[0] -= 1
                getPage(pageIndicator, textAreaWidth, textAreaHeight, txtFontSize)
                pageIndicator[3] = chapterText.size
                pageIndicator[1] = pageIndicator[3]
                updateTextAndRecord(pageIndicator)
            }
        } else {
            //同一章上一页
            pageIndicator[1] -= 1
            updateTextAndRecord(pageIndicator)
        }
    }

    /**
     * 翻页到目录中的某章
     */
    override fun pageByCatalog(
        chapterName: String,
        textAreaWidth: Int,
        textAreaHeight: Int,
        txtFontSize: Float
    ) {
        pageIndicator[0] = SQLHelper.getChapterId(tableName, chapterName)
        pageIndicator[1] = 1
        getPage(pageIndicator, textAreaWidth, textAreaHeight, txtFontSize)
        pageIndicator[3] = chapterText.size
        if (pageIndicator[3] > 1) {
            pageIndicator[1] = 1
        } else {
            pageIndicator[1] = pageIndicator[3]
        }
        updateTextAndRecord(pageIndicator)
    }

    override fun changeFontSize(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float) {
        val s = pageIndicator[3].toFloat() / pageIndicator[1].toFloat()
        getPage(pageIndicator, textAreaWidth, textAreaHeight, txtFontSize)
        pageIndicator[3] = chapterText.size
        pageIndicator[1] = Math.round(pageIndicator[3] / s)
        if (pageIndicator[1] == 0) pageIndicator[1] = 1
        updateTextAndRecord(pageIndicator)
    }

    /**
     * 重新读取目录
     */
    @Synchronized
    override fun updateCatalog(): ObservableArrayList<ReaderBean.Catalog> {
        catalog.clear()
        val catalogCursor = SQLHelper.getDB().rawQuery(
            "select ${SQLHelper.CHAPTERNAME} " +
                    "from $tableName", null
        )
        while (catalogCursor.moveToNext()) {
            catalog.add(ReaderBean.Catalog(catalogCursor.getString(0)))
        }
        catalogCursor.close()
        return catalog
    }

    /**
     * 获取阅读记录
     */
    private fun getRecord(): IntArray {
        val queryResult = SQLHelper.getRecord(bookName) //阅读记录 3#2 表示第3章第2页
        dirName = id2TableName(queryResult[0])
        var readRecord = queryResult[1]
        if (readRecord.length < 1) {
            readRecord = "1#1"
        }
        val array = readRecord.split("#")
        return IntArray(2) { i -> array[i].toInt() }
    }

    /**
     * 获取章节总数
     */
    private fun getChapterCount(): Int {
        return SQLHelper.getChapterCount(tableName)
    }

    /**
     * 获取某一章的字符串，进行分割
     * @chapterNum
     * @dirName
     * @isNext  1 下一章,pagenum=1 ， -1 上一章 pagenum=pageIndicator[3] ,0 pagenum不变
     */
    private fun getPage(
        pageIndicator: IntArray,
        textAreaWidth: Int,
        textAreaHeight: Int,
        txtFontSize: Float
    ) {
        val chapterTxt = chapterCache.getChapter(pageIndicator[0])
        val indexOfDelimiter = chapterTxt.indexOf("|")
        chapterName = chapterTxt.substring(0, indexOfDelimiter)
        chapterText = splitChapterTxt(
            chapterTxt.substring(indexOfDelimiter + 1), textAreaWidth,
            textAreaHeight, txtFontSize
        )
    }

    /**
     * 更改view要显示的字符串 text
     * 保存阅读记录
     */
    @Synchronized
    private fun updateTextAndRecord(pageIndicator: IntArray) {
        text.clear()
        text.add("${chapterName}：${pageIndicator[1]}/${pageIndicator[3]}")
        if (chapterText.size < 1) return
        if (pageIndicator[1] - 1 < chapterText.size) {
            chapterText.get(pageIndicator[1] - 1).forEach { text.add(it) }
        }
        SQLHelper.setRecord(bookName, "${pageIndicator[0]}#${pageIndicator[1]}")
    }

    /**
     * 将一章分割成Vector<ObservableArrayList<String>>，表示：Vector每一项表示一页，ArrayList每一项表示一行
     * @textAreaWidth 屏幕宽
     * @textAreaHeight 屏幕高
     * @txtFontSize 字体大小
     */
    private fun splitChapterTxt(
        chapter: String,
        textAreaWidth: Int,
        textAreaHeight: Int,
        txtFontSize: Float
    )
            : Vector<ArrayList<String>> {
        if (chapter.length < 1) return Vector()
        val tmpArray = chapter.split("\n")
        val tmplist = ArrayList<String>()
        tmpArray.forEach {
            val tmp = "  " + it.trim()
            val totalCount = textAreaWidth / txtFontSize.toInt()
            if (tmp.length > totalCount) {
                val count = tmp.length / totalCount
                for (i in 0..count - 1) {
                    tmplist.add(tmp.substring(i * totalCount, (i + 1) * totalCount))
                }
                if (it.length % totalCount != 0) {
                    tmplist.add(tmp.substring(count * totalCount))
                }
            } else {
                tmplist.add(tmp)
            }
        }
        val arrayList = Vector<ArrayList<String>>()
        val totalCount = textAreaHeight / txtFontSize.toInt()
        if (tmplist.size > totalCount) {
            val count = tmplist.size / totalCount
            for (i in 0..count - 1) {
                val a = ObservableArrayList<String>()
                tmplist.subList(i * totalCount, (i + 1) * totalCount).forEach { a.add(it) }
                arrayList.add(a)
            }
            if (tmplist.size % totalCount != 0) {
                val b = ObservableArrayList<String>()
                tmplist.subList(count * totalCount, tmplist.size - 1).forEach { b.add(it) }
                arrayList.add(b)
            }
        }
        return arrayList
    }
}