package com.example.expensemanager.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth // THÊM FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth // Inject FirebaseAuth vào đây
) : ViewModel() {

    // Hàm bổ trợ để lấy file SharedPreferences riêng cho từng UID
    private fun getPrefs(): SharedPreferences {
        val userId = auth.currentUser?.uid ?: "guest"
        // File sẽ có tên dạng: user_settings_abc123...
        return context.getSharedPreferences("user_settings_$userId", Context.MODE_PRIVATE)
    }

    fun saveSettings(limit: Double, isReminder: Boolean) {
        getPrefs().edit().apply {
            putFloat("monthly_limit", limit.toFloat())
            putBoolean("reminder_enabled", isReminder)
            apply()
        }
    }

    fun getMonthlyLimit(): Float {
        return getPrefs().getFloat("monthly_limit", 0f)
    }

    fun getReminderStatus(): Boolean {
        return getPrefs().getBoolean("reminder_enabled", true)
    }
}