package com.example.expensemanager.domain.model

import java.util.Date

enum class TransactionType { INCOME, EXPENSE }

data class TransactionModel(

    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val date: Date,
    val category: String,
    val note: String? = null,
    val userId: String = ""
)