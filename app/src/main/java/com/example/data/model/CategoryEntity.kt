package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["type"])
    ]
)
@Immutable
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val type: String, // "EXPENSE" or "INCOME"
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
