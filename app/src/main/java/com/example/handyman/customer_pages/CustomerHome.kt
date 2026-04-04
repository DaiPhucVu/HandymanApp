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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.handyman.JobPostingViewModel
import com.example.handyman.R
import com.example.handyman.utils.SessionManager

@Composable
fun CustomerHome(modifier: Modifier = Modifier, navController: NavController, viewModel: JobPostingViewModel = viewModel()) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("...") }
    var lastName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Dhaka City") }

    val currentEmail = SessionManager.getLoggedInEmail(context)

    LaunchedEffect(currentEmail) {
        val userId = SessionManager.getLoggedInUserId(context)
        viewModel.customerId = userId
        val userRef = FirebaseDatabase.getInstance().getReference("User")
        val query = userRef.orderByChild("email").equalTo(currentEmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    city = child.child("city").getValue(String::class.java) ?: "Dhaka City"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading verified user: ${error.message}")
            }
        })
    }

    Box(modifier = modifier.fillMaxSize()) {

        // Top right logout
        Text(
            "Log out",
            color = Color(0xFF30386D),
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 24.dp)
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

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.dp),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(60.dp).padding(end = 8.dp)
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Hello, $firstName $lastName.",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verified",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(Color(0xFFDCF3E3), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.bytesize_location),
                            contentDescription = "Location Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = city, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Box
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("What services are you looking for?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Services", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Grid of services (simplified layout)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ServiceCard(R.drawable.fixture_replacement, "Fixture replacement") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Fixture replacement"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Fixture replacement"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                    ServiceCard(R.drawable.plumbing, "Plumbing") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Plumbing"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Plumbing"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ServiceCard(R.drawable.smart_home, "Smart home") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Smart home"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Smart home"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                    ServiceCard(R.drawable.appliance_repair, "Appliance repair") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Appliance repair"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Appliance repair"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ServiceCard(R.drawable.painting, "Painting") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Painting"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Painting"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                    ServiceCard(R.drawable.floor_repair, "Floor repair") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Floor repair"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Floor repair"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ServiceCard(R.drawable.wall_repair, "Wall repair") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Wall repair"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Wall repair"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                    ServiceCard(R.drawable.small_appliance_repair, "Small appliance repair") {
                        if (viewModel.isEditing) {
                            viewModel.serviceCategory = "Small appliance repair"
                            navController.navigate("jobPostingReview")
                        } else {
                            viewModel.clearData()
                            viewModel.serviceCategory = "Small appliance repair"
                            navController.navigate("jobPostingDescription")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom NavBar
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
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

@Composable
fun ServiceCard(
    iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(155.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFB703))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            contentScale = ContentScale.Fit, // or ContentScale.Crop if you want a tight fit
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp) // makes room for the label if needed
        )
        // Label overlay at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(32.dp)
                .background(
                    Color(0xFFFDF6EC),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
        ) {
            Text(
                title,
                color = Color(0xFF30386D),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
