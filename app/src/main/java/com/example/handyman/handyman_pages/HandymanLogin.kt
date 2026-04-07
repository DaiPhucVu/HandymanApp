package com.example.handyman.handyman_pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.*
import android.content.Intent
import android.util.Log
import com.example.handyman.MainJobBoard


@Composable
fun HandymanLogin(modifier: Modifier = Modifier,navController: NavController) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 8

    Log.d("Navigation:", "HandymanLogin launches")

    val scrollState = rememberScrollState()

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
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {

            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )

            Text("Log In", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.character_handyman),
            contentDescription = "Handyman Illustration",
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("at least 8 characters") },
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Forgot password?",
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp),
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val userRef = FirebaseDatabase.getInstance().getReference("Handyman")
                val query = userRef.orderByChild("email").equalTo(email)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var authenticated = false
                            var isVerified = ""
                            for (child in snapshot.children) {
                                val userPass = child.child("password").getValue(String::class.java)
                                val verified = child.child("verificationStatus").getValue(String::class.java) ?: "unverified"
                                if (userPass == password) {
                                    authenticated = true
                                    isVerified = verified
                                    SessionManager.currentUserID = child.key
                                    SessionManager.currentUserName = child.child("firstName").getValue(String::class.java)
                                    break
                                }
                            }
                            if (authenticated) {
                                val userId = SessionManager.currentUserID ?: ""
                                val firstName = SessionManager.currentUserName ?: "Handyman"
                                val city = snapshot.children.first().child("city").getValue(String::class.java) ?: ""
                                SessionManager.saveSession(context, email, userId, firstName, city)
                                if (isVerified.equals("approved", ignoreCase = true)) {
                                    val intent = Intent(context, MainJobBoard::class.java).apply {
                                        putExtra("user_type", "handyman")
                                    }
                                    context.startActivity(intent)
                                } else if (isVerified.equals("pending", ignoreCase = true)) {
                                    navController.navigate("handymanHomeKYCProcessing")
                                } else {
                                    // Empty status or "unverified" both go here
                                    navController.navigate("handymanHomeUnverified")
                                }
                            } else {
                                Toast.makeText(context, "Incorrect password", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Handyman account not found", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                })
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
            Text("Login", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Don’t have an account?", fontSize = 14.sp)
        Text(
            text = "Sign Up",
            color = Color(0xFF2D2E5E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                navController.navigate("handymanSignup")
            }
        )
    }
}
