package com.example.expensemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.local.converter.DateConverter
import com.example.expensemanager.data.local.converter.TransactionTypeConverter

@Database(
    entities = [TransactionEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class, TransactionTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
}