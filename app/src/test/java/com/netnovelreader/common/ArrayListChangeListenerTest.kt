package com.netnovelreader.common

import android.databinding.ObservableArrayList
import org.junit.After
import org.junit.Before
import org.junit.Test

class ArrayListChangeListenerTest {
    val list = ObservableArrayList<Int>()
    val listener = ArrayListChangeListener<Int> { println("hello") }

    @Before
    fun addListener() {
        list.addAll(0.. 10)
        list.addOnListChangedCallback(listener)
    }

    @After
    fun removeListener() {
        list.removeOnListChangedCallback(listener)
    }

    @Test
    fun onChanged() {
        list.add(11)
    }

    @Test
    fun onItemRangeChanged() {
    }

    @Test
    fun onItemRangeInserted() {
    }

    @Test
    fun onItemRangeMoved() {
    }

    @Test
    fun onItemRangeRemoved() {
    }
}