package com.netnovelreader.base

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.netnovelreader.BR

/**
 * Created by yangbo on 18-1-12.
 */

class BindingAdapter<T>(var itemDetails: ObservableArrayList<T>?, val resId: Int, val clickEvent: IClickEvent?) : RecyclerView.Adapter<BindingAdapter.BindingViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<T> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                resId, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<T>, position: Int) {
        holder.bind(itemDetails?.get(position), clickEvent)
    }

    override fun getItemCount(): Int {
        itemDetails ?: return 0
        return itemDetails!!.size
    }

    fun changeDataSet(itemDetails: ObservableArrayList<T>?){
        this.itemDetails = itemDetails
        notifyDataSetChanged()
    }

    class BindingViewHolder<T>(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(itemData: T?, clickEvent: IClickEvent?){
            binding.setVariable(BR.itemDetail, itemData)
            binding.setVariable(BR.clickEvent, clickEvent)
            binding.executePendingBindings()
        }
    }
}
