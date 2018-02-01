package com.netnovelreader.common

import org.junit.Test

/**
 * Created by yangbo on 18-1-31.
 */
class UtilsKtTest {
    @Test
    fun fixUrl() {
        var reference = "https://www.23zw.me/hello.html"
        var src = "/world/nihao.html"
        val s = com.netnovelreader.common.fixUrl(reference, src)
        println(s)
    }

}