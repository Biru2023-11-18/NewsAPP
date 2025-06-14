package com.example.newsapp

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalItemDecoration(
    private val itemsToShow: Float
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)


        val itemWidth = (parent.width / itemsToShow).toInt()


        view.layoutParams.width = itemWidth
        view.requestLayout()
    }
}