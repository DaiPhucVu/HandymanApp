package com.example.handyman

import android.util.Patterns
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CustomerLoginTest {

    private fun isEmailValid(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))
    }


    @Test
    fun email_is_valid_format() {
        val email = "user@example.com"
        val result = isEmailValid(email)
        assertThat(result).isTrue()
    }

    @Test
    fun email_is_invalid_format() {
        val email = "user@invalid"
        val result = isEmailValid(email)
        assertThat(result).isFalse()
    }

    @Test
    fun password_is_valid_when_length_greater_than_8() {
        val password = "abcdefgh"
        val result = password.length >= 8
        assertThat(result).isTrue()
    }

    @Test
    fun password_is_invalid_when_length_less_than_8() {
        val password = "12345"
        val result = password.length >= 8
        assertThat(result).isFalse()
    }

    @Test
    fun login_form_is_valid_when_email_and_password_are_valid() {
        val email = "test@example.com"
        val password = "securePass"
        val isValid = isEmailValid(email) && password.length >= 8
        assertThat(isValid).isTrue()
    }

    @Test
    fun login_form_is_invalid_when_email_invalid_or_password_short() {
        val email = "invalid"
        val password = "123"
        val isValid = isEmailValid(email) && password.length >= 8
        assertThat(isValid).isFalse()
    }
}
