package com.example.expensemanager.data.local.dao

import androidx.room.*
import com.example.expensemanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // CHỈ GIỮ LẠI hàm có lọc userId
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId AND (title LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%') ORDER BY date DESC")
    fun searchTransactions(userId: String, searchQuery: String): Flow<List<TransactionEntity>>
}
