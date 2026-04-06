package com.example.handyman.handyman_pages

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R
import com.google.firebase.database.FirebaseDatabase
import com.example.handyman.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*
import com.example.handyman.utils.getCurrentYearMonth
import com.example.handyman.utils.incrementMetric

@Composable
fun HandymanSignup(modifier: Modifier = Modifier,navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isValid = firstName.isNotBlank()
            && lastName.isNotBlank()
            && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            && password.length >= 8
            && password == confirmPassword

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Join our crew", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Image(
            painter = painterResource(id = R.drawable.character_handyman),
            contentDescription = "Handyman Illustration",
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
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
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Repeat password") },
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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val handymanId = UUID.randomUUID().toString()
                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())

                val handymanData = mapOf(
                    "handymanId" to handymanId,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "password" to password,
                    "isPhoneVerified" to false,
                    "photoIdCard" to "",
                    "certificates" to "",
                    "houseNumber" to "",
                    "street" to "",
                    "area" to "",
                    "division" to "",
                    "district" to "",
                    "thana" to "",
                    "city" to "",
                    "country" to "",
                    "postcode" to "",
                    "notes" to "",
                    "verificationStatus" to "",
                    "approvedBy" to "",
                    "createdAt" to timestamp,
                    "updatedAt" to timestamp,
                    "averageRating" to 0.0,
                    "reviewCount" to 0
                )

                val ref = FirebaseDatabase.getInstance().getReference("Handyman").child(handymanId)
                ref.setValue(handymanData)
                    .addOnSuccessListener {
                        val (year, month) = getCurrentYearMonth()
                        incrementMetric("serviceAnalytics/2025/$year/$month/newHandymen")
                        incrementMetric("serviceAnalytics/2025/$year/$month/newUsers")

                        SessionManager.saveSession(context, email, handymanId, firstName)
                        Toast.makeText(context, "Account created successfully", Toast.LENGTH_LONG).show()
                        navController.navigate("handymanProfilePictureUpload")
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(context, "Failed to sign up: ${error.message}", Toast.LENGTH_LONG).show()
                    }
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
            Text("Sign Up", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Already have account? ?", fontSize = 14.sp)
        Text(
            "Log in",
            color = Color(0xFF2D2E5E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { navController.navigate("handymanLogin") }
        )
    }
}
