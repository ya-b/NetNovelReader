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
        launch {
            List(10){
                launch {
                    delay(1000)
                    println(it)
                }
            }
        }
        Thread.sleep(10000)

    }
    suspend fun hello(): Int{
        return async {
            delay(2000)
            1
        }.await()
    }
}
