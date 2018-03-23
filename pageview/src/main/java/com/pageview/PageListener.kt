package com.pageview

interface PageListener {
    @FunctionalInterface
    interface DoDrawPrepare {
        fun doDrawPrepare(): Int
    }

    @FunctionalInterface
    interface OnCenterClick {
        fun onCenterClick()
    }

    @FunctionalInterface
    interface NextChapter {
        fun nextChapter()
    }

    @FunctionalInterface
    interface PreviousChapter {
        fun previousChapter()
    }

    @FunctionalInterface
    interface OnPageChange {
        fun onPageChange(index: Int)
    }
}