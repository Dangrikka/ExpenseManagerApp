package com.example.expensemanager.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider // Import cho đăng nhập Google
import kotlinx.coroutines.tasks.await // Rất quan trọng để dùng .await()
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Thêm cái này để repository này được dùng chung duy nhất trong app
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    // 1. Hàm Đăng ký tài khoản mới
    suspend fun register(email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Hàm Đăng nhập bằng Email & Password
    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Hàm Đăng nhập bằng Google (Sử dụng Token từ Fragment gửi qua)
    suspend fun loginWithGoogle(idToken: String): Result<Boolean> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }

    // 4. Kiểm tra trạng thái đăng nhập (để tự động vào Home nếu đã đăng nhập rồi)
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // 5. Đăng xuất
    fun logout() {
        firebaseAuth.signOut()
    }
}