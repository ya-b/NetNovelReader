package com.netnovelreader.ui.adapters

import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.databinding.ItemCatalogBinding
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.uiThread
import com.netnovelreader.vm.ReadViewModel

class CatalogAdapter(
    val vm: ReadViewModel?,
    private val dataList: ObservableArrayList<ChapterInfoEntity>?
) : androidx.recyclerview.widget.RecyclerView.Adapter<CatalogAdapter.CatalogViewHolder>() {
    private val listener by lazy {
        object : ObservableList.OnListChangedCallback<ObservableArrayList<SearchBookResp>>() {
            override fun onChanged(sender: ObservableArrayList<SearchBookResp>?) =
                uiThread { notifyDataSetChanged() }

            override fun onItemRangeChanged(
                sender: ObservableArrayList<SearchBookResp>?,
                positionStart: Int,
                itemCount: Int
            ) = uiThread { notifyDataSetChanged() }

            override fun onItemRangeInserted(
                sender: ObservableArrayList<SearchBookResp>?,
                positionStart: Int,
                itemCount: Int
            ) = uiThread { notifyDataSetChanged() }

            override fun onItemRangeMoved(
                sender: ObservableArrayList<SearchBookResp>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) = uiThread { notifyDataSetChanged() }

            override fun onItemRangeRemoved(
                sender: ObservableArrayList<SearchBookResp>?,
                positionStart: Int,
                itemCount: Int
            ) = uiThread { notifyDataSetChanged() }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        dataList?.addOnListChangedCallback(listener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        dataList?.removeOnListChangedCallback(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val binding = DataBindingUtil.inflate<ItemCatalogBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_catalog, parent, false
        )
        return CatalogViewHolder(binding, vm)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bindTo(dataList?.get(position))
    }

    override fun getItemCount(): Int = dataList?.size ?: 0

    class CatalogViewHolder(var binding: ItemCatalogBinding, val vm: ReadViewModel?) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bindTo(entity: ChapterInfoEntity?) {
            binding.itemDetail = entity
            binding.clickListener = vm
        }
    }
}