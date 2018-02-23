package com.netnovelreader.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView

/**
 * 文件： DividerGridItemDecoration
 * 描述：
 * 作者： YangJunQuan   2018-2-11.
 */
class GridDivider(context: Context) : RecyclerView.ItemDecoration() {

    private var mDividerDarwable: Drawable? = null
    private var mDividerHight = 1
    private var mColorPaint: Paint? = null


    val ATRRS = intArrayOf(android.R.attr.listDivider)

    init {
        val ta = context.obtainStyledAttributes(ATRRS)
        this.mDividerDarwable = ta.getDrawable(0)
        ta.recycle()
    }

    /*
   int dividerHight 分割线的线宽
   int dividerColor 分割线的颜色
   */
    constructor(context: Context, dividerHight: Int, dividerColor: Int) : this(context) {
        mDividerHight = dividerHight
        mColorPaint = Paint()
        mColorPaint!!.color = dividerColor
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDraw(c, parent, state)
        //画水平和垂直分割线
        drawHorizontalDivider(c, parent)
        drawVerticalDivider(c, parent)
    }

    fun drawVerticalDivider(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin

            var left: Int
            var right: Int

            //左边第一列
            if (i % 3 == 0) {
                //item左边分割线
                left = child.left
                right = left + mDividerHight
                mDividerDarwable!!.setBounds(left, top, right, bottom)
                mDividerDarwable!!.draw(c)
                if (mColorPaint != null) {
                    c.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        mColorPaint!!
                    )
                }
                //item右边分割线
                left = child.right + params.rightMargin - mDividerHight
                right = left + mDividerHight
            } else {
                //非左边第一列
                left = child.right + params.rightMargin - mDividerHight
                right = left + mDividerHight
            }
            //画分割线
            mDividerDarwable!!.setBounds(left, top, right, bottom)
            mDividerDarwable!!.draw(c)
            if (mColorPaint != null) {
                c.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    mColorPaint!!
                )
            }

        }
    }

    private fun drawHorizontalDivider(c: Canvas, parent: RecyclerView) {

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val left = child.left - params.leftMargin - mDividerHight
            val right = child.right + params.rightMargin
            var top: Int
            var bottom: Int

            // 最上面一行
            if (i / 3 == 0) {
                //当前item最上面的分割线
                top = child.top
                //当前item下面的分割线
                bottom = top + mDividerHight
                mDividerDarwable!!.setBounds(left, top, right, bottom)
                mDividerDarwable!!.draw(c)
                if (mColorPaint != null) {
                    c.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        right.toFloat(),
                        bottom.toFloat(),
                        mColorPaint!!
                    )
                }
                top = child.bottom + params.bottomMargin
                bottom = top + mDividerHight
            } else {
                top = child.bottom + params.bottomMargin
                bottom = top + mDividerHight
            }
            //画分割线
            mDividerDarwable!!.setBounds(left, top, right, bottom)
            mDividerDarwable!!.draw(c)
            if (mColorPaint != null) {
                c.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    mColorPaint!!
                )
            }
        }
    }
}