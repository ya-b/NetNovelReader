package com.netnovelreader.ui.adapters

import androidx.paging.PagedListAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.databinding.ItemRankingBinding
import com.netnovelreader.databinding.ItemRankingNetworkErrorBinding
import com.netnovelreader.databinding.ItemRankingSourceBinding
import com.netnovelreader.repo.http.paging.NetworkState
import com.netnovelreader.repo.http.resp.BookLinkResp
import com.netnovelreader.vm.RankingViewModel

class RankingPageListAdapter(val vm: RankingViewModel?) :
    PagedListAdapter<BookLinkResp, androidx.recyclerview.widget.RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<BookLinkResp>() {
            override fun areItemsTheSame(oldItem: BookLinkResp, newItem: BookLinkResp) =
                oldItem.bookname == newItem.bookname

            override fun areContentsTheSame(oldItem: BookLinkResp, newItem: BookLinkResp) =
                oldItem == newItem
        }
    }

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_ranking -> DataBindingUtil.inflate<ItemRankingBinding>(
                LayoutInflater.from(parent.context),
                viewType, parent, false
            ).let { RankingViewHolder(it, vm) }

            R.layout.item_ranking_network_error ->
                DataBindingUtil.inflate<ItemRankingNetworkErrorBinding>(
                    LayoutInflater.from(parent.context),
                    viewType, parent, false
                ).also { it.clickHandler = vm }
                    .let { object : androidx.recyclerview.widget.RecyclerView.ViewHolder(it.root) {} }

            else -> DataBindingUtil.inflate<ItemRankingSourceBinding>(
                LayoutInflater.from(parent.context),
                viewType, parent, false
            ).let { object : androidx.recyclerview.widget.RecyclerView.ViewHolder(it.root) {} }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (position > 0) {
            (holder as RankingViewHolder).bindTo(getItem(position - 1))
        }
    }

    override fun getItemCount(): Int = super.getItemCount() + 1

    override fun getItemViewType(position: Int): Int =
        if (position == 0) {
            if (hasExtraRow()) {
                R.layout.item_ranking_network_error
            } else {
                R.layout.item_ranking_source
            }
        } else {
            R.layout.item_ranking
        }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyDataSetChanged()
            } else {
                notifyDataSetChanged()
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyDataSetChanged()
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    class RankingViewHolder(var binding: ItemRankingBinding, val vm: RankingViewModel?) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bindTo(entity: BookLinkResp?) {
            binding.bean = entity
            binding.click = vm
        }
    }
}