package com.expensee.data.sync

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncTransaction(
    val syncId: String,
    val amount: Double,
    val type: String,
    val categoryName: String,
    val date: Long,
    val note: String = "",
    val paymentMethod: String = "Cash",
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SyncBudget(
    val syncId: String,
    val categoryName: String? = null,
    val amount: Double,
    val month: Int,
    val year: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SyncSavingsGoal(
    val syncId: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: Long? = null,
    val iconName: String = "savings",
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SyncSubscription(
    val syncId: String,
    val name: String,
    val amount: Double,
    val billingCycle: String,
    val nextPaymentDate: Long,
    val categoryName: String? = null,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SyncUserProfile(
    val name: String,
    val currencyCode: String,
    val currencySymbol: String,
    val monthlyBudgetLimit: Double,
    val monthlyIncome: Double,
    val themeMode: String,
    val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class ExpenseeSyncPayload(
    val formatVersion: Int = 1,
    val clientDeviceId: String,
    val exportedAt: Long,
    val userProfile: SyncUserProfile?,
    val transactions: List<SyncTransaction> = emptyList(),
    val budgets: List<SyncBudget> = emptyList(),
    val goals: List<SyncSavingsGoal> = emptyList(),
    val subscriptions: List<SyncSubscription> = emptyList()
)
