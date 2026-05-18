package com.example.expensemanager.domain.repository

import com.example.expensemanager.domain.model.TransactionModel
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(userId: String): Flow<List<TransactionModel>>
    fun searchTransactions(userId: String, query: String): Flow<List<TransactionModel>>
    suspend fun addTransaction(transaction: TransactionModel): Result<Boolean>
    suspend fun updateTransaction(transaction: TransactionModel): Result<Boolean>
    suspend fun deleteTransaction(transaction: TransactionModel): Result<Boolean>
    suspend fun syncFromCloud(userId: String): Result<Boolean>
}