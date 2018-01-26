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

    @Test
    fun hello(){
        var arr1 = ArrayList<Int>()
        arr1 .add(3)
        var arr2 = ArrayList<Int>()
        arr2.add(4)
        arr1.forEach {
            arr2.forEach{
                println(it)
            }
        }
    }
}
