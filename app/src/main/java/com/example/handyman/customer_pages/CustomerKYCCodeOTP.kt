package com.example.handyman.customer_pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import com.example.handyman.utils.SessionManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

@Composable
fun CustomerKYCCodeOTP(
    modifier: Modifier = Modifier,
    navController: NavController,
    verificationId: String
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isValidOTP = otpCode.matches(Regex("^\\d{6}$"))

    val context = LocalContext.current
    val currentEmail = SessionManager.getLoggedInEmail(context)

    Log.d("KYC", "currentEmail: $currentEmail")


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 16.dp)
            )
            Text("Account verification", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StepCircle(stepNumber = 1, isActive = true)
            DividerLine()
            StepCircle(stepNumber = 2, isActive = true)
            DividerLine()
            StepCircle(stepNumber = 3, isActive = true)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text("Verify your phone number", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Enter the 6-digit code sent to your mobile number.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { 
                otpCode = it
                errorMessage = null
            },
            label = { Text("OTP Code") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            isError = (otpCode.isNotBlank() && !isValidOTP) || errorMessage != null,
            placeholder = { Text("6-digit code") }
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                val onComplete: (Boolean, String?) -> Unit = { success, error ->
                    if (success) {
                        val userRef = FirebaseDatabase.getInstance().getReference("User")
                        val query = userRef.orderByChild("email").equalTo(currentEmail)

                        query.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                for (child in snapshot.children) {
                                    child.ref.child("isPhoneVerified").setValue(true)
                                    child.ref.child("status").setValue("Verified")
                                }
                                isLoading = false
                                navController.navigate("customerProfilePictureUpload")
                            } else {
                                isLoading = false
                                errorMessage = "User record not found in database."
                                Log.e("KYC", "No database record for email: $currentEmail")
                            }
                        }.addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = "Database update failed"
                            Log.e("KYC", "Failed to update KYC status: ${e.message}")
                        }
                    } else {
                        isLoading = false
                        errorMessage = error ?: "Verification failed. Please try again."
                        Log.e("KYC", "OTP Verification failed: $error")
                    }
                }

                if (currentUser != null) {
                    // Link phone to existing email account
                    currentUser.linkWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onComplete(true, null)
                            } else {
                                onComplete(false, task.exception?.message)
                            }
                        }
                } else {
                    // If not logged in, sign in with phone
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onComplete(true, null)
                            } else {
                                onComplete(false, task.exception?.message)
                            }
                        }
                }
            },
            enabled = isValidOTP && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValidOTP) Color(0xFFFFB703) else Color(0xFFB0B0B0)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.DarkGray, modifier = Modifier.size(24.dp))
            } else {
                Text("Verify", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
        }
    }
}