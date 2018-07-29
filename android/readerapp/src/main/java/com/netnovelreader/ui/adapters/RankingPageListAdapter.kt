package com.netnovelreader.ui.adapters

import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.databinding.HeaderRankingBinding
import com.netnovelreader.databinding.ItemRankingBinding
import com.netnovelreader.repo.http.resp.BookLinkResp
import com.netnovelreader.vm.RankingViewModel

class RankingPageListAdapter(val vm: RankingViewModel?) :
    PagedListAdapter<BookLinkResp, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<BookLinkResp>() {
            override fun areItemsTheSame(oldItem: BookLinkResp?, newItem: BookLinkResp?) =
                oldItem?.bookname == newItem?.bookname

            override fun areContentsTheSame(oldItem: BookLinkResp?, newItem: BookLinkResp?) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> DataBindingUtil.inflate<HeaderRankingBinding>(
                LayoutInflater.from(parent.context),
                R.layout.header_ranking, parent, false
            ).let { object : RecyclerView.ViewHolder(it.root) { } }

            else -> DataBindingUtil.inflate<ItemRankingBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_ranking, parent, false
            ).let { RankingViewHolder(it, vm) }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position > 0) {
            (holder as RankingViewHolder).bindTo(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int = position

    class RankingViewHolder(var binding: ItemRankingBinding, val vm: RankingViewModel?) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(entity: BookLinkResp?) {
            binding.bean = entity
            binding.click = vm
        }
    }
}