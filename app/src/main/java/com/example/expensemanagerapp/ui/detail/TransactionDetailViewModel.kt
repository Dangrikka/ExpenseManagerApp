package com.example.expensemanager.ui.detail

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
class TransactionDetailViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _deleteState = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteState: StateFlow<Resource<Boolean>?> = _deleteState

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            try {
                // 1. Ép kiểu ID từ String về Int (vì Room đang dùng Int làm khóa chính)
                val transactionId = id.toIntOrNull() ?: 0

                // 2. Tạo một Model "giả" chỉ cần chứa đúng ID cần xóa
                val dummyTransaction = TransactionModel(
                    id = transactionId,
                    title = "", // Mấy cái này Room không quan tâm khi xóa
                    amount = 0.0,
                    type = TransactionType.EXPENSE,
                    category = "Khác",
                    date = Date(),
                    note = ""
                )

                // 3. Truyền Model giả đó vào Repository
                val result = repository.deleteTransaction(dummyTransaction)

                if (result.isSuccess) {
                    _deleteState.value = Resource.Success(true)
                } else {
                    _deleteState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Lỗi xoá dữ liệu")
                }
            } catch (e: Exception) {
                _deleteState.value = Resource.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }
}