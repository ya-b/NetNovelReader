package com.netnovelreader.ui.adapters

import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.databinding.ItemShelfBinding
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.vm.ShelfViewModel

class ShelfPageListAdapter(val vm: ShelfViewModel?) :
    PagedListAdapter<BookInfoEntity, ShelfPageListAdapter.ShelfViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<BookInfoEntity>() {
            override fun areItemsTheSame(oldItem: BookInfoEntity?, newItem: BookInfoEntity?) =
                oldItem?._id ?: -1 == newItem?._id ?: -2

            override fun areContentsTheSame(oldItem: BookInfoEntity?, newItem: BookInfoEntity?) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        val binding = DataBindingUtil.inflate<ItemShelfBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_shelf, parent, false
        )
        return ShelfViewHolder(binding, vm)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ShelfViewHolder(var binding: ItemShelfBinding, val vm: ShelfViewModel?) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(entity: BookInfoEntity?) {
            binding.itemDetail = entity
            binding.clickListener = vm
        }
    }
}