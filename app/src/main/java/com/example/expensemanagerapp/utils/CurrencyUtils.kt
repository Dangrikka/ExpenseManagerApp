package com.example.expensemanager.utils

import android.content.Context
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    // Tỷ giá cố định: 1 USD = 25,000 VND
    const val EXCHANGE_RATE = 25000.0

    // Kiểm tra xem ứng dụng đang cấu hình dùng USD hay VND
    fun isUSD(context: Context): Boolean {
        val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_usd", false)
    }

    // Đổi cấu hình tiền tệ
    fun toggleCurrency(context: Context) {
        val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val currentIsUsd = prefs.getBoolean("is_usd", false)
        prefs.edit().putBoolean("is_usd", !currentIsUsd).apply()
    }

    // Lấy chuỗi ký hiệu hiển thị cho form nhập liệu
    fun getCurrencySymbol(context: Context): String {
        return if (isUSD(context)) "USD" else "VNĐ"
    }

    // Hiển thị số tiền tùy theo cài đặt hiện tại
    fun formatCurrency(context: Context, amount: Double): String {
        return if (isUSD(context)) {
            val converted = amount / EXCHANGE_RATE
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.format(converted)
        } else {
            val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            format.format(amount)
        }
    }

    // Hàm phụ: Hiển thị gốc bằng VND (Dùng nếu cần bắt buộc)
    fun formatToVND(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(amount)
    }

    // Khi nhập dữ liệu ở chế độ USD, phải nhân tỷ giá để lưu chuẩn VND vào CSDL
    fun parseInputToBaseCurrency(context: Context, input: Double): Double {
        return if (isUSD(context)) {
            input * EXCHANGE_RATE
        } else {
            input
        }
    }

    // Khi cần chỉnh sửa giao dịch cũ, phải chia tỷ giá nếu đang dùng USD để load lên Form
    fun convertBaseToDisplay(context: Context, baseAmount: Double): Double {
        return if (isUSD(context)) {
            baseAmount / EXCHANGE_RATE
        } else {
            baseAmount
        }
    }
}