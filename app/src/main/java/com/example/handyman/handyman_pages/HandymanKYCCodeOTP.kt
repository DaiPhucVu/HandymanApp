package com.example.handyman.handyman_pages

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.handyman.HandymanSignupViewModel
import com.example.handyman.R
import com.example.handyman.components.DividerLine
import com.example.handyman.components.StepCircle
import com.example.handyman.utils.SessionManager
import com.example.handyman.utils.getCurrentYearMonth
import com.example.handyman.utils.incrementMetric
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

@Composable
fun HandymanKYCCodeOTP(
    modifier: Modifier = Modifier,
    navController: NavController,
    verificationId: String,
    phoneNumber: String,
    signupViewModel: HandymanSignupViewModel
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isValidOTP = otpCode.matches(Regex("^\\d{6}$"))

    val context = LocalContext.current

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
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(stringResource(R.string.account_verification_title), fontSize = 20.sp, fontWeight = FontWeight.Medium)
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

        Text(stringResource(R.string.verify_phone_title), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.enter_otp_hint),
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
            label = { Text(stringResource(R.string.otp_code_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            isError = (otpCode.isNotBlank() && !isValidOTP) || errorMessage != null,
            placeholder = { Text(stringResource(R.string.six_digit_code_placeholder)) }
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
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)

                // Only reached once OTP verification actually succeeds — this is
                // the first point the account record is written anywhere.
                fun createAccount() {
                    val handymanId = UUID.randomUUID().toString()
                    val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())

                    val handymanData = mapOf(
                        "handymanId" to handymanId,
                        "firstName" to signupViewModel.firstName,
                        "lastName" to signupViewModel.lastName,
                        "email" to signupViewModel.email,
                        "password" to signupViewModel.password,
                        "isPhoneVerified" to true,
                        "phoneNumber" to phoneNumber,
                        "photoIdCard" to signupViewModel.photoIdCard,
                        "nid" to signupViewModel.nid,
                        "certificateApprovedStatus" to signupViewModel.certificateApprovedStatus,
                        "professionalCertificate" to signupViewModel.professionalCertificate,
                        "primaryTrade" to signupViewModel.primaryTrade,
                        "experienceYears" to signupViewModel.experienceYears,
                        "hourlyRate" to signupViewModel.hourlyRate,
                        "bio" to signupViewModel.bio,
                        "skills" to listOf(signupViewModel.primaryTrade),
                        "houseNumber" to signupViewModel.houseNumber,
                        "street" to signupViewModel.street,
                        "area" to signupViewModel.area,
                        "division" to signupViewModel.division,
                        "district" to signupViewModel.district,
                        "thana" to signupViewModel.thana,
                        "city" to signupViewModel.city,
                        "country" to signupViewModel.country,
                        "postcode" to signupViewModel.postCode,
                        "notes" to signupViewModel.notes,
                        "verificationStatus" to "Verified",
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

                            SessionManager.saveSession(context, signupViewModel.email, handymanId, signupViewModel.firstName, signupViewModel.city)
                            signupViewModel.clear()
                            isLoading = false
                            Toast.makeText(context, context.getString(R.string.account_created_success_message), Toast.LENGTH_LONG).show()
                            navController.navigate("handymanKycSubmitted")
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = context.getString(R.string.failed_to_sign_up_format, e.message)
                            Log.e("KYC", "Failed to create account: ${e.message}")
                        }
                }

                if (currentUser != null) {
                    // Link phone to an existing Firebase Auth session, if one is lingering
                    currentUser.linkWithCredential(credential)
                        .addOnCompleteListener { task ->
                            val exception = task.exception
                            if (task.isSuccessful || (exception is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                                       exception?.message?.contains("already", ignoreCase = true) == true)) {

                                if (exception != null) {
                                    Log.i("KYC", "Phone already linked, but OTP was valid. Proceeding.")
                                }
                                createAccount()
                            } else {
                                isLoading = false
                                errorMessage = exception?.message ?: context.getString(R.string.verification_failed_retry_message)
                            }
                        }
                } else {
                    // Normal signup path: no Firebase Auth session yet
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                createAccount()
                            } else {
                                isLoading = false
                                errorMessage = task.exception?.message ?: context.getString(R.string.verification_failed_message)
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
                containerColor = if (isValidOTP) Color(0xFF2F3367) else Color(0xFFCCCCCC)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.verify_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
