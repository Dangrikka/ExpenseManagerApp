package com.example.expensemanager.domain.usecase

import com.example.expensemanager.domain.model.TransactionType
import com.example.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CheckLimitUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    // Hạn mức chi tiêu
    private val limitAmount = 5000000.0

    // SỬA: Thêm tham số userId vào đây
    operator fun invoke(userId: String): Flow<Boolean> {
        // Truyền userId vào hàm của Repository
        return repository.getAllTransactions(userId).map { transactions ->
            val totalExpense = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            // Trả về true nếu tổng chi vượt quá hạn mức
            totalExpense > limitAmount
        }
    }
}