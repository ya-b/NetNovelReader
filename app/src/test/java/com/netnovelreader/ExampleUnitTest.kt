package com.netnovelreader

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.junit.Test
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val list = ArrayList<String>()
        for(i in 0.. 10){
            list.add("$i")
        }
        for(i in 3.. 7){
            list.add("$i")
        }
        val set = HashSet<String>()
        list.filter { set.add(it) }.forEach { println(it) }
    }
}
