package com.example.expensemanager.domain.usecase

import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionModel) {
        // Kiểm tra tính hợp lệ của dữ liệu (Validation Logic)
        if (transaction.amount <= 0) {
            throw IllegalArgumentException("Số tiền phải lớn hơn 0")
        }
        if (transaction.title.isBlank()) {
            throw IllegalArgumentException("Tiêu đề không được để trống")
        }
        if (transaction.category.isBlank()) {
            throw IllegalArgumentException("Vui lòng chọn danh mục")
        }

        repository.addTransaction(transaction)
    }
}