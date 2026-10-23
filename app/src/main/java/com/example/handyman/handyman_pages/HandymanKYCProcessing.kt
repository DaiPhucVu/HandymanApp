package com.example.handyman.handyman_pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
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
import com.example.handyman.R
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.*

@Composable
fun HandymanKYCProcessing(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("...") }
    var lastName by remember { mutableStateOf("") }
    var verificationStatus by remember { mutableStateOf("pending") }
    var idApprovedStatus by remember { mutableStateOf("") }
    var certificateApprovedStatus by remember { mutableStateOf("") }

    val currentEmail = SessionManager.getLoggedInEmail(context)
    var refreshTrigger by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            isRefreshing = true
            delay(2000)
            isRefreshing = false
        }
    }

    LaunchedEffect(currentEmail, refreshTrigger) {
        val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman")
        val query = handymanRef.orderByChild("email").equalTo(currentEmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    val status = child.child("verificationStatus").getValue(String::class.java) ?: "pending"
                    verificationStatus = status
                    idApprovedStatus = child.child("idApprovedStatus").getValue(String::class.java) ?: ""
                    certificateApprovedStatus = child.child("certificateApprovedStatus").getValue(String::class.java) ?: ""

                    val photoIdCard = child.child("photoIdCard").getValue(String::class.java)
                    // This step now collects an NID number instead of certificate
                    // uploads. `certificates` is still read so handymen who
                    // completed the old photo-upload flow are not sent back through it.
                    val nid = child.child("nid").getValue(String::class.java)
                    val legacyCertificates = child.child("certificates").getValue(String::class.java)
                    val professionalCertificate = child.child("professionalCertificate").getValue(String::class.java)

                    Log.d("KYC_CHECK", "REFRESHED STATUS: [$status], ID: $photoIdCard, NID: $nid, OldCert: $professionalCertificate")

                    if (photoIdCard.isNullOrBlank()) {
                        navController.navigate("handymanKYCLanding") {
                            popUpTo("handymanHomeKYCProcessing") { inclusive = true }
                        }
                    } else if (nid.isNullOrBlank() && legacyCertificates.isNullOrBlank() && professionalCertificate != "skipped") {
                        // Only redirect if NOT skipped and NOT uploaded
                        navController.navigate("handymanKYCCertificates") {
                            popUpTo("handymanHomeKYCProcessing") { inclusive = true }
                        }
                    }

                    if (status.trim().equals("approved", ignoreCase = true)) {
                        navController.navigate("handymanApprovedSplash") {
                            popUpTo(0)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading handyman data: ${error.message}")
            }
        })
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Top right logout
        Text(
            stringResource(R.string.log_out_link),
            color = Color(0xFF30386D),
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 24.dp)
                .clickable {
                    SessionManager.clearSession(context)
                    navController.navigate("chooseAccountType") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.dp),
                    contentDescription = stringResource(R.string.cd_profile),
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 8.dp)
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.hello_name_format, firstName, lastName), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = verificationStatus,
                            color = Color(0xFFE8A317),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(Color(0x1AFEC260), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.bytesize_location),
                            contentDescription = stringResource(R.string.cd_location),
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.unverified_address_label), fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(stringResource(R.string.kyc_application_under_review_title), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.kyc_processing_hint_handyman),
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.document_icon_yellow),
                contentDescription = stringResource(R.string.cd_kyc_document_icon),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Application details list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.application_details_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.identity_verification_label), fontSize = 14.sp)
                        Text(
                            text = if (idApprovedStatus.isEmpty()) stringResource(R.string.pending_status) else idApprovedStatus.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (idApprovedStatus.lowercase()) {
                                "approved" -> Color(0xFF4CAF50)
                                "declined", "rejected" -> Color(0xFFF44336)
                                else -> Color(0xFFE8A317)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.professional_certificate_label), fontSize = 14.sp)
                        Text(
                            text = when {
                                certificateApprovedStatus == "not_provided" -> stringResource(R.string.not_provided_status)
                                certificateApprovedStatus.isEmpty() -> stringResource(R.string.pending_status)
                                else -> certificateApprovedStatus.replaceFirstChar { it.uppercase() }
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (certificateApprovedStatus.lowercase()) {
                                "approved" -> Color(0xFF4CAF50)
                                "not_provided" -> Color.Gray
                                "declined", "rejected" -> Color(0xFFF44336)
                                else -> Color(0xFFE8A317)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { refreshTrigger++ },
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRefreshing) Color.Gray else Color(0xFFE8A317),
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(R.string.refresh_status_btn),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
