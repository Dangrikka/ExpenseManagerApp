package com.example.expensemanager.data.model

// Data class đại diện cho một giao dịch
// Lưu ý: Các biến phải có giá trị mặc định để Firebase có thể tự động chuyển đổi (deserialize)
data class Transaction(
    val id: String = "",
    val userId: String = "", // RẤT QUAN TRỌNG: Để biết giao dịch này của ai
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "EXPENSE", // "EXPENSE" (Chi tiêu) hoặc "INCOME" (Thu nhập)
    val category: String = "", // Ví dụ: Ăn uống, Mua sắm, Lương...
    val date: Long = System.currentTimeMillis(), // Lưu thời gian dưới dạng số milisecond
    val note: String = ""
)