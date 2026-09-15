package com.example.util

import java.util.Calendar

object TimeGreetingHelper {

    fun getGreetingText(calendar: Calendar = Calendar.getInstance()): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    fun getGreetingEmoji(calendar: Calendar = Calendar.getInstance()): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "☀️"
            in 12..16 -> "🌤️"
            in 17..21 -> "🌆"
            else -> "🌙"
        }
    }

    fun getFullGreeting(name: String, calendar: Calendar = Calendar.getInstance()): String {
        val greeting = getGreetingText(calendar)
        val emoji = getGreetingEmoji(calendar)
        val cleanName = name.trim().ifEmpty { "Friend" }
        return "$greeting, $cleanName $emoji"
    }

    fun getTimeSensitiveSubtitle(calendar: Calendar = Calendar.getInstance()): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Start your morning with clear financial clarity"
            in 12..16 -> "Keep tabs on your daily expenses and income"
            in 17..21 -> "Review your day's budget, savings, and cash flow"
            else -> "Rest easy knowing your financial records are set"
        }
    }
}
