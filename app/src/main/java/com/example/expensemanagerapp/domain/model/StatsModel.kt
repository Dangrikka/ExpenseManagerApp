package com.example.expensemanager.domain.model

data class StatsModel(
    val balance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val expenseByCategory: Map<String, Float> = emptyMap()
)