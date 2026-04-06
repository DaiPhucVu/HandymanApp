package com.example.handyman.customer_pages

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
import com.google.firebase.database.*
import com.example.handyman.R
import com.example.handyman.utils.SessionManager
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Composable
fun CustomerHomeUnverified(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("...") }
    var lastName by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    val currentEmail = SessionManager.getLoggedInEmail(context)

    LaunchedEffect(currentEmail) {
        val userRef = FirebaseDatabase.getInstance().getReference("User")
        val query = userRef.orderByChild("email").equalTo(currentEmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    profileImageUrl = child.child("profileImageUrl").getValue(String::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading user data: ${error.message}")
            }
        })
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top right logout and Profile
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Profile",
                color = Color(0xFF30386D),
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable {
                        navController.navigate("customerProfile")
                    }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Log out",
                color = Color(0xFF30386D),
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable {
                        // Clear session
                        SessionManager.clearSession(context)
                        // Navigate to login, removing the back stack
                        navController.navigate("chooseAccountType") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
            )
        }


        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            // Header Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { navController.navigate("customerProfile") }
            ) {
                if (profileImageUrl != null && profileImageUrl!!.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.character_customer),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(60.dp).padding(end = 8.dp)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Hello, $firstName $lastName.",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                        Text(text = "Unverified address", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome Message
            Text("Welcome, $firstName!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Verify your account and unlock access to our trusted Handymen services.",
                fontSize = 16.sp,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Illustration
            Image(
                painter = painterResource(id = R.drawable.group_405), // Handyman illustration
                contentDescription = "Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Verification Button
            Button(
                onClick = {
                    navController.navigate("customerKycLanding")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    "Start verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom NavBar
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

