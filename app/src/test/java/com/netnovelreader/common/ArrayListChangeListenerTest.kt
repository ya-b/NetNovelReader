package com.netnovelreader.common

import android.databinding.ObservableArrayList
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class ArrayListChangeListenerTest {
    val list = ObservableArrayList<Int>()
    @Mock
    lateinit var hello: Set<Int>

    @Before
    fun addListener() {
        MockitoAnnotations.initMocks(this)
        val listener = ArrayListChangeListener<Int> { hello.isEmpty() }
        list.addAll(0..10)
        list.addOnListChangedCallback(listener)
    }

    @Test
    fun onChanged() {
        list.addAll(arrayOf(3, 6))
        verify(hello).isEmpty()
    }

    @Test
    fun onItemRangeChanged() {
        list[5] = 666
        verify(hello).isEmpty()

    }

    @Test
    fun onItemRangeInserted() {
        list.add(30)
        verify(hello).isEmpty()
    }

//    @Test
//    fun onItemRangeMoved() {
//
//    }

    @Test
    fun onItemRangeRemoved() {
        list.removeAt(5)
        verify(hello).isEmpty()
    }
}