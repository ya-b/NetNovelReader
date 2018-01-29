package com.netnovelreader.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by yangbo on 18-1-20.
 */
class NovelItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {
    private val attrs = IntArray(1) { android.R.attr.listDivider }
    private val diverHeight = 3
    private val mDivider: Drawable

    init {
        val typedArray = context.obtainStyledAttributes(attrs)
        mDivider = typedArray.getDrawable(0)
        typedArray.recycle()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left = parent.left
        val right = parent.right
        val count = parent.childCount
        for (i in 0..count - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + diverHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, diverHeight)
    }
}