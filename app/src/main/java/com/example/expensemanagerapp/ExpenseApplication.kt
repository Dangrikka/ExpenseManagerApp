package com.example.expensemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}