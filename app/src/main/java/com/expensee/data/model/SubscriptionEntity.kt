package com.expensee.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subscriptions")
@Immutable
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val billingCycle: String = "MONTHLY", // "MONTHLY" or "ANNUAL"
    val nextPaymentDate: Long = System.currentTimeMillis(),
    val categoryId: Long? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
