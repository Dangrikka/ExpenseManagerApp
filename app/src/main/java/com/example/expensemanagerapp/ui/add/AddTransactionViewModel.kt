package com.example.expensemanager.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.model.TransactionType
import com.example.expensemanager.domain.repository.TransactionRepository
import com.example.expensemanager.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val auth: com.google.firebase.auth.FirebaseAuth // Inject thêm Auth
) : ViewModel() {

    private val _addState = MutableStateFlow<Resource<Boolean>?>(null)
    val addState: StateFlow<Resource<Boolean>?> = _addState

    fun saveTransaction(title: String, amountStr: String, typeStr: String, note: String, category: String) {
        viewModelScope.launch {
            _addState.value = Resource.Loading()
            try {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                if (title.isBlank() || amount <= 0) {
                    _addState.value = Resource.Error("Vui lòng nhập tên và số tiền hợp lệ")
                    return@launch
                }

                val typeEnum = if (typeStr == "INCOME" || typeStr == "THU") TransactionType.INCOME else TransactionType.EXPENSE

                // Lấy UID người dùng hiện tại
                val currentUserId = auth.currentUser?.uid ?: ""

                val newTransaction = TransactionModel(
                    id = 0,
                    title = title,
                    amount = amount,
                    type = typeEnum,
                    category = category,
                    date = Date(),
                    note = note,
                    userId = currentUserId // GÁN ID NGƯỜI DÙNG Ở ĐÂY
                )

                val result = transactionRepository.addTransaction(newTransaction)
                if (result.isSuccess) _addState.value = Resource.Success(true)
                else _addState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Lỗi khi lưu")

            } catch (e: Exception) {
                _addState.value = Resource.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun updateTransaction(id: Int, title: String, amountStr: String, typeStr: String, note: String, oldDate: Long, oldCategory: String) {
        viewModelScope.launch {
            _addState.value = Resource.Loading()
            try {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val typeEnum = if (typeStr == "INCOME" || typeStr == "THU") TransactionType.INCOME else TransactionType.EXPENSE

                // Lấy UID người dùng hiện tại
                val currentUserId = auth.currentUser?.uid ?: ""

                val updatedTx = TransactionModel(
                    id = id,
                    title = title,
                    amount = amount,
                    type = typeEnum,
                    category = oldCategory,
                    date = Date(oldDate),
                    note = note,
                    userId = currentUserId // GÁN ID KHI CẬP NHẬT
                )

                val result = transactionRepository.updateTransaction(updatedTx)
                if (result.isSuccess) _addState.value = Resource.Success(true)
                else _addState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Lỗi khi cập nhật")
            } catch (e: Exception) {
                _addState.value = Resource.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }
}
