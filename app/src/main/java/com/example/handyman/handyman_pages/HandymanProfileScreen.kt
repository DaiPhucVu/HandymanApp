package com.example.handyman.handyman_pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import coil.compose.rememberAsyncImagePainter
import com.example.handyman.R
import com.example.handyman.Review
import com.example.handyman.utils.SessionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandymanProfileScreen(navController: NavController) {
    val context = LocalContext.current
    
    // Check if handymanId was passed via Intent (for when a customer views it)
    val activity = context as? android.app.Activity
    val intentHandymanId = activity?.intent?.getStringExtra("handymanId")
    
    // Fallback to logged in user ID if no intent ID is present
    val userId = intentHandymanId ?: SessionManager.getLoggedInUserId(context)
    val database = FirebaseDatabase.getInstance().getReference("Handyman")

    var handymanData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handymanData = snapshot.value as? Map<String, Any>
                    
                    // Fetch reviews for this handyman
                    val reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews")
                    reviewsRef.orderByChild("handymanId").equalTo(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(reviewSnapshot: DataSnapshot) {
                                val reviewList = mutableListOf<Review>()
                                for (child in reviewSnapshot.children) {
                                    val review = child.getValue(Review::class.java)
                                    if (review != null && review.reviewerType == "customer") {
                                        reviewList.add(review)
                                    }
                                }
                                reviews = reviewList.sortedByDescending { it.timestamp }
                                isLoading = false
                            }
                            override fun onCancelled(error: DatabaseError) {
                                isLoading = false
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val firstName = handymanData?.get("firstName") as? String ?: ""
        val lastName = handymanData?.get("lastName") as? String ?: ""
        val email = handymanData?.get("email") as? String ?: ""
        val trade = handymanData?.get("primaryTrade") as? String ?: "No Trade Set"
        val bio = handymanData?.get("bio") as? String ?: "No bio available."
        val experience = handymanData?.get("experienceYears") as? String ?: "0"
        val hourlyRate = handymanData?.get("hourlyRate") as? String ?: "0"
        val city = handymanData?.get("city") as? String ?: "Location not set"
        val photoUrl = handymanData?.get("profileImageUrl") as? String ?: ""
        
        val averageRating = (handymanData?.get("averageRating") as? Number)?.toDouble() ?: 0.0
        val reviewCount = (handymanData?.get("reviewCount") as? Number)?.toInt() ?: 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF2D2E5E))
            ) {
                IconButton(
                    onClick = {
                        if (!navController.popBackStack()) {
                            activity?.finish()
                        }
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        if (photoUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(photoUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.sample_handyman),
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$firstName $lastName", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(trade, fontSize = 16.sp, color = Color.Gray)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text(city, fontSize = 14.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(label = "Experience", value = "$experience Yrs")
                    ProfileStat(label = "Rate", value = "৳$hourlyRate/hr")
                    ProfileStat(
                        label = if (reviewCount > 0) "Rating ($reviewCount)" else "No Rating",
                        value = if (reviewCount > 0) String.format("%.1f", averageRating) else "—"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bio Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("About Me", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(bio, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Reviews Section
                if (reviews.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Customer Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        reviews.forEach { review ->
                            HandymanReviewItem(review)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Edit/KYC Button
                val verificationStatus = handymanData?.get("verificationStatus") as? String ?: "pending"
                
                if (verificationStatus.lowercase() == "approved" && intentHandymanId == null) {
                    Button(
                        onClick = { 
                            navController.navigate("handymanEditProfile")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E5E)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                } else if (intentHandymanId == null) {
                    Button(
                        onClick = { 
                            navController.navigate("handymanKYCLanding")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8A317)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Complete Verification")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HandymanReviewItem(review: Review) {
    var customerName by remember { mutableStateOf("Customer") }
    
    LaunchedEffect(review.customerId) {
        FirebaseDatabase.getInstance().getReference("User")
            .child(review.customerId)
            .get().addOnSuccessListener { snapshot ->
                val first = snapshot.child("firstName").getValue(String::class.java) ?: ""
                val last = snapshot.child("lastName").getValue(String::class.java) ?: ""
                if (first.isNotEmpty()) customerName = "$first $last"
            }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(customerName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF2D2E5E), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(review.rating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (review.comment.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(review.comment, fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(review.timestamp.split("T").firstOrNull() ?: "", fontSize = 12.sp, color = Color.LightGray)
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2E5E))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
