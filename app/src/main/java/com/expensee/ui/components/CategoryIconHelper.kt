package com.expensee.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "restaurant", "food" -> Icons.Default.Restaurant
            "shopping_cart", "groceries" -> Icons.Default.ShoppingCart
            "directions_car", "transport" -> Icons.Default.DirectionsCar
            "home", "rent" -> Icons.Default.Home
            "bolt", "utilities" -> Icons.Default.ElectricBolt
            "wifi", "internet", "mobile" -> Icons.Default.Wifi
            "school", "education" -> Icons.Default.School
            "local_mall", "shopping" -> Icons.Default.LocalMall
            "movie", "entertainment" -> Icons.Default.Movie
            "favorite", "health" -> Icons.Default.Favorite
            "flight", "travel" -> Icons.Default.Flight
            "payments", "salary" -> Icons.Default.Payments
            "laptop", "freelance" -> Icons.Default.Laptop
            "store", "business" -> Icons.Default.Store
            "account_balance_wallet", "allowance" -> Icons.Default.AccountBalanceWallet
            "card_giftcard", "gift" -> Icons.Default.CardGiftcard
            "savings" -> Icons.Default.Savings
            "card", "bank" -> Icons.Default.CreditCard
            else -> Icons.Default.Category
        }
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF10B981)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            if (cleanHex.length == 6) {
                Color(android.graphics.Color.parseColor("#$cleanHex"))
            } else if (cleanHex.length == 8) {
                Color(android.graphics.Color.parseColor("#$cleanHex"))
            } else {
                defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }
}
