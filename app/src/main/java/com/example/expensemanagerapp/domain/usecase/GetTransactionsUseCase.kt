package com.example.expensemanager.domain.usecase

import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    // SỬA: Thêm tham số userId để lấy đúng danh sách của người dùng đó
    operator fun invoke(userId: String): Flow<List<TransactionModel>> {
        return repository.getAllTransactions(userId)
    }
}