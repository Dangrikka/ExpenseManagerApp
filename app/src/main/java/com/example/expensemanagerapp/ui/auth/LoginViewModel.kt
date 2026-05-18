package com.example.expensemanagerapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.repository.AuthRepository
import com.example.expensemanager.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Boolean>?>(null)
    val loginState: StateFlow<Resource<Boolean>?> = _loginState

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()

            val result = authRepository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                _loginState.value = Resource.Success(true)
            } else {
                _loginState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Lỗi đăng nhập Google")
            }
        }
    }

    fun login(email: String, password: String) {
        // Kiểm tra xem người dùng có nhập rỗng không
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập đầy đủ Email và Mật khẩu")
            return
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading() // Hiện vòng xoay

            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                _loginState.value = Resource.Success(true) // Đăng nhập thành công
            } else {
                // Đăng nhập thất bại (sai pass, không có mạng...)
                _loginState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Đăng nhập thất bại")
            }
        }
    }
}