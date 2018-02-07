package com.netnovelreader.common.data

import org.junit.Test

internal class ParseHtmlTest {

    @Test
    fun getChapter() {
    }

    @Test
    fun getCatalog() {
        var arraylist = ArrayList<Int>()
        for(i in 0 ..10){
            arraylist.add(i)
        }
        val it = arraylist.iterator()
        while (it.hasNext()){
            val a = it.next()
            if(a > 5){
                arraylist.remove(a)
            }
        }
    }
}