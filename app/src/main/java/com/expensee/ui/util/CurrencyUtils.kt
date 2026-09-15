package com.expensee.ui.util

import java.util.Currency
import java.util.Locale
import java.util.TimeZone

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String
)

object CurrencyUtils {
    val supportedCurrencies = listOf(
        CurrencyItem("USD", "$", "US Dollar"),
        CurrencyItem("EUR", "€", "Euro"),
        CurrencyItem("GBP", "£", "British Pound"),
        CurrencyItem("BDT", "৳", "Bangladeshi Taka"),
        CurrencyItem("INR", "₹", "Indian Rupee"),
        CurrencyItem("CAD", "$", "Canadian Dollar"),
        CurrencyItem("AUD", "$", "Australian Dollar"),
        CurrencyItem("JPY", "¥", "Japanese Yen"),
        CurrencyItem("SGD", "$", "Singapore Dollar"),
        CurrencyItem("AED", "د.إ", "UAE Dirham"),
        CurrencyItem("CHF", "CHF", "Swiss Franc"),
        CurrencyItem("MYR", "RM", "Malaysian Ringgit")
    )

    fun detectSuggestedCurrency(): CurrencyItem {
        val tzId = TimeZone.getDefault().id.lowercase()
        val detectedCode = when {
            tzId.contains("dhaka") || tzId.contains("bangladesh") -> "BDT"
            tzId.contains("calcutta") || tzId.contains("kolkata") || tzId.contains("india") -> "INR"
            tzId.contains("london") || tzId.contains("belfast") -> "GBP"
            tzId.contains("tokyo") || tzId.contains("japan") -> "JPY"
            tzId.contains("berlin") || tzId.contains("paris") || tzId.contains("rome") ||
            tzId.contains("madrid") || tzId.contains("amsterdam") || tzId.contains("brussels") -> "EUR"
            tzId.contains("toronto") || tzId.contains("vancouver") || tzId.contains("montreal") -> "CAD"
            tzId.contains("sydney") || tzId.contains("melbourne") || tzId.contains("brisbane") -> "AUD"
            tzId.contains("singapore") -> "SGD"
            tzId.contains("dubai") -> "AED"
            tzId.contains("zurich") -> "CHF"
            tzId.contains("kuala_lumpur") -> "MYR"
            else -> {
                try {
                    val locale = Locale.getDefault()
                    val c = Currency.getInstance(locale)
                    c.currencyCode
                } catch (e: Exception) {
                    "USD"
                }
            }
        }
        return supportedCurrencies.find { it.code.equals(detectedCode, ignoreCase = true) }
            ?: supportedCurrencies.first()
    }
}
