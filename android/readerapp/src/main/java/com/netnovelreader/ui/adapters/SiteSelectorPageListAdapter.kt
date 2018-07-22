package com.netnovelreader.ui.adapters

import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.databinding.ItemSiteSelectorBinding
import com.netnovelreader.repo.db.SiteSelectorEntity
import com.netnovelreader.vm.SiteSelectorViewModel

class SiteSelectorPageListAdapter(val vm: SiteSelectorViewModel?) :
    PagedListAdapter<SiteSelectorEntity, SiteSelectorPageListAdapter.SelectorViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<SiteSelectorEntity>() {
            override fun areItemsTheSame(oldItem: SiteSelectorEntity?, newItem: SiteSelectorEntity?) =
                oldItem?._id ?: -1 == newItem?._id ?: -2

            override fun areContentsTheSame(oldItem: SiteSelectorEntity?, newItem: SiteSelectorEntity?) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectorViewHolder {
        val binding = DataBindingUtil.inflate<ItemSiteSelectorBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_site_selector, parent, false
        )
        return SelectorViewHolder(binding, vm)
    }

    override fun onBindViewHolder(holder: SelectorViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class SelectorViewHolder(var binding: ItemSiteSelectorBinding, val vm: SiteSelectorViewModel?) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(entity: SiteSelectorEntity?) {
            binding.itemData = entity
            binding.viewModel = vm
        }
    }
}