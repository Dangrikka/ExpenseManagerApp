package com.example.expensemanager.ui.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class BaseViewModel : ViewModel() {

    private val _commonError = MutableSharedFlow<String>()
    val commonError = _commonError.asSharedFlow()

    protected suspend fun emitCommonError(errorMessage: String) {
        _commonError.emit(errorMessage)
    }
}