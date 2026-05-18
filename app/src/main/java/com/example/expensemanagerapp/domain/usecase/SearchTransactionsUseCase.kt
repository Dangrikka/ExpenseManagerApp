package com.example.expensemanager.domain.usecase

import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    // SỬA: Thêm userId vào hàm invoke để tìm kiếm đúng dữ liệu của chủ sở hữu
    operator fun invoke(userId: String, query: String): Flow<List<TransactionModel>> {
        return repository.searchTransactions(userId, query)
    }
}