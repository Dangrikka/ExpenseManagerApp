package com.example.expensemanager.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"

    // Hàm 1: Được gọi khi user bấm nút Switch bật/tắt chế độ tối
    fun toggleDarkMode(context: Context, isDarkTheme: Boolean) {
        // Đổi giao diện lập tức
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Lưu biến isDarkTheme vào SharedPreferences để ghi nhớ
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDarkTheme).apply()
    }

    // Hàm 2: Được gọi khi vừa mở App lên để nạp lại giao diện cũ
    fun applyThemeOnStartup(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkTheme = prefs.getBoolean(KEY_IS_DARK_MODE, false) // Mặc định là false (Sáng)

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}