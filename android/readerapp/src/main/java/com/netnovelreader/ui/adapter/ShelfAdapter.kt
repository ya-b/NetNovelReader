package com.netnovelreader.ui.adapter

import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.data.local.db.ShelfBean
import com.netnovelreader.databinding.ItemShelfBinding
import com.netnovelreader.viewmodel.ShelfViewModel

class ShelfAdapter(var clickEvent: ShelfViewModel?) : PagedListAdapter<ShelfBean, ShelfAdapter.ShelfViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ShelfBean>() {
            override fun areItemsTheSame(oldItem: ShelfBean, newItem: ShelfBean): Boolean =
                oldItem._id == newItem._id
            override fun areContentsTheSame(oldItem: ShelfBean, newItem: ShelfBean): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder{
        var binding = DataBindingUtil.inflate<ItemShelfBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_shelf, parent, false
        )
        return ShelfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        holder.bindTo(getItem(position), clickEvent)
    }

    class ShelfViewHolder(val binding: ItemShelfBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindTo(bean: ShelfBean?, clickEvent: ShelfViewModel?){
            binding.itemDetail = bean
            binding.clickEvent = clickEvent
            binding.executePendingBindings()
        }
    }
}