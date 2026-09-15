package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["categoryId"]),
        Index(value = ["type"])
    ]
)
@Immutable
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val categoryId: Long,
    val date: Long, // Epoch timestamp in milliseconds
    val note: String = "",
    val paymentMethod: String = "Cash", // Cash, Bank, Card, Mobile Banking, Other
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
