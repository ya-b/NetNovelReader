package com.netnovelreader.shelf

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.databinding.ItemShelfRecyclerViewBinding

/**
 * Created by yangbo on 2018/1/11.
 */
class ShelfAdapter(var books: ArrayList<BookInfoBean>?) : RecyclerView.Adapter<ShelfAdapter.ShelfViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ShelfViewHolder {
        val binding = DataBindingUtil.inflate<ItemShelfRecyclerViewBinding>(
                LayoutInflater.from(parent?.context), R.layout.item_shelf_recycler_view, parent,
                false)
        return ShelfViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder?, position: Int) {
        var binding = DataBindingUtil.getBinding<ItemShelfRecyclerViewBinding>(holder?.itemView)
        binding.bookinfo = books?.get(position)
        binding.clickEvent = ShelfClickEvent()
        binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        books ?: return 0
        return books!!.size
    }

    class ShelfViewHolder(v: View) : RecyclerView.ViewHolder(v)
}