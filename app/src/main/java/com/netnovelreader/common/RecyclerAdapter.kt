package com.netnovelreader.common

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.BR
import com.netnovelreader.R
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.lang.ref.WeakReference

/**
 * Created by yangbo on 18-1-12.
 */


/**
 * @isoccupiedFirst 表示第一个item用空view占用（添加到第一个的动画）
 */
class RecyclerAdapter<T, E>(
    private val itemDetails: ObservableArrayList<T>?,
    private val resId: Int,
    val clickEvent: E,
    val isoccupiedFirst: Boolean
) : RecyclerView.Adapter<RecyclerAdapter.BindingViewHolder<T, E>>() {
    lateinit var listener: WeakReference<ArrayListChangeListener<T, E>>

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        listener = WeakReference(ArrayListChangeListener())
        itemDetails?.addOnListChangedCallback(listener.get())
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemDetails?.removeOnListChangedCallback(listener.get())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<T, E> {
        val binding =
            if (viewType != -1) DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context),
                resId, parent, false
            ) else DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_empty, parent, false
            )
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<T, E>, position: Int) {
        if (!isoccupiedFirst) {
            holder.bind(itemDetails?.get(position), clickEvent)
        } else if (position == 0) {
            holder.bind(null, null)
        } else {
            holder.bind(itemDetails?.get(position - 1), clickEvent)
        }
    }

    override fun getItemCount(): Int {
        itemDetails ?: return 0
        return if (isoccupiedFirst) itemDetails.size + 1 else itemDetails.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (isoccupiedFirst && position == 0) -1 else 0
    }

    class BindingViewHolder<in T, in E>(private val binding: ViewDataBinding?) :
        RecyclerView.ViewHolder(binding?.root) {
        fun bind(itemData: T?, clickEvent: E?) {
            binding?.setVariable(BR.itemDetail, itemData)
            binding?.setVariable(BR.clickEvent, clickEvent)
            binding?.executePendingBindings()
        }
    }

    inner class ArrayListChangeListener<T, E> :
        ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {

        override fun onChanged(p0: ObservableArrayList<T>?) {
            launch(UI) { notifyDataSetChanged() }
        }

        override fun onItemRangeChanged(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
            launch(UI) {
                (if (isoccupiedFirst) p1 + 1 else p1).also {
                    notifyItemRangeRemoved(it, p2)
                    notifyItemRangeInserted(it, p2)
                }
            }
        }

        override fun onItemRangeInserted(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
            launch(UI) {
                (if (isoccupiedFirst) p1 + 1 else p1).also {
                    if (p0?.size == p2) {
                        notifyDataSetChanged()           //添加所有，不显示动画效果，避免屏幕闪烁
                    } else {
                        notifyItemRangeInserted(it, p2)
                    }
                }
            }
        }

        override fun onItemRangeMoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int, p3: Int) {
            launch(UI) {
                (if (isoccupiedFirst) p1 + 1 else p1).also {
                    notifyItemRangeRemoved(it, p3)
                    notifyItemRangeInserted(it, p3)
                }
            }
        }

        override fun onItemRangeRemoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
            launch(UI) {
                (if (isoccupiedFirst) p1 + 1 else p1).also {
                    notifyItemRangeRemoved(it, p2)
                }
            }
        }
    }
}
