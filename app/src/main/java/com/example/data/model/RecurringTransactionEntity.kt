package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val frequency: String = "MONTHLY", // DAILY, WEEKLY, MONTHLY, YEARLY
    val categoryId: Long,
    val paymentMethod: String = "Bank",
    val nextRun: Long = System.currentTimeMillis(),
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
