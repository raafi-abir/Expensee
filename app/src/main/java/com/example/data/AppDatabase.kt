package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.RecurringTransactionDao
import com.example.data.dao.SavingsGoalDao
import com.example.data.dao.SubscriptionDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserProfileDao
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        SubscriptionEntity::class,
        RecurringTransactionEntity::class,
        UserProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_tracker.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default categories and realistic initial seed
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).populateInitialData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun populateInitialData() {
        val categoryDao = categoryDao()
        if (categoryDao.getCategoryCount() > 0) return

        val defaultExpenseCategories = listOf(
            CategoryEntity(name = "Food", iconName = "restaurant", colorHex = "#F59E0B", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Groceries", iconName = "shopping_cart", colorHex = "#10B981", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Transport", iconName = "directions_car", colorHex = "#3B82F6", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Rent", iconName = "home", colorHex = "#8B5CF6", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Utilities", iconName = "bolt", colorHex = "#EC4899", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Mobile & Internet", iconName = "wifi", colorHex = "#06B6D4", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Education", iconName = "school", colorHex = "#6366F1", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Shopping", iconName = "local_mall", colorHex = "#F43F5E", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Entertainment", iconName = "movie", colorHex = "#14B8A6", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Health", iconName = "favorite", colorHex = "#EF4444", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Travel", iconName = "flight", colorHex = "#EAB308", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Other", iconName = "more_horiz", colorHex = "#64748B", type = "EXPENSE", isDefault = true)
        )

        val defaultIncomeCategories = listOf(
            CategoryEntity(name = "Salary", iconName = "payments", colorHex = "#10B981", type = "INCOME", isDefault = true),
            CategoryEntity(name = "Freelance", iconName = "laptop", colorHex = "#3B82F6", type = "INCOME", isDefault = true),
            CategoryEntity(name = "Business", iconName = "store", colorHex = "#8B5CF6", type = "INCOME", isDefault = true),
            CategoryEntity(name = "Allowance", iconName = "account_balance_wallet", colorHex = "#F59E0B", type = "INCOME", isDefault = true),
            CategoryEntity(name = "Gift", iconName = "card_giftcard", colorHex = "#EC4899", type = "INCOME", isDefault = true),
            CategoryEntity(name = "Other", iconName = "more_horiz", colorHex = "#64748B", type = "INCOME", isDefault = true)
        )

        categoryDao.insertCategories(defaultExpenseCategories + defaultIncomeCategories)

        // Seed initial profile with zero defaults
        val userProfileDao = userProfileDao()
        userProfileDao.insertOrUpdate(
            UserProfileEntity(
                id = 1,
                name = "User",
                email = "",
                currencyCode = "USD",
                currencySymbol = "$",
                monthlyBudgetLimit = 0.0,
                monthlyIncome = 0.0,
                themeMode = "SYSTEM",
                onboardingCompleted = false
            )
        )
    }
}
