package com.example.expensemanager.data.mapper

import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.domain.model.TransactionModel
import com.example.expensemanager.domain.model.TransactionType
import java.util.Date

fun TransactionEntity.toDomainModel(): TransactionModel {
    return TransactionModel(
        id = id,
        title = title,
        amount = amount,
        type = if (type == "INCOME" || type == "THU") TransactionType.INCOME else TransactionType.EXPENSE,
        category = category,
        date = Date(date),
        note = note,
        userId = userId // MỚI THÊM: Chuyển userId từ Database lên Giao diện
    )
}

fun TransactionModel.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type.name,
        category = category,
        date = date.time,
        note = note ?: "",
        userId = userId // MỚI THÊM: Chuyển userId từ Giao diện xuống Database
    )
}