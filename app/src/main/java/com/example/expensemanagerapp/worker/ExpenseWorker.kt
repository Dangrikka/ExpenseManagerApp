package com.example.expensemanager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensemanager.domain.repository.TransactionRepository
import com.example.expensemanager.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ExpenseWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository // Hilt tự động inject
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Tương lai: Kiểm tra trong DB xem hôm nay có giao dịch nào chưa.
            // Nếu chưa có thì bắn thông báo. Tạm thời gọi trực tiếp:

            val notificationHelper = NotificationHelper(context)
            notificationHelper.showReminderNotification()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}