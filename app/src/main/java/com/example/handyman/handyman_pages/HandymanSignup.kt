package com.example.handyman.handyman_pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.HandymanSignupViewModel
import com.example.handyman.R

@Composable
fun HandymanSignup(modifier: Modifier = Modifier, navController: NavController, signupViewModel: HandymanSignupViewModel) {
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

    val isValid = firstName.isNotBlank()
            && lastName.isNotBlank()
            && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            && password.length >= 8
            && password == confirmPassword

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box, not a Row with SpaceEvenly: SpaceEvenly distributes the back
        // arrow and the title across the width, which pushes the title off
        // centre and leaves the arrow floating. Aligning them independently
        // pins the arrow left and centres the title on the screen.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )

            Text(
                stringResource(R.string.join_our_crew_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.character_handyman),
            contentDescription = stringResource(R.string.cd_handyman_illustration),
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (passwordVisible)
                            R.drawable.lets_icons_eye_duotone
                        else
                            R.drawable.heroicons_solid_eye_off
                    ),
                    contentDescription = null,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotBlank() && password.length < 8,
            supportingText = { if (password.isNotBlank() && password.length < 8) Text(stringResource(R.string.error_password_too_short)) }
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.repeat_password_label)) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (confirmPasswordVisible)
                            R.drawable.lets_icons_eye_duotone
                        else
                            R.drawable.heroicons_solid_eye_off
                    ),
                    contentDescription = null,
                    modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
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
                navController.navigate("handymanSkills")
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValid) Color(0xFF2D2E5E) else Color.LightGray
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(stringResource(R.string.sign_up_btn), fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(stringResource(R.string.already_have_account_message), fontSize = 14.sp)
        Text(
            stringResource(R.string.log_in_link),
            color = Color(0xFF2D2E5E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { navController.navigate("handymanLogin") }
        )
    }
}
