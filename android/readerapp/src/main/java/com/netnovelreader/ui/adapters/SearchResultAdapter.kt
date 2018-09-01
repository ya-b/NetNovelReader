package com.netnovelreader.ui.adapters

import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.databinding.ItemSearchBinding
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.uiThread
import com.netnovelreader.vm.SearchViewModel

class SearchResultAdapter(
    var vm: SearchViewModel?,
    private var searchResultList: ObservableArrayList<SearchBookResp>?
) : androidx.recyclerview.widget.RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder>() {

    lateinit var listener: ItemChangedCallback

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        listener = ItemChangedCallback(this)
        searchResultList?.addOnListChangedCallback(listener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        searchResultList?.removeOnListChangedCallback(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = DataBindingUtil.inflate<ItemSearchBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_search, parent, false
        )
        return SearchViewHolder(binding, vm)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bindTo(searchResultList?.get(position))
    }

    override fun getItemCount(): Int = searchResultList?.size ?: 0

    class SearchViewHolder(var binding: ItemSearchBinding, var vm: SearchViewModel?) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bindTo(entity: SearchBookResp?) {
            binding.itemDetail = entity
            binding.clickEvent = vm
        }
    }

    class ItemChangedCallback(private var adapter: SearchResultAdapter) :
        ObservableList.OnListChangedCallback<ObservableArrayList<SearchBookResp>>() {

        override fun onChanged(sender: ObservableArrayList<SearchBookResp>?) {
            uiThread { adapter.notifyDataSetChanged() }
        }

        override fun onItemRangeRemoved(
            sender: ObservableArrayList<SearchBookResp>?,
            positionStart: Int,
            itemCount: Int
        ) {
            uiThread { adapter.notifyDataSetChanged() }
        }

        override fun onItemRangeMoved(
            sender: ObservableArrayList<SearchBookResp>?,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) {
            uiThread { adapter.notifyDataSetChanged() }
        }

        override fun onItemRangeInserted(
            sender: ObservableArrayList<SearchBookResp>?,
            positionStart: Int,
            itemCount: Int
        ) {
            uiThread { adapter.notifyItemRangeInserted(positionStart, itemCount) }
        }

        override fun onItemRangeChanged(
            sender: ObservableArrayList<SearchBookResp>?,
            positionStart: Int,
            itemCount: Int
        ) {
            uiThread { adapter.notifyItemRangeChanged(positionStart, itemCount) }
        }
    }
}