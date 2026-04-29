package com.example.handyman.customer_pages

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
fun CustomerProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    
    // Check if userId was passed via Intent (e.g. for when a handyman views it)
    val intentUserId = activity?.intent?.getStringExtra("userId")
    val isExternalView = intentUserId != null
    
    // Fallback to logged in user ID if no intent ID is present
    val userId = intentUserId ?: SessionManager.getLoggedInUserId(context)
    val database = FirebaseDatabase.getInstance().getReference("User")

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (!userId.isNullOrEmpty()) {
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userData = snapshot.value as? Map<String, Any>
                        
                        // Fetch reviews for this customer
                        val reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews")
                        reviewsRef.orderByChild("customerId").equalTo(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(reviewSnapshot: DataSnapshot) {
                                    val reviewList = mutableListOf<Review>()
                                    for (child in reviewSnapshot.children) {
                                        val review = child.getValue(Review::class.java)
                                        if (review != null && review.reviewerType == "handyman") {
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
                    } else {
                        isLoading = false
                        Toast.makeText(context, "User profile not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
        } else {
            isLoading = false
            Toast.makeText(context, "Invalid User ID", Toast.LENGTH_SHORT).show()
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val firstName = userData?.get("firstName") as? String ?: ""
        val lastName = userData?.get("lastName") as? String ?: ""
        val city = userData?.get("city") as? String ?: "Location not set"
        val phone = userData?.get("phoneNumber") as? String ?: "No phone set"
        val bio = userData?.get("bio") as? String ?: "No bio available."
        val photoUrl = userData?.get("profileImageUrl") as? String ?: ""
        
        val averageRating = (userData?.get("averageRating") as? Number)?.toDouble() ?: 0.0
        val reviewCount = (userData?.get("reviewCount") as? Number)?.toInt() ?: 0

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
                    .background(Color(0xFFFFB703))
            ) {
                IconButton(
                    onClick = { 
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else if (isExternalView) {
                            // If viewed by someone else (e.g. Handyman), just finish the activity
                            activity?.finish()
                        } else {
                            // If we came from signup or deep link and it's our own profile
                            val intent = android.content.Intent(context, com.example.handyman.MainJobBoard::class.java).apply {
                                putExtra("user_type", "customer")
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
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
                                painter = painterResource(id = R.drawable.character_customer),
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
                        Text("Handyman Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        reviews.forEach { review ->
                            ReviewItem(review)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Edit Button
                if (!isExternalView) {
                    Button(
                        onClick = { 
                            navController.navigate("customerEditProfile")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.DarkGray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Information", color = Color.DarkGray)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    var handymanName by remember { mutableStateOf("Handyman") }
    
    LaunchedEffect(review.handymanId) {
        FirebaseDatabase.getInstance().getReference("Handyman")
            .child(review.handymanId)
            .get().addOnSuccessListener { snapshot ->
                val first = snapshot.child("firstName").getValue(String::class.java) ?: ""
                val last = snapshot.child("lastName").getValue(String::class.java) ?: ""
                if (first.isNotEmpty()) handymanName = "$first $last"
            }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(handymanName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(review.rating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (review.comment.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(review.comment, fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(review.timestamp.split(" ").firstOrNull() ?: "", fontSize = 12.sp, color = Color.LightGray)
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB703))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
