package com.example.handyman.customer_pages

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
                val userRef = FirebaseDatabase.getInstance().getReference("User")
                val query = userRef.orderByChild("email").equalTo(email)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var authenticated = false
                            var isVerified = ""
                            for (child in snapshot.children) {
                                val userPass = child.child("password").getValue(String::class.java)
                                val verified = child.child("status").getValue(String::class.java)
                                if (userPass == password) {
                                    authenticated = true
                                    isVerified = verified.toString()
                                    SessionManager.currentUserID = child.key
                                    SessionManager.currentUserName = child.child("firstName").getValue(String::class.java)
                                    break
                                }
                            }
                            if (authenticated) {
                                val userId = SessionManager.currentUserID ?: ""
                                val firstName = SessionManager.currentUserName ?: "User"
                                val city = snapshot.children.first().child("city").getValue(String::class.java) ?: ""
                                SessionManager.saveSession(context, email, userId, firstName, city)
                                
                                val intent = Intent(context, MainJobBoard::class.java).apply {
                                    putExtra("user_type", "customer")
                                    Log.d("Navigation", "CustomerLogin authenticated")
                                }
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Incorrect password", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "User not found", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                })
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
