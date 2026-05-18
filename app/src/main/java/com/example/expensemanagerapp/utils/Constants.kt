package com.example.expensemanager.utils

object Constants {

    const val DATABASE_NAME = "expense_db"
    const val TABLE_TRANSACTIONS = "transactions"

    const val PREFS_NAME = "expense_prefs"
    const val KEY_MONTHLY_LIMIT = "key_monthly_limit" // Dùng cho CheckLimitUseCase
    const val KEY_FIRST_TIME_OPEN = "key_first_time_open" // Kiểm tra xem user có mở app lần đầu không

    const val FIREBASE_COLLECTION_TRANSACTIONS = "transactions"
    const val FIREBASE_COLLECTION_USERS = "users"

    const val NOTIFICATION_CHANNEL_ID = "expense_reminder_channel"
    const val NOTIFICATION_ID = 1

    const val GROQ_API_KEY = ""

}