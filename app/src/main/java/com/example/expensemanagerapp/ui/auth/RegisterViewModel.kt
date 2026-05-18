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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Sử dụng StateFlow để quản lý trạng thái đăng ký (Loading, Success, Error)
    private val _registerState = MutableStateFlow<Resource<Boolean>?>(null)
    val registerState: StateFlow<Resource<Boolean>?> = _registerState

    /**
     * Hàm xử lý đăng ký tài khoản mới
     */
    fun register(email: String, pass: String, confirmPass: String) {
        // 1. Kiểm tra tính hợp lệ của dữ liệu đầu vào
        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _registerState.value = Resource.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        if (pass.length < 6) {
            _registerState.value = Resource.Error("Mật khẩu phải có ít nhất 6 ký tự")
            return
        }

        if (pass != confirmPass) {
            _registerState.value = Resource.Error("Mật khẩu xác nhận không khớp")
            return
        }

        // 2. Tiến hành gọi Firebase thông qua Repository
        viewModelScope.launch {
            _registerState.value = Resource.Loading() // Thông báo giao diện đang xử lý

            val result = authRepository.register(email, pass)

            if (result.isSuccess) {
                _registerState.value = Resource.Success(true) // Đăng ký thành công
            } else {
                // Trả về thông báo lỗi cụ thể từ Firebase (ví dụ: email đã tồn tại)
                _registerState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Đăng ký thất bại"
                )
            }
        }
    }

    /**
     * Reset lại trạng thái sau khi đã thông báo cho người dùng
     */
    fun resetState() {
        _registerState.value = null
    }
}