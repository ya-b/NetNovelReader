package com.netnovelreader.utils

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.reactivex.subscribers.SerializedSubscriber

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