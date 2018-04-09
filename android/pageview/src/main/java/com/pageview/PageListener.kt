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
    interface OnNextChapter {
        fun onNextChapter()
    }

    @FunctionalInterface
    interface OnPreviousChapter {
        fun onPreviousChapter()
    }

    @FunctionalInterface
    interface OnPageChange {
        fun onPageChange(index: Int)
    }
}