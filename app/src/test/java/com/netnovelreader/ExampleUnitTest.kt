package com.netnovelreader

import io.reactivex.Observable
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        var taskList = ArrayList<String>()
        taskList.add("3iofj")
        taskList.add("fowijf")
        taskList.add("fjoiwifo")
        Observable.fromIterable(taskList).flatMap { s -> Observable.just(s + "====") }.subscribe { s -> println(s) }
    }
}
