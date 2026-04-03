package com.example.handyman.handyman_pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.handyman.R
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.*

@Composable
fun HandymanHomeUnverified(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("...") }
    var lastName by remember { mutableStateOf("") }

    val currentEmail = SessionManager.getLoggedInEmail(context)

    LaunchedEffect(currentEmail) {
        val handymanRef = FirebaseDatabase.getInstance().getReference("Handyman")
        val query = handymanRef.orderByChild("email").equalTo(currentEmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    val verificationStatus = child.child("verificationStatus").getValue(String::class.java) ?: "unverified"

                    if (verificationStatus.equals("pending", ignoreCase = true)) {
                        navController.navigate("handymanHomeKYCProcessing") {
                            popUpTo("handymanHomeUnverified") { inclusive = true }
                        }
                    } else if (verificationStatus.equals("approved", ignoreCase = true)) {
                        // If somehow they get here but are approved, send to job board
                        val intent = android.content.Intent(context, com.example.handyman.MainJobBoard::class.java).apply {
                            putExtra("user_type", "handyman")
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading handyman data: ${error.message}")
            }
        })
    }

    Box(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))

        // Logout
        Text(
            "Log out",
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
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.dp),
                    contentDescription = "Profile",
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Hello, $firstName $lastName.", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unverified",
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
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unverified address", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome
            Text("Welcome, $firstName!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Verify your account and unlock access to our trusted Handymen services.",
                fontSize = 16.sp, color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Illustration
            Image(
                painter = painterResource(id = R.drawable.group_419),
                contentDescription = "Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Button
            Button(
                onClick = {
                    navController.navigate("handymanHomeKYCProcessing")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3367)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Check Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom nav
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.home_icon),
                    contentDescription = "Home",
                    tint = Color(0xFF8A4DFF),
                    modifier = Modifier.size(30.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.list_icon),
                    contentDescription = "List",
                    tint = Color.Gray,
                    modifier = Modifier.size(30.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.chat_icon),
                    contentDescription = "Chat",
                    tint = Color.Gray,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
