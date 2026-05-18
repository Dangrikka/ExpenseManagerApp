package com.example.expensemanager.domain.usecase

import com.example.expensemanager.domain.model.StatsModel
import com.example.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    // 1. CẬP NHẬT "HỢP ĐỒNG": Thêm month và year vào đây để khớp với ViewModel
    operator fun invoke(userId: String, month: Int, year: Int): Flow<StatsModel> {

        return repository.getAllTransactions(userId).map { allTransactions ->

            // 2. LỌC THỜI GIAN: Chỉ giữ lại những giao dịch đúng Tháng và Năm được chọn
            val filteredTransactions = allTransactions.filter { transaction ->
                val calendar = Calendar.getInstance()
                calendar.time = transaction.date // Lấy ngày của giao dịch

                // Lưu ý: Trong Java/Kotlin, tháng của Calendar bắt đầu từ 0 (Tháng 1 = 0), nên phải +1
                val transMonth = calendar.get(Calendar.MONTH) + 1
                val transYear = calendar.get(Calendar.YEAR)

                transMonth == month && transYear == year
            }

            // 3. TÍNH TOÁN: Dựa trên danh sách đã lọc
            var totalIncome = 0.0 // (Nếu model của bạn dùng Float thì đổi thành 0f nhé)
            var totalExpense = 0.0
            val expenseByCategory = mutableMapOf<String, Float>()

            for (transaction in filteredTransactions) {
                // Kiểm tra loại giao dịch (Giả sử enum của bạn là INCOME và EXPENSE)
                if (transaction.type.name == "INCOME") {
                    totalIncome += transaction.amount
                } else if (transaction.type.name == "EXPENSE") {
                    totalExpense += transaction.amount

                    // Cộng dồn tiền chi tiêu theo từng danh mục (Food, Travel...) để vẽ PieChart
                    val currentCatAmount = expenseByCategory[transaction.category] ?: 0f
                    expenseByCategory[transaction.category] = currentCatAmount + transaction.amount.toFloat()
                }
            }

            // 4. TRẢ VỀ: Gói tất cả vào StatsModel đưa lên giao diện
            StatsModel(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                balance = totalIncome - totalExpense,
                expenseByCategory = expenseByCategory
            )
        }
    }
}