package com.example.expensemanager.ui.home

import androidx.lifecycle.viewModelScope
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.repository.TransactionRepository
import com.example.expensemanager.domain.usecase.ParseTransactionUseCase
import com.example.expensemanager.domain.usecase.ChatWithAiUseCase // Đã thêm Import
import com.example.expensemanager.ui.common.BaseViewModel
import com.example.expensemanager.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.expensemanager.domain.model.AiMessage

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val auth: com.google.firebase.auth.FirebaseAuth,
    private val parseTransactionUseCase: ParseTransactionUseCase,
    private val chatUseCase: ChatWithAiUseCase // SỬA LỖI 1: Khai báo để Hilt bơm UseCase vào đây
) : BaseViewModel() {

    private val _transactions = MutableStateFlow<Resource<List<TransactionModel>>>(Resource.Loading())
    val transactions: StateFlow<Resource<List<TransactionModel>>> = _transactions

    private val _smartEntryState = MutableStateFlow<Resource<Map<String, Any>>?>(null)
    val smartEntryState: StateFlow<Resource<Map<String, Any>>?> = _smartEntryState

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

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

    // Thêm giao dịch mới vào Database
    fun addTransaction(transaction: TransactionModel) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
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

    // HÀM CHAT VỚI AI
    fun sendChatMessage(message: String, currentBalance: String) {
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(message, isUser = true))
        currentList.add(ChatMessage("AI đang phân tích...", isUser = false, isLoading = true))
        _chatMessages.value = currentList

        viewModelScope.launch {
            val txList = _transactions.value.data ?: emptyList()
            
            // Lọc ra các tin nhắn hợp lệ (bỏ qua tin nhắn đang loading) và chuyển đổi sang AiMessage
            val historyMessages = currentList.filter { !it.isLoading }.map {
                AiMessage(
                    role = if (it.isUser) "user" else "assistant",
                    content = it.text
                )
            }

            val response = chatUseCase(historyMessages, currentBalance, txList)

            val updatedList = _chatMessages.value.toMutableList()
            if (updatedList.isNotEmpty()) {
                updatedList.removeAt(updatedList.lastIndex)
            }

            if (response != null) {
                updatedList.add(ChatMessage(response.toString(), isUser = false))
            } else {
                updatedList.add(ChatMessage("Kết nối với máy chủ AI thất bại!", isUser = false))
            }
            _chatMessages.value = updatedList
        }
    }

    fun resetSmartEntryState() {
        _smartEntryState.value = null
    }
}