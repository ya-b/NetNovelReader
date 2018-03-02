package com.netnovelreader.common

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.BR
import com.netnovelreader.R

/**
 * Created by yangbo on 18-1-12.
 */

class RecyclerAdapter<T, E>(
    private val itemDetails: ObservableArrayList<T>?,
    private val resId: Int,
    val clickEvent: E
) : RecyclerView.Adapter<RecyclerAdapter.BindingViewHolder<T, E>>() {
    lateinit var listener: ArrayListChangeListener<T, E>

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        listener = ArrayListChangeListener(this)
        itemDetails?.addOnListChangedCallback(listener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemDetails?.removeOnListChangedCallback(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<T, E> {
        val binding = if (viewType != -1) DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            resId, parent, false
        ) else DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_empty, parent, false
        )
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<T, E>, position: Int) {
        if (position == 0) holder.bind(null, null)
        else holder.bind(itemDetails?.get(position - 1), clickEvent)
    }

    override fun getItemCount(): Int {
        itemDetails ?: return 0
        return itemDetails.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) -1 else super.getItemViewType(position)
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
