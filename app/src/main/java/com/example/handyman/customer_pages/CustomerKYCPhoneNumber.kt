package com.example.handyman.customer_pages

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

fun findActivity(context: Context): android.app.Activity? {
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is android.app.Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun CustomerKYCPhoneNumber(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val textFieldModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)

    // Allow +880... or 01... formats
    val isValidPhone = phoneNumber.matches(Regex("^(\\+8801|01)[3-9][0-9]{8}$"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Account verification", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step indicator
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

        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text("Verify your phone number", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Get a one-time-passcode (OTP) to verify your mobile number",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Mobile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it
                errorMessage = null
            },
            placeholder = { Text("+880 1300-000000") },
            modifier = textFieldModifier,
            isError = (phoneNumber.isNotBlank() && !isValidPhone) || errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val currentActivity = findActivity(context)
                if (currentActivity == null) {
                    Toast.makeText(context, "Error: Activity not found", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                errorMessage = null

                // Format number for Firebase (ensure it starts with +880)
                val formattedNumber = if (phoneNumber.startsWith("0")) {
                    "+88" + phoneNumber
                } else {
                    phoneNumber
                }

                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        isLoading = false
                        Log.d("KYC", "Verification completed automatically")
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        isLoading = false
                        errorMessage = e.message
                        Log.e("KYC", "Verification failed: ${e.message}", e)
                        Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        isLoading = false
                        val currentEmail = SessionManager.getLoggedInEmail(context)
                        FirebaseDatabase.getInstance().getReference("User")
                            .orderByChild("email").equalTo(currentEmail)
                            .get().addOnSuccessListener { snapshot ->
                                for (child in snapshot.children) {
                                    child.ref.child("phoneNumber").setValue(phoneNumber)
                                }
                                navController.navigate("customerKycCodeOTP/$verificationId")
                            }.addOnFailureListener {
                                navController.navigate("customerKycCodeOTP/$verificationId")
                            }
                    }
                }

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(formattedNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(currentActivity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            },
            enabled = isValidPhone && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValidPhone) Color(0xFFFFB703) else Color(0xFFB0B0B0)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.DarkGray, modifier = Modifier.size(24.dp))
            } else {
                Text("Get OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
        }
    }
}
