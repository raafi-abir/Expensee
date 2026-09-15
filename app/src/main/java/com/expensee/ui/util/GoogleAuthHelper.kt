package com.expensee.ui.util

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.AccountPicker

object GoogleAuthHelper {

    fun createGoogleAccountPickerIntent(): Intent {
        return try {
            AccountPicker.newChooseAccountIntent(
                AccountPicker.AccountChooserOptions.Builder()
                    .setAllowableAccountsTypes(listOf("com.google"))
                    .setAlwaysShowAccountPicker(false)
                    .setTitleOverrideText("Choose an account for Expensee")
                    .build()
            )
        } catch (e: Exception) {
            // Fallback generic account chooser
            AccountPicker.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                false,
                null,
                null,
                null,
                null
            )
        }
    }

    fun extractAccountEmail(data: Intent?): String? {
        if (data == null) return null
        return data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
    }

    fun formatDisplayNameFromEmail(email: String): String {
        val prefix = email.substringBefore("@").replace(Regex("[._-]"), " ")
        return prefix.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            .ifBlank { "User" }
    }
}
