package com.example.handyman.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.handyman.R
import com.example.handyman.utils.DEFAULT_COUNTRY_CODE
import com.example.handyman.utils.normalizeCountryCode
import com.example.handyman.utils.normalizeNationalNumber
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * Phone entry with an editable country-code box (default +880) next to the number field.
 * Both fields clean their own input; [onChange] always receives the cleaned pair, and the
 * number is re-capped when the country code changes.
 */
@Composable
fun PhoneNumberField(
    countryCode: String,
    nationalNumber: String,
    onChange: (countryCode: String, nationalNumber: String) -> Unit,
    countryCodeError: Boolean,
    numberError: Boolean,
    modifier: Modifier = Modifier
) {
    val countryCodeDescription = stringResource(R.string.country_code_label)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = countryCode,
            onValueChange = {
                val newCode = normalizeCountryCode(it)
                onChange(newCode, normalizeNationalNumber(nationalNumber, newCode))
            },
            placeholder = { Text(DEFAULT_COUNTRY_CODE) },
            prefix = { Text("+") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = countryCodeError,
            modifier = Modifier
                .width(104.dp)
                .heightIn(min = 56.dp)
                .semantics { contentDescription = countryCodeDescription }
        )
        OutlinedTextField(
            value = nationalNumber,
            onValueChange = { onChange(countryCode, normalizeNationalNumber(it, countryCode)) },
            placeholder = { Text(stringResource(R.string.phone_number_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = numberError,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
        )
    }
}

/**
 * User-facing text for a failed phone verification. The raw Firebase message
 * (e.g. "An internal error has occurred") is not shown to users; callers log it instead.
 */
fun verificationErrorMessage(context: Context, e: FirebaseException): String = when (e) {
    is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.error_phone_not_valid)
    is FirebaseTooManyRequestsException -> context.getString(R.string.error_too_many_attempts)
    else -> context.getString(R.string.error_phone_send_failed)
}
