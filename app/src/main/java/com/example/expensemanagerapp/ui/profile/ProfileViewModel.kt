package com.example.expensemanager.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun logout() {
        auth.signOut()
    }

    fun updateUserName(newName: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "Chưa đăng nhập")
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    suspend fun getAllTransactions(): List<TransactionModel> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return transactionRepository.getAllTransactions(userId).first()
    }

    fun syncDataFromCloud() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                transactionRepository.syncFromCloud(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}