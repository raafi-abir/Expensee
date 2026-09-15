package com.expensee.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_records")
@Immutable
data class DeletedRecordEntity(
    @PrimaryKey
    val syncId: String,
    val entityType: String, // "TRANSACTION", "BUDGET", "GOAL", "SUBSCRIPTION", "CATEGORY"
    val deletedAt: Long = System.currentTimeMillis()
)
