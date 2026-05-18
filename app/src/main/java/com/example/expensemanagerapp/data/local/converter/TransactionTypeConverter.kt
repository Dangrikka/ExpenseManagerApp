package com.example.expensemanager.data.local.converter

import androidx.room.TypeConverter
import com.example.expensemanager.domain.model.TransactionType

class TransactionTypeConverter {
    @TypeConverter
    fun fromString(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TransactionType.EXPENSE
        }
    }

    @TypeConverter
    fun typeToString(type: TransactionType): String {
        return type.name
    }
}