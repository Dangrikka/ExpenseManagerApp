package com.example.expensemanager.extension

import android.view.View

// Hiện View
fun View.show() {
    this.visibility = View.VISIBLE
}

// Ẩn View hoàn toàn (giải phóng không gian)
fun View.hide() {
    this.visibility = View.GONE
}

// Làm tàng hình View (vẫn chiếm không gian trên màn hình)
fun View.invisible() {
    this.visibility = View.INVISIBLE
}