package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import android.util.Log
import com.netnovelreader.data.database.ChapterSQLManager
import com.netnovelreader.data.database.ShelfSQLManager
import com.netnovelreader.utils.getSavePath
import java.io.FileReader
import java.io.IOException

/**
 * Created by yangbo on 18-1-13.
 */
class ReaderViewModel(val bookName: String) : IReaderContract.IReaderViewModel {
    var text = ObservableArrayList<String>()

    /**
     * 获取小说章节内容
     * @chapterNum:Int 章节数
     * @dirname:String 代表对应书籍目录名，sqlite表名
     */
    @Throws(IOException::class)
    override fun getChapterText(chapterNum: Int, dirName: String): String {
        val sb = StringBuilder()
        val chapterPath = "${getSavePath()}/$dirName/${ChapterSQLManager().getChapterName(dirName, chapterNum)}"
        val fr = FileReader(chapterPath)
        fr.forEachLine { sb.append(it + "\n") }
        fr.close()
        return sb.toString()
    }

    fun setRecord(bookname: String, record: String){
        ShelfSQLManager().setRecord(bookname, record)
    }

    /**
     * 获取章节总数
     */
    fun getChapterCount(): Int = ChapterSQLManager().getChapterCount("BOOK${ShelfSQLManager().getRecord(bookName)[0]}")

    /**
     * 将一章分割成ArrayList<ArrayList<String>>，表示：arraylist页《arraylist行》
     * @width 屏幕宽
     * @height 屏幕高
     * @txtFontSize 字体大小
     */
    fun splitChapterTxt(chapter: String, width: Int, height: Int, txtFontSize: Float): ArrayList<ObservableArrayList<String>>{
        val tmpArray = chapter.split("\n")
        val tmplist = ArrayList<String>()
        tmpArray.forEach{
            var tmp = "  " + it.trim()
            val totalCount = width / txtFontSize.toInt() - 1
            if(tmp.length > totalCount){
                val count = tmp.length / totalCount
                for(i in 0..count - 1){
                    tmplist.add(tmp.substring(i * totalCount, (i + 1) * totalCount))
                }
                if(it.length % totalCount != 0){
                    tmplist.add(tmp.substring(count * totalCount))
                }
            }else {
                tmplist.add(tmp)
            }
        }
        val arrayList = ArrayList<ObservableArrayList<String>>()
        val totalCount = height / txtFontSize.toInt() - 2
        if( tmplist.size > totalCount){
            val count = tmplist.size / totalCount
            for(i in 0..count -1){
                val a = ObservableArrayList<String>()
                tmplist.subList(i * totalCount, (i + 1) * totalCount).forEach{ a.add(it)}
                arrayList.add(a)
            }
            if(tmplist.size % totalCount != 0){
                val b = ObservableArrayList<String>()
                tmplist.subList(count * totalCount, tmplist.size - 1).forEach{ b.add(it)}
                arrayList.add(b)
            }
        }
        return arrayList
    }
}