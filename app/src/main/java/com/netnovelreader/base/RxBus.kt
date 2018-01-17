package com.netnovelreader.base

import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor

/**
 * Created by yangbo on 2018/1/12.
 */

object RxBus {
    private val mbus: FlowableProcessor<Any>

    init {
        mbus = PublishProcessor.create()
    }

    fun post(obj: Any) {
        mbus.onNext(obj)
    }

    fun <T> toFlowable(type: Class<T>): Flowable<T> {
        return mbus.ofType(type)
    }

    fun toFlowable(): Flowable<Any> {
        return mbus
    }

    fun hasSubscribers(): Boolean {
        return mbus.hasSubscribers()
    }
}