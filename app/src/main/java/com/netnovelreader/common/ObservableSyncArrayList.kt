package com.netnovelreader.common

import android.databinding.ObservableArrayList
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

class ObservableSyncArrayList<T> : ObservableArrayList<T>() {

    @Synchronized
    override fun addAll(elements: Collection<T>): Boolean {
        return super.addAll(elements)
    }

    @Synchronized
    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements)
    }

    @Synchronized
    override fun clear() {
        super.clear()
    }

    @Synchronized
    override fun add(element: T): Boolean {
        return super.add(element)
    }

    @Synchronized
    override fun add(index: Int, element: T) {
        super.add(index, element)
    }

    @Synchronized
    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
    }

    @Synchronized
    override fun removeAt(index: Int): T {
        return super.removeAt(index)
    }

    @Synchronized
    override fun remove(element: T): Boolean {
        return super.remove(element)
    }

    @Synchronized
    override fun set(index: Int, element: T): T {
        return super.set(index, element)
    }

    @Synchronized
    override fun contains(element: T): Boolean {
        return super.contains(element)
    }

    @Synchronized
    override fun replaceAll(operator: UnaryOperator<T>) {
        super<ObservableArrayList>.replaceAll(operator)
    }

    @Synchronized
    override fun listIterator(index: Int): MutableListIterator<T> {
        return super.listIterator(index)
    }

    @Synchronized
    override fun listIterator(): MutableListIterator<T> {
        return super.listIterator()
    }

    @Synchronized
    override fun removeAll(elements: Collection<T>): Boolean {
        return super.removeAll(elements)
    }

    @Synchronized
    override fun clone(): Any {
        return super.clone()
    }

    @Synchronized
    override fun iterator(): MutableIterator<T> {
        return super.iterator()
    }

    @Synchronized
    override fun get(index: Int): T {
        return super.get(index)
    }

    @Synchronized
    override fun forEach(action: Consumer<in T>?) {
        super<ObservableArrayList>.forEach(action)
    }

    @Synchronized
    override fun trimToSize() {
        super.trimToSize()
    }

    @Synchronized
    override fun spliterator(): Spliterator<T> {
        return super<ObservableArrayList>.spliterator()
    }

    @Synchronized
    override fun toArray(): Array<Any> {
        return super.toArray()
    }

    @Synchronized
    override fun <T : Any?> toArray(a: Array<out T>?): Array<T> {
        return super.toArray(a)
    }

    @Synchronized
    override fun indexOf(element: T): Int {
        return super.indexOf(element)
    }

    @Synchronized
    override fun lastIndexOf(element: T): Int {
        return super.lastIndexOf(element)
    }

    @Synchronized
    override fun isEmpty(): Boolean {
        return super.isEmpty()
    }

    @Synchronized
    override fun sort(c: Comparator<in T>?) {
        super<ObservableArrayList>.sort(c)
    }

    @Synchronized
    override fun removeIf(filter: Predicate<in T>): Boolean {
        return super<ObservableArrayList>.removeIf(filter)
    }

    @Synchronized
    override fun ensureCapacity(minCapacity: Int) {
        super.ensureCapacity(minCapacity)
    }

    @Synchronized
    override fun retainAll(elements: Collection<T>): Boolean {
        return super.retainAll(elements)
    }

    @Synchronized
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return super.subList(fromIndex, toIndex)
    }

    override val size: Int
        @Synchronized
        get() = super.size
}