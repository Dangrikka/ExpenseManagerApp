package com.example.expensemanager.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.domain.repository.TransactionRepository // Đã sửa sang domain.repository cho chuẩn
import com.example.expensemanager.domain.model.TransactionModel
import com.google.firebase.auth.FirebaseAuth // THÊM LẠI FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val auth: FirebaseAuth // INJECT lại FirebaseAuth để lấy userId
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<TransactionModel>>(emptyList())
    val searchResults: StateFlow<List<TransactionModel>> = _searchResults

    private var allTransactions: List<TransactionModel> = emptyList()

    init {
        loadAllTransactions()
    }

    private fun loadAllTransactions() {
        viewModelScope.launch {
            // Lấy UID của người dùng hiện tại
            val currentUserId = auth.currentUser?.uid ?: ""

            // TRUYỀN currentUserId vào hàm của Repository để lọc đúng dữ liệu
            repository.getAllTransactions(currentUserId).collect { transactions ->
                allTransactions = transactions
                _searchResults.value = allTransactions
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = allTransactions
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            _searchResults.value = allTransactions.filter {
                it.title.lowercase().contains(lowerCaseQuery) ||
                        it.category.lowercase().contains(lowerCaseQuery) ||
                        (it.note?.lowercase()?.contains(lowerCaseQuery) == true)
            }
        }
    }
}