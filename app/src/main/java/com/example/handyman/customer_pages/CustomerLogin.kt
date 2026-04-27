package com.example.handyman.customer_pages

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.*
import com.example.handyman.R
import com.example.handyman.utils.SessionManager
import com.example.handyman.MainJobBoard


import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun CustomerLogin(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 8

    Log.d("Navigation:", "CustomerLogin launches")

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
                    .align(Alignment.CenterStart)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Log in", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.character_customer),
            contentDescription = "Customer Graphic",
            modifier = Modifier
                .size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = if (passwordVisible) R.drawable.lets_icons_eye_duotone else R.drawable.heroicons_solid_eye_off),
                    contentDescription = null,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { _ ->
                        loadCustomerProfileAndNavigate(
                            context = context,
                            navController = navController,
                            email = email.trim(),
                        )
                    }
                    .addOnFailureListener { exc ->
                        val isUserMissing = exc is FirebaseAuthInvalidUserException ||
                            (exc is FirebaseAuthInvalidCredentialsException) ||
                            exc.message?.contains("user-not-found") == true ||
                            exc.message?.contains("invalid-credential") == true
                        if (!isUserMissing) {
                            Toast.makeText(context, exc.localizedMessage ?: "Login failed", Toast.LENGTH_LONG).show()
                            return@addOnFailureListener
                        }
                        attemptCustomerRtdbMigration(
                            context = context,
                            navController = navController,
                            email = email.trim(),
                            password = password,
                        )
                    }
            },
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(containerColor = if (isValid) Color(0xFFFFB703) else Color.LightGray),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("Login", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Don’t have an account?", fontSize = 14.sp)
        Text(
            text = "Sign Up",
            color = Color(0xFF7D56F3),
            modifier = Modifier.clickable { navController.navigate("customerSignup") }
        )
    }
}

private fun loadCustomerProfileAndNavigate(
    context: Context,
    navController: NavController,
    email: String,
) {
    val userRef = FirebaseDatabase.getInstance().getReference("User")
    userRef.orderByChild("email").equalTo(email)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "Customer profile not found", Toast.LENGTH_LONG).show()
                    return
                }
                val child = snapshot.children.first()
                val userId = child.key ?: ""
                val firstName = child.child("firstName").getValue(String::class.java) ?: "User"
                val city = child.child("city").getValue(String::class.java) ?: ""

                SessionManager.currentUserID = userId
                SessionManager.currentUserName = firstName
                SessionManager.saveSession(context, email, userId, firstName, city)
                navigateAfterCustomerLogin(context, navController)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Profile load failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
}

private fun attemptCustomerRtdbMigration(
    context: Context,
    navController: NavController,
    email: String,
    password: String,
) {
    val userRef = FirebaseDatabase.getInstance().getReference("User")
    userRef.orderByChild("email").equalTo(email)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "User not found", Toast.LENGTH_LONG).show()
                    return
                }
                var matchedKey: String? = null
                var matchedFirstName: String? = null
                var matchedCity: String? = null
                for (child in snapshot.children) {
                    val rtdbPass = child.child("password").getValue(String::class.java)
                    if (rtdbPass == password) {
                        matchedKey = child.key
                        matchedFirstName = child.child("firstName").getValue(String::class.java)
                        matchedCity = child.child("city").getValue(String::class.java)
                        break
                    }
                }
                if (matchedKey == null) {
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_LONG).show()
                    return
                }
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        SessionManager.currentUserID = matchedKey
                        SessionManager.currentUserName = matchedFirstName
                        SessionManager.saveSession(
                            context = context,
                            email = email,
                            userId = matchedKey ?: "",
                            firstName = matchedFirstName ?: "User",
                            city = matchedCity ?: "",
                        )
                        navigateAfterCustomerLogin(context, navController)
                    }
                    .addOnFailureListener { createExc ->
                        Toast.makeText(
                            context,
                            "Login migration failed: ${createExc.localizedMessage ?: createExc.message}",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
}

private fun navigateAfterCustomerLogin(
    context: Context,
    navController: NavController,
) {
    val intent = Intent(context, MainJobBoard::class.java).apply {
        putExtra("user_type", "customer")
        Log.d("Navigation", "CustomerLogin authenticated")
    }
    context.startActivity(intent)
}
