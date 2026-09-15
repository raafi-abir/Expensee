package com.expensee.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["month", "year"]),
        Index(value = ["categoryId"]),
        Index(value = ["syncId"])
    ]
)
@Immutable
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val categoryId: Long? = null, // null means overall monthly budget
    val amount: Double,
    val month: Int, // 1 to 12
    val year: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
