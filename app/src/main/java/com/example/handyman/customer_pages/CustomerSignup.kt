package com.example.handyman.customer_pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.CustomerSignupViewModel
import com.example.handyman.R

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun CustomerSignup(modifier: Modifier = Modifier, navController: NavController, signupViewModel: CustomerSignupViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var firstNameTouched by remember { mutableStateOf(false) }
    var lastNameTouched by remember { mutableStateOf(false) }

    val isValid = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.length >= 8 &&
            password == confirmPassword

    Log.d("Navigation","CustomerSingup launched")

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(stringResource(R.string.create_account_title), fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.weight(1.2f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.character_customer),
            contentDescription = stringResource(R.string.cd_customer_graphic),
            modifier = Modifier
                .size(160.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it; firstNameTouched = true },
            label = { Text(stringResource(R.string.first_name_label)) },
            singleLine = true,
            isError = firstNameTouched && firstName.isBlank(),
            supportingText = { if (firstNameTouched && firstName.isBlank()) Text(stringResource(R.string.error_first_name_required)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it; lastNameTouched = true },
            label = { Text(stringResource(R.string.last_name_label)) },
            singleLine = true,
            isError = lastNameTouched && lastName.isBlank(),
            supportingText = { if (lastNameTouched && lastName.isBlank()) Text(stringResource(R.string.error_last_name_required)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            isError = email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            supportingText = { if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) Text(stringResource(R.string.error_email_invalid)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = if (passwordVisible) R.drawable.lets_icons_eye_duotone else R.drawable.heroicons_solid_eye_off),
                    contentDescription = null,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotBlank() && password.length < 8,
            supportingText = { if (password.isNotBlank() && password.length < 8) Text(stringResource(R.string.error_password_too_short)) }
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.confirm_password_label)) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = if (confirmPasswordVisible) R.drawable.lets_icons_eye_duotone else R.drawable.heroicons_solid_eye_off),
                    contentDescription = null,
                    modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPassword.isNotBlank() && confirmPassword != password,
            supportingText = { if (confirmPassword.isNotBlank() && confirmPassword != password) Text(stringResource(R.string.error_passwords_do_not_match)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Account is not created yet — just hold the details until phone
                // verification succeeds at the end of the KYC flow.
                signupViewModel.firstName = firstName
                signupViewModel.lastName = lastName
                signupViewModel.email = email
                signupViewModel.password = password
                Log.d("Signup", "Signup details captured, continuing to address step")
                navController.navigate("customerKycAddressForm")
            },
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(containerColor = if (isValid) Color(0xFFFFB703) else Color.LightGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(stringResource(R.string.sign_up_btn), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.already_have_account_message), fontSize = 14.sp)
        Text(
            text = stringResource(R.string.log_in_link),
            color = Color(0xFF7D56F3),
            modifier = Modifier.clickable { navController.navigate("customerLogin") }
        )
    }
}
