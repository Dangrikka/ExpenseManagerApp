package com.example.expensemanager.ui.home

import androidx.lifecycle.viewModelScope
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.repository.TransactionRepository
import com.example.expensemanager.domain.usecase.ParseTransactionUseCase
import com.example.expensemanager.ui.common.BaseViewModel
import com.example.expensemanager.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val auth: com.google.firebase.auth.FirebaseAuth,
    private val parseTransactionUseCase: ParseTransactionUseCase // Chuyển vào đây để Hilt quản lý chuẩn hơn
) : BaseViewModel() {

    private val _transactions = MutableStateFlow<Resource<List<TransactionModel>>>(Resource.Loading())
    val transactions: StateFlow<Resource<List<TransactionModel>>> = _transactions

    private val _smartEntryState = MutableStateFlow<Resource<Map<String, Any>>?>(null)
    val smartEntryState: StateFlow<Resource<Map<String, Any>>?> = _smartEntryState

    init {
        loadTransactions()
    }

    // Lấy ID người dùng hiện tại để Fragment sử dụng
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    // Hàm gọi AI để bóc tách chuỗi văn bản
    fun processSmartEntry(text: String) {
        viewModelScope.launch {
            _smartEntryState.value = Resource.Loading()
            val result = parseTransactionUseCase(text)
            if (result != null) {
                _smartEntryState.value = Resource.Success(result)
            } else {
                _smartEntryState.value = Resource.Error("AI không hiểu câu này, bạn thử lại nhé!")
            }
        }
    }

    // HÀM QUAN TRỌNG: Thêm giao dịch mới vào Database
    fun addTransaction(transaction: TransactionModel) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
            // Sau khi thêm, danh sách sẽ tự cập nhật nhờ luồng Flow trong loadTransactions
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _transactions.value = Resource.Loading()
            val currentUserId = getCurrentUserId()

            repository.getAllTransactions(currentUserId)
                .catch { exception ->
                    _transactions.value = Resource.Error(exception.message ?: "Lỗi không xác định")
                }
                .collect { list ->
                    _transactions.value = Resource.Success(list)
                }
        }
    }

    fun resetSmartEntryState() {
        _smartEntryState.value = null
    }
}