package com.example.handyman

import android.content.Context

object FormValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    fun validate(
        context: Context,
        name: String,
        email: String,
        subject: String,
        message: String,
        category: String
    ): String? {
        if (name.isEmpty() || email.isEmpty() || subject.isEmpty() || message.isEmpty() || category.isEmpty()) {
            return context.getString(R.string.please_fill_all_fields_message)
        }

        if (!email.matches(emailRegex)) {
            return context.getString(R.string.please_enter_valid_email_message)
        }

        return null
    }
}
