package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
@Immutable
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "User",
    val email: String = "",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val monthlyBudgetLimit: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val themeMode: String = "SYSTEM",
    val onboardingCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
