package com.netnovelreader.common

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.netnovelreader.BR
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by yangbo on 18-1-12.
 */

class RecyclerAdapter<T, E>(
    private var itemDetails: ObservableArrayList<T>?,
    private val resId: Int,
    val clickEvent: E
) : RecyclerView.Adapter<RecyclerAdapter.BindingViewHolder<T, E>>() {
    val listener: ArrayListChangeListener<T> =
        ArrayListChangeListener { launch(UI) { this@RecyclerAdapter.notifyDataSetChanged() } }

    init {
        itemDetails?.addOnListChangedCallback(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<T, E> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            resId, parent, false
        )
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<T, E>, position: Int) {
        holder.bind(itemDetails?.get(position), clickEvent)
    }

    override fun getItemCount(): Int {
        itemDetails ?: return 0
        return itemDetails!!.size
    }

    fun removeDataChangeListener() {
        itemDetails?.removeOnListChangedCallback(listener)
    }

    class BindingViewHolder<in T, in E>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: T?, clickEvent: E?) {
            binding.setVariable(BR.itemDetail, itemData)
            binding.setVariable(BR.clickEvent, clickEvent)
            binding.executePendingBindings()
        }
    }
}
