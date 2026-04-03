package com.example.handyman.handyman_pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase

@Composable
fun HandymanKYCCodeOTP(modifier: Modifier = Modifier,navController: NavController) {
    var otpCode by remember { mutableStateOf("") }
    val isValidOTP = otpCode.matches(Regex("^\\d{6}$"))

    val context = LocalContext.current
    val currentEmail = SessionManager.getLoggedInEmail(context)

    Log.d("KYC", "currentEmail: $currentEmail")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top bar
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
                    .clickable { navController.navigate("handymanKycPhoneNumber") }
            )
            Text("Account verification", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Step Indicator (Final Step)
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
            DividerLine()
            StepCircle(stepNumber = 4, isActive = true)
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
            onValueChange = { otpCode = it },
            label = { Text("OTP Code") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            isError = otpCode.isNotBlank() && !isValidOTP,
            placeholder = { Text("6-digit code") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman")
                val query = handymanRef.orderByChild("email").equalTo(currentEmail)

                query.get().addOnSuccessListener { snapshot ->
                    for (child in snapshot.children) {
                        child.ref.child("isPhoneVerified").setValue(true)
                        child.ref.child("verificationStatus").setValue("")
                    }
                    navController.navigate("handymanKycSubmitted")
                }.addOnFailureListener { error ->
                    Log.e("KYC", "Failed to update KYC status: ${error.message}")
                }
            },
            enabled = isValidOTP,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValidOTP) Color(0xFF2F3367) else Color(0xFFCCCCCC)
            )
        ) {
            Text("Verify", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
