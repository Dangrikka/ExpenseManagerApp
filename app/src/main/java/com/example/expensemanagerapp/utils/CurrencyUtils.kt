package com.example.expensemanager.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatToVND(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(amount)
    }
}