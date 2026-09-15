package com.expensee.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expensee.data.dao.BudgetDao
import com.expensee.data.dao.CategoryDao
import com.expensee.data.dao.DeletedRecordDao
import com.expensee.data.dao.RecurringTransactionDao
import com.expensee.data.dao.SavingsGoalDao
import com.expensee.data.dao.SubscriptionDao
import com.expensee.data.dao.TransactionDao
import com.expensee.data.dao.UserProfileDao
import com.expensee.data.model.BudgetEntity
import com.expensee.data.model.CategoryEntity
import com.expensee.data.model.DeletedRecordEntity
import com.expensee.data.model.RecurringTransactionEntity
import com.expensee.data.model.SavingsGoalEntity
import com.expensee.data.model.SubscriptionEntity
import com.expensee.data.model.TransactionEntity
import com.expensee.data.model.UserProfileEntity
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
        UserProfileEntity::class,
        DeletedRecordEntity::class
    ],
    version = 4,
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
    abstract fun deletedRecordDao(): DeletedRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE budgets ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE categories ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE categories ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN googleAccountEmail TEXT")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN googleAccountName TEXT")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN isCloudSyncEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastSyncTimestamp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'IDLE'")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS deleted_records (
                        syncId TEXT NOT NULL PRIMARY KEY,
                        entityType TEXT NOT NULL,
                        deletedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expensee.db"
                ).addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration(dropAllTables = true)
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
