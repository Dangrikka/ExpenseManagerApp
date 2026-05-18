package com.example.expensemanager.data.repository

import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.mapper.toDomainModel
import com.example.expensemanager.data.mapper.toEntity
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.repository.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    // --- CÁC HÀM HỖ TRỢ ĐỒNG BỘ CHẠY NGẦM ---
    private suspend fun syncToCloud(transaction: TransactionModel) {
        try {
            val documentId = "${transaction.userId}_${transaction.id}"
            firestore.collection("transactions")
                .document(documentId)
                .set(transaction.toEntity())
                .await()
        } catch (e: Exception) {
            // Lỗi mạng thì bỏ qua
        }
    }

    private suspend fun deleteFromCloud(transaction: TransactionModel) {
        try {
            val documentId = "${transaction.userId}_${transaction.id}"
            firestore.collection("transactions")
                .document(documentId)
                .delete()
                .await()
        } catch (e: Exception) {
            // Lỗi mạng thì bỏ qua
        }
    }

    // --- 6 HÀM BẮT BUỘC PHẢI OVERRIDE TỪ INTERFACE ---

    // 1. Lấy tất cả
    override fun getAllTransactions(userId: String): Flow<List<TransactionModel>> {
        return dao.getAllTransactions(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // 2. Tìm kiếm
    override fun searchTransactions(userId: String, query: String): Flow<List<TransactionModel>> {
        return dao.searchTransactions(userId, query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // 3. Thêm giao dịch
    override suspend fun addTransaction(transaction: TransactionModel): Result<Boolean> {
        return try {
            dao.insertTransaction(transaction.toEntity())
            syncToCloud(transaction)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. Cập nhật giao dịch
    override suspend fun updateTransaction(transaction: TransactionModel): Result<Boolean> {
        return try {
            dao.updateTransaction(transaction.toEntity())
            syncToCloud(transaction)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. Xóa giao dịch
    override suspend fun deleteTransaction(transaction: TransactionModel): Result<Boolean> {
        return try {
            dao.deleteTransaction(transaction.toEntity())
            deleteFromCloud(transaction)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. Tải dữ liệu từ mây về (HÀM BỊ THIẾU Ở BẢN CỦA ĐĂNG)
    override suspend fun syncFromCloud(userId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            for (document in snapshot.documents) {
                val entity = document.toObject(com.example.expensemanager.data.local.entity.TransactionEntity::class.java)
                if (entity != null) {
                    dao.insertTransaction(entity)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}