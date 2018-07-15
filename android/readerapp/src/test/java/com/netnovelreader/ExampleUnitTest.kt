package com.netnovelreader

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val arr = arrayOf(1, 2, 3)
        println("${arr.none { it == 5 }}")
    }
}
