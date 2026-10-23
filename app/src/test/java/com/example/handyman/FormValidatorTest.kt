package com.example.handyman

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class FormValidatorTest {

    private val context = mockk<Context> {
        every { getString(R.string.please_fill_all_fields_message) } returns "Please fill out all fields."
        every { getString(R.string.please_enter_valid_email_message) } returns "Please enter a valid email."
    }

    @Test
    fun emptyFields_returnError() {
        val result = FormValidator.validate(context, "", "a@b.com", "sub", "msg", "General Inquiry")
        assertEquals("Please fill out all fields.", result)
    }

    @Test
    fun invalidEmail_returnError() {
        val result = FormValidator.validate(context, "Name", "invalidemail", "sub", "msg", "Technical Issue")
        assertEquals("Please enter a valid email.", result)
    }

    @Test
    fun validInput_returnNull() {
        val result = FormValidator.validate(context, "Name", "valid@email.com", "Subject", "Message", "Billing & Payments")
        assertNull(result)
    }
}
