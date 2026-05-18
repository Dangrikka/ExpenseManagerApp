package com.example.expensemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensemanager.domain.model.TransactionType
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val note: String,
    val userId: String = ""
)