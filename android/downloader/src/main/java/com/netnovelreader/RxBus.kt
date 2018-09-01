package com.netnovelreader

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.processors.FlowableProcessor


class RxBus private constructor() {

    private val mBus: FlowableProcessor<Any>

    init {
        // toSerialized method made bus thread safe
        mBus = PublishProcessor.create<Any>().toSerialized()
    }

    fun post(obj: Any) {
        mBus.onNext(obj)
    }

    fun <T> toFlowable(tClass: Class<T>): Flowable<T> {
        return mBus.ofType(tClass)
    }

    fun toFlowable(): Flowable<Any> {
        return mBus
    }

    fun hasSubscribers(): Boolean {
        return mBus.hasSubscribers()
    }

    companion object {
        private val BUS by lazy { RxBus() }

        fun get(): RxBus {
            return BUS
        }
    }
}